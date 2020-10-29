package insilico.core.descriptor.blocks.weights.other;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.blocks.weights.iWeight;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Weighting scheme for atomic number.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class WeightsAtomicNumber implements iWeight {

    private static final String SYMBOL = "Z";
    private static final String NAME = "atomic number";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    /**
     * Retrieve atomic number for all atoms in the given molecule,
     * and returns them in a single array.
     *
     * @param molecule molecule to be processed
     * @return array of weights for all molecule's atoms
     */
    public static int[] getWeights(IAtomContainer molecule) {

        int nSK = molecule.getAtomCount();
        int[] w = new int[nSK];

        for (int i=0; i<nSK; i++) {
            IAtom at = molecule.getAtom(i);
            try {
                w[i] = at.getAtomicNumber();
            } catch (Throwable e) {
                w[i] = Descriptor.MISSING_VALUE;
            }
        }

        return w;
    }
}
