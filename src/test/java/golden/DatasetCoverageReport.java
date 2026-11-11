package golden;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.molecule.InsilicoMolecule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Measures how well the golden dataset exercises the descriptor blocks and writes
 * {@code src/test/resources/golden/COVERAGE.md}. A per-molecule check file for manual review is written to
 * {@code target/golden-coverage/molecules-check.tsv}.
 * <p>
 * For every descriptor it counts, on the valid molecules: how many molecules have a value different from zero, how
 * many distinct values occur and how many molecules got {@link Descriptor#MISSING_VALUE}.
 * <p>
 * Run with: {@code mvn test -Pgolden -Dgolden.coverage=true -Dtest=DatasetCoverageReport}
 */
@Tag("golden")
@EnabledIfSystemProperty(named = "golden.coverage", matches = "true")
class DatasetCoverageReport {

    static final Path COVERAGE_FILE = GoldenDataset.GOLDEN_DIR.resolve("COVERAGE.md");
    static final Path CHECK_FILE = Path.of("target", "golden-coverage", "molecules-check.tsv");
    static final Path NON_ZERO_FILE = Path.of("target", "golden-coverage", "non-zero-descriptors.tsv");

    /** A descriptor is considered covered if it is non-zero in at least this many molecules. */
    private static final int MIN_NON_ZERO = 2;

    /** Descriptors that cannot be non-zero with the current code, with the reason (shown in the report). */
    private static final Map<String, String> KNOWN_UNREACHABLE = Map.of(
            "H-055", "H on a C0(sp3) whose neighbouring C carries 4 halogens: that C would have 5 neighbours.",
            "F-101", "`GhoseCrippenACF.ProcessAtom` returns early for every halogen, before the halide-ion branch.",
            "Cl-102", "Same as F-101: halide-ion branch is dead code ([Cl-] is in the dataset, value is 0).",
            "Br-103", "Same as F-101: halide-ion branch is dead code ([Br-] is in the dataset, value is 0).",
            "I-104", "Same as F-101: halide-ion branch is dead code ([I-] is in the dataset, value is 0).");

    /** Free-form notes on the behaviour observed while building the dataset (shown in the report). */
    private static final List<String> NOTES = List.of(
            "`FunctionalGroups`: `nRCOX` and `nArCOX` have the same SMARTS (aromatic only), as do `nRCSSH` and "
                    + "`nArCSSH` (aliphatic only): each pair always has identical values. Aliphatic acyl halides "
                    + "(e.g. acetyl chloride) are counted by neither.",
            "An empty SMILES is converted to a *valid* molecule whose `GetStructure()` fails: every descriptor is "
                    + "MISSING_VALUE (CUR-432).",
            "`Constitutional` returns MISSING_VALUE for Sv/Mv or Se/Me on elements without van der Waals volume "
                    + "or electronegativity (e.g. Pt, Gd, He, U, Ge, Al).",
            "`[238U]` loses its isotope in the regenerated SMILES, `[235U]` keeps it; `[C]` becomes methane.",
            "Fullerene C60 is parsed but rejected by the normalization (kept as invalid on purpose).");

    /** Statistics of a single descriptor. */
    private static final class Stats {
        int nonZero;
        int missing;
        final Set<Double> distinct = new HashSet<>();
        final List<String> nonZeroExamples = new ArrayList<>();
    }

    @Test
    void writeCoverageReport() throws Exception {
        List<GoldenDataset.Item> items = GoldenDataset.load();

        Map<String, Map<String, Stats>> statsByBlock = new LinkedHashMap<>();
        Map<String, Long> timeByBlock = new LinkedHashMap<>();
        Map<String, List<String>> missingOnValid = new TreeMap<>();
        StringBuilder nonZeroByMolecule = new StringBuilder("NAME\tCATEGORY\tN_ATOMS\tNON_ZERO_DESCRIPTORS\n");
        StringBuilder check = new StringBuilder("NAME\tCATEGORY\tVALID\tEXPECTED_INVALID\tN_ATOMS\tSMILES_IN\t"
                + "SMILES_OUT\tERRORS\n");

        for (GoldenBlocks.Block block : GoldenBlocks.ALL) {
            Map<String, Stats> stats = new LinkedHashMap<>();
            for (String name : block.factory().get().GetAllNames())
                stats.put(name, new Stats());
            statsByBlock.put(block.name(), stats);
            timeByBlock.put(block.name(), 0L);
        }

        for (GoldenDataset.Item item : items) {
            InsilicoMolecule mol = item.molecule();
            GoldenDataset.Entry entry = item.entry();
            int nAtoms = -1;
            if (mol.IsValid()) {
                try {
                    nAtoms = mol.GetStructure().getAtomCount();
                } catch (Exception e) {
                    // left at -1
                }
            }
            check.append(entry.name()).append('\t').append(entry.category()).append('\t')
                    .append(mol.IsValid()).append('\t').append(entry.expectedInvalid()).append('\t')
                    .append(nAtoms).append('\t').append(entry.smiles()).append('\t')
                    .append(mol.IsValid() ? mol.GetSMILES() : "").append('\t')
                    .append(mol.IsValid() ? "" : mol.GetErrors().GetMessages().replaceAll("\\s+", " ").replace('\t', ' '))
                    .append('\n');

            if (!mol.IsValid())
                continue;

            List<String> nonZeroNames = new ArrayList<>();
            for (GoldenBlocks.Block block : GoldenBlocks.ALL) {
                DescriptorBlock db = block.factory().get();
                long start = System.nanoTime();
                db.Calculate(mol);
                timeByBlock.merge(block.name(), System.nanoTime() - start, Long::sum);

                Map<String, Stats> stats = statsByBlock.get(block.name());
                String[] names = db.GetAllNames();
                double[] values = db.GetAllValues();
                for (int i = 0; i < names.length; i++) {
                    Stats s = stats.get(names[i]);
                    s.distinct.add(values[i]);
                    if (values[i] == Descriptor.MISSING_VALUE) {
                        s.missing++;
                        missingOnValid.computeIfAbsent(entry.name(), k -> new ArrayList<>())
                                .add(block.name() + "." + names[i]);
                    } else if (values[i] != 0) {
                        s.nonZero++;
                        nonZeroNames.add(block.name() + "." + names[i]);
                        if (s.nonZeroExamples.size() < 3)
                            s.nonZeroExamples.add(entry.name());
                    }
                }
            }
            nonZeroByMolecule.append(entry.name()).append('\t').append(entry.category()).append('\t')
                    .append(nAtoms).append('\t').append(String.join(",", nonZeroNames)).append('\n');
        }

        Files.createDirectories(CHECK_FILE.getParent());
        Files.writeString(CHECK_FILE, check.toString(), StandardCharsets.UTF_8);
        Files.writeString(NON_ZERO_FILE, nonZeroByMolecule.toString(), StandardCharsets.UTF_8);
        String report = buildReport(items, statsByBlock, timeByBlock, missingOnValid);
        Files.writeString(COVERAGE_FILE, report, StandardCharsets.UTF_8);
        System.out.println(report);
    }

    private static String buildReport(List<GoldenDataset.Item> items, Map<String, Map<String, Stats>> statsByBlock,
                                      Map<String, Long> timeByBlock, Map<String, List<String>> missingOnValid) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Golden dataset coverage\n\n");
        sb.append("Generated by `golden.DatasetCoverageReport` on ").append(LocalDate.now())
                .append(" from `molecules.tsv`.\n\n");

        // Dataset composition
        Map<String, int[]> bySource = new TreeMap<>();
        List<GoldenDataset.Item> unexpectedValidity = new ArrayList<>();
        for (GoldenDataset.Item item : items) {
            String prefix = item.entry().name().replaceAll("-.*", "");
            int[] c = bySource.computeIfAbsent(prefix, k -> new int[2]);
            c[item.molecule().IsValid() ? 0 : 1]++;
            if (item.molecule().IsValid() == item.entry().expectedInvalid())
                unexpectedValidity.add(item);
        }
        sb.append("## Dataset\n\n| Source | Valid | Invalid |\n|---|---:|---:|\n");
        int totValid = 0, totInvalid = 0;
        for (Map.Entry<String, int[]> e : bySource.entrySet()) {
            sb.append("| ").append(e.getKey()).append(" | ").append(e.getValue()[0]).append(" | ")
                    .append(e.getValue()[1]).append(" |\n");
            totValid += e.getValue()[0];
            totInvalid += e.getValue()[1];
        }
        sb.append("| **Total** | **").append(totValid).append("** | **").append(totInvalid).append("** |\n\n");
        sb.append("Sources: `BENCH` = benchmark, `CUR` = hand-curated (`curated.tsv`), `TS` = MaxMin sample of the ")
                .append("training sets, `TSR` = random sample of the training sets.\n\n");

        sb.append("### Validity different from the expected one\n\n");
        if (unexpectedValidity.isEmpty())
            sb.append("None.\n\n");
        else {
            sb.append("| Name | SMILES | Category | Valid |\n|---|---|---|---|\n");
            for (GoldenDataset.Item item : unexpectedValidity)
                sb.append("| ").append(item.entry().name()).append(" | `").append(item.entry().smiles())
                        .append("` | ").append(item.entry().category()).append(" | ")
                        .append(item.molecule().IsValid()).append(" |\n");
            sb.append('\n');
        }

        // Timing
        sb.append("## Calculation time (valid molecules)\n\n| Block | Seconds |\n|---|---:|\n");
        for (Map.Entry<String, Long> e : timeByBlock.entrySet())
            sb.append("| ").append(e.getKey()).append(" | ")
                    .append(String.format(Locale.ROOT, "%.1f", e.getValue() / 1e9)).append(" |\n");
        sb.append('\n');

        // Uncovered descriptors
        sb.append("## Descriptors not covered (non-zero in fewer than ").append(MIN_NON_ZERO)
                .append(" molecules)\n\n| Block | Descriptor | Non-zero | Examples | Reason |\n|---|---|---:|---|---|\n");
        int uncovered = 0, total = 0;
        for (Map.Entry<String, Map<String, Stats>> b : statsByBlock.entrySet())
            for (Map.Entry<String, Stats> d : b.getValue().entrySet()) {
                total++;
                if (d.getValue().nonZero < MIN_NON_ZERO) {
                    uncovered++;
                    sb.append("| ").append(b.getKey()).append(" | ").append(d.getKey()).append(" | ")
                            .append(d.getValue().nonZero).append(" | ")
                            .append(String.join(", ", d.getValue().nonZeroExamples)).append(" | ")
                            .append(KNOWN_UNREACHABLE.getOrDefault(d.getKey(), "**to be investigated**"))
                            .append(" |\n");
                }
            }
        sb.append("\nCovered: ").append(total - uncovered).append(" / ").append(total).append(" descriptors.\n\n");

        sb.append("## Notes\n\n");
        for (String note : NOTES)
            sb.append("- ").append(note).append('\n');
        sb.append('\n');

        // Missing values on valid molecules
        sb.append("## Valid molecules with MISSING_VALUE (-999)\n\n");
        if (missingOnValid.isEmpty())
            sb.append("None.\n\n");
        else {
            sb.append("| Name | Descriptors |\n|---|---|\n");
            for (Map.Entry<String, List<String>> e : missingOnValid.entrySet()) {
                List<String> d = e.getValue();
                String shown = d.size() > 6 ? String.join(", ", d.subList(0, 6)) + ", ... (" + d.size() + ")"
                        : String.join(", ", d);
                sb.append("| ").append(e.getKey()).append(" | ").append(shown).append(" |\n");
            }
            sb.append('\n');
        }

        // Full table
        sb.append("## All descriptors\n\n");
        for (Map.Entry<String, Map<String, Stats>> b : statsByBlock.entrySet()) {
            sb.append("### ").append(b.getKey()).append("\n\n| Descriptor | Non-zero | Distinct values | Missing |\n")
                    .append("|---|---:|---:|---:|\n");
            for (Map.Entry<String, Stats> d : b.getValue().entrySet())
                sb.append("| ").append(d.getKey()).append(" | ").append(d.getValue().nonZero).append(" | ")
                        .append(d.getValue().distinct.size()).append(" | ").append(d.getValue().missing)
                        .append(" |\n");
            sb.append('\n');
        }
        return sb.toString();
    }
}
