import insilico.core.descriptor.blocks.Constitutional;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.test.TestDescriptors;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.Element;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.config.IsotopeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.rmi.Remote;
import java.util.ArrayList;


public class main {

    private static Logger logger = LoggerFactory.getLogger(main.class);

    public static void main(String[]  args) throws Exception {

        FileOutputStream fileOutputStream = new FileOutputStream("test.csv");
        PrintStream stream = new PrintStream(fileOutputStream);
//        TestDescriptors.Run(new Constitutional(), System.out);
        TestDescriptors.Run(new Constitutional(), stream);


//        ArrayList<String> SMILES = fetchSmilesFromTXTFile();
//        ArrayList<InsilicoMolecule> molecules = fetchMoleculesFromSMILES(SMILES);

    }

    
    private static ArrayList<InsilicoMolecule> fetchMoleculesFromSMILES(ArrayList<String> SMILES) throws InvalidMoleculeException {
        ArrayList<InsilicoMolecule> molecules = new ArrayList<>();
        int count = 0;
        for(String smiles: SMILES){
            if(count == 0)
                count++;
            InsilicoMolecule mol = new InsilicoMolecule();
            logger.info("["  + count + "] " + smiles);
            mol.SetSMILES(smiles);
            mol.MarkAsValid();
            mol.GetStructure();
            molecules.add(mol);
            logger.info("["  + count + "] + MOLECULE CREATED FROM " + smiles);
            count++;
        }
        return molecules;
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





}
