package golden;

import org.openscience.cdk.CDK;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reading and writing of the golden reference files.
 * <p>
 * Format: tab separated; lines starting with {@code #} are metadata/comments and are ignored when reading; the first
 * other line is the header, whose first column is the row id. Line endings (CRLF / LF) are irrelevant.
 */
public final class GoldenFiles {

    /** System property: when true the tests rewrite the reference files instead of comparing. */
    public static final String UPDATE_PROPERTY = "golden.update";

    private GoldenFiles() { }

    /**
     * An in-memory table: header (first column = id) and rows by id, in insertion order.
     *
     * @param header column names, the first one is the id column
     * @param rows row id to the cell values of the other columns (same order as {@code header.subList(1, ...)})
     */
    public record Table(List<String> header, Map<String, List<String>> rows) {

        public Table(List<String> header) {
            this(header, new LinkedHashMap<>());
        }

        public void addRow(String id, List<String> values) {
            if (values.size() != header.size() - 1)
                throw new IllegalArgumentException("Row " + id + ": " + values.size() + " values, expected "
                        + (header.size() - 1));
            if (rows.put(id, values) != null)
                throw new IllegalArgumentException("Duplicated row id: " + id);
        }
    }

    /** @return true if the reference files have to be regenerated */
    public static boolean isUpdateMode() {
        return Boolean.getBoolean(UPDATE_PROPERTY);
    }

    /**
     * Writes a table, preceded by the metadata line and the given description.
     *
     * @param file destination
     * @param description one line describing the content
     * @param table table to be written
     * @throws IOException on write errors
     */
    public static void write(Path file, String description, Table table) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(metadata()).append('\n');
        sb.append("# ").append(description).append('\n');
        sb.append(String.join("\t", table.header())).append('\n');
        for (Map.Entry<String, List<String>> row : table.rows().entrySet()) {
            sb.append(row.getKey());
            for (String value : row.getValue())
                sb.append('\t').append(value);
            sb.append('\n');
        }
        Files.createDirectories(file.getParent());
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Reads a table written by {@link #write}.
     *
     * @param file file to be read
     * @return the table
     * @throws IOException on read errors or malformed rows
     */
    public static Table read(Path file) throws IOException {
        Table table = null;
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.startsWith("#") || line.isEmpty())
                continue;
            List<String> fields = List.of(line.split("\t", -1));
            if (table == null) {
                table = new Table(fields);
                continue;
            }
            if (fields.size() != table.header().size())
                throw new IOException(file + ":" + lineNumber + ": " + fields.size() + " fields, expected "
                        + table.header().size());
            table.addRow(fields.get(0), new ArrayList<>(fields.subList(1, fields.size())));
        }
        if (table == null)
            throw new IOException(file + ": no header found");
        return table;
    }

    /**
     * Formats a numeric value so that it can be read back exactly ({@link Double#toString} is round-trip safe).
     *
     * @param value the value
     * @return its string representation
     */
    public static String format(double value) {
        return Double.toString(value);
    }

    /** @return the metadata line: core and CDK versions, git commit and date */
    public static String metadata() {
        return "core=" + coreVersion() + " cdk=" + CDK.getVersion() + " commit=" + gitCommit()
                + " date=" + LocalDate.now();
    }

    private static String coreVersion() {
        try {
            String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
            Matcher m = Pattern.compile("<artifactId>Core</artifactId>\\s*<version>([^<]+)</version>").matcher(pom);
            return m.find() ? m.group(1) : "unknown";
        } catch (IOException e) {
            return "unknown";
        }
    }

    private static String gitCommit() {
        try {
            Process p = new ProcessBuilder("git", "rev-parse", "--short", "HEAD").redirectErrorStream(true).start();
            String out;
            try (InputStream is = p.getInputStream()) {
                out = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
            if (!p.waitFor(10, TimeUnit.SECONDS) || p.exitValue() != 0 || out.isEmpty() || out.contains(" "))
                return "unknown";
            return out;
        } catch (IOException e) {
            return "unknown";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "unknown";
        }
    }
}
