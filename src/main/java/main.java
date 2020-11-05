
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;

import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.similarity.Similarity;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.smiles.SmilesParser;


import java.io.*;
import java.util.Arrays;


@Slf4j
public class main {

    public static void main(String[]  args) throws IOException, InitFailureException, GenericFailureException {

        Similarity sim = new Similarity();

        String smiles1 = "C(CCCC)C";
        String smiles2 = "C(CCCC)CCC";

        InsilicoMolecule mol1 = SmilesMolecule.Convert(smiles1);
        InsilicoMolecule mol2 = SmilesMolecule.Convert(smiles2);

        sim.Calculate(mol1.GetSimilarityDescriptors(), mol2.GetSimilarityDescriptors());




//        for(String smiles : smilesList) {
//            mol.GetSimilarityDescriptors();
//            sim.Calculate()
//        }


    }

}
