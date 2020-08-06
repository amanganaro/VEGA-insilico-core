/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import insilico.core.molecule.tools.InsilicoMoleculeNormalization;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/**
 *
 * @author Alberto
 */
public class main_normalization {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        DataInputStream in;
        BufferedReader br;

        FileOutputStream fileOutputStream = new FileOutputStream("normalization_test.csv");
        PrintStream stream = new PrintStream(fileOutputStream);

//        URL TsURL = JavaApplication9.class.getResource("/JavaApplication9/logp.txt");
//        URL TsURL = JavaApplication9.class.getResource("/JavaApplication9/ncs.txt");
        URL TsURL = main_normalization.class.getResource("muta.txt");
        in = new DataInputStream(TsURL.openStream());
        br = new BufferedReader(new InputStreamReader(in));
        
        int count = 1;
        String s = "";
        ArrayList<String> smi = new ArrayList<>();
        while ( (s = br.readLine())!= null ) {              
            smi.add(s);
        }
        
        process(smi, stream);
        if (1==1) return;
        
        IAtomContainer curStructure = null;
        String SMILES = "O=C1C(=C(N=C2C=CC=CN12)C)C";
        
        // Use CDK parser to convert SMILES
        // Order of aromatic bonds are set through its kekulization routine
        // Note that atom and bonds, even if in kekulè form, mantain the 
        // original aromaticity flag
        // If explicit aromatic is wrong in the SMILES like in c1cc(=O)ccc1
        // the method raises a InvalidSmilesException
        // CHECK: togliere tutti i flag aromatici e calcolare dopo?
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        sp.kekulise(true);        
        curStructure = sp.parseSmiles(SMILES);

        curStructure = InsilicoMoleculeNormalization.Normalize(curStructure);

        for (IAtom a : curStructure.atoms()) {
            System.out.println(a.getSymbol() + ", " +  a.getAtomTypeName() + ", " + a.getAtomicNumber() +
                    ", charge:" + a.getFormalCharge() +
                    ", arom:" + a.getFlag(CDKConstants.ISAROMATIC)+ ", H:" + a.getImplicitHydrogenCount());
        }
        
        for (IBond b : curStructure.bonds()) {
            System.out.println(b.getOrder().numeric() + ", " + b.getFlag(CDKConstants.ISAROMATIC));
        }
    }
    
    
    public static void process(ArrayList<String> SMILES, PrintStream out) throws Exception {
        
        IAtomContainer curStructure = null;
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        sp.kekulise(true);    
        
        String h = "smi\tsk\tar\tal\tplus\tminus\tsbnd\tdbnd\tarbnd";
        System.out.println(h);
        out.println(h);
        
        int count = 1;
        for (String smi : SMILES) {

            System.out.print(count++ + "\t");
//            out.println(count++ + "\t");
            
            try {
                curStructure = sp.parseSmiles(smi);
                curStructure = InsilicoMoleculeNormalization.Normalize(curStructure);
            } catch (Throwable e) {
                System.out.println(smi);
                continue;
            }

            int nSk=0, nAr=0, nAl=0;
            int nPlus=0, nMinus=0;
            int nSinBond=0, nDblBond=0, nAromBond=0;

            for (IAtom a : curStructure.atoms()) {
                nSk++;
                if (a.getFormalCharge() == 1) nPlus++;
                if (a.getFormalCharge() == -1) nMinus++;
                if (a.getFlag(CDKConstants.ISAROMATIC)) nAr++;
                else nAl++;
            }

            for (IBond b : curStructure.bonds()) {
                if (b.getFlag(CDKConstants.ISAROMATIC)) nAromBond++;
                else {
                    if (b.getOrder().numeric() == 1) nSinBond++;
                    if (b.getOrder().numeric() == 2) nDblBond++;
                }
            }
            
            String s = smi;
            s += "\t" + nSk +"\t" + nAr +"\t" + nAl +"\t" + nPlus +"\t" + nMinus;
            s += "\t" + nSinBond + "\t" + nDblBond + "\t" + nAromBond;
            System.out.println(s);
            out.println(s);
        }
    }
}
