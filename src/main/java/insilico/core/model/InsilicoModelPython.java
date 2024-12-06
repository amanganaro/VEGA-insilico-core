package insilico.core.model;

import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.InitFailureException;
import insilico.core.python.Communication;
import insilico.core.tools.utils.FileUtilities;
import insilico.core.tools.utils.GeneralUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public abstract class InsilicoModelPython extends InsilicoModel implements iInsilicoModel {

    private Communication communication;

    public InsilicoModelPython(String modelData) throws InitFailureException {
        super(modelData);
        communication = new Communication();
    }

    /***
     * It is better that each model has its own virtual environment, as recommended by conda docs.
     * Otherwise, it can be set to default base conda environment
     * @return
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
    public Map<String, String> calculatePythonModel(String... params) throws IOException, InterruptedException, CsvValidationException, URISyntaxException {
        boolean computationOk = communication.executeScriptInCondaEnv(
                getCondaEnv(), "app.py", params);

        Map<String, String> result;

        if(computationOk){
            result = FileUtilities.readSelectedRowAndHeaderFromFile("out.csv", ',', 2);
        }
        else {
            result=null;
        }

        File file = new File("descriptors.csv");
        file.delete();
        file = new File("out.csv");
        file.delete();

        return result;
    }

    /***
     * Method to set up conda environment, it will be moved to the start of GUI where all env will be set all in once
     * This method should be overridden IF the env require additional file management (like DILI-bayer)
     * @return boolean result to know if the whole execution is done smoothly. If there are some error, they are
     * reported in LOG
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean configureCondaEnv() throws InterruptedException, IOException {
        boolean isSet = false;
        boolean temp=false;
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        String uh=System.getProperty("user.home");
        Map<String, String> additionalEnvVariables = new HashMap<>();
        additionalEnvVariables.put("PATH", uh+"\\miniconda3\\Scripts\\;"+uh+"\\miniconda3\\;"+
                "C:\\Program Files\\Python313\\Scripts\\;C:\\Program Files\\Python313\\;");

        if(isWindows){
            isSet = GeneralUtilities.executeCommandLineAndCheckResult(null, getCondaEnv(), "cmd.exe", "/c", uh+"\\miniconda3\\_conda env list");
        }else {
            isSet = GeneralUtilities.executeCommandLineAndCheckResult(null, getCondaEnv(), "bash", "-c", "source ~/miniconda3/etc/profile.d/conda.sh && conda env list");
        }

        if(!isSet){
            if(isWindows){
                temp= GeneralUtilities.executeCommandLine(additionalEnvVariables, "cmd.exe", "/c", "conda env create --file "+getCondaEnv()+".yml");
            }else {
                temp= GeneralUtilities.executeCommandLine(null, "bash", "-c", "source ~/miniconda3/etc/profile.d/conda.sh && conda env create --file "+getCondaEnv()+".yml");
            }
            if(temp){
                if(isWindows){
                    isSet = GeneralUtilities.executeCommandLineAndCheckResult(null, getCondaEnv(), "cmd.exe", "/c", uh+"\\miniconda3\\_conda env list");
                }else {
                    isSet = GeneralUtilities.executeCommandLineAndCheckResult(null, getCondaEnv(), "bash", "-c", "source ~/miniconda3/etc/profile.d/conda.sh && conda env list");
                }
            }else{
                System.out.println("Conda environment failed to be set");
            }
        }

        return isSet;
    }

}
