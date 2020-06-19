package insilico.core.molecule.matrix;

import insilico.core.exception.GenericFailureException;
import insilico.core.molecule.tools.Manipulator;
import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.List;

/**
 * Calculates Topological Distance matrix.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class TopDistanceMatrixHFilled {


    /**
     * Calculates Topological Distance matrix (on H filled molecule).<p>
     * Each element (i,j) contains the shortes path from atom i to atom j.
     *
     * @param molecule source CDK Molecule
     * @return Topological Distance matrix
     */
    static public int[][] getMatrix(IAtomContainer molecule) {

        IAtomContainer molH;
        try {
            molH = Manipulator.AddHydrogens(molecule);
        } catch (GenericFailureException e) {
            return null;
        }

        int nSK = molH.getAtomCount();
        int[][] matrix = new int[nSK][nSK];

        for (int i=0; i<nSK; i++) {
            matrix[i][i] = 0;  // diagonal
            Atom atStart = (Atom) molH.getAtom(i);

            for (int j=(i+1); j<nSK; j++) {
                Atom atEnd = (Atom) molH.getAtom(j);
                List<IAtom> shortestPaths =
                        PathTools.getShortestPath(molH, atStart, atEnd);
                matrix[i][j] = shortestPaths.size()-1;
                matrix[j][i] = matrix[i][j];
            }
        }

        return matrix;
    }

}
