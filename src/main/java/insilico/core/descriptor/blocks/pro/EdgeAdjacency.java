package insilico.core.descriptor.blocks.pro;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.utils.MoleculeUtilities;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Edge Adjacency descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class EdgeAdjacency extends DescriptorBlock {

    private final static long serialVersionUID = 1L;

    private final static String BlockName = "Edge Adjacency Descriptors";

    private final static String[][] WEIGHTS = {
            {"", ""}, // empty weight for plain EA matrix
            {"ed", "edge degree"},
            {"bo", "bond order"},
            {"dm", "dipole moment"},
            {"ri", "resonance integral"},
    };

    private final static String[][] MATRICES = {
            {"EA", "edge adjacency matrix"},
            {"AEA", "augmented edge adjacency matrix"},
    };

    

    /**
     * Constructor. This should not be used, no weight is specified. The
     * overloaded constructors should be used instead.
     */
    public EdgeAdjacency() {
        super();
        this.Name = EdgeAdjacency.BlockName;
    }



    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();

        EigenvalueBasedDescriptors eig = new EigenvalueBasedDescriptors();
        for (String[] curMat : MATRICES) {
            for (String[] curWeight : WEIGHTS) {

                if ( (curWeight[0].isEmpty()) && (curMat[0].equalsIgnoreCase("AEA")))
                    continue;

                String w_symbol = curWeight[0].isEmpty() ? "" : "(" + curWeight[0] + ")";
                String w_expl = curWeight[0].isEmpty() ? "" : " weighted by " + curWeight[1];

                for (String[] desc : eig.NAMES)
                    Add(desc[0] + curMat[0] + w_symbol, desc[1] + " from " + curMat[1] + w_expl);
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

        IAtomContainer curMol;
        try {
            curMol = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            log.warn("Invalid structure, unable to calculate: " + this.Name);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        int nSK = curMol.getAtomCount();
        int nBO = curMol.getBondCount();

        // Only for mol with nSK>1
        if (nSK < 2) {
            log.warn("Edge adjacency matrix can not be calculated for molecules with less than 2 atoms");
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets basic matrix
        double[][][] EdgeAdjMat = null;
        try {
            EdgeAdjMat = mol.GetMatrixEdgeAdjacency();
        } catch (GenericFailureException e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        for (String[] curMat : MATRICES)
            for (String[] curWeight : WEIGHTS) {

                double[][] curDataMatrix = new double[nBO][nBO];

                // AEA without weights does not exist
                if ( (curWeight[0].isEmpty()) && (curMat[0].equalsIgnoreCase("AEA")))
                    continue;

                // plain EA
                for (int i=0; i<nBO; i++)
                    for (int j=0; j<nBO; j++)
                        curDataMatrix[i][j] = EdgeAdjMat[i][j][0];

                // weights for the matrix
                double[] w = new double[nBO];

                //// Edge degree
                if (curWeight[0].equalsIgnoreCase("ed")) {
                    for (int i=0; i<nBO; i++) {
                        double deg = 0;
                        for (int j=0; j<nBO; j++) {
                            if (i==j) continue;
                            if (curDataMatrix[i][j] != 0)
                                deg++;
                        }
                        w[i] = deg;
                    }
                }

                //// Bond order
                if (curWeight[0].equalsIgnoreCase("bo")) {
                    for (int i=0; i<nBO; i++) {
                        w[i] = MoleculeUtilities.Bond2Double(curMol.getBond(i));
                    }
                }

                //// Dipole moment
                if (curWeight[0].equalsIgnoreCase("dm")) {
                    for (int i=0; i<nBO; i++) {
                        IAtom a =  curMol.getBond(i).getAtom(0);
                        IAtom b =  curMol.getBond(i).getAtom(1);
                        double CurVal = GetDipoleMoment(curMol, a, b);
                        if (CurVal == 0)
                            CurVal = GetDipoleMoment(curMol, b, a);
                        w[i] = CurVal;
                    }
                }

                //// Resonance integral
                if (curWeight[0].equalsIgnoreCase("ri")) {
                    for (int i=0; i<nBO; i++)
                        w[i] = GetResonanceIntegral(curMol.getBond(i));
                }

                // Build EA-based matrix
                // Replace elements with value 1 with the j-th bond weight
                if ( curMat[0].equalsIgnoreCase("EA")) {
                    if (!curWeight[0].isEmpty()) {
                        for (int i=0; i<nBO; i++) {
                            for (int j=0; j<nBO; j++) {
                                if (curDataMatrix[i][j] != 0)
                                    curDataMatrix[i][j] = w[j];
                            }
                        }
                    }
                }

                // Build AEA-based matrix
                // Replace just diagonal EA elements with value of the bond weight
                if ( curMat[0].equalsIgnoreCase("AEA")) {
                    for (int i=0; i<nBO; i++)
                        curDataMatrix[i][i] = w[i];
                }


                String MatSymbolForDesc = curMat[0] + (curWeight[0].isEmpty() ? "" : "(" + curWeight[0] + ")");

                // Calculate standard eigenvalue-based descriptors
                EigenvalueBasedDescriptors eigDesc = new EigenvalueBasedDescriptors();
                eigDesc.Calculate(curDataMatrix, nBO);
                for (String k : eigDesc.Descriptors.keySet())
                    SetByName(k + MatSymbolForDesc, eigDesc.Descriptors.get(k));

            }
    }



    /**
     * Inner class with the implementation of the set of eigenvalue-based descriptors, to be applied to all
     * calculated matrices.
     *
     */
    private class EigenvalueBasedDescriptors {

        public final String[][] NAMES = {
                {"SpMax_", "Leading eigenvalue"}, // 0
                {"SpMaxA_", "Normalized leading eigenvalue"}, // 1
                {"SpDiam_", "Spectral diameter"}, // 2
                {"SpAD_", "Spectral absolute deviation"}, // 3
                {"SpMAD_", "Spectral mean absolute deviation"}, // 4

                {"SM02_", "Spectral moment of order 2"}, // 5
                {"SM03_", "Spectral moment of order 3"}, // 6
                {"SM04_", "Spectral moment of order 4"}, // 7
                {"SM05_", "Spectral moment of order 5"}, // 8
                {"SM06_", "Spectral moment of order 6"}, // 9
                {"SM07_", "Spectral moment of order 7"}, // 10
                {"SM08_", "Spectral moment of order 8"}, // 11
                {"SM09_", "Spectral moment of order 9"}, // 12
                {"SM10_", "Spectral moment of order 10"}, // 13
                {"SM11_", "Spectral moment of order 11"}, // 14
                {"SM12_", "Spectral moment of order 12"}, // 15
                {"SM13_", "Spectral moment of order 13"}, // 16
                {"SM14_", "Spectral moment of order 14"}, // 17
                {"SM15_", "Spectral moment of order 15"}, // 18

                {"Eig01_", "eigenvalue n. 1"}, // 19
                {"Eig02_", "eigenvalue n. 2"}, // 20
                {"Eig03_", "eigenvalue n. 3"}, // 21
                {"Eig04_", "eigenvalue n. 4"}, // 22
                {"Eig05_", "eigenvalue n. 5"}, // 23
                {"Eig06_", "eigenvalue n. 6"}, // 24
                {"Eig07_", "eigenvalue n. 7"}, // 25
                {"Eig08_", "eigenvalue n. 8"}, // 26
                {"Eig09_", "eigenvalue n. 9"}, // 27
                {"Eig10_", "eigenvalue n. 10"}, // 28
                {"Eig11_", "eigenvalue n. 11"}, // 29
                {"Eig12_", "eigenvalue n. 12"}, // 30
                {"Eig13_", "eigenvalue n. 13"}, // 31
                {"Eig14_", "eigenvalue n. 14"}, // 32
                {"Eig15_", "eigenvalue n. 15"}, // 33
        };

        public HashMap<String, Double> Descriptors;


        public void Calculate(double[][] EigMat, int nBO) {

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
            double EigAve = 0;
            double EigMax = eigenvalues[0];
            double EigMin = eigenvalues[0];

            for (double val : eigenvalues) {
                EigAve += val;
                if (val > EigMax)
                    EigMax = val;
                if (val< EigMin)
                    EigMin = val;
            }
            EigAve = EigAve / (double) nBO;
            double NormEigMax = EigMax / (double) nBO;
            double EigDiam = EigMax - EigMin;
            double EigDev = 0;
            for (double val : eigenvalues)
                EigDev += Math.abs(val - EigAve);
            double NormEigDev = EigDev / (double) nBO;

            // Eigenvalue spectral moments
            double[] SpecMoments = new double[16];
            for (double d : SpecMoments) d = 0;
            for (double val : eigenvalues) {
                for (int expIdx=2; expIdx<16; expIdx++)
                    SpecMoments[expIdx] += Math.pow(val, expIdx);
            }
            SpecMoments[2] = Math.log(1 + SpecMoments[2]);
            SpecMoments[3] = Math.signum(SpecMoments[3]) * Math.log(1 + Math.abs(SpecMoments[3]));
            SpecMoments[4] = Math.log(1 + SpecMoments[4]);
            SpecMoments[5] = Math.signum(SpecMoments[5]) * Math.log(1 + Math.abs(SpecMoments[5]));
            SpecMoments[6] = Math.log(1 + SpecMoments[6]);
            SpecMoments[7] = Math.signum(SpecMoments[7]) * Math.log(1 + Math.abs(SpecMoments[7]));
            SpecMoments[8] = Math.log(1 + SpecMoments[8]);
            SpecMoments[9] = Math.signum(SpecMoments[9]) * Math.log(1 + Math.abs(SpecMoments[9]));
            SpecMoments[10] = Math.log(1 + SpecMoments[10]);
            SpecMoments[11] = Math.signum(SpecMoments[11]) * Math.log(1 + Math.abs(SpecMoments[11]));
            SpecMoments[12] = Math.log(1 + SpecMoments[12]);
            SpecMoments[13] = Math.signum(SpecMoments[13]) * Math.log(1 + Math.abs(SpecMoments[13]));
            SpecMoments[14] = Math.log(1 + SpecMoments[14]);
            SpecMoments[15] = Math.signum(SpecMoments[15]) * Math.log(1 + Math.abs(SpecMoments[15]));


            Descriptors.put(NAMES[0][0], EigMax);
            Descriptors.put(NAMES[1][0], NormEigMax);
            Descriptors.put(NAMES[2][0], EigDiam);
            Descriptors.put(NAMES[3][0], EigDev);
            Descriptors.put(NAMES[4][0], NormEigDev);

            for (int i=0; i<14; i++)
                Descriptors.put(NAMES[5 + i][0], SpecMoments[2 + i]);

            for (int i=0; i<15; i++) {
                int idx = eigenvalues.length - 1 - i;
                double eig = (idx >= 0) ? eigenvalues[idx] : 0;
                Descriptors.put(NAMES[19 + i][0], eig);
            }
        }
    }


    /**
     * Calculate resonance integral for the given bond
     *
     * @param bnd
     * @return
     */
    private double GetResonanceIntegral(IBond bnd) {

        IAtom atA = bnd.getAtom(0);
        IAtom atB = bnd.getAtom(1);
        String A = atA.getSymbol();
        String B = atB.getSymbol();

        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("C") == 0)) )
            return 1.00;
        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("B") == 0)) ||
             ((A.compareToIgnoreCase("B") == 0) && (B.compareToIgnoreCase("C") == 0)))
            return 0.7;
        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("N") == 0)) ||
             ((A.compareToIgnoreCase("N") == 0) && (B.compareToIgnoreCase("C") == 0)))
            return 0.9;
        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("O") == 0)) ||
             ((A.compareToIgnoreCase("O") == 0) && (B.compareToIgnoreCase("C") == 0))) {
            if (bnd.getOrder() == IBond.Order.SINGLE)
                return 0.8;
            if (bnd.getOrder() == IBond.Order.DOUBLE)
                return 1.2;
        }
        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("S") == 0)) ||
             ((A.compareToIgnoreCase("S") == 0) && (B.compareToIgnoreCase("C") == 0)))
            return 0.7;
        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("F") == 0)) ||
             ((A.compareToIgnoreCase("F") == 0) && (B.compareToIgnoreCase("C") == 0)))
            return 0.7;
        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("Cl") == 0)) ||
             ((A.compareToIgnoreCase("Cl") == 0) && (B.compareToIgnoreCase("C") == 0)))
            return 0.4;
        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("Br") == 0)) ||
             ((A.compareToIgnoreCase("Br") == 0) && (B.compareToIgnoreCase("C") == 0)))
            return 0.3;
        if ( ((A.compareToIgnoreCase("C") == 0) && (B.compareToIgnoreCase("I") == 0)) ||
             ((A.compareToIgnoreCase("I") == 0) && (B.compareToIgnoreCase("C") == 0)))
            return 0.1;

        return 0.00;
    }


    /**
     * Calculate the dipole moment for two bound atoms
     *
     * @param CurMol
     * @param at1
     * @param at2
     * @return
     */
    private double GetDipoleMoment(IAtomContainer CurMol, IAtom at1, IAtom at2) {

        String a = at1.getSymbol();
        String b = at2.getSymbol();

        // C - something
        if (a.equalsIgnoreCase("C")) {

            // C-F
            if (b.equalsIgnoreCase("F")) {
                return 1.51;
            }

            // C-Cl , C(Cl)-Cl , C(Cl)(Cl)-Cl
            if (b.equalsIgnoreCase("Cl")) {
                int nCl=0;
                for (IAtom at : CurMol.getConnectedAtomsList(at1)) {
                    if (at.getSymbol().equalsIgnoreCase("Cl"))
                        nCl++;
                }
                if (nCl==1)
                    return 1.56;
                if (nCl==2)
                    return 1.20;
                if (nCl==3)
                    return 0.83;
            }

            // C-Br
            if (b.equalsIgnoreCase("Br")) {
                return 1.48;
            }

            // C-I
            if (b.equalsIgnoreCase("I")) {
                return 1.29;
            }

            // C-N , C=N , C#N
            if (b.equalsIgnoreCase("N")) {
                if (CurMol.getBond(at1, at2).getFlag(CDKConstants.ISAROMATIC))
                    return 0;
                IBond.Order ord = CurMol.getBond(at1, at2).getOrder();
                if (ord == IBond.Order.SINGLE)
                    return 0.4;
                if (ord == IBond.Order.DOUBLE)
                    return 0.9;
                if (ord == IBond.Order.TRIPLE)
                    return 3.6;
            }

            // C-O , C=O
            if (b.equalsIgnoreCase("O")) {
                if (CurMol.getBond(at1, at2).getFlag(CDKConstants.ISAROMATIC))
                    return 0;
                IBond.Order ord = CurMol.getBond(at1, at2).getOrder();
                if (ord == IBond.Order.SINGLE)
                    return 0.86;
                if (ord == IBond.Order.DOUBLE)
                    return 2.4;
            }

            // C-S , C=S
            if (b.equalsIgnoreCase("S")) {
                if (CurMol.getBond(at1, at2).getFlag(CDKConstants.ISAROMATIC))
                    return 0;
                IBond.Order ord = CurMol.getBond(at1, at2).getOrder();
                if (ord == IBond.Order.SINGLE)
                    return 2.95;
                if (ord == IBond.Order.DOUBLE)
                    return 2.8;
            }

        }


        // N-O , N-[O-] , N=O
        if ((a.equalsIgnoreCase("N")) && (b.equalsIgnoreCase("O"))) {
            if (CurMol.getBond(at1, at2).getFlag(CDKConstants.ISAROMATIC))
                return 0;
            IBond.Order ord = CurMol.getBond(at1, at2).getOrder();
            int nH=0;
            try { nH = at2.getImplicitHydrogenCount(); } catch (Exception e) {}
            int nConn = CurMol.getConnectedBondsCount(at2) + nH;
            if ((ord == IBond.Order.SINGLE) && (nConn==2))
                return 3.2;
            if ((ord == IBond.Order.SINGLE) && (nConn==1))
                return 2.0;
//                return 0.3;
            if ((ord == IBond.Order.DOUBLE) && (nConn==1))
                return 2.0;
        }


        // S-[O-]
        if ((a.equalsIgnoreCase("S")) && (b.equalsIgnoreCase("O"))) {
            if (CurMol.getBond(at1, at2).getFlag(CDKConstants.ISAROMATIC))
                return 0;
            IBond.Order ord = CurMol.getBond(at1, at2).getOrder();
            int nConn = CurMol.getConnectedBondsCount(at2);
            if ((ord == IBond.Order.SINGLE) && (nConn==2))
                return 2.9;
        }


        // C(*)(*)-C(*)(*)(*) , C(*)(*)-C , CC(*)(*)(*)
        if ((a.equalsIgnoreCase("C")) && (b.equalsIgnoreCase("C"))) {

            if (CurMol.getBond(at1, at2).getFlag(CDKConstants.ISAROMATIC))
                return 0;

            int nH1=0, nH2=0;
            try {
                nH1 = at1.getImplicitHydrogenCount();
            } catch (Exception E) {}
            try {
                nH2 = at2.getImplicitHydrogenCount();
            } catch (Exception E) {}

            int nConn1 = CurMol.getConnectedBondsCount(at1) + nH1;
            int nConn2 = CurMol.getConnectedBondsCount(at2) + nH2;

            if ((nConn1==3) && (nConn2==4))
                return 0.68;
            if ((nConn1==3) && (nConn2==2))
                return 1.15;
            if ((nConn1==2) && (nConn2==4))
                return 1.48;
        }

        return 0;
    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        EdgeAdjacency block = new EdgeAdjacency();
        block.CloneDetailsFrom(this);
        return block;
    }

    
}
