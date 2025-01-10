package insilico.core.python;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.tools.utils.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CdddDescriptors {

    Communication communication;
    private static final Logger log = LoggerFactory.getLogger(CdddDescriptors.class);
    private Path pathToExternalFolder;
    private List<String> smilesList;
    private Map<String, String> smilesFileMap;
    private String descriptorDirectory;
    private String inputSmilesFileName;

    public CdddDescriptors(List<String> smilesList, boolean bypassCheckCondaEnv) throws IOException, URISyntaxException, InterruptedException, InitFailureException {

        communication = new Communication();

        if (System.getProperty("os.name").startsWith("Windows")) {
            pathToExternalFolder = Paths.get(System.getProperty("user.home"),
                    "\\AppData\\Local\\vega-models\\descriptors\\cddd\\").resolve("");
        }
        else {
            pathToExternalFolder = Paths.get(System.getProperty("user.home") ,
                    "/.local/share/vega-models/descriptors/cddd/");
        }

        File f=File.createTempFile("input-smiles", ".csv");
        inputSmilesFileName = f.getAbsolutePath();
        f = Files.createTempDirectory("cddd-descriptors").toFile();
        descriptorDirectory = f.getAbsolutePath();

        if(!bypassCheckCondaEnv){
            boolean isEnvSet=configureCondaEnv();
            if(!isEnvSet) {
                throw new InitFailureException("Conda environment "+getCondaEnv()+" not set");
            }
        }
        this.smilesList=smilesList;
    }

    public void prepareInputData(String inputFile) throws GenericFailureException {
        StringBuilder sb=new StringBuilder();
        sb.append("smiles\n");
        for (String smiles:smilesList) {
            sb.append(smiles).append("\n");
        }

        FileUtilities.WriteByteArrayToFile(inputFile,sb.toString().getBytes());
        log.info("Prepared input file.");
    }

    /***
     * Calculate cddd descriptors from cddd library. It executes a python script that calculate from a csv input file
     * the 512 descriptors and for each smiles generates a csv file within the correspondent descriptors
     * @return true if the computation went smoothly otherwise false
     */
    public boolean calculateDescriptors() throws IOException, InterruptedException, URISyntaxException, GenericFailureException {
        prepareInputData(inputSmilesFileName);

        log.info("Start to calculate descriptors");
        String pathToScriptFile = Paths.get(pathToExternalFolder.toString(), "app-cddd.py").toAbsolutePath().toString();
        boolean result = communication.executeScriptInCondaEnv("cddd", pathToScriptFile,
                "--input "+inputSmilesFileName,
                " --output "+ descriptorDirectory);
        if(result){
            for(int i = 0; i<smilesList.size(); i++){
                if(smilesFileMap==null){
                    smilesFileMap=new HashMap<>();
                }
                smilesFileMap.put(smilesList.get(i), descriptorDirectory+File.separator+i+".csv");
            }
        }

        log.info("Finish to calculate descriptors");
        return result;
    }

    public String getCondaEnv(){
        return "cddd";
    }


    /***
     * Move the .yml .whl and default_model folder into Local data folder to use that files to
     * setup the configuration. This is made to use external data instead the one in the project
     * @return
     */
    public boolean configureCondaEnv() throws InterruptedException, IOException, URISyntaxException {
        boolean isSet = communication.checkCondaEnv(getCondaEnv());
        if(!isSet){
            URL urlEnv = getClass().getResource("/python/" + getCondaEnv() + ".yml");
            URL urlScript = getClass().getResource("/python/app-cddd.py");
            URL urlWheel = getClass().getResource("/python/cddd-1.2.3-py3-none-any.whl");
            URL urlModelDefaultFolder = getClass().getResource("/python/default_model");

            if (urlEnv != null && urlWheel != null && urlModelDefaultFolder != null && urlScript != null) {
                boolean copied=FileUtilities.copyResourcesRecursively(urlEnv, new File(pathToExternalFolder.toString()));
                log.info("{} cddd descriptors env file", copied ? "Copied" : "Already existing and not copied");

                copied = FileUtilities.copyResourcesRecursively(urlScript, new File(pathToExternalFolder.toString()));
                log.info("{} cddd descriptors script file", copied ? "Copied" : "Already existing and not copied");

                copied = FileUtilities.copyResourcesRecursively(urlWheel, new File(pathToExternalFolder.toString()));
                log.info("{} cddd descriptors wheel file", copied ? "Copied" : "Already existing and not copied");

                Path pathToEnvFile = Paths.get(pathToExternalFolder.toString(), getCondaEnv()+".yml");
                isSet = communication.configureCondaEnv(getCondaEnv(), pathToEnvFile);
                if (isSet) {
                    // add default model folder to put the model data into the directory of cddd conda env
                    String destination = System.getProperty("user.home");
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        destination += "\\AppData\\Local\\cddd\\cddd\\default_model";
                    } else {
                        destination += "/.local/share/cddd/default_model";
                    }

                    copied = FileUtilities.copyResourcesRecursively(urlModelDefaultFolder, new File(destination));
                    log.info("{} cddd descriptors default model file", copied ? "Copied" : "Already existing and not copied");
                } else {
                    log.error("Error in set up conda environment {}", getCondaEnv());
                }
            } else {
                log.error("Some files to setup cddd conda environment {} are missing", getCondaEnv());
            }
        }
        log.info("Conda environment {} set up {}", getCondaEnv(), isSet ? "correctly": "failed");
        return isSet;
    }

    public String getFilePathOf(String smiles){
        return smilesFileMap.get(smiles);
    }

    public void dispose() throws IOException {
        FileUtils.deleteDirectory(new File(descriptorDirectory));
        FileUtils.delete(new File(inputSmilesFileName));
    }

    public String getOutputDirectory() {
        return descriptorDirectory;
    }
}
