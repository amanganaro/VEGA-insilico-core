package insilico.core.molecule.matrix;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.pro.weights.basic.*;
import insilico.core.descriptor.pro.weights.other.WeightsAtomicNumber;
import insilico.core.descriptor.weight.VanDerWaals;
import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import java.util.Arrays;
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
     * 0: Z (atomic number)
     * 1: m (mass)
     * 2: v (van der waals volume)
     * 3: e (sanderson electronegativity)
     * 4: p (polarizability)
     * 5: i (ionization potential)
     *
     * @param mol source CDK Molecule
     * @return Barysz Distance matrix
     */
    static public double[][][] getMatrix(IAtomContainer mol) {

        int nWeights = 6;
        int nSK = mol.getAtomCount();
        double[][][] matrix = new double[nSK][nSK][nWeights];

        for (int wLayer = 0; wLayer<nWeights; wLayer++) {

            // calculate current weighing scheme
            double[] w;

            switch (wLayer) {
                case 0:
                    // Z (atomic number)
                    int[] buf = WeightsAtomicNumber.getWeights(mol);
                    w = new double[buf.length];
                    for (int i=0; i<buf.length; i++)
                        w[i] = (double) buf[i] / 6.0;
                    break;
                case 1:
                    // m (mass)
                    w = (new WeightsMass()).getScaledWeights(mol);
                    break;
                case 2:
                    // v (van der waals volume)
                    w = (new WeightsVanDerWaals()).getScaledWeights(mol);
                    break;
                case 3:
                    // e (sanderson electronegativity)
                    w = (new WeightsElectronegativity()).getScaledWeights(mol);
                    break;
                case 4:
                    w = (new WeightsPolarizability()).getScaledWeights(mol);
                    // p (polarizability)
                    break;
                case 5:
                    // i (ionization potential)
                    w = (new WeightsIonizationPotential()).getScaledWeights(mol);
                    break;
                default:
                    w = new double[nSK];
                    for (double val : w) val = Descriptor.MISSING_VALUE;
            }

            // calculate matrix layer
            for (int i = 0; i < nSK; i++) {

                // Diagonal
                matrix[i][i][wLayer] = 1.0 - (1.0 / w[i]);

                IAtom atStart = mol.getAtom(i);

                for (int j = (i + 1); j < nSK; j++) {
                    IAtom atEnd = mol.getAtom(j);

                    ShortestPaths sp = new ShortestPaths(mol, atStart);
                    List<IAtom> shortestPath = Arrays.asList(sp.atomsTo(atEnd));

                    double w_sum = 0;
                    for (int a = 0; a < (shortestPath.size() - 1); a++) {
                        IAtom at1 = shortestPath.get(a);
                        IAtom at2 = shortestPath.get(a + 1);
                        IBond bnd = mol.getBond(at1, at2);

                        double bnd_order = MoleculeUtilities.Bond2Double(bnd);
                        double w1 = w[mol.indexOf(at1)];
                        double w2 = w[mol.indexOf(at2)];

                        if ((w1 == Descriptor.MISSING_VALUE) || (w2 == Descriptor.MISSING_VALUE)) {
                            w_sum = Descriptor.MISSING_VALUE;
                            break;
                        }

                        w_sum += (1.0 / bnd_order) * (1.0 / (w1 * w2));
                    }
                    matrix[i][j][wLayer] = w_sum;
                    matrix[j][i][wLayer] = matrix[i][j][wLayer];
                }
            }
        }

        return matrix;
    }

}
