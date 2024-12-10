package insilico.core.model;

import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.python.Communication;
import insilico.core.tools.utils.FileUtilities;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.ErrorManager;

@Slf4j
public abstract class InsilicoModelPython extends InsilicoModel implements iInsilicoModelPython {

    private final Communication communication;
    private boolean CHECK_SETUP = true;
    public String descriptorTempFile;
    protected String inputTempFile;
    protected String outputTempFile;
    protected Path pathToExternalFolder;


    public InsilicoModelPython(String modelData) throws InitFailureException, GenericFailureException {
        super(modelData);
        communication = new Communication();
    }

    /***
     * It is better that each model has its own virtual environment, as recommended by conda docs.
     * Otherwise, it can be set to default base conda environment
     */
    public abstract String getCondaEnv();

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
        boolean computationOk = communication.executeScriptInCondaEnv(
                getCondaEnv(), scriptPath.toString(), params);
        log.info("Finish calculating model results.");

        Map<String, String> result;

        if(computationOk){
            result = FileUtilities.readSelectedRowAndHeaderFromFile(outputTempFile, ',', 1);
        }
        else {
            result=null;
        }

        File file = new File(outputTempFile);
        file.delete();

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

        log.info("Start setting conda env.");
        if(urlSourceEnv!=null && urlSourceAppFile != null) {
            FileUtilities.copyExternalData(Paths.get(urlSourceEnv.toURI()).toString(), pathToExternalFolder.toString());
            FileUtilities.copyExternalData(Paths.get(urlSourceAppFile.toURI()).toString(), pathToExternalFolder.toString());
            return communication.configureCondaEnv(getCondaEnv(), Paths.get(urlSourceAppFile.toURI()));
        }
        else {
            log.error("Error in copying files of conda environments: app.py or {}.yml",getCondaEnv());
            return false;
        }

    }

    /**
     * Set the setup check to true if the class checks every time that execute a python command if the correspondent
     * conda virtual environment is set up
     * @param value
     */
    public void setCheckSetup(boolean value){
        CHECK_SETUP = value;
    }

    public void prepareInputData() throws GenericFailureException {
        FileUtilities.WriteByteArrayToFile(inputTempFile,
                ("smiles\r\n"+CurMolecule.GetSMILES()).getBytes());
        log.info("Prepared input file.");
    }

}
