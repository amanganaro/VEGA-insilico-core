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
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Burden eigenvalue molecular descriptors.
 *
 * NOTE: In DRAGON 5.5 Burden Eigenvalues are calculated on hydrogen filled
 * molecules
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class BurdenEigenvalue extends DescriptorBlock {

    private static final long serialVersionUID = 1L;

    Logger logger = LoggerFactory.getLogger(BurdenEigenvalue.class);

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
    public BurdenEigenvalue() {
        super();
        this.Name = BurdenEigenvalue.BlockName;
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

        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets matrices
        double[][] BurdenMat;
        try {
            BurdenMat = mol.GetMatrixBurden();
        } catch (GenericFailureException e) {
            InsilicoLogger.getLogger().warn(e.getMessage());
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

            // SpAD, SpMAD, SpPosA
            double meanEig = 0;
            double posSum = 0;
            for (double e : eigenvalues) {
                meanEig += e;
                if (e>0) posSum += e;
            }
            meanEig /= eigenvalues.length;

            double SpAD = 0;
            for (double e : eigenvalues)
                SpAD += Math.abs(e - meanEig);

            double SpMAD = SpAD / eigenvalues.length;

            double SpPosA = posSum / nSK;

            SetByName("SpAD_" + WEIGHT_SYMBOL[curWeight], SpAD);
            SetByName("SpMAD_" + WEIGHT_SYMBOL[curWeight], SpMAD);
            SetByName("SpPosA_" + WEIGHT_SYMBOL[curWeight], SpPosA);
        }
    }

    @Override
    protected void GenerateDescriptors() {
        DescList.clear();
        ArrayList<Integer> weightList = BuildWeightList();
        for (Integer curWeight : weightList) {
            for (int i=0; i<MaxEig; i++) {
                Add("BEH" + (i+1) + WEIGHT_SYMBOL[curWeight], "");
                Add("BEL" + (i+1) + WEIGHT_SYMBOL[curWeight], "");
            }
            Add("SpAD_" + WEIGHT_SYMBOL[curWeight], "Spectral absolute deviation");
            Add("SpMAD_" + WEIGHT_SYMBOL[curWeight], "Spectral absolute mean deviation");
            Add("SpPosA_" + WEIGHT_SYMBOL[curWeight], "Normalized spectral positive sum");
        }
        SetAllValues(Descriptor.MISSING_VALUE);
    }

    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        BurdenEigenvalue block = new BurdenEigenvalue();
        block.CloneDetailsFrom(this);
        return block;
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

}
