package insilico.core.molecule.matrix;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.weight.VanDerWaals;
import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import java.util.List;

/**
 * Calculates Barysz matrix.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class BaryszMatrix {

    /**
     * Calculates Barysz matrix on different weights.<p>
     * Weight indices:
     * 0: v (van der waals volume)
     *
     * @param mol source CDK Molecule
     * @return Barysz Distance matrix
     */
    static public double[][][] getMatrix(IAtomContainer mol) {

        int weigths = 1;
        int nSK = mol.getAtomCount();
        double[][][] matrix = new double[nSK][nSK][weigths];

        // van der waals volumes (already scaled on carbon)
        double[] w_v = VanDerWaals.getWeights(mol);

        for (int i=0; i<nSK; i++) {

            // Diagonal
            matrix[i][i][0] = 1.0 - w_v[i];

            Atom atStart = (Atom) mol.getAtom(i);

            for (int j=(i+1); j<nSK; j++) {
                Atom atEnd = (Atom) mol.getAtom(j);
                List<IAtom> shortestPath = PathTools.getShortestPath(mol, atStart, atEnd);

                // van der waals volumes
                double w_sum = 0;
                for (int a=0; a<(shortestPath.size()-1); a++) {
                    IAtom at1 = shortestPath.get(a);
                    IAtom at2 = shortestPath.get(a+1);
                    IBond bnd = mol.getBond(at1, at2);

                    double bnd_order = MoleculeUtilities.Bond2Double(bnd);
                    double w1 = VanDerWaals.GetVdWVolume(at1.getSymbol());
                    double w2 = VanDerWaals.GetVdWVolume(at2.getSymbol());

                    if ( (w1 == Descriptor.MISSING_VALUE) || (w2 == Descriptor.MISSING_VALUE) ) {
                        w_sum = Descriptor.MISSING_VALUE;
                        break;
                    }

                    w_sum += (1.0 / bnd_order) * ( 1 / (w1 * w2) );
                }
                matrix[i][j][0] = w_sum;
                matrix[j][i][0] = matrix[i][j][0];
            }
        }

        return matrix;
    }

}
