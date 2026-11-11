package golden;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Compares a freshly computed table with its golden reference file and reports every difference.
 * <p>
 * Cells are compared exactly: numeric cells by value (so {@code -999} equals {@code -999.0}), the others as text.
 * All the differences are collected (the comparison does not stop at the first one) and written to
 * {@code target/golden-diff/}:
 * <ul>
 *     <li>{@code <name>-diff.tsv}: one line per changed cell (ID, column, expected, actual);</li>
 *     <li>{@code summary.txt}: for every table, rows/columns added or removed, and for every column how many rows
 *     changed and the maximum absolute difference.</li>
 * </ul>
 */
public final class GoldenDiff {

    public static final Path DIFF_DIR = Path.of("target", "golden-diff");
    private static final Path SUMMARY_FILE = DIFF_DIR.resolve("summary.txt");

    /** Maximum number of summary lines shown in the assertion message (the file has all of them). */
    private static final int MAX_MESSAGE_LINES = 60;

    private static boolean diffDirCleaned = false;

    /** One changed cell. */
    public record CellDiff(String id, String column, String expected, String actual) { }

    private GoldenDiff() { }

    /**
     * Checks a computed table against its reference file. In update mode ({@code -Dgolden.update=true}) the reference
     * file is rewritten instead.
     *
     * @param name short name of the table, used in report file names
     * @param referenceFile the golden reference file
     * @param description one line describing the content, written in the reference file
     * @param actual the freshly computed table
     * @throws IOException on read/write errors
     */
    public static void check(String name, Path referenceFile, String description, GoldenFiles.Table actual)
            throws IOException {
        cleanDiffDirOnce();
        Files.deleteIfExists(DIFF_DIR.resolve(name + "-diff.tsv"));

        if (GoldenFiles.isUpdateMode()) {
            GoldenFiles.write(referenceFile, description, actual);
            System.out.printf("[golden] %s: reference file rewritten (%s, %d rows)%n", name, referenceFile,
                    actual.rows().size());
            return;
        }

        if (!Files.exists(referenceFile))
            fail("Golden reference file " + referenceFile + " not found: generate it with "
                    + "mvn test -Pgolden -D" + GoldenFiles.UPDATE_PROPERTY + "=true");

        GoldenFiles.Table expected = GoldenFiles.read(referenceFile);
        List<String> summary = new ArrayList<>();
        List<CellDiff> diffs = compare(expected, actual, summary);
        if (summary.isEmpty())
            return;

        writeDiffFile(name, diffs);
        List<String> lines = new ArrayList<>();
        lines.add("=== " + name + " (" + referenceFile + ") ===");
        lines.addAll(summary);
        lines.add("");
        Files.write(SUMMARY_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        List<String> shown = lines.size() > MAX_MESSAGE_LINES
                ? new ArrayList<>(lines.subList(0, MAX_MESSAGE_LINES)) : lines;
        if (shown != lines)
            shown.add("... (" + (lines.size() - MAX_MESSAGE_LINES) + " more lines)");
        fail(name + " differs from the golden reference. Details in " + DIFF_DIR.toAbsolutePath() + "\n"
                + String.join("\n", shown));
    }

    /**
     * Compares two tables by row id and column name.
     *
     * @param expected reference table
     * @param actual computed table
     * @param summary receives the human readable summary lines (empty if the tables are equal)
     * @return all the changed cells, in row/column order
     */
    static List<CellDiff> compare(GoldenFiles.Table expected, GoldenFiles.Table actual, List<String> summary) {
        Map<String, Integer> expCols = columnIndex(expected);
        Map<String, Integer> actCols = columnIndex(actual);

        List<String> removedCols = expCols.keySet().stream().filter(c -> !actCols.containsKey(c)).toList();
        List<String> addedCols = actCols.keySet().stream().filter(c -> !expCols.containsKey(c)).toList();
        List<String> removedRows = expected.rows().keySet().stream()
                .filter(r -> !actual.rows().containsKey(r)).toList();
        List<String> addedRows = actual.rows().keySet().stream()
                .filter(r -> !expected.rows().containsKey(r)).toList();
        addList(summary, "Columns removed", removedCols);
        addList(summary, "Columns added", addedCols);
        addList(summary, "Rows removed", removedRows);
        addList(summary, "Rows added", addedRows);

        List<CellDiff> diffs = new ArrayList<>();
        Map<String, int[]> changedByColumn = new LinkedHashMap<>();
        Map<String, Double> maxDiffByColumn = new LinkedHashMap<>();
        Map<String, Set<String>> changedRowsByColumn = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> row : expected.rows().entrySet()) {
            List<String> actRow = actual.rows().get(row.getKey());
            if (actRow == null)
                continue;
            for (Map.Entry<String, Integer> col : expCols.entrySet()) {
                Integer actIdx = actCols.get(col.getKey());
                if (actIdx == null)
                    continue;
                String e = row.getValue().get(col.getValue());
                String a = actRow.get(actIdx);
                if (sameValue(e, a))
                    continue;
                diffs.add(new CellDiff(row.getKey(), col.getKey(), e, a));
                changedByColumn.computeIfAbsent(col.getKey(), k -> new int[1])[0]++;
                changedRowsByColumn.computeIfAbsent(col.getKey(), k -> new LinkedHashSet<>()).add(row.getKey());
                double d = numericDifference(e, a);
                maxDiffByColumn.merge(col.getKey(), d, Math::max);
            }
        }

        if (!diffs.isEmpty()) {
            Set<String> changedRows = new LinkedHashSet<>();
            diffs.forEach(d -> changedRows.add(d.id()));
            summary.add(String.format(Locale.ROOT, "Changed cells: %d, in %d rows and %d columns",
                    diffs.size(), changedRows.size(), changedByColumn.size()));
            summary.add("Column\tChanged rows\tMax abs difference\tFirst rows");
            for (Map.Entry<String, int[]> c : changedByColumn.entrySet()) {
                double max = maxDiffByColumn.get(c.getKey());
                List<String> first = changedRowsByColumn.get(c.getKey()).stream().limit(5).toList();
                summary.add(c.getKey() + "\t" + c.getValue()[0] + "\t"
                        + (Double.isNaN(max) ? "n/a (text)" : GoldenFiles.format(max)) + "\t"
                        + String.join(", ", first));
            }
        }
        return diffs;
    }

