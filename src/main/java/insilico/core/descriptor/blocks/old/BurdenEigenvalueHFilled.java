package insilico.core.descriptor.blocks.old;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.old.weight.Electronegativity;
import insilico.core.descriptor.blocks.old.weight.Mass;
import insilico.core.descriptor.blocks.old.weight.Polarizability;
import insilico.core.descriptor.blocks.old.weight.VanDerWaals;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Burden eigenvalue (on H-Filled molecule) molecular descriptors.
 *
 * This version has the same implementation of BE descriptors, it just works
 * on a H-filled molecule. Several models need this block to be consistent
 * with original BE descriptors calculated by Dragon.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class BurdenEigenvalueHFilled extends DescriptorBlock {

    Logger logger = LoggerFactory.getLogger(BurdenEigenvalueHFilled.class);

    private boolean defaultDescriptors;


    private static final long serialVersionUID = 1L;
    private final static String BlockName = "Burden Eigenvalue Descriptors";

    public final static String PARAMETER_WEIGHT_M = "weightm";
    public final static String PARAMETER_WEIGHT_P = "weightp";
    public final static String PARAMETER_WEIGHT_E = "weighte";
    public final static String PARAMETER_WEIGHT_V = "weightv";

    private final static short WEIGHT_M_IDX = 0;
    private final static short WEIGHT_P_IDX = 1;
    private final static short WEIGHT_E_IDX = 2;
    private final static short WEIGHT_V_IDX = 3;
    private final static String[] WEIGHT_SYMBOL = {"m", "p", "e", "v"};
    private final static int MaxEig = 8;


    /**
     * Constructor. This should not be used, no weight is specified. The
     * overloaded constructors should be used instead.
     */
    public BurdenEigenvalueHFilled() {
        super();
        this.Name = BurdenEigenvalueHFilled.BlockName;
        this.defaultDescriptors = true;
    }

    public BurdenEigenvalueHFilled(boolean defaultDescriptors) {
        super();
        this.Name = BurdenEigenvalueHFilled.BlockName;
        this.defaultDescriptors = defaultDescriptors;
    }

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        ArrayList<Integer> weightList = BuildWeightList();
        for (Integer curWeight : weightList) {
            for (int i=0; i<MaxEig; i++) {
                Add("BEH" + (i+1) + WEIGHT_SYMBOL[curWeight], "");
                Add("BEL" + (i+1) + WEIGHT_SYMBOL[curWeight], "");
            }
        }
        SetAllValues(Descriptor.MISSING_VALUE);
    }


    private ArrayList<Integer> BuildWeightList() {
        ArrayList<Integer> w = new ArrayList<>();
        if(defaultDescriptors) {
            w.add((int) WEIGHT_M_IDX);
            w.add((int) WEIGHT_P_IDX);
            w.add((int) WEIGHT_E_IDX);
            w.add((int) WEIGHT_V_IDX);

        } else {
            if (getBoolProperty(PARAMETER_WEIGHT_M) )
                w.add((int) WEIGHT_M_IDX);
            if (getBoolProperty(PARAMETER_WEIGHT_P) )
                w.add((int) WEIGHT_P_IDX);
            if (getBoolProperty(PARAMETER_WEIGHT_E) )
                w.add((int) WEIGHT_E_IDX);
            if (getBoolProperty(PARAMETER_WEIGHT_V) )
                w.add((int) WEIGHT_V_IDX);

        }
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

        InsilicoMolecule HMol;
        IAtomContainer m;
        try {
            HMol = (InsilicoMolecule) mol.Clone();
            HMol.SetExplicitHydrogen(true);
            m = HMol.GetStructure();
        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets matrices
        double[][] BurdenMat;
        try {
            BurdenMat = HMol.GetMatrixBurden();
        } catch (GenericFailureException e) {
            logger.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        int nSK = m.getAtomCount();

        // Cycle for all found weighting schemes
        ArrayList<Integer> weightList = BuildWeightList();
        for (Integer curWeight : weightList) {

            // Sets needed weights
            double[] w = null;

            if (curWeight == WEIGHT_M_IDX)
                w = Mass.getWeights(m);
            if (curWeight == WEIGHT_P_IDX)
                w = Polarizability.getWeights(m);
            if (curWeight == WEIGHT_E_IDX)
                w = Electronegativity.getWeights(m);
            if (curWeight == WEIGHT_V_IDX)
                w = VanDerWaals.getWeights(m);

            // If one or more weights are not available, sets all to missing value
            boolean MissingWeight = false;
            for (int i=0; i<nSK; i++)
                if (w[i] == Descriptor.MISSING_VALUE)
                    MissingWeight = true;
            if (MissingWeight)
                continue;

            // Builds the weighted matrix
            for (int i=0; i<nSK; i++) {
                BurdenMat[i][i] = w[i];
            }

            // Calculates eigenvalues
            Matrix DataMatrix = new Matrix(BurdenMat);
            double[] eigenvalues;
            EigenvalueDecomposition ed = new EigenvalueDecomposition(DataMatrix);
            eigenvalues = ed.getRealEigenvalues();
            Arrays.sort(eigenvalues);

            for (int i=0; i<MaxEig; i++) {
                double valH, valL;
                if (i>(eigenvalues.length-1)) {
                    valH = 0;
                    valL = 0;
                } else {
                    valH = eigenvalues[eigenvalues.length-1-i];
                    valL = eigenvalues[i];
                }
                SetByName("BEH" + (i+1) + WEIGHT_SYMBOL[curWeight], valH);
                SetByName("BEL" + (i+1) + WEIGHT_SYMBOL[curWeight], valL);
            }
        }
    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        BurdenEigenvalueHFilled block = new BurdenEigenvalueHFilled();
        block.CloneDetailsFrom(this);
        return block;
    }


}
