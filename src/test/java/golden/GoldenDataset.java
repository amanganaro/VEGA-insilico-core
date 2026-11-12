package golden;

import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Golden regression dataset: reads {@code src/test/resources/golden/molecules.tsv} and converts each SMILES with
 * {@link SmilesMolecule#Convert(String)}, exactly as {@code model.ModelExecutionTest} does.
 * <p>
 * File format: tab separated, header {@code NAME SMILES SOURCE CATEGORY}; lines starting with {@code #} are comments.
 */
public final class GoldenDataset {

    /** Root folder of the golden data, relative to the project base directory. */
    public static final Path GOLDEN_DIR = Path.of("src", "test", "resources", "golden");

    /** The dataset file. */
    public static final Path MOLECULES_FILE = GOLDEN_DIR.resolve("molecules.tsv");

    /** Value of the CATEGORY column (prefix) for molecules that are invalid on purpose. */
    public static final String INVALID_CATEGORY_PREFIX = "invalid";

    /** One row of the dataset. */
    public record Entry(String name, String smiles, String source, String category) {

        /** @return true if the molecule is expected to be rejected by the SMILES conversion */
        public boolean expectedInvalid() {
            return category.startsWith(INVALID_CATEGORY_PREFIX);
        }
    }

    /** A dataset row together with its converted molecule. */
    public record Item(Entry entry, InsilicoMolecule molecule) { }

    private static List<Item> cachedItems;
    private static Map<String, String> cachedSmiles;

    private GoldenDataset() { }

    /**
     * Reads a tab separated file with header {@code NAME SMILES SOURCE CATEGORY}.
     *
     * @param file file to be read
     * @return the rows, in file order
     * @throws IOException on read errors or malformed lines
     */
    public static List<Entry> readEntries(Path file) throws IOException {
        List<Entry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        boolean headerSkipped = false;
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line.isBlank() || line.startsWith("#"))
                continue;
            if (!headerSkipped) {
                headerSkipped = true;
                continue;
            }
            // limit -1 keeps empty trailing fields (e.g. an empty SMILES on purpose)
            String[] fields = line.split("\t", -1);
            if (fields.length < 4)
                throw new IOException(file + ":" + lineNumber + ": expected 4 tab separated fields, found "
                        + fields.length);
            entries.add(new Entry(fields[0], fields[1], fields[2], fields[3]));
        }
        return entries;
    }

    /**
     * Writes rows in the dataset format.
     *
     * @param file destination file
     * @param headerComments comment lines (without the leading {@code #}) written before the header
     * @param entries rows to be written
     * @throws IOException on write errors
     */
    public static void writeEntries(Path file, List<String> headerComments, List<Entry> entries) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String comment : headerComments)
            sb.append("# ").append(comment).append('\n');
        sb.append("NAME\tSMILES\tSOURCE\tCATEGORY\n");
        for (Entry e : entries)
            sb.append(e.name()).append('\t').append(e.smiles()).append('\t')
                    .append(e.source()).append('\t').append(e.category()).append('\n');
        Files.createDirectories(file.getParent());
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Input SMILES of every molecule of the dataset, by NAME (file order). Cached for the whole JVM; if the dataset
     * cannot be read an empty map is returned, so that reports simply show the bare names.
     *
     * @return unmodifiable map NAME -&gt; SMILES
     */
    public static synchronized Map<String, String> smilesByName() {
        if (cachedSmiles == null) {
            Map<String, String> map = new LinkedHashMap<>();
            try {
                for (Entry e : readEntries(MOLECULES_FILE))
                    map.put(e.name(), e.smiles());
            } catch (IOException e) {
                System.out.println("WARNING: cannot read " + MOLECULES_FILE + ": " + e.getMessage());
            }
            cachedSmiles = Collections.unmodifiableMap(map);
        }
        return cachedSmiles;
    }

    /**
     * Label of a molecule for human readable reports: {@code NAME (SMILES)}. Names not in the dataset are returned
     * unchanged.
     *
     * @param name molecule NAME
     * @return the label
     */
    public static String label(String name) {
        String smiles = smilesByName().get(name);
        if (smiles == null)
            return name;
        return name + " (" + (smiles.isEmpty() ? "empty SMILES" : smiles) + ")";
    }

    /**
     * Converts every row of the golden dataset. The result is cached for the whole JVM.
     *
     * @return converted molecules, in file order
     * @throws IOException if the dataset cannot be read
     */
    public static synchronized List<Item> load() throws IOException {
        if (cachedItems == null) {
            List<Item> items = new ArrayList<>();
            for (Entry e : readEntries(MOLECULES_FILE))
                items.add(new Item(e, convert(e)));
            cachedItems = Collections.unmodifiableList(items);
        }
        return cachedItems;
    }

    /**
     * Converts a single row, setting its NAME as molecule id.
     *
     * @param entry the dataset row
     * @return the converted molecule (possibly marked as invalid)
     */
    public static InsilicoMolecule convert(Entry entry) {
        InsilicoMolecule molecule = SmilesMolecule.Convert(entry.smiles());
        molecule.SetId(entry.name());
        return molecule;
    }
}
