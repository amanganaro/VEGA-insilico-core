package insilico.core;

import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
public class CoreUtils {

    public CoreUtils(){}


    private static final String[] datasets = {
            "VP",
            "logp",
            "ncs",
            "muta"
    };

    public static List<String> fetchSmilesFromDatasetsFile() throws IOException {

        List<String> smiles = new ArrayList<>();

        for (String dataset : datasets) {

            String datasetLoc = "datasets/" + dataset + ".txt";
            BufferedReader datasetsReader = new BufferedReader(new FileReader(datasetLoc));

            String datasetRow = datasetsReader.readLine();


            while( datasetRow != null){

                smiles.add(datasetRow);
                datasetRow = datasetsReader.readLine();
            }

            datasetsReader.close();
        }

        return smiles;
    }

    public static void mergeDatasetsAndClearDuplicates(List<String> Smiles, String fileName) throws IOException {

        LinkedHashSet<String> hashSet = new LinkedHashSet<>(Smiles);
        List<String> finalSmiles = new ArrayList<>(hashSet);
        System.out.println("Found " + (Smiles.size() - finalSmiles.size()) + " duplicates SMILES");

        String file = "datasets/" + fileName + ".txt";
        File txt = new File(file);
        FileWriter finalDatasetWriter = new FileWriter(txt);


        for(String smiles : finalSmiles){
            finalDatasetWriter.append(smiles).append("\n");
        }
        finalDatasetWriter.flush();
        finalDatasetWriter.close();
    }



    private static ArrayList<String> fetchSmilesFromTXTFile() {
        ArrayList<String> smiles = new ArrayList<>();
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/src/main/java/VP.txt"));
            String lineFetched = null;
            String[] smilesArray;

            while (true) {
                lineFetched = buffer.readLine();
                if (lineFetched == null){
                    break;
                }
                smilesArray = lineFetched.split("\t");
                smiles.add(smilesArray[0]);
            }

            buffer.close();


        } catch (IOException ex){
            ex.printStackTrace();
        }

        return smiles;
    }

    private static ArrayList<InsilicoMolecule> fetchMoleculesFromSMILES(ArrayList<String> SMILES) throws InvalidMoleculeException {
        ArrayList<InsilicoMolecule> molecules = new ArrayList<>();
        int count = 0;
        for(String smiles: SMILES){

            if(count == 0)
                count++;
            InsilicoMolecule mol = new InsilicoMolecule();
            log.info("["  + count + "] " + smiles);
            mol.SetSMILES(smiles);
            mol.MarkAsValid();
            mol.GetStructure();
            molecules.add(mol);
            log.info("["  + count + "] + MOLECULE CREATED FROM " + smiles);
            count++;
        }
        return molecules;
    }

}
