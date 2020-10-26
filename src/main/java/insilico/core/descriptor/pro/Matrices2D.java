package insilico.core.descriptor.pro;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.pro.weights.basic.*;
import insilico.core.descriptor.pro.weights.iWeight;
import insilico.core.descriptor.pro.weights.other.WeightsAtomicNumber;
import insilico.core.descriptor.pro.weights.other.WeightsIState;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Matrices 2D molecular descriptors.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class Matrices2D extends DescriptorBlock {

    private final static long serialVersionUID = 1L;
    private final static String BlockName = "Matrices 2D";

    private final static String[][] MATRICES = {
            {"A", "adjacency matrix"},
            {"D", "topological distance matrix"},
            {"L", "laplace matrix"},
            {"H2", "reciprocal squared distance matrix"},
            {"D/Dt", "distance/detour matrix"},
            {"Dz", "Barysz matrix", "Z", "atomic number"},
            {"Dz", "Barysz matrix", "m", "mass"},
            {"Dz", "Barysz matrix", "v", "van der waals volume"},
            {"Dz", "Barysz matrix", "e", "sanderson electronegativity"},
            {"Dz", "Barysz matrix", "p", "polarizability"},
            {"Dz", "Barysz matrix", "i", "ionization potential"},
            {"B", "Burden matrix", "m", "mass"},
            {"B", "Burden matrix", "v", "van der waals volume"},
            {"B", "Burden matrix", "e", "sanderson electronegativity"},
            {"B", "Burden matrix", "p", "polarizability"},
            {"B", "Burden matrix", "i", "ionization potential"},
            // manca s
    };

    // matrici in dragon:
    // ** Adjacency
    // ** topo distance
    // ** laplace
    // chi
    // ** reciprocal squared distance
    // detour
    // ** distance / detour
    // ** barysz - Z, m, v, e, p, i
    // ** burden - m, v, e, p, i, s [MANCA s]


    /**
     * Constructor. This should not be used, no weight is specified. The
     * overloaded constructors should be used instead.
     */
    public Matrices2D() {
        super();
        this.Name = Matrices2D.BlockName;
    }

    @Override
    protected final void GenerateDescriptors() {

        DescList.clear();

        EigenvalueBasedDescriptors eig = new EigenvalueBasedDescriptors();
        MatrixBasedDescriptors mat = new MatrixBasedDescriptors();
        for (String[] curMat : MATRICES) {

            if (curMat.length > 2) {
                for (String[] desc : mat.NAMES)
                    Add(desc[0] + curMat[0] + "(" + curMat[2] + ")", desc[1] + " from " + curMat[1] + " weighted by " + curMat[3]);
                for (String[] desc : eig.NAMES)
                    Add(desc[0] + curMat[0] + "(" + curMat[2] + ")", desc[1] + " from " + curMat[1] + " weighted by " + curMat[3]);
            } else {
                for (String[] desc : mat.NAMES)
                    Add(desc[0] + curMat[0], desc[1] + " from " + curMat[1]);
                for (String[] desc : eig.NAMES)
                    Add(desc[0] + curMat[0], desc[1] + " from " + curMat[1]);
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

        try {

            // Generate/clears descriptors
            GenerateDescriptors();

            IAtomContainer curMol;
            try {
                curMol = mol.GetStructure();
            } catch (InvalidMoleculeException e) {
                log.warn("Invalid structure, unable to calculate: " + this.Name);
                SetAllValues(Descriptor.MISSING_VALUE);
                return;
            }

            int nSK = curMol.getAtomCount();

            for (String[] curMat : MATRICES) {

                // Gets current matrix
                String MatSymbol = curMat[0];
                String MatSymbolForDesc = curMat[0] + (curMat.length>2? ("(" + curMat[2] + ")") : "");
                double[][] Mat = new double[nSK][nSK];
                try {

                    if (MatSymbol.equalsIgnoreCase("A")) {
                        int[][] AdjMatrix = mol.GetMatrixAdjacency();
                        for (int i=0; i<nSK; i++)
                            for (int j=0; j<nSK; j++)
                                Mat[i][j] = AdjMatrix[i][j];
                    }

                    if (MatSymbol.equalsIgnoreCase("D")) {
                        int[][] TopoMatrix = mol.GetMatrixTopologicalDistance();
                        for (int i=0; i<nSK; i++)
                            for (int j=0; j<nSK; j++)
                                Mat[i][j] = TopoMatrix[i][j];
                    }

                    if (MatSymbol.equalsIgnoreCase("L")) {
                        int[][] LapMatrix = mol.GetMatrixLaplace();
                        for (int i=0; i<nSK; i++)
                            for (int j=0; j<nSK; j++)
                                Mat[i][j] = LapMatrix[i][j];
                    }

                    if (MatSymbol.equalsIgnoreCase("H2")) {
                        int[][] TopoMatrix = mol.GetMatrixTopologicalDistance();
                        for (int i=0; i<nSK; i++)
                            for (int j=0; j<nSK; j++)
                                Mat[i][j] = TopoMatrix[i][j] == 0 ? 0 : 1.0 / Math.pow(TopoMatrix[i][j], 2);
                    }

                    if (MatSymbol.equalsIgnoreCase("D/Dt")) {
                        Mat = mol.GetMatrixDistanceDetour();
                    }

                    if (MatSymbol.equalsIgnoreCase("Dz")) {
                        double[][][] BarMat = mol.GetMatrixBarysz();
                        int BarLayer = 0;
                        if (curMat[2].equalsIgnoreCase("Z")) BarLayer = 0;
                        if (curMat[2].equalsIgnoreCase("m")) BarLayer = 1;
                        if (curMat[2].equalsIgnoreCase("v")) BarLayer = 2;
                        if (curMat[2].equalsIgnoreCase("e")) BarLayer = 3;
                        if (curMat[2].equalsIgnoreCase("p")) BarLayer = 4;
                        if (curMat[2].equalsIgnoreCase("i")) BarLayer = 5;
                        for (int i=0; i<nSK; i++)
                            for (int j=0; j<nSK; j++)
                                Mat[i][j] = BarMat[i][j][BarLayer];
                    }

                    if (MatSymbol.equalsIgnoreCase("B")) {
                        Mat = mol.GetMatrixBurden();
                        double[] w = new double[nSK];
                        if (curMat[2].equalsIgnoreCase("m"))
                            w = (new WeightsMass()).getScaledWeights(curMol);
                        if (curMat[2].equalsIgnoreCase("v"))
                            w = (new WeightsVanDerWaals()).getScaledWeights(curMol);
                        if (curMat[2].equalsIgnoreCase("e"))
                            w = (new WeightsElectronegativity()).getScaledWeights(curMol);
                        if (curMat[2].equalsIgnoreCase("p"))
                            w = (new WeightsPolarizability()).getScaledWeights(curMol);
                        if (curMat[2].equalsIgnoreCase("i"))
                            w = (new WeightsIonizationPotential()).getScaledWeights(curMol);
                        // MANCA s
                        for (int i=0; i<nSK; i++)
                            Mat[i][i] = w[i];
                    }

                } catch (Exception e) {
                    log.warn(e.getMessage());
                    SetAllValues(Descriptor.MISSING_VALUE);
                    return;
                }


                // Calculate standard matrix-based descriptors
                MatrixBasedDescriptors matDesc = new MatrixBasedDescriptors();
                matDesc.Calculate(Mat, nSK);
                for (String k : matDesc.Descriptors.keySet())
                    SetByName(k + MatSymbolForDesc, matDesc.Descriptors.get(k));

                // Calculate standard eigenvalue-based descriptors
                EigenvalueBasedDescriptors eigDesc = new EigenvalueBasedDescriptors();
                eigDesc.Calculate(Mat, nSK);
                for (String k : eigDesc.Descriptors.keySet())
                    SetByName(k + MatSymbolForDesc, eigDesc.Descriptors.get(k));

            }

        } catch (Throwable e) {
            log.warn("Unable to calculate: " + this.Name + " - " + e.getMessage());
            this.SetAllValues(Descriptor.MISSING_VALUE);
        }

    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        Matrices2D block = new Matrices2D();
        block.CloneDetailsFrom(this);
        return block;
    }



    /**
     * Inner class with the implementation of the set of eigenvalue-based descriptors, to be applied to all
     * calculated matrices.
     *
     */
    private class EigenvalueBasedDescriptors {

        public final String[][] NAMES = {
                {"SpAbs_", "Graph energy (sum of absolute eigenvalues)"}, // 0
                {"SpPos_", "Spectral positive sum"}, // 1
                {"SpPosA_", "Normalized spectral positive sum"}, // 2
                {"SpPosLog_", "Logarithmic spectral positive sum"}, // 3
                {"SpMax_", "Leading eigenvalue"}, // 4
                {"SpMaxA_", "Normalized leading eigenvalue"}, // 5
                {"SpDiam_", "Spectral diameter"}, // 6
                {"SpAD_", "Spectral absolute deviation"}, // 7
                {"SpMAD_", "Spectral mean absolute deviation"}, // 8
                {"EE_", "Estrada-like index"}, // 9
                {"SM1_", "Spectral moment of order 1"}, // 10
                {"SM2_", "Spectral moment of order 2"}, // 11
                {"SM3_", "Spectral moment of order 3"}, // 12
                {"SM4_", "Spectral moment of order 4"}, // 13
                {"SM5_", "Spectral moment of order 5"}, // 14
                {"SM6_", "Spectral moment of order 6"}, // 15
        };

        public HashMap<String, Double> Descriptors;


        public void Calculate(double[][] EigMat, int nSK) {

            Descriptors = new HashMap<>();

            // Calculates eigenvalues
            Matrix DataMatrix = new Matrix(EigMat);
            double[] eigenvalues;

            try {
                EigenvalueDecomposition ed = new EigenvalueDecomposition(DataMatrix);
                eigenvalues = ed.getRealEigenvalues();
                Arrays.sort(eigenvalues);
            } catch (Throwable e) {
                log.warn("Unable to calculate eigenvalues - " + e.getMessage());
                return;
            }

            // Eigenvalue based descriptors
            double SpAbs = 0;
            double SpPos = 0;
            double EigAve = 0;
            double EigMax = eigenvalues[0];
            double EigMin = eigenvalues[0];
            double EigExpSum = 0;
            for (double val : eigenvalues) {
                EigAve += val;
                SpAbs += Math.abs(val);
                if (val > 0)
                    SpPos += val;
                if (val > EigMax)
                    EigMax = val;
                if (val< EigMin)
                    EigMin = val;
                EigExpSum += Math.exp(val);
            }
            EigAve = EigAve / (double) nSK;
            double SpPosA = SpPos / (double) nSK;
            double SpPosLog = (((double) nSK) / 10.0) * Math.log10(SpPos);
            double NormEigMax = EigMax / (double) nSK;
            double EigDiam = EigMax - EigMin;
            double EigDev = 0;
            for (double val : eigenvalues)
                EigDev += Math.abs(val - EigAve);
            double NormEigDev = EigDev / (double) nSK;
            double EstradaLike = Math.log(1 + EigExpSum);

            // Eigenvalue spectral moments
            double[] SpecMoments = new double[7];
            for (double d : SpecMoments) d = 0;
            for (double val : eigenvalues) {
                for (int expIdx=1; expIdx<7; expIdx++)
                    SpecMoments[expIdx] += Math.pow(val, expIdx);
            }
            SpecMoments[1] = Math.signum(SpecMoments[1]) * Math.log(1 + Math.abs(SpecMoments[1]));
            SpecMoments[2] = Math.log(1 + SpecMoments[2]);
            SpecMoments[3] = Math.signum(SpecMoments[3]) * Math.log(1 + Math.abs(SpecMoments[3]));
            SpecMoments[4] = Math.log(1 + SpecMoments[4]);
            SpecMoments[5] = Math.signum(SpecMoments[5]) * Math.log(1 + Math.abs(SpecMoments[5]));
            SpecMoments[6] = Math.log(1 + SpecMoments[6]);

            Descriptors.put(NAMES[0][0], SpAbs);
            Descriptors.put(NAMES[1][0], SpPos);
            Descriptors.put(NAMES[2][0], SpPosA);
            Descriptors.put(NAMES[3][0], SpPosLog);
            Descriptors.put(NAMES[4][0], EigMax);
            Descriptors.put(NAMES[5][0], NormEigMax);
            Descriptors.put(NAMES[6][0], EigDiam);
            Descriptors.put(NAMES[7][0], EigDev);
            Descriptors.put(NAMES[8][0], NormEigDev);
            Descriptors.put(NAMES[9][0], EstradaLike);
            Descriptors.put(NAMES[10][0], SpecMoments[1]);
            Descriptors.put(NAMES[11][0], SpecMoments[2]);
            Descriptors.put(NAMES[12][0], SpecMoments[3]);
            Descriptors.put(NAMES[13][0], SpecMoments[4]);
            Descriptors.put(NAMES[14][0], SpecMoments[5]);
            Descriptors.put(NAMES[15][0], SpecMoments[6]);
        }

    }


    /**
     * Inner class with the implementation of the set of matrix-based descriptors, to be applied to all
     * calculated matrices.
     *
     */
    private class MatrixBasedDescriptors {

        public final String[][] NAMES = {
                {"Wi_", "Wiener-like index"}, // 0
                {"WiA_", "Average Wiener-like index"}, // 1
                {"AVS_", "Average vertex sum"}, // 2
                {"H_", "Harary-like index"}, // 3
        };

        public HashMap<String, Double> Descriptors;


        public void Calculate(double[][] Mat, int nSK) {

            Descriptors = new HashMap<>();

            double Wi = 0;
            for (int i=0; i<nSK; i++)
                Wi += Mat[i][i];
            double Harary = 0;
            for (int i=0; i<(nSK-1); i++)
                for (int j=(i+1); j<nSK; j++) {
                    Wi += Mat[i][j];
                    Harary += 1.0 / Mat[i][j];
                }
            double WiAve = (2.0 * Wi) / (double) (nSK * (nSK + 1)); //// DA VEDERE

            double AVS = 0;
            for (int i=0; i<nSK; i++)
                for (int j=0; j<nSK; j++)
                    AVS += Mat[i][j];
            AVS = AVS / (double) nSK;

            Descriptors.put(NAMES[0][0], Wi);
            Descriptors.put(NAMES[1][0], WiAve);
            Descriptors.put(NAMES[2][0], AVS);
            Descriptors.put(NAMES[3][0], Harary);
        }

    }


}
