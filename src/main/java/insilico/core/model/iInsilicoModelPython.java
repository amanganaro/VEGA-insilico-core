package insilico.core.model;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.PythonEnvironemntFailedException;
import insilico.core.molecule.InsilicoMolecule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface iInsilicoModelPython {


    String getCondaEnv();

    String getPythonEnv();

    String getScriptName();

    Map<String, String> calculatePythonModel(Path scriptPath, String... params) throws GenericFailureException;

    boolean configureCondaEnv() throws InitFailureException;

    boolean configurePythonEnv() throws PythonEnvironemntFailedException;

    boolean removeCondaEnv() throws IOException, InterruptedException;

    boolean removePythonEnv() throws IOException, InterruptedException;

    void setSupportFiles() throws InitFailureException;

    void setDescriptorGenerator(Object descriptorGenerator);

    boolean isUsingCdddDescriptor();

    /**
     * Tells whether this model can predict several molecules with a single execution of its Python script.
     * <p>
     * It is false by default: a model opts in only after its script has been verified to read an input file with
     * more than one row and to write one output row per input row, in the same order. Models that have not been
     * verified keep running one Python process per molecule, exactly as before.
     *
     * @return true if {@link #prepareBatch(List)} can be used on this model
     */
    boolean supportsBatchExecution();

    /**
     * Predicts every given molecule with a single execution of the Python script (or one execution per chunk) and
     * caches the results, so that the subsequent per-molecule calls only read from the cache instead of starting a
     * process each. This is the whole point of the batch mode: the cost of starting Python and loading the model
     * weights is paid once instead of once per molecule.
     * <p>
     * It must be called before running the model on those molecules, and the cache must be released afterwards with
     * {@link #clearBatch()}. Calling it on a model whose {@link #supportsBatchExecution()} is false does nothing.
     * If the batch results cannot be matched to the input molecules, the cache is left empty and the model silently
     * falls back to the per-molecule execution, which is slower but always correct.
     *
     * @param molecules the molecules that are about to be executed
     * @throws GenericFailureException if the batch input cannot be prepared
     */
    void prepareBatch(List<InsilicoMolecule> molecules) throws GenericFailureException;

    /**
     * Releases the results cached by {@link #prepareBatch(List)}, bringing the model back to the per-molecule
     * behaviour. It is safe to call it when no batch is active.
     */
    void clearBatch();
}
