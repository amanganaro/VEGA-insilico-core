package regression;

import insilico.core.descriptor.Descriptor;

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
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Compares a freshly computed table with its reference and reports every difference.
 * <p>
 * Cells are compared exactly: numeric cells by value (so {@code -999} equals {@code -999.0}), the others as text.
 * All the differences are collected (the comparison does not stop at the first one) and written to
 * {@code target/golden-diff/}:
 * <ul>
 *     <li>{@code <name>-diff.tsv}: one line per changed cell (ID, SMILES, column, expected, actual);</li>
 *     <li>{@code summary.txt}: for every table, rows/columns added or removed, and for every column how many rows
 *     changed and the maximum absolute difference.</li>
 * </ul>
 * Molecules are shown as {@code NAME (SMILES)} in the summary, using the id to SMILES map given by the caller.
 * {@link Descriptor#MISSING_VALUE} is treated as a missing value, not as a number: changes to or from it are counted
 * separately and never enter the maximum difference.
 */
public final class RegressionDiff {

    public static final Path DIFF_DIR = Path.of("target", "golden-diff");
    private static final Path SUMMARY_FILE = DIFF_DIR.resolve("summary.txt");

    /** Maximum number of summary lines shown in the assertion message (the file has all of them). */
    private static final int MAX_MESSAGE_LINES = 60;

    private static boolean diffDirCleaned = false;

    /** One changed cell. */
    public record CellDiff(String id, String column, String expected, String actual) { }

    private RegressionDiff() { }

    /**
     * Checks a computed table against its reference file on disk. In update mode ({@code -Dgolden.update=true}) the
     * reference file is rewritten instead.
     *
     * @param name short name of the table, used in report file names
     * @param referenceFile the reference file
     * @param description one line describing the content, written in the reference file
     * @param actual the freshly computed table
     * @param smilesById SMILES of the molecules by id, used to label rows (and molecule columns) in the reports
     * @param extraSummary builds additional summary lines from the changed cells (placed before the generic summary)
     * @param regenerateHint command suggested to generate a missing reference file
     * @throws IOException on read/write errors
     */
    public static void check(String name, Path referenceFile, String description, RegressionFiles.Table actual,
                             Map<String, String> smilesById, Function<List<CellDiff>, List<String>> extraSummary,
                             String regenerateHint) throws IOException {
        cleanDiffDirOnce();
        Files.deleteIfExists(DIFF_DIR.resolve(name + "-diff.tsv"));

        if (RegressionFiles.isUpdateMode()) {
            RegressionFiles.write(referenceFile, description, actual);
            System.out.printf("[golden] %s: reference file rewritten (%s, %d rows)%n", name, referenceFile,
                    actual.rows().size());
            return;
        }

        if (!Files.exists(referenceFile))
            fail("Reference file " + referenceFile + " not found: generate it with " + regenerateHint);

        assertSame(name, referenceFile.toString(), RegressionFiles.read(referenceFile), actual, smilesById,
                extraSummary, List.of());
    }

    /**
     * Compares two tables and fails, with a full report, if they differ.
     *
     * @param name short name of the table, used in report file names
     * @param referenceName where the reference comes from, shown in the report
     * @param expected the reference table
     * @param actual the freshly computed table
     * @param smilesById SMILES of the molecules by id, used to label rows (and molecule columns) in the reports
     * @param extraSummary builds additional summary lines from the changed cells (placed before the generic summary)
     * @param notes lines always added to the report when the tables differ (e.g. warnings on the dataset)
     * @throws IOException on write errors of the report
     */
    public static void assertSame(String name, String referenceName, RegressionFiles.Table expected,
                                  RegressionFiles.Table actual, Map<String, String> smilesById,
                                  Function<List<CellDiff>, List<String>> extraSummary, List<String> notes)
            throws IOException {
        cleanDiffDirOnce();
        Files.deleteIfExists(DIFF_DIR.resolve(name + "-diff.tsv"));

        List<String> summary = new ArrayList<>();
        List<CellDiff> diffs = compare(expected, actual, summary, smilesById);
        if (summary.isEmpty())
            return;

        writeDiffFile(name, diffs, smilesById);
        List<String> lines = new ArrayList<>();
        lines.add("=== " + name + " (" + referenceName + ") ===");
        lines.addAll(notes);
        lines.addAll(extraSummary.apply(diffs));
        lines.addAll(summary);
        lines.add("");
        appendSummary(lines);

        List<String> shown = lines.size() > MAX_MESSAGE_LINES
                ? new ArrayList<>(lines.subList(0, MAX_MESSAGE_LINES)) : lines;
        if (shown != lines)
            shown.add("... (" + (lines.size() - MAX_MESSAGE_LINES) + " more lines)");
        fail(name + " differs from the reference. Details in " + DIFF_DIR.toAbsolutePath() + "\n"
                + String.join("\n", shown));
    }

    /**
     * Appends lines to {@code target/golden-diff/summary.txt}.
     *
     * @param lines lines to be appended
     * @throws IOException on write errors
     */
    public static void appendSummary(List<String> lines) throws IOException {
        cleanDiffDirOnce();
        Files.write(SUMMARY_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Compares two tables by row id and column name.
     *
     * @param expected reference table
     * @param actual computed table
     * @param summary receives the human readable summary lines (empty if the tables are equal)
     * @param smilesById SMILES of the molecules by id, used to label rows and columns
     * @return all the changed cells, in row/column order
     */
    public static List<CellDiff> compare(RegressionFiles.Table expected, RegressionFiles.Table actual,
                                         List<String> summary, Map<String, String> smilesById) {
        Function<String, String> label = id -> label(id, smilesById);
        Map<String, Integer> expCols = columnIndex(expected);
        Map<String, Integer> actCols = columnIndex(actual);

        List<String> removedCols = expCols.keySet().stream().filter(c -> !actCols.containsKey(c)).toList();
        List<String> addedCols = actCols.keySet().stream().filter(c -> !expCols.containsKey(c)).toList();
        List<String> removedRows = expected.rows().keySet().stream()
                .filter(r -> !actual.rows().containsKey(r)).toList();
        List<String> addedRows = actual.rows().keySet().stream()
                .filter(r -> !expected.rows().containsKey(r)).toList();
        addList(summary, "Columns removed", removedCols.stream().map(label).toList());
        addList(summary, "Columns added", addedCols.stream().map(label).toList());
        addList(summary, "Rows removed", removedRows.stream().map(label).toList());
        addList(summary, "Rows added", addedRows.stream().map(label).toList());

        List<CellDiff> diffs = new ArrayList<>();
        Map<String, ColumnStats> statsByColumn = new LinkedHashMap<>();

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
                statsByColumn.computeIfAbsent(col.getKey(), k -> new ColumnStats()).add(row.getKey(), e, a);
            }
        }

        if (!diffs.isEmpty()) {
            Set<String> changedRows = new LinkedHashSet<>();
            diffs.forEach(d -> changedRows.add(d.id()));
            summary.add(String.format(Locale.ROOT, "Changed cells: %d, in %d rows and %d columns",
                    diffs.size(), changedRows.size(), statsByColumn.size()));
            summary.add("Column\tChanged rows\tMax abs difference (both values present)\t"
                    + "Changes to/from missing value\tFirst rows");
            for (Map.Entry<String, ColumnStats> c : statsByColumn.entrySet()) {
                ColumnStats s = c.getValue();
                String max = s.numericChanges > 0 ? RegressionFiles.format(s.maxDiff)
                        : (s.textChanges > 0 ? "n/a (text)" : "n/a");
                List<String> first = s.rows.stream().limit(5).map(label).toList();
                summary.add(label.apply(c.getKey()) + "\t" + s.rows.size() + "\t" + max + "\t"
                        + s.missingChanges + "\t" + String.join(", ", first));
            }
        }
        return diffs;
    }

    /**
     * Label of a molecule for human readable reports: {@code NAME (SMILES)}. Ids without a SMILES are returned
     * unchanged.
     *
     * @param id molecule id
     * @param smilesById SMILES by id
     * @return the label
     */
    public static String label(String id, Map<String, String> smilesById) {
        String smiles = smilesById.get(id);
        if (smiles == null)
            return id;
        return id + " (" + (smiles.isEmpty() ? "empty SMILES" : smiles) + ")";
    }

    /**
     * Statistics of the changed cells of one column. A change involving {@link Descriptor#MISSING_VALUE} on either side
     * is only counted: the missing value is a convention, not a number, so it never enters the maximum difference.
     */
    private static final class ColumnStats {
        final Set<String> rows = new LinkedHashSet<>();
        int numericChanges = 0;
        int missingChanges = 0;
        int textChanges = 0;
        double maxDiff = 0;

        void add(String rowId, String expected, String actual) {
            rows.add(rowId);
            Double e = parse(expected), a = parse(actual);
            if (e == null || a == null)
                textChanges++;
            else if (isMissing(e) || isMissing(a))
                missingChanges++;
            else {
                numericChanges++;
                maxDiff = Math.max(maxDiff, Math.abs(e - a));
            }
        }
    }

    /**
     * @param value a numeric cell value
     * @return true if the value is the {@link Descriptor#MISSING_VALUE} convention
     */
    public static boolean isMissing(double value) {
        return value == Descriptor.MISSING_VALUE;
    }

    /** Exact comparison: numerically when both cells are numbers, as text otherwise. */
    public static boolean sameValue(String expected, String actual) {
        if (expected.equals(actual))
            return true;
        Double e = parse(expected), a = parse(actual);
        return e != null && a != null && Double.compare(e, a) == 0;
    }

    /**
     * @param s cell text
     * @return the numeric value, or null if the cell is not a number
     */
    public static Double parse(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, Integer> columnIndex(RegressionFiles.Table table) {
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

    /**
     * Writes one line per changed cell, with the SMILES of the row molecule. When the columns are molecules too (as
     * in the similarity matrix), the SMILES of the column molecule is added as well.
     */
    private static void writeDiffFile(String name, List<CellDiff> diffs, Map<String, String> smiles)
            throws IOException {
        boolean moleculeColumns = !diffs.isEmpty() && diffs.stream().allMatch(d -> smiles.containsKey(d.column()));
        StringBuilder sb = new StringBuilder(moleculeColumns
                ? "ID\tSMILES\tCOLUMN\tCOLUMN_SMILES\tEXPECTED\tACTUAL\n"
                : "ID\tSMILES\tCOLUMN\tEXPECTED\tACTUAL\n");
        for (CellDiff d : diffs) {
            sb.append(d.id()).append('\t').append(smiles.getOrDefault(d.id(), "")).append('\t').append(d.column());
            if (moleculeColumns)
                sb.append('\t').append(smiles.get(d.column()));
            sb.append('\t').append(d.expected()).append('\t').append(d.actual()).append('\n');
        }
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
