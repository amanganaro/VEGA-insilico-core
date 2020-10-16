package insilico.core.descriptor.pro.weights.basic;

import insilico.core.descriptor.Descriptor;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Weighting scheme for Ionization Potential. Values are taken from: CRC Handbook of Chemistry and Physics by
 * D.R. Lide (editor), CRC press 2009-2010, 90th edition.<p>
 * NOTE: values are NOT scaled on the reference value (Carbon), many descriptor algorithms (from Dragon) are based on
 * values scaled on Carbon.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class WeightsIonizationPotential implements iBasicWeight {

    private final static Object[][] mass = {
        {"H", 13.5984},
        {"He", 24.5874},
        {"Li", 5.3917},
        {"Be", 9.3226},
        {"B", 8.298},
        {"C", 11.2603},
        {"N", 14.5341},
        {"O", 13.6181},
        {"F", 17.4228},
        {"Ne", 21.5645},
        {"Na", 5.1391},
        {"Mg", 7.6462},
        {"Al", 5.9858},
        {"Si", 8.1517},
        {"P", 10.4867},
        {"S", 10.36},
        {"Cl", 12.9676},
        {"Ar", 15.7596},
        {"K", 4.3407},
        {"Ca", 6.1132},
        {"Sc", 6.5615},
        {"Ti", 6.8281},
        {"V", 6.7462},
        {"Cr", 6.7665},
        {"Mn", 7.434},
        {"Fe", 7.9024},
        {"Co", 7.881},
        {"Ni", 7.6398},
        {"Cu", 7.7264},
        {"Zn", 9.3942},
        {"Ga", 5.9993},
        {"Ge", 7.9},
        {"As", 9.8152},
        {"Se", 9.7524},
        {"Br", 11.8138},
        {"Kr", 13.9996},
        {"Rb", 4.1771},
        {"Sr", 5.6949},
        {"Y", 6.2173},
        {"Zr", 6.6339},
        {"Nb", 6.7589},
        {"Mo", 7.0924},
        {"Tc", 7.28},
        {"Ru", 7.3605},
        {"Rh", 7.4589},
        {"Pd", 8.3369},
        {"Ag", 7.5762},
        {"Cd", 8.9938},
        {"In", 5.7864},
        {"Sn", 7.3438},
        {"Sb", 8.6084},
        {"Te", 9.0096},
        {"I", 10.4513},
        {"Xe", 12.1298},
        {"Cs", 3.8939},
        {"Ba", 5.2117},
        {"La", 5.5769},
        {"Ce", 5.5387},
        {"Pr", 5.473},
        {"Nd", 5.525},
        {"Pm", 5.55},
        {"Sm", 5.6437},
        {"Eu", 5.6704},
        {"Gd", 6.1498},
        {"Tb", 5.8639},
        {"Dy", 5.9389},
        {"Ho", 6.0215},
        {"Er", 6.1077},
        {"Tm", 6.1843},
        {"Yb", 6.2542},
        {"Lu", 5.4259},
        {"Hf", 6.8251},
        {"Ta", 7.5496},
        {"W", 7.864},
        {"Re", 7.8335},
        {"Os", 8.4382},
        {"Ir", 8.967},
        {"Pt", 8.9588},
        {"Au", 9.2255},
        {"Hg", 10.4375},
        {"Tl", 6.1082},
        {"Pb", 7.4166},
        {"Bi", 7.2855},
        {"Po", 8.414},
        {"Rn", 10.7485},
        {"Fr", 4.0727},
        {"Ra", 5.2784},
        {"Ac", 5.17},
        {"Th", 6.3067},
        {"Pa", 5.89},
        {"U", 6.1941},
        {"Np", 6.2657},
        {"Pu", 6.026},
        {"Am", 5.9738},
        {"Cm", 5.9914},
        {"Bk", 6.1979},
        {"Cf", 6.2817},
        {"Es", 6.42},
        {"Fm", 6.5},
        {"Md", 6.58},
        {"No", 6.65},
        {"Lr", 4.9},
        {"Rf", 6}
    };


    /**
     * Calculate Ionization Potential for all atoms in the given molecule, and returns
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
     * Returns the Ionization Potential for the given atom type.
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
