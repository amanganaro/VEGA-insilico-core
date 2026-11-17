package regression;

import insilico.core.model.InsilicoModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Base class of the model regression tests of the {@code VegaModels-*} modules: each module has a tiny subclass that
 * returns its model(s) from {@link #getModels()}, and this class turns every (model, dataset) pair into a dynamic test.
 * <p>
 * {@link #getModels()} returns a list because a single module may hold several models (e.g. ONTOX-assay bundles 25).
 * The datasets come from {@link ModelRegression#selectedDatasets()} ({@code -Dregression.dataset=ts|core}, both when
 * unset); the actual check is delegated to {@link ModelRegression#check(InsilicoModel, ModelRegression.Dataset)}.
 * <p>
 * Run from a module (or from the repo parent) with {@code mvn test -Pregression}; regenerate the reference files with
 * {@code -Dgolden.update=true}.
 */
@Tag("model-regression")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ModelRegressionTest {

    /**
     * The model(s) of this module. Each is run on every selected dataset.
     *
     * @return the models, never empty
     * @throws Exception if a model cannot be instantiated
     */
    protected abstract List<InsilicoModel> getModels() throws Exception;

    /**
     * Whether the model(s) of this module have a training set. Rule-based models (e.g. structural classifiers) have
     * none: a module overrides this to return {@code false}, and its {@code [ts]} tests are then reported as skipped
     * instead of failing on an empty training set. The default is {@code true}, so an unexpectedly empty training set
     * still fails the test.
     *
     * @return true if the ts dataset applies to this module's models
     */
    protected boolean hasTrainingSet() {
        return true;
    }

    /**
     * One dynamic test per (model, dataset) pair, named like {@code HydrolysisCoral [core]}.
     *
     * @return the dynamic tests
     * @throws Exception if the models cannot be instantiated
     */
    @TestFactory
    Stream<DynamicTest> modelRegression() throws Exception {
        List<InsilicoModel> models = getModels();
        if (models == null || models.isEmpty())
            throw new IllegalStateException(getClass().getName() + ".getModels() returned no model");

        List<ModelRegression.Dataset> datasets = ModelRegression.selectedDatasets();
        List<DynamicTest> tests = new ArrayList<>();
        // Dataset first, model second: the models of a module share the core dataset, so running them one after the
        // other on the same molecules lets the Python models reuse the CDDD descriptors instead of recomputing them.
        for (ModelRegression.Dataset dataset : datasets) {
            for (InsilicoModel model : models) {
                String label = model.getInfo().getName();
                if (label == null || label.isEmpty())
                    label = model.getInfo().getKey();
                String testName = label + " [" + dataset.id() + "]";
                if (dataset == ModelRegression.Dataset.TS && !hasTrainingSet())
                    tests.add(DynamicTest.dynamicTest(testName, () -> Assumptions.abort(
                            "model " + model.getInfo().getKey() + " has no training set (hasTrainingSet() is false)")));
                else
                    tests.add(DynamicTest.dynamicTest(testName, () -> ModelRegression.check(model, dataset)));
            }
        }
        return tests.stream();
    }

    /** Releases the CDDD descriptors kept between models once the whole module has been checked. */
    @AfterAll
    void releaseDescriptors() throws IOException {
        ModelRegression.releaseCddd();
    }
}
