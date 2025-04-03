package insilico.core.python;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.runner.iInsilicoModelRunnerMessenger;
import insilico.core.tools.utils.FileUtilities;
import insilico.core.tools.utils.HTTPUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class CdddDescriptors {

    Communication communication;
    private Path pathToExternalFolder;
    private Path pathToVEGAFolder;
    private List<String> smilesList;
    private Map<String, String> smilesFileMap;
    private String descriptorDirectory;
    private String inputSmilesFileName;
    protected iInsilicoModelRunnerMessenger messenger;

    public CdddDescriptors(List<String> smilesList, boolean bypassCheckCondaEnv, iInsilicoModelRunnerMessenger messenger) throws InitFailureException {

        if(messenger!=null) {
            this.messenger = messenger;
        }else{
            this.messenger = new iInsilicoModelRunnerMessenger() {
                @Override
                public void SendMessage(String msg) {
                    System.out.println(msg);
                }

                @Override
                public void UpdateProgress() {
                    System.out.println("No progress update");
                }
            };
        }
        communication = new Communication();

        if (System.getProperty("os.name").startsWith("Windows")) {
            pathToExternalFolder = Paths.get(System.getProperty("user.home"),
                    "\\AppData\\Local\\vega-models\\descriptors\\cddd\\").resolve("");
            pathToVEGAFolder = Paths.get(System.getProperty("user.home"),
                    "\\AppData\\Local\\vega-models\\descriptors\\").resolve("");
        }
        else {
            pathToExternalFolder = Paths.get(System.getProperty("user.home") ,
                    "/.local/share/vega-models/descriptors/cddd/");
            pathToVEGAFolder = Paths.get(System.getProperty("user.home") ,
                    "/.local/share/vega-models/descriptors/");
        }

        try {
            File f = File.createTempFile("input-smiles", ".csv");
            inputSmilesFileName = f.getAbsolutePath();
            f = Files.createTempDirectory("cddd-descriptors").toFile();
            descriptorDirectory = f.getAbsolutePath();

            if (!bypassCheckCondaEnv) {
                boolean isEnvSet = configureCondaEnv();
                if (!isEnvSet) {
                    throw new InitFailureException("Conda environment " + getCondaEnv() + " not set");
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InitFailureException("Error in creating cddd support files. Conda environment " + getCondaEnv() + " not set");
        } catch (URISyntaxException | InterruptedException e) {
            log.error(e.getMessage());
            throw new InitFailureException("Conda environment " + getCondaEnv() + " not set");
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
        boolean result = communication.executeScriptInCondaEnv(getCondaEnv(), pathToScriptFile,
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
        return "VEGA_cddd";
    }


    /***
     * Move the .yml .whl and default_model folder into Local data folder to use that files to
     * setup the configuration. This is made to use external data instead the one in the project
     * @return
     */
    public boolean configureCondaEnv() throws InterruptedException, IOException, URISyntaxException {
        boolean isSet = communication.checkCondaEnv(getCondaEnv());
        if(!isSet){
            File f = new File(pathToVEGAFolder.toString());
            if(!f.exists()) {
                messenger.SendMessage("CDDD descriptors are downloading support files");
                log.info("Start to download the zip file.");
                File zipFile = File.createTempFile("CDDD", ".zip");
                HTTPUtils.downloadFile("https://amcc.it/vega/cddd.zip", zipFile.getAbsolutePath());
                log.info("Finish to download the zip file.");
                FileUtilities.extractFilesFromZip(zipFile.getAbsolutePath(), pathToVEGAFolder.toString());

                // add default model folder into the directory wanted from cddd
                String destination = System.getProperty("user.home");
                if (System.getProperty("os.name").startsWith("Windows")) {
                    destination += "\\AppData\\Local\\cddd\\cddd\\default_model";
                } else {
                    destination += "/.local/share/cddd/default_model";
                }

                URL urlModelDefaultFolder = Paths.get(pathToVEGAFolder.toString(),"cddd", "default_model").toUri().toURL();
                boolean copied = FileUtilities.copyResourcesRecursively(urlModelDefaultFolder, new File(destination));
                log.info("{} cddd descriptors default model file", copied ? "Copied" : "Already existing and not copied");

                f = new File(urlModelDefaultFolder.toString());
                f.delete();
                zipFile.delete();
                log.info("Copied all necessary file from zip file.");
            }
            else{
                log.info("All support file already present. Not copied.");
            }

            Path pathToEnvFile = Paths.get(pathToExternalFolder.toString(), getCondaEnv()+".yml");
            messenger.SendMessage("CDDD descriptors installing conda environment.");
            isSet = communication.configureCondaEnv(getCondaEnv(), pathToEnvFile);
            if (!isSet) {
                log.error("Error in set up conda environment {}", getCondaEnv());
            }

        }
        log.info("Conda environment {} set up {}", getCondaEnv(), isSet ? "correctly": "failed");
        return isSet;
    }

    public boolean removeCondaEnv() throws IOException, InterruptedException {
        return communication.removeCondaEnv(getCondaEnv());
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

    public boolean checkIfCdddFileIsValid(String smiles) {

        if (getFilePathOf(smiles) == null) {
            return false;
        }

        Scanner myReader = null;
        try {
            File f = new File(getFilePathOf(smiles));
            myReader = new Scanner(f);
            //skip the header
            myReader.nextLine();
            String[] parsedString = myReader.nextLine().split(",");
            if (parsedString[1] == null && !parsedString[1].trim().isEmpty()) {
                myReader.close();
                return false;
            }

        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException ex) {
            return false;
        } finally {
            if(myReader != null)
                myReader.close();
        }

        return true;
    }
}
