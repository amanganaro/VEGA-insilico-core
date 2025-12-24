package insilico.core.model;

import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.runner.iInsilicoModelRunnerMessenger;
import insilico.core.model.trainingset.TrainingSet;
import insilico.core.python.CdddDescriptors;
import insilico.core.python.Communication;
import insilico.core.tools.utils.FileUtilities;
import insilico.core.tools.utils.GeneralUtilities;
import insilico.core.tools.utils.HTTPUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class InsilicoModelPython extends InsilicoModel implements iInsilicoModelPython {

    private final Communication communication;
    protected String inputTempFile;
    protected String outputTempFile;
    protected Path pathToVEGAFolder;
    protected Path pathToExternalFolder;
    protected boolean isUsingCdddDescriptor=false;
    protected iInsilicoModelRunnerMessenger messenger;
    protected String envTag;
    String httpUrl;

    public InsilicoModelPython(String modelData) throws InitFailureException {
        super(modelData);
        communication = new Communication();
    }

    public InsilicoModelPython(String modelData, iInsilicoModelRunnerMessenger messenger, String modelReferenceName, String envTag, boolean bypassCheckCondaEnv) throws InitFailureException, GenericFailureException {
        super(modelData);

        communication = new Communication();
        this.envTag = envTag;

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

        httpUrl = "https://amcc.it/vega/"+modelReferenceName+".zip";

        if (SystemUtils.IS_OS_WINDOWS) {
            pathToVEGAFolder = Paths.get(System.getProperty("user.home"),"AppData", "Local", "vega-models").resolve("");
            pathToExternalFolder = Paths.get(System.getProperty("user.home"),"AppData","Local","vega-models", modelReferenceName).resolve("");
        }
        else if (SystemUtils.IS_OS_LINUX){
            pathToVEGAFolder = Paths.get(System.getProperty("user.home") ,".local", "share", "vega-models").resolve("");
            pathToExternalFolder = Paths.get(System.getProperty("user.home") ,".local", "share", "vega-models", modelReferenceName).resolve("");
        }
        else if (SystemUtils.IS_OS_MAC){
            pathToVEGAFolder = Paths.get(System.getProperty("user.home") ,"Library", "Application Support", "vega-models").resolve("");
            pathToExternalFolder = Paths.get(System.getProperty("user.home") ,"Library", "Application Support", "vega-models", modelReferenceName).resolve("");
        }

        setSupportFiles();

        if(!bypassCheckCondaEnv) {
            boolean isEnvSet = configureCondaEnv();
            if(!isEnvSet) {
                throw new InitFailureException("Conda environment "+getCondaEnv()+" not set");
            }
        }
    }

    /***
     * Each model has own conda environment, an environment can be used for more than one model, if the requirements
     * can be satisfied. There are already some conda environments set by default
     */
    public String getCondaEnv(){
        switch(envTag){
            case "GLOBAL":
                return "VEGA_global_V2";
            case "TEST":
                return "test";
            default:
                return "VEGA_global_V2";
        }
    }

    /**
     * Each python model must have a script file with unique name
     * Suggestion "app+model-name.py"
     * @return
     */
    public abstract String getScriptName();

    /***
     * Method that calculate the model prediction. The prediction it is stored in out.csv file.
     * The Python script file for our standard must be called app.py . Optionally can be passed
     * other parameters that are required from the script
     * @return It is returned the entire rows value in pair with the correspondent header value
     * Therefore it can be used with multitask model or single task
     * The management of the name is left to the implemented model
     */
    public Map<String, String> calculatePythonModel(Path scriptPath, String... params) throws GenericFailureException{
        Map<String, String> result=null;
        try {
            log.info("Start calculating model results.");
            boolean computationOk = communication.executeScriptInCondaEnv(getCondaEnv(), scriptPath.toString(), params);
            log.info("Finish calculating model results.");

            if (computationOk) {
                result = FileUtilities.readSelectedRowAndHeaderFromFile(outputTempFile, ',', 1);
            }

            FileUtils.delete(new File(outputTempFile));
        }catch(IOException | InterruptedException | CsvValidationException | URISyntaxException ex){
            throw new GenericFailureException(ex.getMessage());
        }
        return result;
    }

    public void setSupportFiles() throws InitFailureException {
        try {
            File f = new File(pathToExternalFolder.toString());
            if (!f.exists()) {
                if (messenger != null) {
                    messenger.SendMessage("Model " + super.getInfo().getName() + " is downloading support files");
                }
                log.info("Start to download the zip file.");
                File zipFile = File.createTempFile(this.getInfo().getKey(), ".zip");
                HTTPUtils.downloadFile(httpUrl, zipFile.getAbsolutePath());
                log.info("Finish to download the zip file.");
                FileUtilities.extractFilesFromZip(zipFile.getAbsolutePath(), pathToVEGAFolder.toString());
                zipFile.delete();
                log.info("Copied all necessary file from zip file.");
            } else {
                log.info("Already existing files and not copied.");
            }
        }
        catch(IOException ex){
            try {
                FileUtilities.deleteFolder(pathToExternalFolder.toString());
            } catch (IOException e) {
                throw new InitFailureException(ex.getMessage());
            }
            throw new InitFailureException(ex.getMessage());
        }
    }

    /**
     * Set up the conda environment, if there are additional files specific to the model, add them in the subclass
     * override method.
     * It will be moved to the startup of GUI, where all envs will be set
     * all in once. This method should be overridden IF the env require additional file management (like DILI-bayer)
     * @return boolean result to know if the whole execution is done smoothly. If there are some error, they are
     * reported in LOG
     */
    public boolean configureCondaEnv() throws InitFailureException {
        boolean isSet;
        try {
            if (messenger != null) {
                messenger.SendMessage("Model " + super.getInfo().getName() + " is checking conda environment");
            }

            isSet = communication.checkCondaEnv(getCondaEnv());

            if (!isSet) {
                if (messenger != null)
                    messenger.SendMessage("Model " + super.getInfo().getName() + " installing conda environment.");
                isSet = communication.configureCondaEnv(getCondaEnv(), Paths.get(pathToExternalFolder.toString(), getCondaEnv() + ".yml"));
            }
        }catch (InterruptedException | IOException ex){
            throw new InitFailureException(ex.getMessage());
        }

        return isSet;
    }

    public boolean removeCondaEnv() throws IOException, InterruptedException {
        return communication.removeCondaEnv(getCondaEnv());
    }

    public void prepareInputData() throws GenericFailureException {
        FileUtilities.WriteByteArrayToFile(inputTempFile,
                ("smiles\r\n"+CurMolecule.GetSMILES()).getBytes());
        log.info("Prepared input file.");
    }

    public abstract void setDescriptorGenerator(Object descriptorGenerator);

    public boolean isUsingCdddDescriptor() {
        return isUsingCdddDescriptor;
    }

    public iInsilicoModelRunnerMessenger getMessenger() {
        return messenger;
    }
    public void setMessenger(iInsilicoModelRunnerMessenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public void ProcessTrainingSet() throws Exception {
        this.setSkipADandTSLoading(true);
        TrainingSet TS = new TrainingSet();
        String TSPath = this.getInfo().getTrainingSetURL();
        String[] buf = TSPath.split("/");
        String DatName = buf[buf.length-1];
        TSPath = TSPath.substring(0, TSPath.length()-3) + "txt";

        //build the csv file with all the molecules
        List<String> molecules = buildMoleculeListFromTxt(TSPath);

        //if the model use the cddd then calculate them
        if(isUsingCdddDescriptor){
            CdddDescriptors cddd = new CdddDescriptors(molecules, true, messenger);
            if(!cddd.calculateDescriptors()){
                throw new GenericFailureException(String.format(StringSelectorCore
                                .getString("runner_consensus_exception_init_blocks"),
                        "CDDD descriptors failing execution"));
            }
            this.setDescriptorGenerator(cddd);
            //calculate the results
            TS.Build(TSPath, this);
            TS.SerializeToFile(DatName);
            cddd.dispose();
        }
        else{
            TS.Build(TSPath, this);
            TS.SerializeToFile(DatName);
        }
    }

    private List<String> buildMoleculeListFromTxt(String molFilePath) throws IOException, GenericFailureException {
        URL tsURL = getClass().getResource(molFilePath);
        DataInputStream in = new DataInputStream(tsURL.openStream());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String[] parsedString = bufferedReader.readLine().split("\t");
        if(parsedString.length < 5)
            throw new GenericFailureException(StringSelectorCore.getString("trainingset_header_error"));

        List<String> molecules = new ArrayList<>();
        String string;

        while((string = bufferedReader.readLine()) != null) {
            string = GeneralUtilities.TrimString(string);
            if (string.isEmpty())
                continue;
            molecules.add(string.split("\t")[2]);
        }

        return molecules;
    }
}
