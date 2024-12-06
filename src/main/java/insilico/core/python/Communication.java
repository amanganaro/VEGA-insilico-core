package insilico.core.python;

import insilico.core.tools.utils.GeneralUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Communication {

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
                            + env +" && python3 " + command + " " + p);
        }

        return result;
    }

}
