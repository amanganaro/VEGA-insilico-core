package regression;

import insilico.core.model.InsilicoModel;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.InsilicoModelPython;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.python.CdddDescriptors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Static logic of the model regression tests: it runs a model exactly as the application does, builds a table of its
 * final results (and, on the core dataset, of the main ADI value) and either rewrites the reference file
 * ({@code -Dgolden.update=true}) or compares the fresh results against it, cell by cell.
 * <p>
 * The reference files live next to the model in each {@code VegaModels-*} module, under
 * {@code src/test/resources/regression/<KEY>-<dataset>.tsv} ({@code KEY = model.getInfo().getKey()}). They are read
 * from the classpath (the module's own test resources) and rewritten on disk in update mode. This class is published
 * in the core test-jar so that every module can reuse it; the core dataset ({@code regression/core-molecules.tsv}) is
 * published there too.
 * <p>
 * The reference files are self-sufficient (id, SMILES and values per molecule). In check mode the molecules are taken
 * from the reference itself ("recompute on its own molecules"), so a change in Core, CDK or the model is isolated from
 * a change of the dataset; the latter is reported separately as a warning telling the user to regenerate.
 * <p>
 * What is compared:
 * <ul>
 *     <li>always the final results, {@link InsilicoModelOutput#getResults()}, one column per
 *     {@link InsilicoModel#GetResultsName() result name};</li>
 *     <li>the {@code STATUS} of each molecule, so a change in the behaviour on errors is reported too;</li>
 *     <li>only on the {@code core} dataset, the value of the main ADI, {@code getADI().GetIndexValue()}, without the
 *     partial indices. On {@code ts} the ADI makes no sense (every molecule is in the training set) and the column is
 *     not produced. {@code -Dregression.adi=false} drops the ADI column from the comparison without needing to
 *     regenerate the reference, which always contains it.</li>
 * </ul>
 * {@code -Dregression.skip=ID1,ID2} leaves some molecules out of both the computation and the comparison.
 *
 * @see ModelRegressionTest
 */
public final class ModelRegression {

    /** Classpath resource of the core dataset, published in the core test-jar. */
    public static final String CORE_MOLECULES_RESOURCE = "regression/core-molecules.tsv";

    /** Folder (relative to the module base dir) where the reference files are written in update mode. */
    public static final Path REFERENCE_DIR = Path.of("src", "test", "resources", "regression");

    /** System property selecting the dataset(s): {@code ts}, {@code core}, or unset for both. */
    public static final String DATASET_PROPERTY = "regression.dataset";

    /** System property enabling the ADI comparison on the core dataset (default true). */
    public static final String ADI_PROPERTY = "regression.adi";

    /**
     * System property listing molecule ids to leave out, separated by commas (e.g. {@code CUR-366}). The molecules are
     * neither computed nor compared, and their rows are dropped from the reference before the comparison. Meant for
     * trials where a molecule cannot be computed (for instance one that makes a model run forever).
     */
    public static final String SKIP_PROPERTY = "regression.skip";

    private static final String ID_COLUMN = "ID";
    private static final String SMILES_COLUMN = "SMILES";
    private static final String ADI_COLUMN = "ADI";
    private static final String STATUS_COLUMN = "STATUS";

    private static final String REGENERATE_HINT =
            "mvn test -Pregression -D" + RegressionFiles.UPDATE_PROPERTY + "=true";

    /** Molecules of the CDDD descriptors kept in {@link #cachedCddd}, or null when nothing is cached. */
    private static List<String> cachedCdddSmiles;

    /** Last CDDD descriptor set computed, reused when the next model is run on exactly the same molecules. */
    private static CdddDescriptors cachedCddd;

    private ModelRegression() { }

    /** The two reference datasets. */
    public enum Dataset {

        /** All the molecules of the model training set (training + test). */
        TS("ts"),
        /** The core golden dataset ({@value #CORE_MOLECULES_RESOURCE}, the same molecules of the core golden tests). */
        CORE("core");

        private final String id;

        Dataset(String id) {
            this.id = id;
        }

        /** @return the short id, used in file names and test names */
        public String id() {
            return id;
        }
    }

    /** One dataset row, before conversion: a stable id and its SMILES. */
    public record DatasetEntry(String id, String smiles) { }

    /** One input molecule: a stable id, its SMILES, and the converted molecule. */
    public record MoleculeInput(String id, String smiles, InsilicoMolecule molecule) { }

    /**
     * Datasets to run, from the {@value #DATASET_PROPERTY} system property.
     *
     * @return {@code [TS, CORE]} when the property is unset, or the single selected dataset
     * @throws IllegalArgumentException if the property has an unknown value
     */
    public static List<Dataset> selectedDatasets() {
        String p = System.getProperty(DATASET_PROPERTY);
        if (p == null || p.isBlank())
            return List.of(Dataset.TS, Dataset.CORE);
        return switch (p.trim().toLowerCase(Locale.ROOT)) {
            case "ts" -> List.of(Dataset.TS);
            case "core" -> List.of(Dataset.CORE);
            default -> throw new IllegalArgumentException("Unknown " + DATASET_PROPERTY + " value '" + p
                    + "': use 'ts' or 'core', or leave it unset for both");
        };
    }

    /** @return true if the ADI value is compared on the core dataset (default true) */
    public static boolean adiEnabled() {
        return !"false".equalsIgnoreCase(System.getProperty(ADI_PROPERTY, "true"));
    }

    /** @return the molecule ids listed in the {@value #SKIP_PROPERTY} system property (empty when unset) */
    public static Set<String> skippedIds() {
        String p = System.getProperty(SKIP_PROPERTY);
        Set<String> ids = new LinkedHashSet<>();
        if (p != null)
            for (String id : p.split(","))
                if (!id.isBlank())
                    ids.add(id.trim());
        return ids;
    }

    /**
     * Removes the molecules listed in {@value #SKIP_PROPERTY}, printing which ones were left out.
     *
     * @param entries the dataset rows
     * @param name name of the run (model key and dataset), for the message
     * @return the rows that are not skipped
     */
    private static List<DatasetEntry> withoutSkipped(List<DatasetEntry> entries, String name) {
        Set<String> skip = skippedIds();
        if (skip.isEmpty())
            return entries;
        List<DatasetEntry> kept = new ArrayList<>(entries.size());
        List<String> skipped = new ArrayList<>();
        for (DatasetEntry e : entries) {
            if (skip.contains(e.id()))
                skipped.add(e.id());
            else
                kept.add(e);
        }
        if (!skipped.isEmpty())
            System.out.printf("[model-regression] %s: skipped %s (%s)%n", name, String.join(", ", skipped),
                    SKIP_PROPERTY);
        return kept;
    }

    /**
     * Runs the regression check of one model on one dataset: rewrites the reference file in update mode, otherwise
     * recomputes the model on the reference molecules and compares, failing with a full report on any difference.
     * The model training set is released ({@link InsilicoModel#Purge()}) at the end, so a module with many models
     * does not keep every serialized training set in memory.
     *
     * @param model the model to run
     * @param dataset the dataset to run it on
     * @throws Exception on any calculation, read or write error (the test must not pass silently on failure)
     */
    public static void check(InsilicoModel model, Dataset dataset) throws Exception {
        String key = model.getInfo().getKey();
        if (key == null || key.isEmpty())
            fail("Model '" + model.getInfo().getName() + "' has no Key in its model.xml: cannot name its reference file");

        boolean includeAdi = dataset == Dataset.CORE; // the reference always stores the ADI on core; the option only affects the comparison
        String name = key + "-" + dataset.id();
        String description = "Model " + key + " (" + model.getInfo().getName() + ") results on the "
                + dataset.id() + " dataset, generated by ModelRegressionTest";

        try {
            if (RegressionFiles.isUpdateMode()) {
                update(model, dataset, key, name, description, includeAdi);
            } else {
                compare(model, dataset, key, name, includeAdi);
            }
        } finally {
            model.Purge(); // release the serialized training set from memory
        }
    }

    /** Update mode: recompute on the live dataset and rewrite the reference file. */
    private static void update(InsilicoModel model, Dataset dataset, String key, String name, String description,
                               boolean includeAdi) throws Exception {
        List<DatasetEntry> entries = withoutSkipped(datasetEntries(model, dataset, key), name);
        if (entries.isEmpty())
            fail("Dataset " + dataset.id() + " for model " + key + " is empty");

        RegressionFiles.Table actual = timedBuild(model, convert(entries), includeAdi, name);
        RegressionFiles.write(REFERENCE_DIR.resolve(name + ".tsv"), description, actual);
        System.out.printf("[model-regression] %s: reference file rewritten (%d molecules)%n",
                name, actual.rows().size());
    }

    /** Check mode: recompute on the reference molecules and compare. */
    private static void compare(InsilicoModel model, Dataset dataset, String key, String name, boolean includeAdi)
            throws Exception {
        RegressionFiles.Table expected = RegressionFiles.readResource("regression/" + name + ".tsv");
        if (expected == null)
            fail("Reference regression/" + name + ".tsv not found on the classpath for model " + key
                    + ": generate it with " + REGENERATE_HINT);

        // Skipped molecules are neither computed nor compared: drop them from the reference too.
        List<DatasetEntry> referenceEntries = withoutSkipped(entriesFromReference(expected, name), name);
        expected = expected.withoutRows(skippedIds());
        if (referenceEntries.isEmpty())
            fail("Reference regression/" + name + ".tsv has no molecules");

        // The live dataset is read only to warn when it no longer matches the reference (read before Purge).
        // Skipped molecules are left out here too, so they are not reported as a dataset change.
        Set<String> skip = skippedIds();
        List<DatasetEntry> currentEntries = datasetEntries(model, dataset, key).stream()
                .filter(e -> !skip.contains(e.id()))
                .toList();

        RegressionFiles.Table actual = timedBuild(model, convert(referenceEntries), includeAdi, name);

        // ADI column handling: ignored on ts, optional on core.
        RegressionFiles.Table expectedCmp = expected;
        RegressionFiles.Table actualCmp = actual;
        if (dataset == Dataset.CORE && !adiEnabled()) {
            expectedCmp = expected.withoutColumn(ADI_COLUMN);
            actualCmp = actual.withoutColumn(ADI_COLUMN);
        }
        if (dataset == Dataset.TS && System.getProperty(ADI_PROPERTY) != null)
            System.out.printf("[model-regression] %s: %s is ignored on the ts dataset (no ADI for training molecules)%n",
                    name, ADI_PROPERTY);

        Map<String, String> smilesById = toSmilesById(referenceEntries);
        List<String> notes = moleculeSetNotes(referenceEntries, currentEntries, name);
        RegressionDiff.assertSame(name, "regression/" + name + ".tsv", expectedCmp, actualCmp, smilesById,
                ModelRegression::changeBreakdown, notes);
    }

    /** Runs {@link #buildActual} measuring and printing the elapsed time. */
    private static RegressionFiles.Table timedBuild(InsilicoModel model, List<MoleculeInput> molecules,
                                                    boolean includeAdi, String name) throws Exception {
        long start = System.nanoTime();
        RegressionFiles.Table actual = buildActual(model, molecules, includeAdi);
        System.out.printf("[model-regression] %s: %d molecules computed in %d ms%n",
                name, actual.rows().size(), (System.nanoTime() - start) / 1_000_000);
        return actual;
    }

    /**
     * Runs the model on every molecule of the dataset and builds the result table: one row per molecule with its id,
     * SMILES, one column per result, the main ADI value (only when {@code includeAdi}) and the output status.
     * <p>
     * The model is run exactly as the application does: for {@link InsilicoModelPython} models that use the CDDD
     * descriptors, these are computed first on the <em>valid</em> molecules (invalid molecules fail in
     * {@link InsilicoModel#Execute(InsilicoMolecule) Execute} before the descriptor lookup, so they are left out of the
     * CDDD batch, which would otherwise reject the whole dataset) and the descriptor generator is set on the model.
     * If the CDDD computation fails, the test fails. Python models that support it then predict the whole dataset in
     * one batch ({@link #prepareBatch}), as the application runner does. {@code Execute} is then called without
     * {@code setSkipADandTSLoading}, which would change the predictions of the models that rely on the training set.
     * A molecule whose execution throws is recorded as an {@code EXCEPTION} row instead of aborting the whole dataset,
     * so a single failure does not hide the results of the others and a change in error behaviour is still reported.
     *
     * @param model the model to run
     * @param molecules the input molecules
     * @param includeAdi whether to add the ADI column (core dataset only)
     * @return the result table
     * @throws Exception on CDDD setup errors
     */
    public static RegressionFiles.Table buildActual(InsilicoModel model, List<MoleculeInput> molecules,
                                                    boolean includeAdi) throws Exception {
        String key = model.getInfo().getKey();
        boolean usesCddd = (model instanceof InsilicoModelPython python) && python.isUsingCdddDescriptor();
        if (usesCddd) {
            // Only valid molecules: an invalid SMILES would make the whole CDDD batch fail.
            List<String> inputSmiles = new ArrayList<>();
            for (MoleculeInput m : molecules)
                if (m.molecule().IsValid())
                    inputSmiles.add(m.molecule().getInputSMILES());
            if (!inputSmiles.isEmpty()) {
                CdddDescriptors cddd = cdddFor(inputSmiles, key);
                ((InsilicoModelPython) model).setDescriptorGenerator(cddd);
                ((InsilicoModelPython) model).setCdddDescriptorsForBatch(cddd);
            }
        }

        try {
            prepareBatch(model, molecules);

            // First pass: execute every molecule and keep its output values.
            List<Row> rows = new ArrayList<>();
            String[] resultNames = null;
            for (MoleculeInput m : molecules) {
                String[] results = null;
                String adi = "";
                String status;
                try {
                    InsilicoModelOutput out = model.Execute(m.molecule());
                    results = out.getResults();
                    if (results != null && resultNames == null)
                        resultNames = model.GetResultsName().clone();
                    if (includeAdi && out.getADI() != null)
                        adi = RegressionFiles.format(out.getADI().GetIndexValue());
                    status = statusLabel(out.getStatus());
                } catch (Exception e) {
                    status = "EXCEPTION";
                    System.out.printf("[model-regression] %s: molecule %s threw %s: %s%n",
                            key, m.id(), e.getClass().getSimpleName(), e.getMessage());
                }
                rows.add(new Row(m.id(), m.smiles(), results, adi, status));
            }
            if (resultNames == null)
                resultNames = new String[0]; // every molecule failed: no result columns available

            // Second pass: build the table now that the result columns are known.
            List<String> header = new ArrayList<>();
            header.add(ID_COLUMN);
            header.add(SMILES_COLUMN);
            header.addAll(List.of(resultNames));
            if (includeAdi)
                header.add(ADI_COLUMN);
            header.add(STATUS_COLUMN);
            RegressionFiles.Table table = new RegressionFiles.Table(header);

            for (Row row : rows) {
                List<String> values = new ArrayList<>();
                values.add(RegressionFiles.cell(row.smiles));
                for (int i = 0; i < resultNames.length; i++) {
                    boolean present = row.results != null && row.results.length == resultNames.length
                            && row.results[i] != null;
                    values.add(present ? RegressionFiles.cell(row.results[i]) : "");
                }
                if (includeAdi)
                    values.add(row.adi);
                values.add(row.status);
                table.addRow(row.id, values);
            }
            return table;
        } finally {
            if (model instanceof InsilicoModelPython python)
                python.clearBatch();
            // the CDDD descriptors are not disposed here: they are kept for the next model, see cdddFor
        }
    }

    /**
     * CDDD descriptors of a set of molecules, computed once and reused while the molecules do not change.
     * <p>
     * The descriptors depend only on the molecules, not on the model, and a module may hold many models sharing the
     * same dataset: ONTOX-assay bundles 25 models, which on the {@code core} dataset would otherwise mean computing the
     * same 607 descriptor sets 25 times. {@link ModelRegressionTest} runs one dataset at a time across all the models
     * of a module, so keeping only the last set is enough to serve every model of a dataset.
     * <p>
     * The cached set is released by {@link #releaseCddd()}, called at the end of the test class.
     *
     * @param inputSmiles the molecules, in dataset order
     * @param key the model key, for the log lines and the error message
     * @return the descriptors, already computed
     * @throws Exception if the descriptors cannot be computed or the previous ones cannot be released
     */
    private static CdddDescriptors cdddFor(List<String> inputSmiles, String key) throws Exception {
        if (cachedCddd != null && cachedCdddSmiles.equals(inputSmiles)) {
            System.out.printf("[model-regression] %s: CDDD descriptors of the previous model reused (%d molecules)%n",
                    key, inputSmiles.size());
            return cachedCddd;
        }

        releaseCddd();
        long start = System.nanoTime();
        // false: configure the CDDD conda/python environment if needed, so the test is self-contained.
        CdddDescriptors cddd = new CdddDescriptors(inputSmiles, false, null);
        boolean ok = cddd.calculateDescriptors();
        System.out.printf("[model-regression] %s: CDDD on %d valid molecules in %d ms%n",
                key, inputSmiles.size(), (System.nanoTime() - start) / 1_000_000);
        if (!ok) {
            cddd.dispose();
            fail("CDDD descriptors calculation failed for model " + key);
        }
        cachedCddd = cddd;
        cachedCdddSmiles = List.copyOf(inputSmiles);
        return cddd;
    }

    /**
     * Releases the cached CDDD descriptors, deleting their temporary files.
     *
     * @throws IOException if the temporary files cannot be deleted
     */
    public static void releaseCddd() throws IOException {
        if (cachedCddd == null)
            return;
        CdddDescriptors cddd = cachedCddd;
        cachedCddd = null;
        cachedCdddSmiles = null;
        cddd.dispose();
    }

    /**
     * Predicts the whole dataset in a single Python execution on the models that support it, as
     * {@code InsilicoModelRunnerByMolecule} does, so that {@link InsilicoModel#Execute(InsilicoMolecule) Execute}
     * then reads the cached result instead of starting one Python process per molecule.
     * <p>
     * A model that declares {@link InsilicoModelPython#supportsBatchExecution()} but ends up without an active batch
     * has silently fallen back to the per-molecule execution: the predictions would still be right, but the test would
     * be far too slow to be usable, and the fallback itself is a regression of the batch. The test fails in that case.
     *
     * @param model the model to run
     * @param molecules the input molecules
     * @throws Exception if the batch cannot be prepared
     */
    private static void prepareBatch(InsilicoModel model, List<MoleculeInput> molecules) throws Exception {
        if (!(model instanceof InsilicoModelPython python) || !python.supportsBatchExecution())
            return;

        List<InsilicoMolecule> mols = new ArrayList<>(molecules.size());
        boolean anyValid = false;
        for (MoleculeInput m : molecules) {
            mols.add(m.molecule());
            anyValid |= m.molecule().IsValid();
        }
        if (!anyValid)
            return; // nothing to predict: prepareBatch would legitimately leave the batch inactive

        String key = model.getInfo().getKey();
        long start = System.nanoTime();
        python.prepareBatch(mols);
        System.out.printf("[model-regression] %s: Python batch on %d molecules in %d ms%n",
                key, mols.size(), (System.nanoTime() - start) / 1_000_000);
        if (!python.isBatchActive())
            fail("Model " + key + " supports batch execution but the batch did not start (it fell back to one "
                    + "Python process per molecule): see the log above for the cause");
    }

    /** One molecule output, kept between the two passes of {@link #buildActual}. */
    private record Row(String id, String smiles, String[] results, String adi, String status) { }

    /**
     * Rows of a dataset (id and SMILES), without conversion.
     *
     * @param model the model (for the ts dataset)
     * @param dataset the dataset
     * @param key the model key, for error messages
     * @return the rows, in dataset order
     * @throws Exception on read errors or a missing training set
     */
    private static List<DatasetEntry> datasetEntries(InsilicoModel model, Dataset dataset, String key)
            throws Exception {
        return dataset == Dataset.TS ? trainingSetEntries(model, key) : coreEntries();
    }

    /** Training-set rows: a stable id (the training-set molecule id, disambiguated if repeated) and its SMILES. */
    private static List<DatasetEntry> trainingSetEntries(InsilicoModel model, String key) throws Exception {
        iTrainingSet ts = model.GetTrainingSet();
        if (ts == null)
            fail("Model " + key + " has no training set: cannot build the ts dataset");

        int size = ts.getMoleculesSize();
        List<DatasetEntry> entries = new ArrayList<>(size);
        Set<String> usedIds = new HashSet<>();
        for (int i = 0; i < size; i++) {
            String id = String.valueOf(ts.getId(i));
            if (!usedIds.add(id)) {
                id = id + "_" + i;
                usedIds.add(id);
            }
            entries.add(new DatasetEntry(id, ts.getSMILES(i)));
        }
        return entries;
    }

    /** Core dataset rows, read from the classpath resource ({@code NAME SMILES SOURCE CATEGORY}). */
    private static List<DatasetEntry> coreEntries() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CORE_MOLECULES_RESOURCE);
        if (is == null) {
            fail("Core dataset " + CORE_MOLECULES_RESOURCE + " not found on the classpath (core test-jar)");
            return List.of(); // unreachable, fail() throws
        }
        List<DatasetEntry> entries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#"))
                    continue;
                if (!headerSkipped) { // header: NAME SMILES SOURCE CATEGORY
                    headerSkipped = true;
                    continue;
                }
                String[] fields = line.split("\t", -1);
                if (fields.length < 2)
                    throw new IOException(CORE_MOLECULES_RESOURCE + ":" + lineNumber
                            + ": expected at least NAME and SMILES, found " + fields.length + " fields");
                entries.add(new DatasetEntry(fields[0], fields[1]));
            }
        }
        return entries;
    }

    /** Extracts the molecules (id and SMILES column) stored in a reference table. */
    private static List<DatasetEntry> entriesFromReference(RegressionFiles.Table table, String name) {
        int smilesIdx = table.valueIndex(SMILES_COLUMN);
        if (smilesIdx < 0) {
            fail("Reference regression/" + name + ".tsv has no " + SMILES_COLUMN
                    + " column: regenerate it with " + REGENERATE_HINT);
            return List.of(); // unreachable, fail() throws
        }
        List<DatasetEntry> entries = new ArrayList<>(table.rows().size());
        for (Map.Entry<String, List<String>> row : table.rows().entrySet())
            entries.add(new DatasetEntry(row.getKey(), row.getValue().get(smilesIdx)));
        return entries;
    }

    /** Converts dataset rows into molecules. */
    private static List<MoleculeInput> convert(List<DatasetEntry> entries) {
        List<MoleculeInput> molecules = new ArrayList<>(entries.size());
        for (DatasetEntry e : entries) {
            InsilicoMolecule mol = SmilesMolecule.Convert(e.smiles());
            mol.SetId(e.id());
            molecules.add(new MoleculeInput(e.id(), e.smiles(), mol));
        }
        return molecules;
    }

    /** @return an id -&gt; SMILES map, in dataset order */
    private static Map<String, String> toSmilesById(List<DatasetEntry> entries) {
        Map<String, String> map = new LinkedHashMap<>();
        for (DatasetEntry e : entries)
            map.put(e.id(), e.smiles());
        return map;
    }

    /**
     * Builds the "prediction changed vs only ADI changed" breakdown added to the diff report.
     *
     * @param diffs the changed cells
     * @return the summary lines
     */
    private static List<String> changeBreakdown(List<RegressionDiff.CellDiff> diffs) {
        Set<String> predictionRows = new HashSet<>();
        Set<String> adiRows = new HashSet<>();
        Set<String> statusRows = new HashSet<>();
        Set<String> smilesRows = new HashSet<>();
        for (RegressionDiff.CellDiff d : diffs) {
            switch (d.column()) {
                case ADI_COLUMN -> adiRows.add(d.id());
                case STATUS_COLUMN -> statusRows.add(d.id());
                case SMILES_COLUMN -> smilesRows.add(d.id());
                default -> predictionRows.add(d.id()); // a result column
            }
        }
        Set<String> onlyAdi = new HashSet<>(adiRows);
        onlyAdi.removeAll(predictionRows);
        onlyAdi.removeAll(statusRows);
        onlyAdi.removeAll(smilesRows);

        List<String> lines = new ArrayList<>();
        lines.add("Rows with a changed prediction: " + predictionRows.size());
        lines.add("Rows where only the ADI changed: " + onlyAdi.size());
        if (!statusRows.isEmpty())
            lines.add("Rows with a changed status: " + statusRows.size());
        if (!smilesRows.isEmpty())
            lines.add("Rows with a changed SMILES: " + smilesRows.size());
        return lines;
    }

    /**
     * Warns when the live dataset (current training set or core file) no longer has the same molecules as the
     * reference: ids added or removed, and ids whose SMILES changed.
     *
     * @param referenceEntries molecules stored in the reference
     * @param currentEntries molecules of the current live dataset
     * @param name the table name, for the regenerate hint
     * @return the warning lines (empty when the molecule sets match)
     */
    private static List<String> moleculeSetNotes(List<DatasetEntry> referenceEntries,
                                                 List<DatasetEntry> currentEntries, String name) {
        Map<String, String> reference = toSmilesById(referenceEntries);
        Map<String, String> current = toSmilesById(currentEntries);

        Set<String> removed = new HashSet<>(reference.keySet());
        removed.removeAll(current.keySet());
        Set<String> added = new HashSet<>(current.keySet());
        added.removeAll(reference.keySet());
        int changedSmiles = 0;
        for (Map.Entry<String, String> e : reference.entrySet()) {
            String cur = current.get(e.getKey());
            if (cur != null && !cur.equals(e.getValue()))
                changedSmiles++;
        }
        if (removed.isEmpty() && added.isEmpty() && changedSmiles == 0)
            return List.of();
        return List.of("WARNING: the live dataset no longer matches regression/" + name + ".tsv: "
                + removed.size() + " molecules only in the reference, " + added.size()
                + " only in the current dataset, " + changedSmiles + " with a changed SMILES. "
                + "Regenerate the reference with " + REGENERATE_HINT);
    }

    /**
     * Stable label of an output status, so a change of behaviour on errors is visible in the report.
     *
     * @param status an {@link InsilicoModelOutput} status constant
     * @return the label
     */
    private static String statusLabel(short status) {
        return switch (status) {
            case InsilicoModelOutput.OUTPUT_OK -> "OK";
            case InsilicoModelOutput.OUTPUT_OK_AD_MISSING -> "OK_AD_MISSING";
            case InsilicoModelOutput.OUTPUT_OK_AD_NOT_APPLICABLE -> "OK_AD_NOT_APPLICABLE";
            case InsilicoModelOutput.OUTPUT_NOT_CALCULATED -> "NOT_CALCULATED";
            case InsilicoModelOutput.OUTPUT_ERROR -> "ERROR";
            default -> "STATUS_" + status;
        };
    }
}
