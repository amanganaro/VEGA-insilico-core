package insilico.core.descriptor.blocks.weights.other;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.blocks.weights.iWeight;
import insilico.core.localization.StringSelector;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

public class WeightsQuantumNumber implements iWeight {

    private static final String SYMBOL = "deltaV";
    private static final String NAME = StringSelector.getString("descriptors_quantumnum_name");

    private final static Object[][] quantum = {
        {"H",1},
        {"He",1},
        {"Li",2},
        {"Be",2},
        {"B",2},
        {"C",2},
        {"N",2},
        {"O",2},
        {"F",2},
        {"Ne",2},
        {"Na",3},
        {"Mg",3},
        {"Al",3},
        {"Si",3},
        {"P",3},
        {"S",3},
        {"Cl",3},
        {"Ar",3},
        {"K",4},
        {"Ca",4},
        {"Sc",4},
        {"Ti",4},
        {"V",4},
        {"Cr",4},
        {"Mn",4},
        {"Fe",4},
        {"Co",4},
        {"Ni",4},
        {"Cu",4},
        {"Zn",4},
        {"Ga",4},
        {"Ge",4},
        {"As",4},
        {"Se",4},
        {"Br",4},
        {"Kr",4},
        {"Rb",5},
        {"Sr",5},
        {"Y",5},
        {"Zr",5},
        {"Nb",5},
        {"Mo",5},
        {"Tc",5},
        {"Ru",5},
        {"Rh",5},
        {"Pd",5},
        {"Ag",5},
        {"Cd",5},
        {"In",5},
        {"Sn",5},
        {"Sb",5},
        {"Te",5},
        {"I",5},
        {"Xe",5},
        {"Cs",6},
        {"Ba",6},
        {"La",6},
        {"Ce",6},
        {"Pr",6},
        {"Nd",6},
        {"Pm",6},
        {"Sm",6},
        {"Eu",6},
        {"Gd",6},
        {"Tb",6},
        {"Dy",6},
        {"Ho",6},
        {"Er",6},
        {"Tm",6},
        {"Yb",6},
        {"Lu",6},
        {"Hf",6},
        {"Ta",6},
        {"W",6},
        {"Re",6},
        {"Os",6},
        {"Ir",6},
        {"Pt",6},
        {"Au",6},
        {"Hg",6},
        {"Tl",6},
        {"Pb",6},
        {"Bi",6},
        {"Po",6},
        {"At",6},
        {"Rn",6},
        {"Fr",7},
        {"Ra",7},
        {"Ac",7},
        {"Th",7},
        {"Pa",7},
        {"U",7},
        {"Np",7},
        {"Pu",7},
        {"Am",7},
        {"Cm",7},
        {"Bk",7},
        {"Cf",7},
        {"Es",7},
        {"Fm",7},
        {"Md",7},
        {"No",7},
        {"Lr",7},
        {"Rf",7},
        {"Db",7},
        {"Sg",7},
        {"Bh",7},
        {"Hs",7},
        {"Mt",7},
        {"Ds",7},
        {"Rg",7},
        {"Uub",7},
        {"Uut",7},
        {"Uuq",7},
        {"Uup",7},
        {"Uuh",7},
        {"Uus",7},
        {"Uuo",7}
    };

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    /**
     * Calculate the quantum number for all atoms in the given molecule, and returns
     * them in a single array.
     *
     * @param mol molecule to be processed
     * @return array of quantum numbers for all atoms
     */
    public int[] getWeights(IAtomContainer mol)  {

        int nSK = mol.getAtomCount();
        int[] w = new int[nSK];

        for (int i=0; i<nSK; i++) {
            IAtom at = mol.getAtom(i);
            w[i] = getWeight(at.getSymbol());
        }

        return w;
    }


    /**
     * Returns the quantum number for the given atom type.
     *
     * @param AtomType element symbol of the atom to be processed
     * @return the value of the weight
     */
    public int getWeight(String AtomType) {

        try {
            for (Object[] o : quantum) {
                if (((String) o[0]).equalsIgnoreCase(AtomType))
                    return (Integer) o[1];
            }
        } catch (Throwable e) {
            return Descriptor.MISSING_VALUE;
        }

        return Descriptor.MISSING_VALUE;
    }
}
