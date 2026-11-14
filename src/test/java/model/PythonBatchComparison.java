package model;

import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.InsilicoModelPython;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.python.CdddDescriptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Checks that running a Python model in batch gives exactly the same results as running it one molecule at a time,
 * and reports how much time the batch saves.
 * <p>
 * It is published in the test-jar of the core so that every {@code VegaModels-*} module can reuse it: a module only
 * has to build its own model (the constructors differ from model to model) and call {@link #run(InsilicoModelPython,
 * List)}. Correctness comes first and the timings are only meaningful once the results are identical: a model whose
 * two runs disagree must not be enabled for batch execution.
 * <p>
 * The guard that refuses a misaligned batch belongs to the core mechanism, not to the single model, and is verified
 * separately once; this class checks what depends on the model and on its Python script.
 *
 * @see InsilicoModelPython#prepareBatch(List)
 */
public final class PythonBatchComparison {

    /** Molecules taken from the golden dataset of the core, varied enough to exercise a model. */
    public static final List<String> DEFAULT_MOLECULES = Collections.unmodifiableList(Arrays.asList(
            "c1ccc2c(c1)ccc1c3ccccc3ccc21",
            "c1ccc2c(c1)c1ccccc1c1ccccc21",
            "C1=Cc2cccc3cccc1c23",
            "C1=Cc2ccccc2C1",
            "C1C2C3CC4C2C1C34",
            "CC12CCC(CC1=O)C2(C)C",
            "C1CCC2C(C1)CCC1C3CCCCC3CCC21",
            "CC(C)CCCC(C)C1CCC2C3CC=C4CC(O)CCC4(C)C3CCC12C",
            "CC1(C)SC2C(NC(=O)Cc3ccccc3)C(=O)N2C1C(=O)O",
            "Cc1cc(NS(=O)(=O)c2ccc(N)cc2)no1",
            "OC[C@H]1O[C@@](CO)(O[C@H]2O[C@H](CO)[C@@H](O)[C@H](O)[C@H]2O)[C@@H](O)[C@@H]1O",
            "COc1cc(C=O)ccc1O",
            "C=CC1CN2CCC1CC2C(O)c1ccnc2ccc(OC)cc12",
            "CN1CC[C@]23c4c5ccc(O)c4O[C@H]2[C@@H](O)C=C[C@H]3[C@H]1C5",
            "CN1CCC[C@H]1c1cccnc1",
            "NCCc1c[nH]c2ccc(O)cc12",
            "NCCc1ccc(O)c(O)c1",
            "Nc1ncnc2c1ncn2[C@@H]1O[C@H](COP(=O)(O)OP(=O)(O)OP(=O)(O)O)[C@@H](O)[C@H]1O",
            "C1COCCOCCOCCOCCOCCO1",
            "CC1=C(/C=C/C(C)=C/C=C/C(C)=C/C=C/C=C(C)/C=C/C=C(C)/C=C/C2=C(C)CCCC2(C)C)C(C)(C)CCC1"
    ));

    private static final String INVALID_SMILES = "this-is-not-a-smiles";

    private PythonBatchComparison() { }

    /**
     * Runs the whole check on the default molecules: results must be identical, then the edge cases, then the times.
     *
     * @param model the model to check, already built
     * @return the number of problems found, 0 when everything agrees
     * @throws Exception on setup errors
     */
    public static int run(InsilicoModelPython model) throws Exception {
        return run(model, DEFAULT_MOLECULES);
    }

    /**
     * Runs the whole check: identical results, edge cases, and timings.
     *
     * @param model the model to check, already built
     * @param smilesList the molecules to use
     * @return the number of problems found, 0 when everything agrees
     * @throws Exception on setup errors
     */
    public static int run(InsilicoModelPython model, List<String> smilesList) throws Exception {

        System.out.println("=== Batch comparison of " + model.getInfo().getName()
                + " on " + smilesList.size() + " molecules ===");

        if (!model.supportsBatchExecution()) {
            System.out.println("FAILED: supportsBatchExecution() is false, this model is not enabled for batch");
            return 1;
        }

        int problems = compareAndTime(model, smilesList);
        problems += edgeCases(model, smilesList);

        System.out.println();
        System.out.println(problems == 0
                ? "ALL CHECKS OK for " + model.getInfo().getName()
                : problems + " PROBLEM(S) for " + model.getInfo().getName());
        return problems;
    }

