package insilico.core.descriptor.blocks;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.weight.Electronegativity;
import insilico.core.descriptor.weight.Mass;
import insilico.core.descriptor.weight.Polarizability;
import insilico.core.descriptor.weight.VanDerWaals;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.logger.InsilicoLogger;
import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Eigenvalue based descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class EigenvalueBased extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private final static String BlockName = "Eigenvalue-based Descriptors";
    
    public final static String PARAMETER_WEIGHT_M = "weightm";
    public final static String PARAMETER_WEIGHT_P = "weightp";
    public final static String PARAMETER_WEIGHT_E = "weighte";
    public final static String PARAMETER_WEIGHT_V = "weightv";

    private final static short WEIGHT_M_IDX = 0;
    private final static short WEIGHT_P_IDX = 1;
    private final static short WEIGHT_E_IDX = 2;
    private final static short WEIGHT_V_IDX = 3;
    private final static String[] WEIGHT_SYMBOL = {"m", "p", "e", "v"};


    /**
     * Constructor. This should not be used, no weight is specified. The 
     * overloaded constructors should be used instead.
     */    
    public EigenvalueBased() {
        super();
        this.Name = EigenvalueBased.BlockName;
    }

    
    
    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        ArrayList<Integer> weightList = BuildWeightList();
        for (Integer curWeight : weightList) {
            Add("Eig1" + WEIGHT_SYMBOL[curWeight], "");
            Add("SEig" + WEIGHT_SYMBOL[curWeight], "");
            Add("AEig" + WEIGHT_SYMBOL[curWeight], "");
        }     
        SetAllValues(Descriptor.MISSING_VALUE);
    }
    
    
    private ArrayList<Integer> BuildWeightList() {
        ArrayList<Integer> w = new ArrayList<>();
        if (getBoolProperty(PARAMETER_WEIGHT_M))
            w.add((int) WEIGHT_M_IDX);
        if (getBoolProperty(PARAMETER_WEIGHT_P))
            w.add((int) WEIGHT_P_IDX);
        if (getBoolProperty(PARAMETER_WEIGHT_E))
            w.add((int) WEIGHT_E_IDX);
        if (getBoolProperty(PARAMETER_WEIGHT_V))
            w.add((int) WEIGHT_V_IDX);
        return w;
    }

    

    /**
     * Calculate descriptors for the given molecule.
     * 
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {
        
        // Generate/clears descriptors
        GenerateDescriptors();

        // Builds needed matrices
        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        double[][] ConnMatrix;
        try {
            ConnMatrix = mol.GetMatrixConnectionAugmented();
        } catch (GenericFailureException e) {
            InsilicoLogger.getLogger().warn(e);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        int nSK = m.getAtomCount();
        
        // Cycle for all found weighting schemes
        ArrayList<Integer> weightList = BuildWeightList();
        for (Integer curWeight : weightList) {
        
            // Sets needed weights
            double[] w = null;
            double refW = 0;

            if (curWeight == WEIGHT_M_IDX) {
                w = Mass.getWeights(m);
                refW = Mass.GetMass("C");
            }
            if (curWeight == WEIGHT_P_IDX) {
                w = Polarizability.getWeights(m);
                refW = Polarizability.GetPolarizability("C");
            }
            if (curWeight == WEIGHT_E_IDX) {
                w = Electronegativity.getWeights(m);
                refW = Electronegativity.GetElectronegativity("C");
            }
            if (curWeight == WEIGHT_V_IDX) {
                w = VanDerWaals.getWeights(m);
                refW = VanDerWaals.GetVdWVolume("C");
            }

            // If one or more weights are not available, sets all to missing value
            boolean MissingWeight = false;
            for (int i=0; i<nSK; i++) 
                if (w[i] == Descriptor.MISSING_VALUE)
                    MissingWeight = true;
            if (MissingWeight)        
                continue;

            // Builds matrix
            double[][] EigMat = new double[nSK][nSK];
            for (int i=0; i<nSK; i++)
                for (int j=0; j<nSK; j++) {

                    if (i==j) {

                        EigMat[i][j] = 1 - (refW / w[i]);

                    } else {

                        // builds shortest path between i and j
                        Atom at1 = (Atom) m.getAtom(i);
                        Atom at2 = (Atom) m.getAtom(j);
                        List<IAtom> Path = PathTools.getShortestPath(m, at1, at2);

                        double val = 0;
                        for (int k=0; k<(Path.size()-1); k++) {
                            int a1 = m.getAtomNumber(Path.get(k));
                            int a2 = m.getAtomNumber(Path.get(k+1));
                            double bond = ConnMatrix[a1][a2];
                            val += (1 / bond) * (Math.pow(refW, 2) / (w[a1] * w[a2]) );
                        }

                        EigMat[i][j] = val;

                    }
                }

            // Calculates eigenvalues
            Matrix DataMatrix = new Matrix(EigMat);
            double[] eigenvalues;
            EigenvalueDecomposition ed = new EigenvalueDecomposition(DataMatrix);
            eigenvalues = ed.getRealEigenvalues();
            Arrays.sort(eigenvalues);

            double FirstEig = eigenvalues[eigenvalues.length-1];

            double AEig=0, SEig=0;
            for (int i=0; i<eigenvalues.length; i++) {
                AEig += Math.abs(eigenvalues[i]);
                SEig += eigenvalues[i];
            }

            SetByName("Eig1" + WEIGHT_SYMBOL[curWeight], FirstEig);
            SetByName("AEig" + WEIGHT_SYMBOL[curWeight], AEig);
            SetByName("SEig" + WEIGHT_SYMBOL[curWeight], SEig);
        }
    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        EigenvalueBased block = new EigenvalueBased();
        block.CloneDetailsFrom(this);
        return block;
    }

    
    
}
