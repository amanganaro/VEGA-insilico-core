package insilico.core.python;

import insilico.core.exception.InitFailurePythonException;
import insilico.core.exception.PythonEnvironemntFailedException;
import insilico.core.exception.PythonModelResourceNotFoundException;
import insilico.core.tools.utils.FileUtilities;
import insilico.core.tools.utils.GeneralUtilities;
import insilico.core.tools.utils.HTTPUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
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

    public boolean executeScriptInPythonEnv(String env, String scriptName, String... params) throws IOException, InterruptedException {
        log.info("Executing script {} in python env {}.", scriptName, env);
        String p = String.join(" ", params);
        boolean result = false;
        String pythonPath = Paths.get(System.getProperty("user.home"), "vega", "python", env, "python.exe").toAbsolutePath().toString();

        if(SystemUtils.IS_OS_WINDOWS){
            result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c",
                    "\"" + pythonPath + "\" \"" + scriptName + "\" " + p);

        }else {
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c",
                    pythonPath +" \"" + scriptName + "\" " + p);
        }
        return result;
    }

    /***
     * Method to execute a script into a python environment. These environment must be found under user/vega/python folder
     * The environment is specified by the envName, equals to the name of the folder where python is.
     * This is suitable to execute commands that do not need of shell feature such as &&, || and similar.
     *
     * @param env = Name of the folder within the python to execute (under user/vega/python folder)
     * @param scriptName = path of the script to be executed
     * @param params = list of parameters passed to script
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean executePureCommandInPythonEnv(String env, String scriptName, String... params) throws IOException, InterruptedException {
        log.info("Executing script {} in python env {}.", scriptName, env);

        String pythonPath = Paths.get(System.getProperty("user.home"), "vega", "python", env, "python.exe").toAbsolutePath().toString();

        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptName);
        Collections.addAll(command, params);

        return GeneralUtilities.executeCommandLine(null, command);
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

    public boolean checkPythonEnv(String envName) throws IOException, InterruptedException {
        log.info("Check python env {}.", envName);
        boolean result=false;
        String pythonPath = Paths.get(System.getProperty("user.home"), "vega", "python", envName, "python.exe").toAbsolutePath().toString();

        if(SystemUtils.IS_OS_WINDOWS){
            String cmd = "\""+pythonPath+"\" --version" ;
            result = GeneralUtilities.executeCommandLine(null,"cmd.exe", "/c", cmd);
        }else {
            String cmd = pythonPath + " --version" ;
            result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c", cmd);
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

    public boolean configurePythonEnv(String envName) throws PythonEnvironemntFailedException {
        log.info("Start setting python env {}.", envName);
        boolean isSet=false;

        Path vegaPythonPath = Paths.get(System.getProperty("user.home"), "vega", "python").resolve("");
        Path envPath = Paths.get(vegaPythonPath.toAbsolutePath().toString(), envName).resolve("");

        try {

            try {

                log.info("Start to download the zip file for {} environment.", envName);

                String httpUrl = "https://amcc.it/vega/" + envName + ".zip";
                File zipFile = File.createTempFile(envName, ".zip");
                HTTPUtils.downloadFile(httpUrl, zipFile.getAbsolutePath());

                log.info("Finish to download the zip file.");
                log.info("Start to extract the file from zip file.");

                FileUtilities.extractFilesFromZip(zipFile.getAbsolutePath(), envPath.toString());
                zipFile.delete();

                log.info("Extracted all necessary file from zip file.");

            } catch (ConnectException ex) {
                log.error("Url of the environment {} is unreachable", envName);
                throw new PythonEnvironemntFailedException(ex.getMessage());
            }

            log.info("Start to unpack the {} environment.", envName);

            if (SystemUtils.IS_OS_WINDOWS) {

                String cmd = Paths.get(vegaPythonPath.toAbsolutePath().toString(), envName, "Scripts",
                        "conda-unpack.exe").toAbsolutePath().toString();
                isSet = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c", cmd);

            } else {
                // TODO CONTROLLARE ESTENSIONE DI CONDA-PACK
                String cmd = Paths.get(vegaPythonPath.toAbsolutePath().toString(), envName, "bin",
                        "conda-unpack").toAbsolutePath().toString();
                isSet = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c", cmd);
            }

            log.info("Unpacked the {} environment.", envName);

            if (isSet) {
                isSet = checkPythonEnv(envName);
            }
        }
        catch(InterruptedException | IOException ex){
            log.error(ex.getMessage());
            throw new PythonEnvironemntFailedException(ex.getMessage());
        }

        log.info("{} setting python env {}.", isSet ? "Finish correctly" : "Failed", envName);
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

    public boolean removePythonEnv(String condaEnv) throws IOException, InterruptedException {
        boolean result = false;
        log.info("Removing python env {}.", condaEnv);
        Path envPath = Paths.get(System.getProperty("user.home"), "vega", "python", condaEnv);
        String envFolder = envPath.toAbsolutePath().toString();

        if(Files.exists(envPath)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                String cmd = "rmdir /s /q \"" + envFolder + "\"";
                result = GeneralUtilities.executeCommandLine(null, "cmd.exe", "/c", cmd);

            } else {
                String cmd = "rm -rf" + envFolder;
                result = GeneralUtilities.executeCommandLine(null, "bash", "--login", "-c", cmd);
            }
            log.info("{} in removing conda env {}.", result ? "Success" : "Error" , condaEnv);
        }
        else{
            result = true;
        }
        return result;
    }
}