package insilico.core.molecule.conversion.custom;

import lombok.Data;
import org.openscience.cdk.interfaces.IAtom;

@Data
public class BrokenBond {

    /**
     * Atoms which close the ring
     */
    private IAtom atom1, atom2;

    /**
     * Number of the marker
     */
    private int marker;

    /**
     * Construct a BrokenBond between <code>atom1</code> and <code>atom1code> with
     * 		 *  the marker <code>marker</code>.
     * @param atom1
     * @param atom2
     * @param marker
     */
    BrokenBond(IAtom atom1, IAtom atom2, int marker){
        this.atom1 = atom1;
        this.atom2 = atom2;
        this.marker = marker;
    }

    public String toString(){
        return Integer.toString(marker);
    };

    public boolean equals(Object o){
        if(!(o instanceof BrokenBond))
            return false;
        BrokenBond bond = (BrokenBond) o;
        return (atom1.equals(bond.getAtom1()) && atom2.equals(bond.getAtom2()) || (atom1.equals(bond.getAtom2()) && atom2.equals(bond.getAtom1())));
    }

}