    /**
     * Checks a model that is <em>not</em> enabled for batch execution: it must still run one molecule at a time and
     * produce its results, and no batch must ever become active.
     * <p>
     * It is needed because the batch mode changed {@code calculatePythonModel}, which every Python model calls,
     * including the ones that stay per-molecule. This verifies that the new code is inert for them: that
     * {@link InsilicoModelPython#prepareBatch(List)} does nothing and the cache is never used.
     * <p>
     * Note what this does and does not show: it shows that the per-molecule path still works end to end, not that
     * the predicted values are the same as before the change, which would need a reference recorded with the
     * previous version of the core.
     *
     * @param model the model to check, already built
     * @param smilesList the molecules to use
     * @return the number of problems found, 0 when the model behaves as expected
     * @throws Exception on setup errors
     */
    public static int runPerMoleculeOnly(InsilicoModelPython model, List<String> smilesList) throws Exception {

        System.out.println("=== Per-molecule check of " + model.getInfo().getName()
                + " on " + smilesList.size() + " molecules ===");

        int problems = 0;
        if (model.supportsBatchExecution()) {
            System.out.println("FAILED: supportsBatchExecution() is true, this model is enabled for batch");
            problems++;
        }

        CdddDescriptors cddd = prepareDescriptors(model, smilesList);
        if (model.isUsingCdddDescriptor() && cddd == null) {
            System.out.println("FAILED: the CDDD descriptors could not be computed");
            return problems + 1;
        }

        try {
            // prepareBatch must be a no-op on a model that did not opt in
            model.prepareBatch(convert(smilesList));
            if (model.isBatchActive()) {
                System.out.println("FAILED: a batch became active on a model that does not support it");
                problems++;
            }

            long start = System.nanoTime();
            List<InsilicoMolecule> molecules = convert(smilesList);
            List<String[]> results = execute(model, molecules);
            System.out.printf("Per-molecule run: %d ms%n", elapsedMs(start));

            String[] names = model.GetResultsName();
            for (int i = 0; i < results.size(); i++) {
                String[] row = results.get(i);
                System.out.println("--- " + smilesList.get(i) + " ---");
                if (row == null) {
                    System.out.println("  no result");
                    problems++;
                    continue;
                }
                for (int c = 0; c < row.length; c++) {
                    System.out.println("  " + (names != null && c < names.length ? names[c] : "column " + c)
                            + ": " + row[c]);
                }
            }
        } finally {
            release(model, cddd);
        }

        System.out.println();
        System.out.println(problems == 0
                ? "PER-MOLECULE PATH OK for " + model.getInfo().getName()
                : problems + " PROBLEM(S) for " + model.getInfo().getName());
        return problems;
    }

    /**
     * Main check: the same molecules are run per molecule and then in batch, and every result cell must match.
     *
     * @return the number of problems found
     */
    private static int compareAndTime(InsilicoModelPython model, List<String> smilesList) throws Exception {

        CdddDescriptors cddd = prepareDescriptors(model, smilesList);
        if (model.isUsingCdddDescriptor() && cddd == null) {
            System.out.println("FAILED: the CDDD descriptors could not be computed");
            return 1;
        }

        List<InsilicoMolecule> molecules = convert(smilesList);

        try {
            model.clearBatch();
            long start = System.nanoTime();
            List<String[]> perMolecule = execute(model, molecules);
            long perMoleculeMs = elapsedMs(start);
            System.out.printf("Per-molecule run: %d ms%n", perMoleculeMs);

            start = System.nanoTime();
            model.prepareBatch(molecules);
            long prepareMs = elapsedMs(start);
            if (!model.isBatchActive()) {
                System.out.println("FAILED: the batch was not prepared, the model fell back to the per-molecule run");
                return 1;
            }

            start = System.nanoTime();
            List<String[]> batch = execute(model, molecules);
            long batchMs = elapsedMs(start) + prepareMs;
            System.out.printf("Batch run: %d ms (of which %d ms to prepare)%n", batchMs, prepareMs);

            int differences = compare(model, smilesList, perMolecule, batch);
            if (differences > 0) {
                System.out.println("FAILED: " + differences + " differing cells between the two runs");
                return differences;
            }

            System.out.println("OK: the two runs give identical results");
            if (batchMs > 0) {
                System.out.printf("Speed-up: %.1fx (%d ms -> %d ms)%n",
                        (double) perMoleculeMs / batchMs, perMoleculeMs, batchMs);
            }
            return 0;
        } finally {
            release(model, cddd);
        }
    }

    /**
     * The cases where a batch can silently go wrong: a single molecule, a molecule the model cannot predict, the
     * same molecule twice, and a set larger than the chunk size.
     *
     * @return the number of problems found
     */
    private static int edgeCases(InsilicoModelPython model, List<String> smilesList) throws Exception {

        int problems = 0;
        String first = smilesList.get(0);
        String second = smilesList.size() > 1 ? smilesList.get(1) : first;

        problems += edgeCase(model, "single molecule", Arrays.asList(first), 0);
        problems += edgeCase(model, "molecule the model cannot predict",
                Arrays.asList(first, INVALID_SMILES, second), 0);
        problems += edgeCase(model, "repeated molecule", Arrays.asList(first, second, first), 0);

        if (smilesList.size() >= 7) {
            problems += edgeCase(model, "set crossing the chunk boundary", smilesList.subList(0, 7), 3);
        }
        return problems;
    }

