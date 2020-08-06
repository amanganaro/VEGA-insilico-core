package insilico.core.molecule.matrix;

import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.List;

/**
 * Calculates Distance/Detour matrix.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DistanceDetourMatrix {

    /**
     * Calculates Distance/Detour matrix.<p>
     * Each elements (i,j) contains the ratio between the shortest and the
     * longest path from i to j.
     *
     * @param molecule source CDK Molecule
     * @return Distance/Detour matrix
     */
    static public double[][] getMatrix(IAtomContainer molecule) {

        int nSK = molecule.getAtomCount();
        double[][] matrix = new double[nSK][nSK];

        for (int i=0; i<nSK; i++) {
            matrix[i][i] = 0;  // diagonal
            IAtom atStart =  molecule.getAtom(i);

            for (int j=(i+1); j<nSK; j++) {
                IAtom atEnd =  molecule.getAtom(j);
                List<List<IAtom>> allPaths =
                        PathTools.getAllPaths(molecule, atStart, atEnd);
                int min=99999, max=0;
                for (List<IAtom> allPath : allPaths) {
                    int curVal = allPath.size();
                    if (curVal < min)
                        min = curVal;
                    if (curVal > max)
                        max = curVal;
                }
                matrix[i][j] = ((double) (min-1)) / ((double) (max-1));
                matrix[j][i] = matrix[i][j];
            }
        }

        return matrix;
    }

}
