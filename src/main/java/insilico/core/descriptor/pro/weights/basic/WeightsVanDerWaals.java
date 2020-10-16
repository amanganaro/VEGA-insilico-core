package insilico.core.descriptor.pro.weights.basic;

import insilico.core.descriptor.Descriptor;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Weighting scheme for van der Waals volume. Values are taken from: CRC Handbook of Chemistry and Physics by
 * D.R. Lide (editor), CRC press 2009-2010, 90th edition.<p>
 * NOTE: values are NOT scaled on the reference value (Carbon), many descriptor algorithms (from Dragon) are based on
 * values scaled on Carbon.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class WeightsVanDerWaals implements iBasicWeight {

    private final static Object[][] mass = {
        {"H", 5.42},
        {"He", 11.49},
        {"Li", 25.25},
        {"C", 20.58},
        {"N", 15.6},
        {"O", 14.71},
        {"F", 13.31},
        {"Ne", 15.3},
        {"Na", 49},
        {"Mg", 21.69},
        {"Si", 38.79},
        {"P", 24.43},
        {"S", 24.43},
        {"Cl", 22.45},
        {"Ar", 27.83},
        {"K", 87.11},
        {"Ni", 18.14},
        {"Cu", 11.49},
        {"Zn", 11.25},
        {"Ga", 27.39},
        {"As", 26.52},
        {"Se", 28.73},
        {"Br", 26.52},
        {"Kr", 34.53},
        {"Pd", 18.14},
        {"Ag", 21.31},
        {"Cd", 16.52},
        {"In", 30.11},
        {"Sn", 42.8},
        {"Te", 36.62},
        {"I", 32.52},
        {"Xe", 42.21},
        {"Pt", 21.31},
        {"Au", 19.16},
        {"Hg", 15.6},
        {"Tl", 31.54},
        {"Pb", 34.53},
        {"U", 26.95}
    };


    /**
     * Calculate van der Waals volume for all atoms in the given molecule, and returns
     * them in a single array.
     *
     * @param mol molecule to be processed
     * @return array of weights for all atoms
     */
    @Override
    public double[] getWeights(IAtomContainer mol)  {

        int nSK = mol.getAtomCount();
        double[] w = new double[nSK];

        for (int i=0; i<nSK; i++) {
            IAtom at = mol.getAtom(i);
            w[i] = getWeight(at.getSymbol());
        }

        return w;
    }


    /**
     * Returns the van der Waals volume for the given atom type.
     *
     * @param AtomType element symbol of the atom to be processed
     * @return the value of the weight
     */
    @Override
    public double getWeight(String AtomType) {

        try {
            for (Object[] o : mass) {
                if (((String) o[0]).equalsIgnoreCase(AtomType))
                    return (Double) o[1];
            }
        } catch (Throwable e) {
            return Descriptor.MISSING_VALUE;
        }

        return Descriptor.MISSING_VALUE;
    }

}
