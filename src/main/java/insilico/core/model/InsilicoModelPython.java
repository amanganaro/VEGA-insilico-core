package insilico.core.model;

import java.nio.charset.StandardCharsets;
import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.*;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.runner.iInsilicoModelRunnerMessenger;
import insilico.core.model.trainingset.TrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.python.CdddDescriptors;
import insilico.core.python.Communication;
import insilico.core.tools.utils.FileUtilities;
import insilico.core.tools.utils.GeneralUtilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public abstract class InsilicoModelPython extends InsilicoModel implements iInsilicoModelPython {

    private final Communication communication;
    protected String inputTempFile;
    protected String outputTempFile;
    protected Path pathToVEGAFolder;
    protected Path pathToExternalFolder;
    protected boolean isUsingCdddDescriptor=false;
    protected iInsilicoModelRunnerMessenger messenger;
    protected String envTag;
    String resourceFileZip;

    /** Name of the column the Python scripts echo back, used to check that the batch rows are aligned. */
    protected static final String BATCH_SMILES_COLUMN = "smiles";

    /** Default number of molecules sent to the Python script in one execution. */
    protected static final int DEFAULT_BATCH_CHUNK_SIZE = 500;

    /**
     * Predictions computed in advance by {@link #prepareBatch(List)}, keyed by the input SMILES of the molecule, the
     * same key {@link CdddDescriptors#getFilePathOf(String)} uses. Null when no batch is active, which is the
     * default and makes the model behave exactly as before.
     */
    private Map<String, Map<String, String>> batchPredictions;

    /**
     * The CDDD descriptors of the current run, needed to build the batch input file.
     * <p>
     * It is kept here in addition to the subclass field because {@code setDescriptorGenerator} is abstract and every
     * model stores the generator in a field of its own, which this base class cannot reach. The runner sets it
     * through {@link #setCdddDescriptorsForBatch(CdddDescriptors)} next to {@code setDescriptorGenerator}, so no
     * model had to be modified.
     */
    private CdddDescriptors batchCdddDescriptors;

    /** Number of molecules per Python execution; see {@link #DEFAULT_BATCH_CHUNK_SIZE}. */
    private int batchChunkSize = DEFAULT_BATCH_CHUNK_SIZE;

    public InsilicoModelPython(String modelData) throws InitFailureException {
        super(modelData);
        try {
            communication = new Communication();
        }catch (IOException e){
            log.error("Communication class can not be initialized: {}", e.getMessage());
            throw new InitFailureException(e.getMessage());
        }
    }

    public InsilicoModelPython(String modelData, iInsilicoModelRunnerMessenger messenger, String modelReferenceName,
                               String envTag, boolean bypassCheckCondaEnv)
            throws InitFailureException, GenericFailureException, InitFailurePythonException {
        super(modelData);

        try {
            communication = new Communication();
        }catch (IOException e){
            log.error("Communication class can not be initialized: {}", e.getMessage());
            throw new InitFailureException(e.getMessage());
        }

        this.envTag = envTag;

        installConda();

        this.messenger = Objects.requireNonNullElseGet(messenger, () -> new iInsilicoModelRunnerMessenger() {
            @Override
            public void SendMessage(String msg) {
                System.out.println(msg);
            }

            @Override
            public void UpdateProgress() {
                System.out.println("No progress update");
            }
        });

        resourceFileZip = modelReferenceName+".zip";

        if (SystemUtils.IS_OS_WINDOWS) {
            pathToVEGAFolder = Paths.get(System.getProperty("user.home"),"AppData", "Local", "vega-models").resolve("");
            pathToExternalFolder = Paths.get(System.getProperty("user.home"),"AppData","Local","vega-models", modelReferenceName).resolve("");
        }
        else if (SystemUtils.IS_OS_LINUX){
            pathToVEGAFolder = Paths.get(System.getProperty("user.home") ,".local", "share", "vega-models").resolve("");
            pathToExternalFolder = Paths.get(System.getProperty("user.home") ,".local", "share", "vega-models", modelReferenceName).resolve("");
        }
        else if (SystemUtils.IS_OS_MAC){
            pathToVEGAFolder = Paths.get(System.getProperty("user.home") ,"Library", "Application Support", "vega-models").resolve("");
            pathToExternalFolder = Paths.get(System.getProperty("user.home") ,"Library", "Application Support", "vega-models", modelReferenceName).resolve("");
        }

        setSupportFiles();

        if(!bypassCheckCondaEnv) {
            boolean isEnvSet;
            if (SystemUtils.IS_OS_WINDOWS && checkIfThereIsCondaPackEnv()) {
                isEnvSet = configurePythonEnv();
                if(!isEnvSet) {
                    throw new InitFailurePythonException("Python environment "+getPythonEnv()+" not set");
                }
            }
            else{
                isEnvSet = configureCondaEnv();
                if(!isEnvSet) {
                    throw new InitFailurePythonException("Conda environment "+getCondaEnv()+" not set");
                }
            }
        }
    }

    /***
     * Each model has own conda environment, an environment can be used for more than one model, if the requirements
     * can be satisfied. There are already some conda environments set by default
     */
    public String getCondaEnv(){
        switch(envTag){
            case "GLOBAL":
                return "VEGA_global_V3";
            case "TEST":
                return "test";
            default:
                return "VEGA_global_V3";
        }
    }

    public String getPythonEnv(){
        switch(envTag){
            case "GLOBAL":
                return "VEGA_global_V3";
            case "TEST":
                return "test";
            default:
                return "VEGA_global_V3";
        }
    }

    /**
     * Each python model must have a script file with unique name
     * Suggestion "app+model-name.py"
     * @return
     */
    public abstract String getScriptName();

    /***
     * Method that calculate the model prediction. The prediction it is stored in out.csv file.
     * The Python script file for our standard must be called app.py . Optionally can be passed
     * other parameters that are required from the script
     * @return It is returned the entire rows value in pair with the correspondent header value
     * Therefore it can be used with multitask model or single task
     * The management of the name is left to the implemented model
     */
    public Map<String, String> calculatePythonModel(Path scriptPath, String... params) throws GenericFailureException{

        // When a batch has been prepared, the prediction of the current molecule has already been computed and no
        // process needs to be started. Models keep calling this method unchanged and keep reading the same Map.
        Map<String, String> cached = getCachedPrediction();
        if (cached != null) {
            // the caller created an empty output file before calling: nothing will write to it, so remove it
            deleteOutputTempFile();
            log.debug("Using the batch prediction of the current molecule, no Python process started.");
            return cached;
        }

        Map<String, String> result=null;
        try {
            log.info("Start calculating model results.");
            boolean computationOk;

            if(SystemUtils.IS_OS_WINDOWS){
                computationOk = communication.executePureCommandInPythonEnv(getPythonEnv(), scriptPath.toString(), params);
            }
            else{
                computationOk = communication.executeScriptInCondaEnv(getCondaEnv(), scriptPath.toString(), params);
            }

            log.info("Finish calculating model results.");

            if (computationOk) {
                result = FileUtilities.readSelectedRowAndHeaderFromFile(outputTempFile, ',', 1);
            }

            FileUtils.delete(new File(outputTempFile));

            if(!computationOk) {
                log.error("Error while calculating model {}", this.getInfo().getName());
                throw new GenericFailureException("Error while calculating model " + this.getInfo().getName());
            }

        }catch(IOException | InterruptedException | CsvValidationException | URISyntaxException ex){
            throw new GenericFailureException(ex.getMessage());
        }
        return result;
    }

    public void setSupportFiles() throws InitFailurePythonException, PythonModelResourceNotFoundException {
        try {
            File f = new File(pathToExternalFolder.toString());
            if (!f.exists()) {

                log.info("Start to extract the {} file.", resourceFileZip);

                InputStream is = getClass().getResourceAsStream("/data/"+resourceFileZip);
                if (is == null) {
                    log.error("File {} not found", resourceFileZip);
                    throw new PythonModelResourceNotFoundException("File " + resourceFileZip + " in resource folder not found");
                }

                // extract the file form resource jar to a temporary file zip
                // then from that temporary file extract the files
                File zipFile = File.createTempFile("insilico-model-", ".zip");
                zipFile.deleteOnExit();
                try(
                        InputStream in = is;
                        OutputStream out = new FileOutputStream(zipFile)
                ){
                    in.transferTo(out);
                }
                FileUtilities.extractFilesFromZip(zipFile.getAbsolutePath(), pathToVEGAFolder.toString());

                log.info("Copied all necessary files from {}", resourceFileZip);
            } else {
                log.info("Already existing files and not copied of {}", resourceFileZip);
            }
        }
        catch (ConnectException ex){
            log.error("Url of the model is unreachable");
            throw new PythonModelResourceNotFoundException(ex.getMessage());
        }
        catch(IOException ex){
            log.error("Problem in extracting {} file", resourceFileZip);
            try {
                FileUtilities.deleteFolder(pathToExternalFolder.toString());
            } catch (IOException e) {
                throw new InitFailurePythonException(ex.getMessage());
            }
            throw new InitFailurePythonException(ex.getMessage());
        }
    }

    /**
     * Set up the conda environment, if there are additional files specific to the model, add them in the subclass
     * override method.
     * It will be moved to the startup of GUI, where all envs will be set
     * all in once. This method should be overridden IF the env require additional file management (like DILI-bayer)
     * @return boolean result to know if the whole execution is done smoothly. If there are some error, they are
     * reported in LOG
     */
    public boolean configureCondaEnv() throws InitFailurePythonException {
        boolean isSet;
        try {
            if (messenger != null) {
                messenger.SendMessage("Model " + super.getInfo().getName() + " is checking conda environment");
            }

            isSet = communication.checkCondaEnv(getCondaEnv());

            if (!isSet) {
                if (messenger != null)
                    messenger.SendMessage("Model " + super.getInfo().getName() + " installing conda environment.");
                isSet = communication.configureCondaEnv(getCondaEnv(), Paths.get(pathToExternalFolder.toString(), getCondaEnv() + ".yml"));
            }
        }catch (InterruptedException | IOException ex){
            throw new InitFailurePythonException(ex.getMessage());
        }

        return isSet;
    }

    public boolean configurePythonEnv() throws PythonEnvironemntFailedException {
        boolean isSet;
        try {
            if (messenger != null) {
                messenger.SendMessage("Model " + super.getInfo().getName() + " is checking python environment");
            }

            isSet = communication.checkPythonEnv(getPythonEnv());

            if (!isSet) {
                if (messenger != null)
                    messenger.SendMessage("Model " + super.getInfo().getName() + " installing python environment.");
                isSet = communication.configurePythonEnv(getPythonEnv());
            }
        }catch (InterruptedException | IOException ex){
            throw new PythonEnvironemntFailedException(ex.getMessage());
        }

        return isSet;
    }

    public boolean removeCondaEnv() throws IOException, InterruptedException {
        return communication.removeCondaEnv(getCondaEnv());
    }

    public boolean removePythonEnv() throws IOException, InterruptedException {
        return communication.removePythonEnv(getPythonEnv());
    }

    public void removeResourceFolder() throws IOException {
        FileUtilities.deleteFolder(pathToExternalFolder.toString());
    }

    public void prepareInputData() throws GenericFailureException {
        prepareInputData(List.of(CurMolecule.GetSMILES()), inputTempFile);
    }

    /**
     * Writes the SMILES input file read by the models that do not use the CDDD descriptors and compute their own
     * descriptors in Python. It is the multi-molecule form of {@link #prepareInputData()}: same format, one row per
     * molecule instead of one.
     *
     * @param smilesList the molecules to write, in the order their results are expected back
     * @param inputFile the file to write
     * @throws GenericFailureException if the file cannot be written
     */
    public void prepareInputData(List<String> smilesList, String inputFile) throws GenericFailureException {
        StringBuilder sb = new StringBuilder("smiles\r\n");
        for (String smiles : smilesList) {
            sb.append(smiles).append("\r\n");
        }
        FileUtilities.WriteByteArrayToFile(inputFile, sb.toString().getBytes(StandardCharsets.UTF_8));
        log.info("Prepared input file with {} molecules.", smilesList.size());
    }

    // ---------------------------------------------------------------------------------------------------------
    // Batch execution
    // ---------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>
     * False here: a model opts in by overriding this method, once its Python script has been verified to handle an
     * input file with more than one row.
     */
    @Override
    public boolean supportsBatchExecution() {
        return false;
    }

    @Override
    public void clearBatch() {
        batchPredictions = null;
    }

    /**
     * @return true if a batch has been prepared and is being used, false if the model is running one Python process
     * per molecule. Useful to tell a successful batch from a silent fallback.
     */
    public boolean isBatchActive() {
        return batchPredictions != null;
    }

    /**
     * Gives this model the CDDD descriptors of the current run, so that it can build its batch input file.
     * Called by the runner together with {@code setDescriptorGenerator}; see {@link #batchCdddDescriptors}.
     *
     * @param cdddDescriptors the descriptors computed for the whole set of molecules
     */
    public void setCdddDescriptorsForBatch(CdddDescriptors cdddDescriptors) {
        this.batchCdddDescriptors = cdddDescriptors;
    }

    /** @return the number of molecules sent to the Python script in one execution */
    public int getBatchChunkSize() {
        return batchChunkSize;
    }

    /**
     * @param batchChunkSize molecules per Python execution; values below 1 are ignored. Smaller chunks use less
     *                       memory and give more frequent progress updates, larger ones start fewer processes.
     */
    public void setBatchChunkSize(int batchChunkSize) {
        if (batchChunkSize > 0) {
            this.batchChunkSize = batchChunkSize;
        }
    }

    @Override
    public void prepareBatch(List<InsilicoMolecule> molecules) throws GenericFailureException {

        clearBatch();

        if (!supportsBatchExecution() || molecules == null || molecules.isEmpty()) {
            return;
        }

        // Only the molecules the model could actually predict, in order and without repetitions: an invalid molecule
        // fails before the prediction anyway, and the same SMILES always gives the same result.
        List<String> keys = new ArrayList<>();
        Set<String> alreadyAdded = new LinkedHashSet<>();
        for (InsilicoMolecule molecule : molecules) {
            if (molecule == null || !molecule.IsValid()) {
                continue;
            }
            String key = molecule.getInputSMILES();
            if (key == null || key.isEmpty()) {
                continue;
            }
            if (isUsingCdddDescriptor()
                    && (batchCdddDescriptors == null || !batchCdddDescriptors.checkIfCdddFileIsValid(key))) {
                continue;
            }
            if (alreadyAdded.add(key)) {
                keys.add(key);
            }
        }

        if (keys.isEmpty()) {
            log.info("No molecule can be predicted in batch by model {}: keeping the per-molecule execution.",
                    this.getInfo().getName());
            return;
        }

        Map<String, Map<String, String>> predictions = new HashMap<>();
        for (int from = 0; from < keys.size(); from += batchChunkSize) {
            List<String> chunk = keys.subList(from, Math.min(from + batchChunkSize, keys.size()));
            if (!runBatchChunk(chunk, predictions)) {
                // Anything unexpected and the batch is abandoned as a whole: the per-molecule execution is slower
                // but always correct, and is far better than silently returning misaligned predictions.
                log.error("Batch execution failed for model {}: falling back to the per-molecule execution.",
                        this.getInfo().getName());
                return;
            }
            if (messenger != null) {
                messenger.SendMessage("Model " + this.getInfo().getName() + ": predicted "
                        + Math.min(from + batchChunkSize, keys.size()) + " of " + keys.size() + " molecules");
            }
        }

        batchPredictions = predictions;
        log.info("Model {}: {} molecules predicted in batch.", this.getInfo().getName(), predictions.size());
    }

    /**
     * Runs the Python script once on a chunk of molecules and stores one prediction per molecule.
     * <p>
     * The rows that come back are matched to the input molecules by position, and the match is verified against the
     * SMILES column the scripts echo back. If anything does not line up, nothing is stored and false is returned.
     *
     * @param keys the input SMILES of the chunk, in the order they are written to the input file
     * @param predictions the map to fill
     * @return true if every molecule of the chunk got its prediction
     */
    private boolean runBatchChunk(List<String> keys, Map<String, Map<String, String>> predictions) {

        File inputFile = null;
        File outputFile = null;
        try {
            inputFile = File.createTempFile("insilico-batch-input-", ".csv");
            outputFile = File.createTempFile("insilico-batch-output-", ".csv");

            buildBatchInputFile(keys, inputFile.toPath());

            Path scriptPath = Paths.get(pathToExternalFolder.toString(), getScriptName());
            String[] params = buildBatchCommandParams(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());

            log.info("Executing model {} in batch on {} molecules.", this.getInfo().getName(), keys.size());
            boolean computationOk;
            if (SystemUtils.IS_OS_WINDOWS) {
                computationOk = communication.executePureCommandInPythonEnv(getPythonEnv(), scriptPath.toString(), params);
            } else {
                computationOk = communication.executeScriptInCondaEnv(getCondaEnv(), scriptPath.toString(), params);
            }

            if (!computationOk) {
                log.error("Batch execution of the Python script of model {} failed.", this.getInfo().getName());
                return false;
            }

            List<Map<String, String>> rows =
                    FileUtilities.readAllRowsAndHeaderFromFile(outputFile.getAbsolutePath(), ',');

            if (rows.size() != keys.size()) {
                log.error("Model {} returned {} rows for {} molecules: the batch output cannot be matched to the input.",
                        this.getInfo().getName(), rows.size(), keys.size());
                return false;
            }

            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> row = rows.get(i);
                String returnedSmiles = row.get(BATCH_SMILES_COLUMN);
                if (returnedSmiles != null && !returnedSmiles.equals(keys.get(i))) {
                    log.error("Model {} returned the prediction of '{}' where '{}' was expected: "
                                    + "the batch output is not aligned with the input.",
                            this.getInfo().getName(), returnedSmiles, keys.get(i));
                    return false;
                }
                predictions.put(keys.get(i), row);
            }

            return true;

        } catch (Exception ex) {
            log.error("Batch execution of model {} failed: {}", this.getInfo().getName(), ex.getMessage());
            return false;
        } finally {
            deleteQuietly(inputFile);
            deleteQuietly(outputFile);
        }
    }

    /**
     * Writes the input file of a batch execution. The default implementation covers both families of Python models,
     * which is what keeps this API usable by future models:
     * <ul>
     *     <li>models using the CDDD descriptors get the per-molecule descriptor files merged into one file, which is
     *     exactly the format their script already reads;</li>
     *     <li>the other models get the plain SMILES file of {@link #prepareInputData(List, String)}.</li>
     * </ul>
     * A model whose script expects something else overrides this method.
     *
     * @param smilesList the molecules of the chunk, in order
     * @param target the file to write
     * @throws IOException if the file cannot be written
     * @throws GenericFailureException if the descriptors needed to build it are not available
     */
    protected void buildBatchInputFile(List<String> smilesList, Path target)
            throws IOException, GenericFailureException {

        if (isUsingCdddDescriptor()) {
            if (batchCdddDescriptors == null) {
                throw new GenericFailureException("Model " + this.getInfo().getName()
                        + " uses the CDDD descriptors but none were given for the batch execution");
            }
            batchCdddDescriptors.writeCombinedDescriptorFile(smilesList, target);
        } else {
            prepareInputData(smilesList, target.toString());
        }
    }

    /**
     * Command line arguments of a batch execution. The default is the {@code --input}/{@code --output} form used by
     * the Python scripts; a model whose script takes them differently overrides this method.
     *
     * @param inputFile the input file of the chunk
     * @param outputFile the file the script must write
     * @return the arguments to append to the script path
     */
    protected String[] buildBatchCommandParams(String inputFile, String outputFile) {
        return new String[] { "--input", inputFile, "--output", outputFile };
    }

    /**
     * @return the prediction computed in advance for the current molecule, or null when no batch is active or the
     * current molecule is not part of it, in which case the Python script must be executed as usual
     */
    private Map<String, String> getCachedPrediction() {
        if (batchPredictions == null || CurMolecule == null) {
            return null;
        }
        return batchPredictions.get(CurMolecule.getInputSMILES());
    }

    /** Removes the output file the caller created before asking for a prediction that came from the cache. */
    private void deleteOutputTempFile() {
        if (outputTempFile != null) {
            deleteQuietly(new File(outputTempFile));
        }
    }

    /** Deletes a temporary file without failing the computation if it cannot be removed. */
    private void deleteQuietly(File file) {
        if (file != null && file.exists() && !file.delete()) {
            log.warn("Unable to delete the temporary file {}", file.getAbsolutePath());
        }
    }

    public abstract void setDescriptorGenerator(Object descriptorGenerator);

    public boolean isUsingCdddDescriptor() {
        return isUsingCdddDescriptor;
    }

    public iInsilicoModelRunnerMessenger getMessenger() {
        return messenger;
    }

    public void setMessenger(iInsilicoModelRunnerMessenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public void ProcessTrainingSet() throws Exception {
        this.setSkipADandTSLoading(true);
        TrainingSet TS = new TrainingSet();
        String TSPath = this.getInfo().getTrainingSetURL();
        String[] buf = TSPath.split("/");
        String DatName = buf[buf.length-1];
        TSPath = TSPath.substring(0, TSPath.length()-3) + "txt";

        //build the csv file with all the molecules
        List<String> molecules = buildMoleculeListFromTxt(TSPath);

        //if the model use the cddd then calculate them
        if(isUsingCdddDescriptor){
            CdddDescriptors cddd = new CdddDescriptors(molecules, true, messenger);
            if(!cddd.calculateDescriptors()){
                throw new GenericFailureException(String.format(StringSelectorCore
                                .getString("runner_consensus_exception_init_blocks"),
                        "CDDD descriptors failing execution"));
            }
            this.setDescriptorGenerator(cddd);
            //calculate the results
            TS.Build(TSPath, this);
            TS.SerializeToFile(DatName);
            cddd.dispose();
        }
        else{
            TS.Build(TSPath, this);
            TS.SerializeToFile(DatName);
        }
    }

    private List<String> buildMoleculeListFromTxt(String molFilePath) throws IOException, GenericFailureException {
        URL tsURL = getClass().getResource(molFilePath);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new DataInputStream(tsURL.openStream()), StandardCharsets.UTF_8))) {
            String[] parsedString = bufferedReader.readLine().split("\t");
            if(parsedString.length < 5)
                throw new GenericFailureException(StringSelectorCore.getString("trainingset_header_error"));

            List<String> molecules = new ArrayList<>();
            String string;

            while((string = bufferedReader.readLine()) != null) {
                string = GeneralUtilities.TrimString(string);
                if (string.isEmpty())
                    continue;
                molecules.add(string.split("\t")[2]);
            }

            return molecules;
        }
    }

    private boolean checkIfThereIsCondaPackEnv(){
        // only checks that the resource exists, without opening a stream on it
        return getClass().getResource("/condapack-env/"+getPythonEnv()+".zip") != null;
    }

    private void installConda() throws InitFailurePythonException {
        // check if conda or conda pack is installed
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                if (!checkIfThereIsCondaPackEnv()) {
                    if (!communication.checkConda()) {
                        communication.installConda();
                    }
                }
            } else if (SystemUtils.IS_OS_MAC) {
                if (!communication.checkConda()) {
                    communication.installConda();
                }
            } else if (SystemUtils.IS_OS_LINUX) {
                if (!communication.checkConda()) {
                    communication.installConda();
                }
            }
        }catch (IOException | InterruptedException e){
            log.error("Error installing conda: {}", e.getMessage());
            throw new InitFailurePythonException(e.getMessage());
        }
    }

    public void uninstallConda() throws GenericFailureException {
        try {
            communication.removeCondaInstallation();
        }
        catch (IOException | InterruptedException e){
            throw new GenericFailureException("Conda not uninstalled properly beacuse: " + e.getMessage());
        }
    }
}
