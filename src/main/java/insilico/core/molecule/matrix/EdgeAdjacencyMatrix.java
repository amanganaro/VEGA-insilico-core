package insilico.core.molecule.matrix;

import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import java.util.List;

/**
 * Calculates Edge Adjacency and Edge Degree matrices.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class EdgeAdjacencyMatrix {
    /**
     * Calculates Edge Adjacency and Edge Degree matrices.<p>
     * The returning matrix has three dimensions, where the last one has only
     * two indices (layers). The first layer [][][0] contains the edge
     * adjacency matrix. The second layer [][][1] contains the edge degree
     * matrix. NOTE: the calculation of the matrix is consistent with
     * Dragon 5.5 even if it has an error (the Bond Order is used as weight
     * instead of the true Edge Degree)
     *
     * @param molecule source CDK Molecule
     * @return edge adjacency and degree matrix
     */
    static public double[][][] getMatrix(IAtomContainer molecule) {

        // matrix first layer [][][0] is the edge adjacency matrix
        // matrix second layer [][][1] is the edge degree matrix

        // NOTE: as there is an error in DRAGON, matrix is built in order
        // to be consistent with its results; Bond Order is used as weight
        // instead of the true Edge Degree

        int nBO = molecule.getBondCount();
        double[][][] matrix = new double[nBO][nBO][2];
        double[] wEdgeDegree = new double[nBO];

        for (int i=0; i<nBO; i++)
            for (int j=0; j<nBO; j++) {
                matrix[i][j][0] = 0;
                matrix[i][j][1] = 0;
            }

        for (int i=0; i<nBO; i++) {
            wEdgeDegree[i] = 0;
            IBond b = molecule.getBond(i);
            wEdgeDegree[i] = MoleculeUtilities.Bond2Double(b);
            for (int k=0; k<b.getAtomCount(); k++) {
                IAtom at = b.getAtom(k);
                List<IBond> connBonds = molecule.getConnectedBondsList(at);
                for (int z=0; z<molecule.getConnectedBondsCount(at); z++) {
                    IBond connBond = connBonds.get(z);
                    int BondNum = molecule.getBondNumber(connBond);
                    if (BondNum != i) {
                        matrix[i][BondNum][0] = 1;
                        matrix[BondNum][i][0] = 1;
                        matrix[i][BondNum][1] = 1;
                        matrix[BondNum][i][1] = 1;
                    }
                }

            }
        }

        for (int i=0; i<nBO; i++)
            matrix[i][i][1] = wEdgeDegree[i];

        return matrix;

    }
}