    /** Exact comparison: numerically when both cells are numbers, as text otherwise. */
    static boolean sameValue(String expected, String actual) {
        if (expected.equals(actual))
            return true;
        Double e = parse(expected), a = parse(actual);
        return e != null && a != null && Double.compare(e, a) == 0;
    }

    private static double numericDifference(String expected, String actual) {
        Double e = parse(expected), a = parse(actual);
        if (e == null || a == null)
            return Double.NaN;
        return Math.abs(e - a);
    }

    private static Double parse(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, Integer> columnIndex(GoldenFiles.Table table) {
        Map<String, Integer> index = new LinkedHashMap<>();
        List<String> header = table.header();
        for (int i = 1; i < header.size(); i++)
            index.put(header.get(i), i - 1);
        return index;
    }

    private static void addList(List<String> summary, String title, List<String> items) {
        if (items.isEmpty())
            return;
        String shown = items.size() > 20 ? String.join(", ", items.subList(0, 20)) + ", ..." : String.join(", ", items);
        summary.add(title + " (" + items.size() + "): " + shown);
    }

    private static void writeDiffFile(String name, List<CellDiff> diffs) throws IOException {
        StringBuilder sb = new StringBuilder("ID\tCOLUMN\tEXPECTED\tACTUAL\n");
        for (CellDiff d : diffs)
            sb.append(d.id()).append('\t').append(d.column()).append('\t').append(d.expected()).append('\t')
                    .append(d.actual()).append('\n');
        Files.writeString(DIFF_DIR.resolve(name + "-diff.tsv"), sb.toString(), StandardCharsets.UTF_8);
    }

    /** Removes the reports of a previous run, once per JVM. */
    private static synchronized void cleanDiffDirOnce() throws IOException {
        if (diffDirCleaned)
            return;
        diffDirCleaned = true;
        if (Files.exists(DIFF_DIR))
            try (Stream<Path> files = Files.list(DIFF_DIR)) {
                for (Path f : files.toList())
                    Files.deleteIfExists(f);
            }
        Files.createDirectories(DIFF_DIR);
    }
}
