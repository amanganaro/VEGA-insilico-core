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
import java.util.*;

/**
 * Some consideration about the choices made in this class:
 * The conda scripts are run by a process builder where the command are passed both as single string and as a string
 * list. The single string is already formatted while the list is not necessary to format it because the process builder
 * formats it by itself. This is encouraged, but I found that the parameters are not recognized as parameters but like
 * the command continuing.
 * Hence, where there are some command parameters, I used the single string.
 * In the first betas I used the activate.bat to add automatically all the paths needed to conda to execute all the scripts;
 * Unfortunately as now, conda does not support the path with space within, that on Windows is pretty common (so much so
 * also the user folder can have space).
 * The workaround used is to use the base script that uses conda when execute the commands, conda-script.py file followed
 * by the conda command parameter, indeed conda accept tos become conda-script.py accept tos.
 * Also for this workaround, there was a problem, to activate the conda environment I have to put the environment variables
 * that conda put automatically with the activate.bat script. This is made only when there is the activation of a conda
 * environment
 * */
@Slf4j
public class Communication {

    @Setter
    private Map<String, String> additionalEnvVariables;
    private final Path condaInstallationPath = Paths.get(System.getProperty("user.home"), "vega", "conda");
    private final Path pythonOfcondaPath = Paths.get(System.getProperty("user.home"), "vega", "conda", "python.exe");
    private final Path condaScriptPath = Paths.get(System.getProperty("user.home"), "vega", "conda", "Scripts", "conda-script.py");
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
        String p = String.join(" ", params);
        boolean result = false;

        if(SystemUtils.IS_OS_WINDOWS){

            additionalEnvVariables.put("CONDA_PREFIX", Paths.get(
                    condaInstallationPath.toAbsolutePath().toString(), "envs", env).toAbsolutePath().toString());
            additionalEnvVariables.put("PATH", Paths.get(
                    condaInstallationPath.toAbsolutePath().toString(), "envs", env, "Library", "bin").toAbsolutePath().toString());
            additionalEnvVariables.put("CONDA_DEFAULT_ENV", env);
            result = GeneralUtilities.executeCommandLine(additionalEnvVariables,
                    (USE_CUSTOM_CONDA ? "\"" + Paths.get(condaInstallationPath.toAbsolutePath().toString(), "envs", env, "python.exe").toAbsolutePath().toString() + "\" "
                            : ("conda activate " + env + " && python ")) +
                            "\""+scriptName + "\" " + p);

        }else {
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source \""+condaInstallationPath.toAbsolutePath().toString()+"/bin/activate\"" +
                            " && " : "") +
                    "conda activate " + env +" && python \"" + scriptName + "\" " + p);
        }
        return result;
    }

    public boolean checkCondaEnv(String envName) throws IOException, InterruptedException {
        log.info("Check conda env {}.", envName);
        boolean result=false;

        if(SystemUtils.IS_OS_WINDOWS){
            String cmd;
            if (USE_CUSTOM_CONDA) {
                cmd = "\""+pythonOfcondaPath.toAbsolutePath().toString()+"\" " +
                        "\""+condaScriptPath.toAbsolutePath().toString()+"\"";
            }else{
                cmd = "conda";
            }
            cmd += " env list";

            result = GeneralUtilities.executeCommandLineAndCheckResult(null, envName,
                    "cmd.exe", "/c", cmd);
        }else {
            result = GeneralUtilities.executeCommandLineAndCheckResult(null, envName,
                    "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source " + "\"" +condaInstallationPath.toAbsolutePath().toString()
                            +"/bin/activate" + "\"" +" && " : "") +
                            "conda env list");
        }
        return result;
    }

    public boolean configureCondaEnv(String envName, Path pathToEnvFile) throws InterruptedException, IOException {
        log.info("Start setting conda env {}.", envName);
        boolean isSet=false;
        boolean temp;
        if(SystemUtils.IS_OS_WINDOWS){

            String cmd;
            if (USE_CUSTOM_CONDA) {
                cmd = "\""+pythonOfcondaPath.toAbsolutePath().toString()+"\" " +
                        "\""+condaScriptPath.toAbsolutePath().toString()+"\"";
            }else{
                cmd = "conda";
            }
            cmd += " env create  --quiet --file \""+ pathToEnvFile.toAbsolutePath().toString()+"\"";
            temp = GeneralUtilities.executeCommandLine(null, cmd);

        }else {
            temp= GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source " + "\"" + condaInstallationPath.toAbsolutePath().toString()
                            +"/bin/activate\"" + " && " : "") +
                            "conda env create  --quiet --file \"" + pathToEnvFile.toString()+"\"");
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

            String cmd;
            if (USE_CUSTOM_CONDA) {
                cmd = "\""+pythonOfcondaPath.toAbsolutePath().toString()+"\" " +
                        "\""+condaScriptPath.toAbsolutePath().toString()+"\"";
            }else{
                cmd = "conda";
            }
            cmd += " env remove --n " + condaEnv + " --yes";
            result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c", cmd);

        }else{
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    (USE_CUSTOM_CONDA ? "source " + "\"" + condaInstallationPath.toAbsolutePath().toString()
                            +"/bin/activate" + "\"" + " && " : "") +
                            "conda env remove -n " + condaEnv + " --yes");
        }
        log.info("{} in removing conda env {}.", result ? "Success" : "Error" , condaEnv);
        return result;
    }
}