    /**
     * Runs one edge case per molecule and in batch, and compares.
     *
     * @param chunkSize the chunk size to use, or 0 to keep the current one
     * @return the number of problems found
     */
    private static int edgeCase(InsilicoModelPython model, String name, List<String> smilesList, int chunkSize)
            throws Exception {

        System.out.println();
        System.out.println("--- " + name + " (" + smilesList.size() + " molecules"
                + (chunkSize > 0 ? ", chunk size " + chunkSize : "") + ") ---");

        CdddDescriptors cddd = prepareDescriptors(model, smilesList);
        if (model.isUsingCdddDescriptor() && cddd == null) {
            System.out.println("FAILED (" + name + "): the CDDD descriptors could not be computed");
            return 1;
        }

        int previousChunkSize = model.getBatchChunkSize();
        if (chunkSize > 0) {
            model.setBatchChunkSize(chunkSize);
        }

        List<InsilicoMolecule> molecules = convert(smilesList);

        try {
            model.clearBatch();
            List<String[]> perMolecule = execute(model, molecules);

            model.prepareBatch(molecules);
            List<String[]> batch = execute(model, molecules);

            int differences = compare(model, smilesList, perMolecule, batch);
            if (differences > 0) {
                System.out.println("FAILED (" + name + "): " + differences + " differing cells");
                return differences;
            }
            System.out.println("OK");
            return 0;
        } finally {
            model.setBatchChunkSize(previousChunkSize);
            release(model, cddd);
        }
    }

    /**
     * Computes the CDDD descriptors of the given molecules and hands them to the model, as the runner does.
     *
     * @return the descriptors, or null when the model does not use them or they could not be computed
     */
    private static CdddDescriptors prepareDescriptors(InsilicoModelPython model, List<String> smilesList)
            throws Exception {

        if (!model.isUsingCdddDescriptor()) {
            return null;
        }
        CdddDescriptors cddd = new CdddDescriptors(new ArrayList<>(smilesList), true, null);
        if (!cddd.calculateDescriptors()) {
            return null;
        }
        model.setDescriptorGenerator(cddd);
        model.setCdddDescriptorsForBatch(cddd);
        return cddd;
    }

    /** Releases the batch cache and the descriptors of one case. */
    private static void release(InsilicoModelPython model, CdddDescriptors cddd) throws Exception {
        model.clearBatch();
        if (cddd != null) {
            cddd.dispose();
        }
    }

    /** Converts the SMILES into molecules, in order. */
    private static List<InsilicoMolecule> convert(List<String> smilesList) {
        List<InsilicoMolecule> molecules = new ArrayList<>(smilesList.size());
        for (String smiles : smilesList) {
            molecules.add(SmilesMolecule.Convert(smiles));
        }
        return molecules;
    }

    /** Executes the model on every molecule and keeps its results. */
    private static List<String[]> execute(InsilicoModelPython model, List<InsilicoMolecule> molecules)
            throws Exception {

        List<String[]> results = new ArrayList<>(molecules.size());
        for (InsilicoMolecule molecule : molecules) {
            InsilicoModelOutput out = model.Execute(molecule);
            String[] row = out.getResults();
            results.add(row == null ? null : row.clone());
        }
        return results;
    }

    /**
     * Compares the results of the two runs cell by cell, printing every difference.
     *
     * @return the number of differing cells, 0 when the two runs agree
     */
    private static int compare(InsilicoModelPython model, List<String> smilesList,
                               List<String[]> expected, List<String[]> actual) {

        int differences = 0;
        String[] names = model.GetResultsName();

        for (int i = 0; i < expected.size(); i++) {
            String[] e = expected.get(i);
            String[] a = actual.get(i);

            if (e == null || a == null) {
                if (e != a) {
                    System.out.printf("  [%d] %s: per-molecule=%s, batch=%s%n", i, smilesList.get(i),
                            e == null ? "no result" : "result", a == null ? "no result" : "result");
                    differences++;
                } else {
                    System.out.printf("  [%d] %s: no result in either run%n", i, smilesList.get(i));
                }
                continue;
            }

            for (int c = 0; c < Math.min(e.length, a.length); c++) {
                if (!Objects.equals(e[c], a[c])) {
                    System.out.printf("  [%d] %s | %s: per-molecule='%s' batch='%s'%n", i, smilesList.get(i),
                            names != null && c < names.length ? names[c] : ("column " + c), e[c], a[c]);
                    differences++;
                }
            }
        }
        return differences;
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
