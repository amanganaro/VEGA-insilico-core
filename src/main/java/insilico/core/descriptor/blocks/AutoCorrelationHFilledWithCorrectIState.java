package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.weight.*;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.matrix.TopoDistanceMatrix;
import insilico.core.molecule.tools.Manipulator;
import insilico.core.tools.logger.InsilicoLogger;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;

public class AutoCorrelationHFilledWithCorrectIState extends DescriptorBlock {

    private final static long serialVersionUID = 1L;
    private final static String BlockName = "AutoCorrelation Descriptors";

    public final static String PARAMETER_WEIGHT_M = "weightm";
    public final static String PARAMETER_WEIGHT_P = "weightp";
    public final static String PARAMETER_WEIGHT_E = "weighte";
    public final static String PARAMETER_WEIGHT_V = "weightv";
    public final static String PARAMETER_WEIGHT_S = "weights";
    public final static String PARAMETER_LAG_01 = "lag01";
    public final static String PARAMETER_LAG_02 = "lag02";
    public final static String PARAMETER_LAG_03 = "lag03";
    public final static String PARAMETER_LAG_04 = "lag04";
    public final static String PARAMETER_LAG_05 = "lag05";
    public final static String PARAMETER_LAG_06 = "lag06";
    public final static String PARAMETER_LAG_07 = "lag07";
    public final static String PARAMETER_LAG_08 = "lag08";

    private final static short WEIGHT_M_IDX = 0;
    private final static short WEIGHT_P_IDX = 1;
    private final static short WEIGHT_E_IDX = 2;
    private final static short WEIGHT_V_IDX = 3;
    private final static short WEIGHT_S_IDX = 4;
    private final static String[] WEIGHT_SYMBOL = {"m", "p", "e", "v", "s"};



    /**
     * Constructor. This should not be used, no weight is specified. The
     * overloaded constructors should be used instead.
     */
    public AutoCorrelationHFilledWithCorrectIState() {
        super();
        this.Name = AutoCorrelationHFilledWithCorrectIState.BlockName;
    }

    @Override
    protected void GenerateDescriptors() {
        DescList.clear();
        ArrayList<Integer> weightList = BuildWeightList();
        ArrayList<Integer> lagList = BuildLagList();
        for (Integer curWeight : weightList) {
            for (Integer curLag : lagList) {
                Add("ATS" + curLag.toString() + WEIGHT_SYMBOL[curWeight], "");
                Add("ATSC" + curLag.toString() + WEIGHT_SYMBOL[curWeight], "");
                Add("MATS" + curLag.toString() + WEIGHT_SYMBOL[curWeight], "");
                Add("GATS" + curLag.toString() + WEIGHT_SYMBOL[curWeight], "");
            }
        }
        SetAllValues(Descriptor.MISSING_VALUE);
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
            IAtomContainer orig_m = mol.GetStructure();
            m = Manipulator.AddHydrogens(orig_m);
        } catch (InvalidMoleculeException | GenericFailureException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets matrices
        int[][] TopoMatrix;
        try {
            TopoMatrix = TopoDistanceMatrix.getMatrix(m);
        } catch (Exception e) {
            InsilicoLogger.getLogger().warn(e);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // !!! in origine era usata la topological matrix del cdk

        int nSK = m.getAtomCount();

        // Cycle for all found weighting schemes
        ArrayList<Integer> weightList = BuildWeightList();
        ArrayList<Integer> lagList = BuildLagList();

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
            if (curWeight == WEIGHT_S_IDX) {
                try {
                    EStateCorrectForH ES = new EStateCorrectForH(m);
                    w = ES.getIS();

                    // correction for compatibility with D7
                    // H I-state is always 1
                    for (int i=0; i<nSK; i++) {
                        if (m.getAtom(i).getSymbol().equalsIgnoreCase("H"))
                            w[i] = 1;
                    }
                } catch (Exception e) {
                    w = new double[nSK];
                    for (int i=0; i<nSK; i++) w[i]=Descriptor.MISSING_VALUE;
                }
            }


            // If one or more weights are not available, sets all to missing value
            boolean MissingWeight = false;
            for (int i=0; i<nSK; i++)
                if (w[i] == Descriptor.MISSING_VALUE)
                    MissingWeight = true;
            if (MissingWeight)
                continue;

            // Calculates weights averages
            double wA = 0;
            for (int i=0; i<nSK; i++)
                wA += w[i];
            wA = wA / ((double) nSK);

            // Calculates autocorrelations
            for (Integer lag : lagList) {

                double AC=0, ACS=0, MoranAC=0, GearyAC=0;
                double denom = 0, delta = 0;

                for (int i=0; i<nSK; i++) {

                    denom += Math.pow((w[i] - wA), 2);

                    for (int j=0; j<nSK; j++)
                        if (TopoMatrix[i][j] == lag) {
                            AC += w[i] * w[j];
                            ACS += Math.abs((w[i]-wA) * (w[j]-wA));
                            MoranAC += (w[i] - wA) * (w[j] - wA);
                            GearyAC += Math.pow((w[i] - w[j]), 2);
                            delta++;
                        }
                }

                if (delta > 0) {
                    if (denom == 0) {
                        MoranAC = 1;
                        GearyAC = 0;
                    } else {
                        MoranAC = ((1 / delta) * MoranAC) / ((1 / ((double)nSK)) * denom);
                        GearyAC = ((1 / (2 * delta)) * GearyAC) / ((1 / ((double)(nSK - 1))) * denom);
                    }
                }

                // AC transformed in log form
                AC /= 2.0;
                AC = Math.log(1 + AC);

                ACS /= 2.0;

                // Sets descriptors
                SetByName("ATS" + lag.toString() + WEIGHT_SYMBOL[curWeight], AC);
                SetByName("ATSC" + lag.toString() + WEIGHT_SYMBOL[curWeight], ACS);
                SetByName("MATS" + lag.toString() + WEIGHT_SYMBOL[curWeight], MoranAC);
                SetByName("GATS" + lag.toString() + WEIGHT_SYMBOL[curWeight], GearyAC);
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
        AutoCorrelationHFilledWithCorrectIState block = new AutoCorrelationHFilledWithCorrectIState();
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
        if (getBoolProperty(PARAMETER_WEIGHT_S))
            w.add((int) WEIGHT_S_IDX);
        return w;
    }


    private ArrayList<Integer> BuildLagList() {
        ArrayList<Integer> LagList = new ArrayList<>();
        if (getBoolProperty(PARAMETER_LAG_01)) LagList.add(1);
        if (getBoolProperty(PARAMETER_LAG_02)) LagList.add(2);
        if (getBoolProperty(PARAMETER_LAG_03)) LagList.add(3);
        if (getBoolProperty(PARAMETER_LAG_04)) LagList.add(4);
        if (getBoolProperty(PARAMETER_LAG_05)) LagList.add(5);
        if (getBoolProperty(PARAMETER_LAG_06)) LagList.add(6);
        if (getBoolProperty(PARAMETER_LAG_07)) LagList.add(7);
        if (getBoolProperty(PARAMETER_LAG_08)) LagList.add(8);
        return LagList;
    }
}
