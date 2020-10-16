package insilico.core.descriptor.pro.weights.other;

import insilico.core.molecule.tools.Manipulator;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Weighting scheme for vertex degree.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class WeightsVertexDegree {
    /**
     * Calculate vertex degree for all atoms in the given molecule,
     * and returns them in a single array.
     *
     * @param molecule molecule to be processed
     * @param CountHydrogens true if implicit hydrogen atoms should be counted in the degree
     * @return array of weights for all molecule's atoms
     */
    public static int[] getWeights(IAtomContainer molecule, boolean CountHydrogens) {

        int nSK = molecule.getAtomCount();
        int[] w = new int[nSK];

        for (int i=0; i<nSK; i++) {
            IAtom at = molecule.getAtom(i);
            w[i] = molecule.getConnectedBondsCount(at);
            if (CountHydrogens)
                w[i] += Manipulator.CountImplicitHydrogens(at);
        }

        return w;
    }
}
