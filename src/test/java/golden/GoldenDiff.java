package golden;

import regression.RegressionDiff;
import regression.RegressionFiles;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Golden-test front end of {@link RegressionDiff}: rows and molecule columns are labelled with the SMILES of the
 * golden dataset, and missing reference files point to the {@code golden} Maven profile.
 */
public final class GoldenDiff {

    public static final Path DIFF_DIR = RegressionDiff.DIFF_DIR;

    private static final String REGENERATE_HINT = "mvn test -Pgolden -D" + RegressionFiles.UPDATE_PROPERTY + "=true";

    private GoldenDiff() { }

    /**
     * Checks a computed table against its golden reference file. In update mode ({@code -Dgolden.update=true}) the
     * reference file is rewritten instead.
     *
     * @param name short name of the table, used in report file names
     * @param referenceFile the golden reference file
     * @param description one line describing the content, written in the reference file
     * @param actual the freshly computed table
     * @throws IOException on read/write errors
     */
    public static void check(String name, Path referenceFile, String description, RegressionFiles.Table actual)
            throws IOException {
        check(name, referenceFile, description, actual, diffs -> List.of());
    }

    /**
     * Same as {@link #check(String, Path, String, RegressionFiles.Table)}, with a table-specific report added to the
     * summary when the table differs.
     *
     * @param name short name of the table, used in report file names
     * @param referenceFile the golden reference file
     * @param description one line describing the content, written in the reference file
     * @param actual the freshly computed table
     * @param extraSummary builds the additional summary lines from the changed cells
     * @throws IOException on read/write errors
     */
    public static void check(String name, Path referenceFile, String description, RegressionFiles.Table actual,
                             Function<List<RegressionDiff.CellDiff>, List<String>> extraSummary) throws IOException {
        RegressionDiff.check(name, referenceFile, description, actual, GoldenDataset.smilesByName(), extraSummary,
                REGENERATE_HINT);
    }
}
