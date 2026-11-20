package insilico.core.auxiliary.protoqsar.sludge;

import insilico.core.molecule.InsilicoMolecule;
import padeladapter.PadelInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * PaDEL descriptors of the ProtoQSAR sludge models (classification and quantitative), calculated through the PaDEL
 * service in a single request per molecule.
 *
 * @author Alberto
 */
public class SludgeDescriptors {
    
    private final ArrayList<Double> DescList;
    
    
    public SludgeDescriptors() {
        DescList = new ArrayList<>();
    }    
    
    
    public void Calculate(InsilicoMolecule mol) throws Exception {

        double[][] res = new PadelInterface().newRequest(mol.GetSMILES())
                // classification
                .add("AutocorrelationDescriptor", List.of("ATSC4p"))
                .add("ExtendedTopochemicalAtomDescriptor", List.of("ETA_BetaP_ns_d"))
                .add("AutocorrelationDescriptor", List.of("GATS1i", "GATS3c"))
                .add("EStateAtomTypeDescriptor", List.of("maxHother", "minsCH3", "minwHBa"))
                .add("BurdenModifiedEigenvaluesDescriptor", List.of("SpMax1_Bhm"))
                // regression
                .add("EStateAtomTypeDescriptor", List.of("minHBint2"))
                .add("AutocorrelationDescriptor", List.of("ATSC7v"))
                .add("BaryszMatrixDescriptor", List.of("VE3_DzZ"))
                .add("AutocorrelationDescriptor", List.of("AATSC4e"))
                .add("BCUTDescriptor", List.of("BCUTp-1l"))
                .calculate();

        // 8 descriptors for the classification model, then 5 for the quantitative one
        for (double[] block : res)
            for (double d : block)
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
