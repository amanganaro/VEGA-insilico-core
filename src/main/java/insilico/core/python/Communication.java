package insilico.core.python;

import insilico.core.tools.utils.GeneralUtilities;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
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

    @Setter
    private Map<String, String> additionalEnvVariables;
    private final Path condaInstallationPath = Paths.get(System.getProperty("user.home"), "vega", "conda");
    @Setter
    private static boolean USE_CUSTOM_CONDA = true;

    public Communication(){
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
        if(SystemUtils.IS_OS_WINDOWS){
            result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    (USE_CUSTOM_CONDA ? condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " : "") +
                            "conda activate " + env + " && python " + scriptName + " " + p);
        }else {
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && " : "") +
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
        if(SystemUtils.IS_OS_WINDOWS){
            result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    (USE_CUSTOM_CONDA ? condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " : "") +
                            "conda activate " + env + " && " + command + " " + p);
        }else {
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && " : "") +
                            "conda activate " + env +" && " + command + " " + p);
        }
        return result;
    }

    public boolean checkCondaEnv(String envName) throws IOException, InterruptedException {
        log.info("Check conda env {}.", envName);
        boolean result=false;

        if(SystemUtils.IS_OS_WINDOWS){
            result = GeneralUtilities.executeCommandLineAndCheckResult(null, envName,
                    "cmd.exe", "/c",
                    (USE_CUSTOM_CONDA ? condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " : "") +
                            "conda env list");
        }else {
            result = GeneralUtilities.executeCommandLineAndCheckResult(null, envName,
                    "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && " : "") +
                            "conda env list");
        }
        return result;
    }

    public boolean configureCondaEnv(String envName, Path pathToEnvFile) throws InterruptedException, IOException {
        log.info("Start setting conda env {}.", envName);
        boolean isSet=false;
        boolean temp;
        if(SystemUtils.IS_OS_WINDOWS){
            temp= GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    (USE_CUSTOM_CONDA ? condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " : "") +
                            "conda env create --file "+pathToEnvFile.toString());
        }else {
            temp= GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && " : "") +
                            "conda env create --file \"" + pathToEnvFile.toString()+"\"");
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
        if(SystemUtils.IS_OS_WINDOWS){
            result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    (USE_CUSTOM_CONDA ? condaInstallationPath.toAbsolutePath().toString()+"\\Scripts\\activate.bat && " : "") +
                            "conda env remove -n " + condaEnv + " --yes");
        }else{
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source "+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate && " : "") +
                            "conda env remove -n " + condaEnv + " --yes");
        }
        log.info("{} in removing conda env {}.", result ? "Success" : "Error" , condaEnv);
        return result;
    }

}
