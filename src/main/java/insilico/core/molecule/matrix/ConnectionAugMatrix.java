package insilico.core.molecule.matrix;

import insilico.core.molecule.tools.AtomicNumber;
import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 * Calculates augmented Connection matrix.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ConnectionAugMatrix {

    /**
     * Calculates augmented Connection matrix.<p>
     * Each element on the diagonal has the value of Z for the i-th atom.
     * Each element (i,j) outside the diagonal has the value of the bond order
     * between i and j, or 0 if they are not adjacent.
     * Aromatic bond is coded as 1.5. If some Z values are not available, the
     * diagonal cell value is set to -999.
     *
     * @param molecule source CDK Molecule
     * @return augmented connection matrix
     */
    static public double[][] getMatrix(IAtomContainer molecule) {

        int nSK = molecule.getAtomCount();
        double[][] matrix = new double[nSK][nSK];

        int[][] AdjMat = AdjacencyMatrix.getMatrix(molecule);

        for (int i=0; i<(nSK-1); i++) {

            for (int j=(i+1); j<nSK; j++) {

                double CellVal = 0;

                if (AdjMat[i][j]!=0) {
                    IBond b = molecule.getBond(molecule.getAtom(i), molecule.getAtom(j));
                    CellVal = MoleculeUtilities.Bond2Double(b);
                }

                matrix[i][j] = CellVal;
                matrix[j][i] = CellVal;
            }
        }

        // Diagonal values
        try {
            AtomicNumber anf = new AtomicNumber();
            for (int i=0; i<nSK; i++)
                matrix[i][i] = anf.GetAtomicNumber(molecule.getAtom(i).getSymbol());
        } catch (Exception e) {
            for (int i=0; i<nSK; i++)
                matrix[i][i] = -999;
        }

        return matrix;
    }
}
