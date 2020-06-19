package insilico.core.molecule.matrix;

import insilico.core.descriptor.weight.VertexDegree;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Calculates Laplace matrix.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class LaplaceMatrix {

    /**
     * Calculates Laplace matrix.<p>
     * Laplace matrix (L) is obtained by the difference between a diagonal
     * vertex degree matrix and the adjacency matrix; therefore, its diagonal
     * entries are the vertex degrees of molecule atoms and off-diagonal
     * entries corresponding to pairs of bonded atoms are set at –1,
     * otherwise at 0.
     *
     * @param molecule source CDK Molecule
     * @return adjacency matrix
     */
    static public int[][] getMatrix(IAtomContainer molecule) {

        // adjacency matrix with connectec atoms set to -1
        int[][] matrix = org.openscience.cdk.graph.matrix.AdjacencyMatrix.getMatrix(molecule);
        for (int i=0; i<matrix.length; i++)
            for (int j=0; j<matrix[0].length; j++)
                matrix[i][j] = -matrix[i][j];

        // diagonal with vertex degree
        int[] VD = VertexDegree.getWeights(molecule, true);
        for (int i=0; i<matrix.length; i++) {
            matrix[i][i] = VD[i];
        }

        return matrix;
    }
}
