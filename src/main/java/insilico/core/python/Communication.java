package insilico.core.python;

import insilico.core.tools.utils.GeneralUtilities;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Communication {

    private boolean isWindows;
    @Setter
    private Map<String, String> additionalEnvVariables;
    private final Path condaInstallationPath = Paths.get(System.getProperty("user.home"), "vega", "conda");

    public Communication(){
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        additionalEnvVariables = new HashMap<>();
    }

    /**
     * execute a python script in a specific conda environment
     * @return
     */
    public boolean executeScriptInCondaEnv(String env, String scriptName, String... params) throws IOException, InterruptedException {
        log.info("Executing script {} in conda env {}.", scriptName, env);
        String p=String.join(" ", params);
        boolean result=false;
        if(isWindows){
            result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " +
                            "conda activate " + env + " && python " + scriptName + " " + p);
        }else {
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && "+
                    "conda activate " + env +" && python " + scriptName + " " + p);
        }
        return result;
    }

    /**
     * execute a generic command in a specific conda environment with optional parameters
     * @return boolean value to know the execution result. The console result are reported into LOG
     */
    public boolean executeCommandInCondaEnv(String env, String command, String... params) throws IOException, InterruptedException {
        log.info("Executing command {} in conda env {}.", command, env);
        String p=String.join(" ", params);
        boolean result;
        if(isWindows){
            result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " +
                            "conda activate " + env + " && " + command + " " + p);
        }else {
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && "+
                            "conda activate " + env +" && " + command + " " + p);
        }
        return result;
    }

    public boolean checkCondaEnv(String envName) throws IOException, InterruptedException {
        log.info("Check conda env {}.", envName);
        boolean result=false;

        if(isWindows){
            result = GeneralUtilities.executeCommandLineAndCheckResult(null, envName,
                    "cmd.exe", "/c",
                    condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " +
                            "conda env list");
        }else {
            result = GeneralUtilities.executeCommandLineAndCheckResult(null, envName,
                    "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && "+
                            "bash", "--login", "-c", "conda env list");
        }
        return result;
    }

    public boolean configureCondaEnv(String envName, Path pathToEnvFile) throws InterruptedException, IOException {
        log.info("Start setting conda env {}.", envName);
        boolean isSet=false;
        boolean temp;
        if(isWindows){
            temp= GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat " +
                            "&& conda env create --file "+pathToEnvFile.toString());
                    //"conda env create --file " + pathToEnvFile.toString() + " --debug");
        }else {
            temp= GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && "+
                            "conda env create --file " + pathToEnvFile.toString());
        }
        if(temp){
            isSet=checkCondaEnv(envName);
            log.info("Finished to set conda environment.");
        }else{
            log.error("Conda environment {} failed to be set", envName);
        }
        log.info("{} setting conda env {}.", isSet ? "Finish correctly" : "Failed", envName);
        return isSet;
    }

    public boolean removeCondaEnv(String condaEnv) throws IOException, InterruptedException {
        boolean result = false;
        log.info("Removing conda env {}.", condaEnv);
        if(isWindows){
            result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " +
                            "conda env remove -n " + condaEnv + " --yes");
        }else{
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && "+
                            "conda env remove -n " + condaEnv + " --yes");
        }
        log.info("{} in removing conda env {}.", result ? "Success" : "Error" , condaEnv);
        return result;
    }

}
