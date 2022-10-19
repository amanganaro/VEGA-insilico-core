package insilico.core.descriptor.blocks.weights.basic;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.blocks.weights.iBasicWeight;
import insilico.core.localization.StringSelectorCore;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Weighting scheme for mass. Values are taken from: CRC Handbook of Chemistry and Physics by D.R. Lide (editor),
 * CRC press 2009-2010, 90th edition.<p>
 * NOTE: values are NOT scaled on the reference value (Carbon), many descriptor algorithms (from Dragon) are based on
 * values scaled on Carbon.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class WeightsMass implements iBasicWeight {

    private static final String SYMBOL = "m";
    private static final String NAME = StringSelectorCore.getString("descriptors_mass_name");

    private final static Object[][] mass = {
        {"H", 1.01},
        {"He", 4.003},
        {"Li", 6.941},
        {"Be", 9.012},
        {"B", 10.81},
        {"C", 12.01},
        {"N", 14.01},
        {"O", 16.00},
        {"F", 19.00},
        {"Ne", 20.18},
        {"Na", 22.991},
        {"Mg", 24.305},
        {"Al", 26.98},
        {"Si", 28.09},
        {"P", 30.97},
        {"S", 32.07},
        {"Cl", 35.45},
        {"Ar", 39.948},
        {"K", 39.098},
        {"Ca", 40.078},
        {"Sc", 44.956},
        {"Ti", 47.867},
        {"V", 50.942},
        {"Cr", 52.00},
        {"Mn", 54.94},
        {"Fe", 55.85},
        {"Co", 58.93},
        {"Ni", 58.69},
        {"Cu", 63.55},
        {"Zn", 65.39},
        {"Ga", 69.72},
        {"Ge", 72.61},
        {"As", 74.92},
        {"Se", 78.96},
        {"Br", 79.9},
        {"Kr", 83.8},
        {"Rb", 85.468},
        {"Sr", 87.62},
        {"Y", 88.906},
        {"Zr", 91.224},
        {"Nb", 92.906},
        {"Mo", 95.94},
        {"Tc", 98.00},
        {"Ru", 101.07},
        {"Rh", 102.906},
        {"Pd", 106.42},
        {"Ag", 107.87},
        {"Cd", 112.41},
        {"In", 114.82},
        {"Sn", 118.71},
        {"Sb", 121.76},
        {"Te", 127.6},
        {"I", 126.9},
        {"Xe", 131.29},
        {"Cs", 132.905},
        {"Ba", 137.327},
        {"La", 138.906},
        {"Ce", 140.116},
        {"Pr", 140.908},
        {"Nd", 144.24},
        {"Pm", 145.00},
        {"Sm", 150.36},
        {"Eu", 151.964},
        {"Gd", 157.25},
        {"Tb", 158.925},
        {"Dy", 162.5},
        {"Ho", 164.93},
        {"Er", 167.26},
        {"Tm", 168.934},
        {"Yb", 173.04},
        {"Lu", 174.967},
        {"Hf", 178.49},
        {"Ta", 180.948},
        {"W", 183.84},
        {"Re", 186.207},
        {"Os", 190.23},
        {"Ir", 192.217},
        {"Pt", 195.08},
        {"Au", 196.97},
        {"Hg", 200.59},
        {"Tl", 204.38},
        {"Pb", 207.2},
        {"Bi", 208.98},
        {"Po", 210.00},
        {"At", 210.00},
        {"Rn", 222.00},
        {"Fr", 223.00},
        {"Ra", 226.00},
        {"Ac", 227.00},
        {"Th", 232.038},
        {"Pa", 231.036},
        {"U", 238.029},
        {"Np", 237.00},
        {"Pu", 244.00},
        {"Am", 243.00},
        {"Cm", 247.00},
        {"Bk", 247.00},
        {"Cf", 251.00},
        {"Es", 252.00},
        {"Fm", 257.00},
        {"Md", 258.00},
        {"No", 259.00},
        {"Lr", 262.00},
        {"Rf", 261.00},
        {"Db", 262.00},
        {"Sg", 266.00},
        {"Bh", 264.00},
        {"Hs", 269.00},
        {"Mt", 268.00},
        {"Ds", 271.00}
    };


    /**
     * Calculate mass weights for all atoms in the given molecule, and returns
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
     * Returns the mass weight for the given atom type.
     *
     * @param AtomType element symbol of the atom to be processed
     * @return the value of the weight
     */
    @Override
    public double getWeight(String AtomType) {

        try {
            for (Object[] o : mass) {
                if (((String) o[0]).equalsIgnoreCase(AtomType))
                    return (double) o[1];
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
