package insilico.core.model;

import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.python.Communication;
import insilico.core.tools.utils.FileUtilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.ErrorManager;

@Slf4j
public abstract class InsilicoModelPython extends InsilicoModel implements iInsilicoModelPython {

    private final Communication communication;
    protected String inputTempFile;
    protected String outputTempFile;
    protected Path pathToExternalFolder;
    public boolean isUsingCdddDescriptor=false;


    public InsilicoModelPython(String modelData) throws InitFailureException, GenericFailureException {
        super(modelData);
        communication = new Communication();
    }

    /***
     * It is better that each model has its own virtual environment, as recommended by conda docs.
     * Otherwise, it can be set to default base conda environment
     */
    public abstract String getCondaEnv();

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
     * @throws IOException
     * @throws InterruptedException
     * @throws CsvValidationException
     * @throws URISyntaxException
     */
    public Map<String, String> calculatePythonModel(Path scriptPath, String... params) throws IOException, InterruptedException, CsvValidationException, URISyntaxException {
        log.info("Start calculating model results.");
        boolean computationOk = communication.executeScriptInCondaEnv(getCondaEnv(), scriptPath.toString(), params);
        log.info("Finish calculating model results.");

        Map<String, String> result;
        if(computationOk){
            result = FileUtilities.readSelectedRowAndHeaderFromFile(outputTempFile, ',', 1);
        }
        else {
            result=null;
        }

        FileUtils.delete(new File(outputTempFile));
        return result;
    }

    /**
     * Set up the conda environment, if there are additional files specific to the model, add them in the subclass
     * override method.
     * It will be moved to the startup of GUI, where all envs will be set
     * all in once. This method should be overridden IF the env require additional file management (like DILI-bayer)
     * @return boolean result to know if the whole execution is done smoothly. If there are some error, they are
     * reported in LOG
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean configureCondaEnv(URL urlSourceEnv, URL urlSourceAppFile) throws InterruptedException, IOException, URISyntaxException {
        boolean isSet=communication.checkCondaEnv(getCondaEnv());
        if(!isSet) {
            if (urlSourceEnv != null && urlSourceAppFile != null) {
                FileUtilities.copyResourcesRecursively(urlSourceEnv,
                        new File(pathToExternalFolder.toString()));
                log.info("Env file copied successfully.");
                FileUtilities.copyResourcesRecursively(urlSourceAppFile,
                        new File(pathToExternalFolder.toString()));
                log.info("App file successfully.");
                isSet = communication.configureCondaEnv(getCondaEnv(), Paths.get(urlSourceAppFile.toURI()));
            } else {
                log.error("Error in copying files of conda environments: app.py or {}.yml", getCondaEnv());
            }
        }
        return isSet;
    }

    public void prepareInputData() throws GenericFailureException {
        FileUtilities.WriteByteArrayToFile(inputTempFile,
                ("smiles\r\n"+CurMolecule.GetSMILES()).getBytes());
        log.info("Prepared input file.");
    }

}
