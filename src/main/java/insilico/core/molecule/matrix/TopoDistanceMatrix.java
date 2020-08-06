package insilico.core.molecule.matrix;

import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.Arrays;
import java.util.List;

/**
 * Calculates Topological Distance matrix.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class TopoDistanceMatrix {


    /**
     * Calculates Topological Distance matrix.<p>
     * Each element (i,j) contains the shortes path from atom i to atom j.
     *
     * @param molecule source CDK Molecule
     * @return Topological Distance matrix
     */
    static public int[][] getMatrix(IAtomContainer molecule) {

        int nSK = molecule.getAtomCount();
        int[][] matrix = new int[nSK][nSK];

        for (int i=0; i<nSK; i++) {
            matrix[i][i] = 0;  // diagonal
            IAtom atStart = molecule.getAtom(i);

            for (int j=(i+1); j<nSK; j++) {
                IAtom atEnd = molecule.getAtom(j);

                ShortestPaths sp = new ShortestPaths(molecule, atStart);
                List<IAtom> shortestPaths = Arrays.asList(sp.atomsTo(atEnd));

                // DEPRECATED METHOD
//                List<IAtom> shortestPaths = PathTools.getShortestPath(molecule, atStart, atEnd);

                matrix[i][j] = shortestPaths.size()-1;
                matrix[j][i] = matrix[i][j];
            }
        }

        return matrix;
    }

}
