package insilico.core.python;

import insilico.core.tools.utils.GeneralUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Communication {

    private static final Logger log = LoggerFactory.getLogger(Communication.class);
    private boolean isWindows;
    private Map<String, String> additionalEnvVariables;

    public Communication(){
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        additionalEnvVariables = new HashMap<>();
    }

    public void setAdditionalEnvVariables(Map<String, String> additionalEnvVariables) {
        this.additionalEnvVariables = additionalEnvVariables;
    }

    /**
     * execute a python script in a specific conda environment
     * @return
     */
    public boolean executeScriptInCondaEnv(String env, String scriptName, String... params) throws IOException, InterruptedException {
        String p=String.join(" ", params);
        boolean result=false;
        if(isWindows){
            result = GeneralUtilities.executeCommandLine(additionalEnvVariables, "cmd.exe", "/c",
                    "conda activate " + env + " && python " + scriptName + " " + p);
        }else {
            result = GeneralUtilities.executeCommandLine(null, "bash", "-c",
                    "source ~/miniconda3/etc/profile.d/conda.sh && conda activate "
                            + env +" && python3 " + scriptName + " " + p);
        }

        return result;
    }

    /**
     * execute a generic command in a specific conda environment with optional parameters
     * @return boolean value to know the execution result. The console result are reported into LOG
     */
    public boolean executeCommandInCondaEnv(String env, String command, String... params) throws IOException, InterruptedException {
        String p=String.join(" ", params);
        boolean result;
        if(isWindows){
            result = GeneralUtilities.executeCommandLine(additionalEnvVariables, "cmd.exe", "/c",
                    "conda activate " + env + " && " + command + " " + p);
        }else {
            result = GeneralUtilities.executeCommandLine(null, "bash", "-c",
                    "source ~/miniconda3/etc/profile.d/conda.sh && conda activate "
                            + env +" && " + command + " " + p);
        }

        return result;
    }

    public boolean checkCondaEnv(String envName) throws IOException, InterruptedException {
        boolean result=false;
        String uh=System.getProperty("user.home");

        if(isWindows){
            result = GeneralUtilities.executeCommandLineAndCheckResult(null, envName,
                    "cmd.exe", "/c", uh+"\\miniconda3\\_conda env list");
        }else {
            result = GeneralUtilities.executeCommandLineAndCheckResult(null, envName,
                    "bash", "-c", "source ~/miniconda3/etc/profile.d/conda.sh && conda env list");
        }
        return result;
    }

    public boolean configureCondaEnv(String envName, Path pathToEnvFile) throws InterruptedException, IOException {
        boolean isSet;
        boolean temp;

        isSet=checkCondaEnv(envName);
        if(!isSet){
            if(isWindows){
                temp= GeneralUtilities.executeCommandLine(additionalEnvVariables, "cmd.exe", "/c",
                        "conda env create --file "+pathToEnvFile.toString());
            }else {
                temp= GeneralUtilities.executeCommandLine(null, "bash", "-c",
                        "source ~/miniconda3/etc/profile.d/conda.sh && conda env create --file "+pathToEnvFile.toString());
            }
            if(temp){
                isSet=checkCondaEnv(envName);
                log.info("Finished to set conda environment.");
            }else{
                log.error("Conda environment {} failed to be set", envName);
            }
        }

        return isSet;
    }

}
