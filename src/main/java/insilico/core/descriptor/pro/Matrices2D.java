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
    // reciprocal squared distance
    // detour
    // distance / detour
    // ** barysz - Z, m, v, e, p, i
    // **burden - m, v, e, p, i, s [MANCA s]


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
        for (String[] curMat : MATRICES) {

            if (curMat.length > 2) {
                for (String[] desc : eig.NAMES)
                    Add(desc[0] + curMat[0] + "(" + curMat[2] + ")", desc[1] + " from " + curMat[1] + " weighted by " + curMat[3]);
            } else {
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
                double[][] eigMat = new double[nSK][nSK];
                try {

                    if (MatSymbol.equalsIgnoreCase("A")) {
                        int[][] AdjMatrix = mol.GetMatrixAdjacency();
                        for (int i=0; i<nSK; i++)
                            for (int j=0; j<nSK; j++)
                                eigMat[i][j] = AdjMatrix[i][j];
                    }

                    if (MatSymbol.equalsIgnoreCase("D")) {
                        int[][] TopoMatrix = mol.GetMatrixTopologicalDistance();
                        for (int i=0; i<nSK; i++)
                            for (int j=0; j<nSK; j++)
                                eigMat[i][j] = TopoMatrix[i][j];
                    }

                    if (MatSymbol.equalsIgnoreCase("L")) {
                        int[][] LapMatrix = mol.GetMatrixLaplace();
                        for (int i=0; i<nSK; i++)
                            for (int j=0; j<nSK; j++)
                                eigMat[i][j] = LapMatrix[i][j];
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
                                eigMat[i][j] = BarMat[i][j][BarLayer];
                    }

                    if (MatSymbol.equalsIgnoreCase("B")) {
                        eigMat = mol.GetMatrixBurden();
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
                            eigMat[i][i] = w[i];
                    }

                } catch (Exception e) {
                    log.warn(e.getMessage());
                    SetAllValues(Descriptor.MISSING_VALUE);
                    return;
                }

                // Calculate standard eigenvalue-based descriptors
                EigenvalueBasedDescriptors eig = new EigenvalueBasedDescriptors();
                eig.Calculate(eigMat, nSK);

                for (String k : eig.Descriptors.keySet())
                    SetByName(k + MatSymbolForDesc, eig.Descriptors.get(k));

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
//                {"", ""}, // 9
//                {"", ""}, // 10
//                {"", ""}, // 11
//                {"", ""}, // 12
//                {"", ""}, // 13
//                {"", ""}, // 14
//                {"", ""}, // 15
//                {"", ""}, // 16
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

            double SpAbs = 0;
            double SpPos = 0;
            double EigAve = 0;
            double EigMax = eigenvalues[0];
            double EigMin = eigenvalues[0];
            for (double val : eigenvalues) {
                EigAve += val;
                SpAbs += Math.abs(val);
                if (val > 0)
                    SpPos += val;
                if (val > EigMax) EigMax = val;
                if (val< EigMin) EigMin = val;
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


            Descriptors.put(NAMES[0][0], SpAbs);
            Descriptors.put(NAMES[1][0], SpPos);
            Descriptors.put(NAMES[2][0], SpPosA);
            Descriptors.put(NAMES[3][0], SpPosLog);
            Descriptors.put(NAMES[4][0], EigMax);
            Descriptors.put(NAMES[5][0], NormEigMax);
            Descriptors.put(NAMES[6][0], EigDiam);
            Descriptors.put(NAMES[7][0], EigDev);
            Descriptors.put(NAMES[8][0], NormEigDev);
        }

    }

}
