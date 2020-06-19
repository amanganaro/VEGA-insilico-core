package insilico.core.molecule.matrix;

import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Calculates Adjacency matrix.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class AdjacencyMatrix {

    /**
     * Calculates Adjacency matrix.<p>
     * Each element (i,j) outside the diagonal is 1 if atoms i and j are
     * connected, 0 otherwise. This class is a plain wrapper for CDK
     * adjacency matrix.
     *
     * @param mol source CDK Molecule
     * @return adjacency matrix
     */
    static public int[][] getMatrix(IAtomContainer mol) {
        return org.openscience.cdk.graph.matrix.AdjacencyMatrix.getMatrix(mol);
    }


}
