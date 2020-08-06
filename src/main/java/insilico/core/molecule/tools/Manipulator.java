package insilico.core.molecule.tools;

import insilico.core.exception.GenericFailureException;
import insilico.core.molecule.matrix.ConnectionAugMatrix;
import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides utilities for manipulation of CDK Molecule structure.
 * sds
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class Manipulator {

    /**
     * Removes explicit hydrogen atoms and updates implicit hydrogen count
     * of the molecule. The molecule is cloned (the original object is
     * not modified).
     * @param molecule CDK Molecule object to be processed
     * @return A CDK Molecule object without explicit hydrogen atoms
     * @throws CDKException
     */
    public static IAtomContainer RemoveHydrogens(IAtomContainer molecule)
            throws CDKException {

        // Note: original routine taken from CDK code

        Map<IAtom, IAtom> map = new HashMap<>(); // maps original atoms to clones.
        List<IAtom> remove = new ArrayList<>();        // lists removed Hs.

        // Clone atoms except those to be removed.
        IAtomContainer NewMol = molecule.getBuilder().newInstance(IAtomContainer.class);
        int count = molecule.getAtomCount();
        for (int i = 0; i < count; i++) {

            IAtom atom = molecule.getAtom(i);
            if (!atom.getSymbol().equals("H")) {
                IAtom clonedAtom = null;
                try {
                    clonedAtom = (IAtom) atom.clone();
                } catch (CloneNotSupportedException e) {
                    throw new CDKException("Unable to clone atoms while removing hydrogens");
                }
                NewMol.addAtom(clonedAtom);
                map.put(atom, clonedAtom);
            } else
                remove.add(atom);   // maintain list of removed H.
        }

        // Clone bonds except those involving removed atoms.
        count = molecule.getBondCount();
        for (int i = 0; i < count; i++) {

            final IBond bond = molecule.getBond(i);
            boolean removedBond = false;
            final int length = bond.getAtomCount();
            for (int k = 0; k < length; k++) {
                if (remove.contains(bond.getAtom(k))) {
                    removedBond = true;
                    break;
                }
            }

            if (!removedBond) {
                IBond clone = null;
                try {
                    clone = (IBond) molecule.getBond(i).clone();
                } catch (CloneNotSupportedException e) {
                    throw new CDKException("Unable to clone bonds while removing hydrogens");
                }
                assert clone != null;
                clone.setAtoms(new IAtom[]{(IAtom) map.get(bond.getAtom(0)), (IAtom) map.get(bond.getAtom(1))});
                NewMol.addBond(clone);
            }
        }

        // Recompute hydrogen counts of neighbours of removed Hydrogens.
        for (IAtom aRemove : remove) {

            for (IAtom iAtom : molecule.getConnectedAtomsList(aRemove)) {
                final IAtom neighb = map.get(iAtom);
                if (neighb == null) continue; // case of H2
                int Hcount = 0;
                if (neighb.getImplicitHydrogenCount() == null)
                    Hcount = 0;
                else
                    Hcount = neighb.getImplicitHydrogenCount();
                Hcount++;
                neighb.setImplicitHydrogenCount(Hcount);
            }
        }
        NewMol.setProperties(molecule.getProperties());
        NewMol.setFlags(molecule.getFlags());

        return NewMol;
    }

    /**
     * Adds lacking implicit hydrogen to the molecule. It assumes that the
     * molecule has already all the hydrogens in implicit form, or that there
     * is no information about hydorgens at all. Hydrogens are not added in any
     * case for aromatic atoms.
     *
     * @param molecule CDK Molecule to be processed
     * @return The number of (implicit) hydrogen atoms added
     * @throws CDKException
     */
    public static int AddLackingImplicitHydrogens(IAtomContainer molecule)
            throws CDKException  {

        int AddedHydrogens = 0;

        try {

            double[][] ConnMatrix = ConnectionAugMatrix.getMatrix(molecule);

            // Adds hydrogens for non-aromatic atoms

            for (int i=0; i<molecule.getAtomCount(); i++) {

                // Checks if atom has already some hydrogens set
                IAtom at = molecule.getAtom(i);
                if (at.getImplicitHydrogenCount() != null)
                    if (at.getImplicitHydrogenCount() > 0)
                        continue;

                boolean isAromatic = false;
                double bufBondOrd = 0;
                for (int j=0; j<molecule.getAtomCount(); j++)
                    if (j!=i) {
                        bufBondOrd += ConnMatrix[i][j];
                        if (ConnMatrix[i][j] == 1.5)
                            isAromatic = true;
                    }
                int BondOrder = (int) bufBondOrd;
                int Z = (int)ConnMatrix[i][i];
                int H = 0;

                // Takes into account formal charge
                int Charge = 0;
                if (at.getFormalCharge() != null)
                    Charge = at.getFormalCharge();
                BondOrder = BondOrder - Charge;

                // if atom is into an aromatic bond, hydrogens are not assigned here
                if (isAromatic)
                    continue;

                // C
                if (Z==6) {
                    H = 4 - BondOrder;
                }

                // Halogen
                else if ((Z==9)||(Z==17)||(Z==35)||(Z==53)) {
                    H = 1 - BondOrder;
                }

                // N or P
                else if ((Z==7)||(Z==15)) {
                    if (BondOrder <= 3)
                        H = 3 - BondOrder;
                    else if (BondOrder <= 5)
                        H = 5 - BondOrder;
                }

                // O
                else if (Z==8) {
                    H = 2 - BondOrder;
                }

                // B
                else if (Z==5) {
                    H = 3 - BondOrder;
                }

                // S
                else if (Z==16) {
                    if (BondOrder <= 2)
                        H = 2 - BondOrder;
                    else if (BondOrder <= 4)
                        H = 4 - BondOrder;
                    else if (BondOrder <= 6)
                        H = 6 - BondOrder;
                }

                at.setImplicitHydrogenCount(H);
                AddedHydrogens += H;

            }

        } catch (Exception e) {
            throw new CDKException("unable to add lacking hydrogens");
        }

        return AddedHydrogens;
    }

    /**
     * Counts implicit H atoms for a given atom. Useful to avoid exception
     * when no implicit H count was set in the molecule.
     *
     * @param atom Atom object to be checked
     * @return number of H atoms
     */
    public static int CountImplicitHydrogens(IAtom atom) {
        int H=0;
        if (atom.getImplicitHydrogenCount()!=null)
            H = atom.getImplicitHydrogenCount();
        return H;
    }

    /**
     * Adds explicit hydrogen atom (as found counted in the implicit hydrogen
     * atoms field). Returns a cloned molecule.
     * @param molecule Molecule object to be processed
     * @return a cloned Molecule with explicit hydrogen atoms
     * @throws GenericFailureException
     */
    public static IAtomContainer AddHydrogens(IAtomContainer molecule)
            throws GenericFailureException {

        IAtomContainer NewMol=null;

        try {
            NewMol = molecule.clone();
        } catch (CloneNotSupportedException e) {
            throw new GenericFailureException("Unable to clone molecule");
        }

        int OldCount=molecule.getAtomCount();

        for (int i=0;i<OldCount;i++) {
            IAtom atom = NewMol.getAtom(i);
            int HCount = CountImplicitHydrogens(atom);

            atom.setImplicitHydrogenCount(0);

            for (int j=1;j<=HCount;j++) {
                Atom hydrogen = new Atom("H");
                hydrogen.setAtomicNumber(1);
                NewMol.addAtom(hydrogen);
                Bond newBond = new Bond(atom, hydrogen, IBond.Order.SINGLE);
                NewMol.addBond(newBond);
            }
        }

        return NewMol;
    }


}
