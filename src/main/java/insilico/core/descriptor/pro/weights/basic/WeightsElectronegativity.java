package insilico.core.descriptor.pro.weights.basic;

import insilico.core.descriptor.Descriptor;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Weighting scheme for Sanderson Electronegativity. Values are taken from: CRC Handbook of Chemistry and Physics by
 * D.R. Lide (editor), CRC press 2009-2010, 90th edition.<p>
 * NOTE: values are NOT scaled on the reference value (Carbon), many descriptor algorithms (from Dragon) are based on
 * values scaled on Carbon.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class WeightsElectronegativity implements iBasicWeight {

    private final static Object[][] mass = {
        {"H", 2.59},
        {"Li", 0.89},
        {"Be", 1.81},
        {"B", 2.28},
        {"C", 2.75},
        {"N", 3.19},
        {"O", 3.65},
        {"F", 4},
        {"Ne", 4.5},
        {"Na", 0.56},
        {"Mg", 1.32},
        {"Al", 1.71},
        {"Si", 2.14},
        {"P", 2.52},
        {"S", 2.96},
        {"Cl", 3.48},
        {"Ar", 3.31},
        {"K", 0.45},
        {"Ca", 0.95},
        {"Sc", 1.02},
        {"Ti", 1.09},
        {"V", 1.39},
        {"Cr", 1.66},
        {"Mn", 2.2},
        {"Fe", 2.2},
        {"Co", 2.56},
        {"Ni", 1.94},
        {"Cu", 1.98},
        {"Zn", 2.23},
        {"Ga", 2.42},
        {"Ge", 2.62},
        {"As", 2.82},
        {"Se", 3.01},
        {"Br", 3.22},
        {"Kr", 2.91},
        {"Rb", 0.31},
        {"Sr", 0.72},
        {"Y", 0.65},
        {"Zr", 0.9},
        {"Nb", 1.42},
        {"Mo", 1.15},
        {"Ag", 1.83},
        {"Cd", 1.98},
        {"In", 2.14},
        {"Sn", 2.3},
        {"Sb", 2.46},
        {"Te", 2.62},
        {"I", 2.78},
        {"Xe", 2.34},
        {"Cs", 0.22},
        {"Ba", 0.68},
        {"W", 0.98},
        {"Hg", 2.2},
        {"Tl", 2.25},
        {"Pb", 2.29},
        {"Bi", 2.34}
    };


    /**
     * Calculate Sanderson Electronegativity for all atoms in the given molecule, and returns
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
     * Returns the Sanderson Electronegativity for the given atom type.
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
