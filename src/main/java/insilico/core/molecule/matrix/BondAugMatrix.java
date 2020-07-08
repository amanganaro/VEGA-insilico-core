package insilico.core.molecule.matrix;

import java.util.List;

import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 * Calculates augmented Bonds matrix.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class BondAugMatrix {

    /**
     * Calculates augmented Bonds matrix.
     * Each element on the diagonal has the value of the order for the i-th bond
     * Each element (i,j) outside the diagonal has the value of 1 if bonds are
     * adjacentj, or 0 if they are not adjacent.
     * Aromatic bond is coded as 1.5
     *
     * @param mol
     * @return
     */
    static public double[][] getMatrix(IAtomContainer mol) {

        int nBO = mol.getBondCount();
        double[][] matrix = new double[nBO][nBO];

        // Sets all to zero and set diagonal values
        for (int i=0; i<nBO; i++) {
            for (int j=0; j<nBO; j++) {

                if (i==j)
                    matrix[i][j] = MoleculeUtilities.Bond2Double(mol.getBond(i));
                else
                    matrix[i][j] = 0;

            }
        }

        // Sets bonds adjacency
        for (int i=0; i<nBO; i++) {

            IBond b = mol.getBond(i);

            List<IBond> connBonds = mol.getConnectedBondsList(b.getAtom(0));
            SetAdjaceny(mol, matrix, i, connBonds);

            connBonds = mol.getConnectedBondsList(b.getAtom(1));
            SetAdjaceny(mol, matrix, i, connBonds);

        }

        return matrix;
    }

    /**
     * Set Adjaceny for each AdjacenyMatrix element
     * @param mol molecule object
     * @param matrix adjaceny matrix to fill
     * @param i index
     * @param connBonds connected bonds
     */
    private static void SetAdjaceny(IAtomContainer mol, double[][] matrix, int i, List<IBond> connBonds) {
        for (IBond connBond : connBonds) {
            int BondNum = mol.indexOf(connBond);
            if (BondNum != i) {
                matrix[i][BondNum] = 1;
                matrix[BondNum][i] = 1;
            }
        }
    }

}

