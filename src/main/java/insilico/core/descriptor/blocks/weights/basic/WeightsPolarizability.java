package insilico.core.descriptor.blocks.weights.basic;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.blocks.weights.iBasicWeight;
import insilico.core.localization.StringSelectorCore;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Weighting scheme for Polarizability. Values are taken from: CRC Handbook of Chemistry and Physics by
 * D.R. Lide (editor), CRC press 2009-2010, 90th edition.<p>
 * NOTE: values are NOT scaled on the reference value (Carbon), many descriptor algorithms (from Dragon) are based on
 * values scaled on Carbon.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class WeightsPolarizability implements iBasicWeight {

    private static final String SYMBOL = "p";
    private static final String NAME = StringSelectorCore.getString("descriptors_polarizability_name");

    private final static Object[][] mass = {
        {"H", 0.67},
        {"He", 0.2},
        {"Li", 24.3},
        {"Be", 5.6},
        {"B", 3.03},
        {"C", 1.76},
        {"N", 1.1},
        {"O", 0.8},
        {"F", 0.56},
        {"Ne", 0.39},
        {"Na", 23.6},
        {"Mg", 10.6},
        {"Al", 6.8},
        {"Si", 5.38},
        {"P", 3.63},
        {"S", 2.9},
        {"Cl", 2.18},
        {"Ar", 1.64},
        {"K", 43.4},
        {"Ca", 22.8},
        {"Sc", 17.8},
        {"Ti", 14.6},
        {"V", 12.4},
        {"Cr", 11.6},
        {"Mn", 9.4},
        {"Fe", 8.4},
        {"Co", 7.5},
        {"Ni", 6.8},
        {"Cu", 6.1},
        {"Zn", 7.1},
        {"Ga", 8.12},
        {"Ge", 6.07},
        {"As", 4.31},
        {"Se", 3.77},
        {"Br", 3.05},
        {"Kr", 2.48},
        {"Rb", 47.3},
        {"Sr", 27.6},
        {"Y", 22.7},
        {"Zr", 17.9},
        {"Nb", 15.7},
        {"Mo", 12.8},
        {"Tc", 11.4},
        {"Ru", 9.6},
        {"Rh", 8.6},
        {"Pd", 4.8},
        {"Ag", 7.2},
        {"Cd", 7.2},
        {"In", 10.2},
        {"Sn", 7.7},
        {"Sb", 6.6},
        {"Te", 5.5},
        {"I", 5.35},
        {"Xe", 4.04},
        {"Cs", 59.6},
        {"Ba", 39.7},
        {"La", 31.1},
        {"Ce", 29.6},
        {"Pr", 28.2},
        {"Nd", 31.4},
        {"Pm", 30.1},
        {"Sm", 28.8},
        {"Eu", 27.7},
        {"Gd", 23.5},
        {"Tb", 25.5},
        {"Dy", 24.5},
        {"Ho", 23.6},
        {"Er", 22.7},
        {"Tm", 21.8},
        {"Yb", 21.00},
        {"Lu", 21.9},
        {"Hf", 16.2},
        {"Ta", 13.1},
        {"W", 11.1},
        {"Re", 9.7},
        {"Os", 8.5},
        {"Ir", 7.6},
        {"Pt", 6.5},
        {"Au", 5.8},
        {"Hg", 5.7},
        {"Tl", 7.6},
        {"Pb", 6.8},
        {"Bi", 7.4},
        {"Po", 6.8},
        {"At", 6.00},
        {"Rn", 5.3},
        {"Fr", 48.7},
        {"Ra", 38.3},
        {"Ac", 32.1},
        {"Th", 32.1},
        {"Pa", 25.4},
        {"U", 27.4},
        {"Np", 24.8},
        {"Pu", 24.5},
        {"Am", 23.3},
        {"Cm", 23.00},
        {"Bk", 22.7},
        {"Cf", 20.5},
        {"Es", 19.7},
        {"Fm", 23.8},
        {"Md", 18.2},
        {"No", 17.5}
    };


    /**
     * Calculate Polarizability for all atoms in the given molecule, and returns
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
     * Returns the Polarizability for the given atom type.
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

    /**
     * Return the weights for the molecule, scaled on Carbon.
     *
     * @param mol molecule to be processed
     * @return array of scaled weights
     */
    public double[] getScaledWeights(IAtomContainer mol) {
        double[] w = getWeights(mol);
        for (int i=0; i<w.length; i++)
            w[i] = w[i]  == Descriptor.MISSING_VALUE ? Descriptor.MISSING_VALUE :  w[i] / getWeight("C");
        return w;
    }


    /**
     * Return the weight of an atom type, scaled on Carbon.
     *
     * @param AtomType element symbol of the atom to be processed
     * @return scaled weight
     */
    public double getScaledWeight(String AtomType) {

        return getWeight(AtomType) == Descriptor.MISSING_VALUE ? Descriptor.MISSING_VALUE : ( getWeight(AtomType) / getWeight("C"));
    }

    /**
     * Return the name of the weighting scheme
     * @return name of the weighting scheme
     */
    @Override
    public String getName() {
        return NAME;
    }


    /**
     * Return the symbol of the weighting scheme
     * @return symbol of the weighting scheme
     */
    @Override
    public String getSymbol() {
        return SYMBOL;
    }
}
