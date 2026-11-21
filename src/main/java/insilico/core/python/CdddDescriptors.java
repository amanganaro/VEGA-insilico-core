package insilico.core.python;

import java.nio.charset.StandardCharsets;
import insilico.core.exception.*;
import insilico.core.model.runner.iInsilicoModelRunnerMessenger;
import insilico.core.tools.utils.FileUtilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
public class CdddDescriptors {

    /** Column of the CDDD descriptor files holding the SMILES of the molecule, in the CDDD canonical form. */
    private static final String CDDD_SMILES_COLUMN = "smiles";

    Communication communication;
    private Path pathToExternalFolder;
    private Path pathToVEGAFolder;
    private List<String> smilesList;
    private Map<String, String> smilesFileMap;
    private String descriptorDirectory;
    private String inputSmilesFileName;
    protected iInsilicoModelRunnerMessenger messenger;
    private Path destinationCdddModelDefault = null;

    public CdddDescriptors(List<String> smilesList, boolean bypassCheckCondaEnv, iInsilicoModelRunnerMessenger messenger) throws InitFailureException, InitFailurePythonException {

        this.messenger = Objects.requireNonNullElseGet(messenger, () -> new iInsilicoModelRunnerMessenger() {
            @Override
            public void SendMessage(String msg) {
                System.out.println(msg);
            }

            @Override
            public void UpdateProgress() {
                System.out.println("No progress update");
            }
        });
        try {
            communication = new Communication();
        }catch (IOException e){
            log.error("Communication class can not be initialized: {}", e.getMessage());
            throw new InitFailurePythonException(e.getMessage());
        }

        // check if conda or conda pack is installed
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                if (!checkIfThereIsCondaPackEnv()) {
                    if (!communication.checkConda()) {
                        communication.installConda();
                    }
                }
            } else if (SystemUtils.IS_OS_MAC) {
                if (!communication.checkConda()) {
                    communication.installConda();
                }
            } else if (SystemUtils.IS_OS_LINUX) {
                if (!communication.checkConda()) {
                    communication.installConda();
                }
            }
        }catch (IOException | InterruptedException e){
            log.error("Error installing conda: {}", e.getMessage());
            throw new InitFailurePythonException(e.getMessage());
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            pathToExternalFolder = Paths.get(System.getProperty("user.home"),
                    "AppData", "Local", "vega-models", "descriptors", "cddd").resolve("");
            pathToVEGAFolder = Paths.get(System.getProperty("user.home"),
                    "AppData", "Local", "vega-models", "descriptors").resolve("");
        }
        else if(SystemUtils.IS_OS_LINUX){
            pathToExternalFolder = Paths.get(System.getProperty("user.home") ,
                    ".local", "share", "vega-models", "descriptors", "cddd");
            pathToVEGAFolder = Paths.get(System.getProperty("user.home") ,
                    ".local", "share", "vega-models", "descriptors");
        }
        else if (SystemUtils.IS_OS_MAC) {
            pathToExternalFolder = Paths.get(System.getProperty("user.home") ,
                    "Library", "Application Support", "vega-models", "descriptors", "cddd");
            pathToVEGAFolder = Paths.get(System.getProperty("user.home") ,
                    "Library", "Application Support", "vega-models", "descriptors");
        }

        try {
            if(smilesList != null &&  !smilesList.isEmpty()) {
                File f = File.createTempFile("input-smiles", ".csv");
                inputSmilesFileName = f.getAbsolutePath();
                f = Files.createTempDirectory("cddd-descriptors").toFile();
                descriptorDirectory = f.getAbsolutePath();
            }

            setSupportFiles();

            if (!bypassCheckCondaEnv) {

                if (SystemUtils.IS_OS_WINDOWS && checkIfThereIsCondaPackEnv()) {
                    boolean isEnvSet = configurePythonEnv();
                    if (!isEnvSet) {
                        throw new InitFailurePythonException("Python environment " + getPythonEnv() + " not set");
                    }
                }
                else{
                    boolean isEnvSet = configureCondaEnv();
                    if (!isEnvSet) {
                        throw new InitFailurePythonException("Conda environment " + getCondaEnv() + " not set");
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InitFailurePythonException("Error in creating cddd support files. Conda environment " + getCondaEnv() + " not set");
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new InitFailurePythonException("Conda environment " + getCondaEnv() + " not set");
        }
        this.smilesList=smilesList;
    }

    /***
     * Writes the input file of the CDDD script: one SMILES per line.
     * <p>
     * No filler molecule is added here. The encoder needs a number of rows that is a multiple of 128 to give
     * reproducible descriptors, but the molecules it rejects are dropped by the script itself, so only the script
     * knows how many actually reach it: the filling lives in {@code app-cddd.py}.
     * @param inputFile path of the file to write
     * @throws GenericFailureException if the file cannot be written
     */
    public void prepareInputData(String inputFile) throws GenericFailureException {
        StringBuilder sb=new StringBuilder();
        sb.append("smiles\n");
        for (String smiles:smilesList) {
            sb.append(smiles).append("\n");
        }

        FileUtilities.WriteByteArrayToFile(inputFile,sb.toString().getBytes(StandardCharsets.UTF_8));
        log.info("Prepared input file with {} molecules.", smilesList.size());
    }

    /***
     * Calculate cddd descriptors from cddd library. It executes a python script that calculate from a csv input file
     * the 512 descriptors and for each smiles generates a csv file within the correspondent descriptors
     * @return true if the computation went smoothly otherwise false
     */
    public boolean calculateDescriptors() throws GenericFailureException {
        try {
            prepareInputData(inputSmilesFileName);

            log.info("Start to calculate descriptors");
            String pathToScriptFile = Paths.get(pathToExternalFolder.toString(), "app-cddd.py").toAbsolutePath().toString();
//            boolean result = communication.executeScriptInCondaEnv(getCondaEnv(), pathToScriptFile,
//                    "--input \"" + inputSmilesFileName+"\"",
//                    " --output \"" + descriptorDirectory+"\"");
            boolean result = communication.executePureCommandInPythonEnv(getPythonEnv(), pathToScriptFile,
                    "--input", inputSmilesFileName, "--output", descriptorDirectory);
            if (result) {
                for (int i = 0; i < smilesList.size(); i++) {
                    if (smilesFileMap == null) {
                        smilesFileMap = new HashMap<>();
                    }
                    smilesFileMap.put(smilesList.get(i), descriptorDirectory + File.separator + i + ".csv");
                }
            }
            else{
                dispose();
            }

            log.info("Finish to calculate descriptors");
            return result;

        }catch(IOException | InterruptedException ex){
            log.error(ex.getMessage());
            throw new GenericFailureException(ex.getMessage());
        }
    }

    public String getCondaEnv(){
        return "VEGA_cddd_V2";
    }

    public String getPythonEnv(){
        return "VEGA_cddd_V2";
    }

    public void setSupportFiles() throws InitFailurePythonException {


        if (SystemUtils.IS_OS_WINDOWS) {
            destinationCdddModelDefault = Paths.get(System.getProperty("user.home"),
                    "AppData", "Local", "cddd", "cddd", "default_model");
        }
        else if(SystemUtils.IS_OS_LINUX) {
            destinationCdddModelDefault = Paths.get(System.getProperty("user.home"),
                    ".local", "share", "cddd", "default_model");
        }
        else if(SystemUtils.IS_OS_MAC){
            destinationCdddModelDefault = Paths.get(System.getProperty("user.home"),
                    "Library", "Application Support", "cddd", "default_model");
        }

        try {

            File f = new File(pathToExternalFolder.toString());
            File f3 = new File(destinationCdddModelDefault.toString());
            if (!f.exists() || !f3.exists()) {
                if (messenger != null) {
                    messenger.SendMessage("CDDD descriptors package is extracting support files");
                }

                log.info("Start to extract the cddd.zip file.");

                // The serialized CDDD model is not part of the Core: it comes from the InSilico:VegaResources-Cddd
                // artifact, which the model modules using CDDD descriptors declare as a dependency
                InputStream is = getClass().getResourceAsStream("/descriptor/cddd.zip");
                if (is == null) {
                    log.error("File cddd.zip not found on the classpath");
                    throw new PythonModelResourceNotFoundException("File /descriptor/cddd.zip not found on the " +
                            "classpath: add the dependency InSilico:VegaResources-Cddd");
                }

                File zipFile = File.createTempFile("insilico-model-", ".zip");
                zipFile.deleteOnExit();
                try(
                        InputStream in = is;
                        OutputStream out = new FileOutputStream(zipFile)
                ){
                    in.transferTo(out);
                }
                FileUtilities.extractFilesFromZip(zipFile.getAbsolutePath(), pathToVEGAFolder.toString());
                log.info("Extracted cddd folder.");

                // add default model folder into the directory wanted from cddd
                final Path destinationCdddModelDefaultFinal = destinationCdddModelDefault;
                Path sourceDir = Paths.get(pathToVEGAFolder.toString(),"cddd", "default_model");

                Files.walk(sourceDir)
                        .forEach(source -> {
                            Path relative = sourceDir.relativize(source);
                            Path destination = destinationCdddModelDefaultFinal.resolve(relative);

                            try {
                                if (Files.isDirectory(source)) {
                                    Files.createDirectories(destination);
                                } else {
                                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException e) {
                                log.error("Error in copy CDDD default model. Error copying {} to {}: {}", source, destination, e.getMessage());
                                throw new PythonEnvironemntFailedException("Cddd support files do not set");
                            }
                        });

                log.info("Copied cddd descriptors default model file");

                zipFile.delete();
                FileUtilities.deleteFolder(Paths.get(pathToVEGAFolder.toString(),"cddd", "default_model").toString());

                log.info("Copied all necessary file from cddd zip file.");
            } else {
                log.info("Already existing files and not copied.");
            }
        }
        catch(IOException ex){
            try {
                FileUtilities.deleteFolder(pathToExternalFolder.toString());
                FileUtilities.deleteFolder(destinationCdddModelDefault.toString());
            } catch (IOException e) {
                log.error("CDDD files are not copied, an network error might be occurred.");
                throw new InitFailurePythonException(ex.getMessage());
            }
            log.error("CDDD files are not copied, an network error might be occurred.");
            throw new InitFailurePythonException(ex.getMessage());
        }
    }


    /***
     * Move the .yml .whl and default_model folder into Local data folder to use that files to
     * setup the configuration. This is made to use external data instead the one in the project
     * @return
     */
    public boolean configureCondaEnv() throws InterruptedException, IOException {

        if (messenger != null) {
            messenger.SendMessage("CDDD descriptors checking conda environment.");
        }

        boolean isSet = communication.checkCondaEnv(getCondaEnv());

        if(!isSet) {
            Path pathToEnvFile = Paths.get(pathToExternalFolder.toString(), getCondaEnv()+".yml");
            if (messenger != null) {
                messenger.SendMessage("CDDD descriptors installing conda environment.");
            }
            isSet = communication.configureCondaEnv(getCondaEnv(), pathToEnvFile);
            if (!isSet) {
                log.error("Error in set up conda environment {}", getCondaEnv());
            }

            log.info("Conda environment {} set up {}", getCondaEnv(), isSet ? "correctly": "failed");
        }

        return isSet;
    }

    public boolean configurePythonEnv() throws InterruptedException, IOException {

        if (messenger != null) {
            messenger.SendMessage("CDDD descriptors checking python environment.");
        }

        boolean isSet = communication.checkPythonEnv(getPythonEnv());

        if(!isSet) {
            if (messenger != null) {
                messenger.SendMessage("CDDD descriptors installing python environment.");
            }
            isSet = communication.configurePythonEnv(getPythonEnv());
            if (!isSet) {
                log.error("Error in set up python environment {}", getPythonEnv());
            }

            log.info("Python environment {} set up {}", getPythonEnv(), isSet ? "correctly": "failed");
        }

        return isSet;
    }

    public boolean removeCondaEnv() throws IOException, InterruptedException {
        return communication.removeCondaEnv(getCondaEnv());
    }

    public boolean removePythonEnv() throws IOException, InterruptedException {
        return communication.removePythonEnv(getPythonEnv());
    }

    public void removeResourceFolder() throws IOException {
        FileUtilities.deleteFolder(pathToVEGAFolder.toString());
        if(destinationCdddModelDefault != null) {
            FileUtilities.deleteFolder(destinationCdddModelDefault.toString());
        }
    }

    public String getFilePathOf(String smiles){
        return smilesFileMap.get(smiles);
    }

    /***
     * Writes a single CSV file holding the descriptors of several molecules, one row per molecule, in the order of
     * the given SMILES list. The per-molecule files produced by the CDDD script all share the same header, so the
     * combined file has exactly the format the model scripts already expect to read: concatenating them needs no
     * change to any Python script.
     * <p>
     * This is what allows a Python model to be executed in batch mode: instead of one descriptor file per molecule
     * (and therefore one Python process per molecule), the model receives one file with every molecule in it.
     *
     * The returned map is what lets the caller recognise the rows that come back: the CDDD script rewrites the SMILES
     * of each molecule in its own canonical form, so the model script echoes that form and not the SMILES this method
     * was given. Mapping one to the other here, where the file is written, is the only place where the two are known
     * together.
     *
     * @param smilesList the molecules to include, in the order their results are expected back
     * @param target the file to write
     * @return the SMILES written in the file, each mapped to the molecule of {@code smilesList} it belongs to. Two
     * molecules with the same canonical form keep only the first, so no SMILES is ever mapped to two molecules
     * @throws IOException if a descriptor file is missing or unreadable, which would silently misalign the rows
     */
    public Map<String, String> writeCombinedDescriptorFile(List<String> smilesList, Path target) throws IOException {

        boolean headerWritten = false;
        int smilesColumn = -1;
        Map<String, String> writtenSmiles = new LinkedHashMap<>();

        try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            for (String smiles : smilesList) {
                String descriptorFile = getFilePathOf(smiles);
                if (descriptorFile == null) {
                    throw new IOException("No CDDD descriptor file for molecule '" + smiles
                            + "': cannot build the combined descriptor file");
                }

                List<String> lines = Files.readAllLines(Paths.get(descriptorFile), StandardCharsets.UTF_8);
                if (lines.size() < 2) {
                    throw new IOException("CDDD descriptor file " + descriptorFile + " of molecule '" + smiles
                            + "' has no data row");
                }

                if (!headerWritten) {
                    writer.write(lines.get(0));
                    writer.newLine();
                    headerWritten = true;
                    smilesColumn = columnIndexOf(lines.get(0), CDDD_SMILES_COLUMN);
                }

                // copy the data rows verbatim, so quoting and separators stay exactly as the CDDD script wrote them
                for (int i = 1; i < lines.size(); i++) {
                    writer.write(lines.get(i));
                    writer.newLine();
                    if (smilesColumn >= 0) {
                        String written = valueAt(lines.get(i), smilesColumn);
                        if (written != null) {
                            writtenSmiles.putIfAbsent(written, smiles);
                        }
                    }
                }
            }
        }

        log.info("Written combined descriptor file with {} molecules in {}", smilesList.size(), target);
        return writtenSmiles;
    }

    /**
     * @param header the header line of a CDDD descriptor file
     * @param column the column to look for
     * @return the index of the column, or -1 if the header does not have it
     */
    private static int columnIndexOf(String header, String column) {
        String[] names = header.split(",", -1);
        for (int i = 0; i < names.length; i++) {
            if (column.equalsIgnoreCase(unquote(names[i].trim()))) {
                return i;
            }
        }
        log.warn("No '{}' column in the CDDD descriptor file header: the rows of a batch execution will have to be "
                + "matched by position", column);
        return -1;
    }

    /**
     * @param line a data line of a CDDD descriptor file
     * @param index the column to read
     * @return the value of that column, or null if the line is shorter than expected
     */
    private static String valueAt(String line, int index) {
        String[] values = line.split(",", -1);
        return index < values.length ? unquote(values[index]) : null;
    }

    /** @param value a CSV value @return the value without its surrounding double quotes, if it has any */
    private static String unquote(String value) {
        if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
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
            myReader = new Scanner(f, StandardCharsets.UTF_8.name());
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

    private boolean checkIfThereIsCondaPackEnv(){
        // only checks that the resource exists, without opening a stream on it
        return getClass().getResource("/condapack-env/"+getPythonEnv()+".zip") != null;
    }
}
