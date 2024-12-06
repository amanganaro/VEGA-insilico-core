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

    public abstract String getCondaEnv();

    public double calculatePythonModel(String endpointName) throws IOException, InterruptedException, CsvValidationException, URISyntaxException {
        boolean computationOk = communication.executeScriptInCondaEnv(
                getCondaEnv(), "app.py", "descriptors.csv");

        double result=0;

        if(computationOk){
            Map<String, String> resultRow = FileUtilities.readRowFromFile("out.csv", ',', 2);

            result=Double.parseDouble(resultRow.get(endpointName));
        }
        else {
            result=-1;
        }


        File file = new File("descriptors.csv");
        file.delete();
        file = new File("out.csv");
        file.delete();

        return result;
    }

    /***
     * Method to set up conda environment, it will be moved to the start of GUI where all env will be set all in once
     * This method will be overrided IF the env require additional file management (like DILI-bayer)
     * @return
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
