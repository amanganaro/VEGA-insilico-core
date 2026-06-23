package insilico.core.auxiliary.protoqsar.sludge;

import insilico.core.molecule.InsilicoMolecule;
import libpadeldescriptor.*;
import org.openscience.cdk1.qsar.IMolecularDescriptor;
import org.openscience.cdk1.qsar.descriptors.molecular.BCUTDescriptor;
import padeladapter.PadelInterface;

import java.util.ArrayList;

/**
 *
 * @author Alberto
 */
public class SludgeDescriptors {
    
    private final ArrayList<Double> DescList;
    
    
    public SludgeDescriptors() {
        DescList = new ArrayList<>();
    }    
    
    
    public void Calculate(InsilicoMolecule mol) throws Exception {

        PadelInterface PI = new PadelInterface();
        
        IMolecularDescriptor MD;
        ArrayList<String> DescNames;

        PI.SetSMILES(mol.GetSMILES());

        // classification

        MD = new AutocorrelationDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("ATSC4p");
        double[] res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res) 
            DescList.add(d);

        MD = new ExtendedTopochemicalAtomDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("ETA_BetaP_ns_d");
        res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);

        MD = new AutocorrelationDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("GATS1i");
        DescNames.add("GATS3c");
        res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);

        MD = new EStateAtomTypeDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("maxHother");
        DescNames.add("minsCH3");
        DescNames.add("minwHBa");
        res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);

        MD = new BurdenModifiedEigenvaluesDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("SpMax1_Bhm");
        res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);

        // regression

        MD = new EStateAtomTypeDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("minHBint2");
        res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);          

        MD = new AutocorrelationDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("ATSC7v");
        res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);          

        MD = new BaryszMatrixDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("VE3_DzZ");
            res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);          

        MD = new AutocorrelationDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("AATSC4e");
        res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);          

        MD = new BCUTDescriptor();
        DescNames = new ArrayList<>();
        DescNames.add("BCUTp-1l");
        res = PI.CalculateDescriptors(MD, DescNames);
        for (double d : res)
            DescList.add(d);          
                
    }
    
    
    public ArrayList<Double> GetDescriptors() {
        return DescList;
    }
    
    
    public double[] GetDescriptorsForSludgeClassificationModel() throws Exception {
        double[] desc = new double[8];
        for (int i=0; i<8; i++)
            desc[i] = DescList.get(i);
        return desc;
    }


    public double[] GetDescriptorsForSludgeQuantitativeModel() throws Exception {
        double[] desc = new double[5];
        for (int i=0; i<5; i++)
            desc[i] = DescList.get(8+i);
        return desc;
    }
    
}
