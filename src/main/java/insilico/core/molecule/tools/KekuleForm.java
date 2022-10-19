package insilico.core.molecule.tools;

import insilico.core.exception.MoleculeConversionException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.molecule.matrix.BondAugMatrix;
import insilico.core.molecule.matrix.ConnectionAugMatrix;
import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Conversion of aromatic rings into Kekule form.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class KekuleForm {

    private final static int BND_IGNORE = 1;
    private final static int BND_SNG = 2;
    private final static int BND_DBL = 3;
    private final static int BND_SNG_OR_DBL = 4;
    private final static int BND_AROM = 5;
    private final static int MAX_CYCLES = 20;
    private final String ERR_HEADER = StringSelectorCore.getString("tool_kekule_error_header");

    private int[] ChangeBonds;
    private int[] ChangeFinalBonds;
    double[][] ConnMatrix;
    double[][] BondConnMatrix; // Bond connection matrix (augmented with bond order)


    /**
     * Constructor.
     */
    public KekuleForm() {
        // Do nothing
    }

    /**
     * Converts an aromatic ring to Kekule structure (single/double bonds).
     * Atom types should be already set. Throws exception if it is unable to
     * find a proper Kekule form for the ring.
     *
     * @param molecule CDK Molecule object
     * @param ring Ring to be processed
     * @throws MoleculeConversionException
     */
    public void Convert(IAtomContainer molecule, IRing ring) throws MoleculeConversionException {

        // Checks if ring is aromatic
        if (!MoleculeUtilities.IsRingAromatic(ring))
            return;

        ConnMatrix = ConnectionAugMatrix.getMatrix(molecule);
        BondConnMatrix = BondAugMatrix.getMatrix(molecule);

        Cycles cycles = Cycles.sssr(molecule);
        IRingSet allRings =  cycles.toRingSet();

        //// Calculates bonds possible orders

        // Creates the array with the information on how to change aromatic bonds
        ChangeBonds = new int[ring.getBondCount()];
        ChangeFinalBonds = new int[ring.getBondCount()];
        for (int i=0; i<ring.getBondCount(); i++) {
            IBond bnd = ring.getBond(i);

            boolean BelongsToFusedAromRings = false;
            IRingSet FusedRings = allRings.getRings(bnd);
            if (FusedRings.getAtomContainerCount() > 1) {
                int AromaticRings = 0;
                Iterator<IAtomContainer> RIterator = FusedRings.atomContainers().iterator();
                while (RIterator.hasNext())
                    if (MoleculeUtilities.IsRingAromatic((IRing) RIterator.next()))
                        AromaticRings++;
                if (AromaticRings > 1)
                    BelongsToFusedAromRings = true;
            }

            if (BelongsToFusedAromRings)
                ChangeBonds[i] = BND_AROM;
            else {
                if (bnd.getOrder() == IBond.Order.DOUBLE)
                    ChangeBonds[i] = BND_DBL;
                else
                    ChangeBonds[i] = BND_IGNORE;
            }
        }

        // Cycle on ring. Bonds are first checked and set
        // with generic directives (like DBL_SNG_OR_DBL) then in a second step
        // tries to set exact bonds with a euristic procedure

        ArrayList<ArrayList<Integer>> RingBonds = new ArrayList<>();

        try {

            for (int nRingAt=0; nRingAt<ring.getAtomCount(); nRingAt++)  {

                IAtom at = ring.getAtom(nRingAt);
                int nAt = molecule.indexOf(at);
                ArrayList<Integer> ArBonds = new ArrayList<Integer>();

                // Calculates vertex degree and type of bonds
                int nVertex=0, nAr=0, nSng=0, nDbl=0, nH=0, Charge=0;
                for (int k=0; k<molecule.getAtomCount(); k++) {
                    if (k == nAt)
                        continue;
                    if (ConnMatrix[nAt][k] == 0)
                        continue;
                    if (ConnMatrix[nAt][k] == 1.5) {
                        nVertex++;
                        nAr++;

                        IBond bnd = molecule.getBond(at, molecule.getAtom(k));
                        if (ring.contains(bnd))
                            ArBonds.add(ring.indexOf(bnd));

                        continue;
                    }
                    // CONTINUA DA QUA =>
                    if (ConnMatrix[nAt][k] == 1) {
                        nVertex++;
                        nSng++;
                        continue;
                    }
                    if (ConnMatrix[nAt][k] == 2) {
                        nVertex++;
                        nDbl++;
                    }
                }
                try {
                    nH = at.getImplicitHydrogenCount();
                } catch (Exception e) {
                    nH = 0;
                }
                try {
                    Charge = at.getFormalCharge();
                } catch (Exception e) {
                    Charge = 0;
                }

                RingBonds.add(ArBonds);

                // Carbon
                if (at.getSymbol().equalsIgnoreCase("C")) {
                    if ( ((nVertex==2) && (nAr==2)) ||
                            ((nVertex==3) && (nAr==3)) ||
                            ((nVertex==3) && (nAr==2) && (nSng==1)) )
                        SetChangeBond(ArBonds, BND_SNG_OR_DBL);

                    if ( ((nVertex==3) && (nAr==2) && (nDbl==1)) )
                        SetChangeBond(ArBonds, BND_SNG);

                    continue;
                }

                // Nitrogen or Phosphorus
                if ( (at.getSymbol().equalsIgnoreCase("N")) ||
                        (at.getSymbol().equalsIgnoreCase("P")) ){
                    if ((nVertex == 2) && (nH == 0)){
                        SetChangeBond(ArBonds, BND_SNG_OR_DBL);
                    } else if ((nVertex == 3) && (Charge == +1) && (nDbl == 0) && (nH == 0)){
                        SetChangeBond(ArBonds, BND_SNG_OR_DBL);
                    } else {
                        SetChangeBond(ArBonds, BND_SNG);
                    }

                    continue;
                }

                // Oxygen or Sulfur or Selenium
                if ( (at.getSymbol().equalsIgnoreCase("O")) ||
                        (at.getSymbol().equalsIgnoreCase("S")) ||
                        (at.getSymbol().equalsIgnoreCase("Se")) ) {

                    if (Charge == +1)
                        SetChangeBond(ArBonds, BND_SNG_OR_DBL);
                    else
                        SetChangeBond(ArBonds, BND_SNG);
                    continue;
                }


            }

        } catch (Exception e) {
            throw new MoleculeConversionException(ERR_HEADER + StringSelectorCore.getString("tool_kekule_bond_error"));
        }


        int[] OrigChangeBonds = new int[ring.getBondCount()];
        int RingCycles = 0;

        // Saves actual bonds information
        for (int i=0; i<ring.getBondCount(); i++)
            OrigChangeBonds[i] = ChangeBonds[i];

        while (RingCycles<MAX_CYCLES) {

            // Restores original bonds info because
            // each cycle means something went wrong
            // so previous data must be restored
            // also in final change bonds
            for (int i=0; i<ring.getBondCount(); i++) {
                ChangeBonds[i] = OrigChangeBonds[i];
                ChangeFinalBonds[i] = OrigChangeBonds[i];
            }

            try {

                // Sets final bond orders
                for (int i=0; i<RingBonds.size(); i++) {
                    ArrayList<Integer> CurBonds = RingBonds.get(i);
                    SetFinalBond(CurBonds);
                }

                // Checks set bond orders
                CheckNewAtomValences(molecule, ring);

                // if no exception is raised, settings are ok
                // and should quit from the cycle
                break;

            } catch (Exception e)  {
                RingCycles++;
            }
        }

        if (RingCycles == MAX_CYCLES)
            throw new MoleculeConversionException(ERR_HEADER + StringSelectorCore.getString("tool_kekule_bond_order_error"));

        // Stores new calculated bond orders
        for (int i=0; i<ring.getBondCount(); i++)
            ChangeBonds[i] = ChangeFinalBonds[i];

        // End of all cycles, bonds are finally set to their value
        for (int i=0; i<ring.getBondCount(); i++) {
            IBond bnd = ring.getBond(i);
            if (ChangeBonds[i] == BND_SNG) {
                bnd.setOrder(IBond.Order.SINGLE);
                bnd.setFlag(CDKConstants.ISAROMATIC, false);
            }
            if (ChangeBonds[i] == BND_DBL) {
                bnd.setOrder(IBond.Order.DOUBLE);
                bnd.setFlag(CDKConstants.ISAROMATIC, false);
            }
        }

    }

    ////// Utilities functions for Convert.class //////
    /**
     *
     * @param bonds
     * @param Value
     * @throws CDKException
     */
    private void SetChangeBond(ArrayList<Integer> bonds, int Value)
            throws CDKException {

        int nIgnore=0, nSng=0, nDbl=0, nArom=0;
        for (int b : bonds) {
            if (ChangeBonds[b] == BND_IGNORE) nIgnore++;
            if (ChangeBonds[b] == BND_SNG) nSng++;
            if (ChangeBonds[b] == BND_DBL) nDbl++;
            if (ChangeBonds[b] == BND_AROM) nArom++;
        }

        if (Value == BND_SNG) {
            if (nDbl>0)
                throw new CDKException(StringSelectorCore.getString("tool_kekule_bond_fix_error"));
            for (int i=0; i<bonds.size(); i++) {
                int b = bonds.get(i);
                if (ChangeBonds[b] != BND_AROM)
                    ChangeBonds[b] = BND_SNG;
            }

            return;
        }

        if (Value == BND_SNG_OR_DBL) {

            if ((nDbl > 1) || ((nSng==2)&&(bonds.size()==2)) || ((nSng==3)&&(bonds.size()==3)))
                throw new CDKException(StringSelectorCore.getString("tool_kekule_bond_fix_error"));

            if (nDbl==1) {

                // already one double, sets other bonds to single
                for (int b : bonds) {
                    if (ChangeBonds[b] != BND_AROM)
                        if (ChangeBonds[b] != BND_DBL)
                            ChangeBonds[b] = BND_SNG;
                }

                return;

            } else if (nDbl==0) {

                for (int b : bonds) {
                    if (ChangeBonds[b] != BND_AROM)
                        if (ChangeBonds[b] != BND_SNG)
                            ChangeBonds[b] = BND_SNG_OR_DBL;
                }
            }
        }

    }


    /**
     *
     * @param bonds
     * @throws CDKException
     */
    private void SetFinalBond(ArrayList<Integer> bonds)
            throws CDKException {

        int nIgnore=0, nSng=0, nDbl=0, nSngDbl=0, nArom=0;
        for (int i=0; i<bonds.size(); i++) {
            int b = bonds.get(i);
            if (ChangeFinalBonds[b] == BND_IGNORE) nIgnore++;
            if (ChangeFinalBonds[b] == BND_SNG) nSng++;
            if (ChangeFinalBonds[b] == BND_DBL) nDbl++;
            if (ChangeFinalBonds[b] == BND_AROM) nArom++;
            if (ChangeFinalBonds[b] == BND_SNG_OR_DBL) nSngDbl++;
        }

        if (nSngDbl == 0)
            return;

        int DblToAssing = (1 - nDbl);

        if (DblToAssing == 0) {

            // all remaining bonds set to single
            for (int b : bonds) {
                if (ChangeFinalBonds[b] != BND_AROM)
                    if (ChangeFinalBonds[b] == BND_SNG_OR_DBL)
                        ChangeFinalBonds[b] = BND_SNG;
            }

            return;

        } else {

            // random assigns double bond
            int rnd = (int)(Math.random() * nSngDbl);
            for (int b : bonds) {
                if (ChangeFinalBonds[b] != BND_AROM)
                    if (ChangeFinalBonds[b] == BND_SNG_OR_DBL) {
                        if (rnd == 0)
                            ChangeFinalBonds[b] = BND_DBL;
                        rnd--;
                    }
            }

            // all remaining bonds set to single
            for (int b : bonds) {
                if (ChangeFinalBonds[b] != BND_AROM)
                    if (ChangeFinalBonds[b] == BND_SNG_OR_DBL)
                        ChangeFinalBonds[b] = BND_SNG;
            }

        }

    }


    /**
     *
     * @param molecule
     * @param ring
     * @throws CDKException
     */
    private void CheckNewAtomValences(IAtomContainer molecule, IRing ring)
            throws CDKException {

        for (int i=0; i<molecule.getAtomCount(); i++) {
            if (ring.contains(molecule.getAtom(i))) {

                int TBO=0, nH=0, Charge=0;
                for (int k=0; k<molecule.getAtomCount(); k++) {
                    if (k==i) continue;
                    if (ConnMatrix[i][k]>0) {
                        IBond b = molecule.getBond(molecule.getAtom(i), molecule.getAtom(k));
                        int bn = molecule.indexOf(b);
                        if (ChangeFinalBonds[bn] == BND_IGNORE)
                            TBO += ConnMatrix[i][k];
                        else {
                            if (ChangeFinalBonds[bn] == BND_SNG) TBO += 1;
                            if (ChangeFinalBonds[bn] == BND_DBL) TBO += 2;
                        }
                    }
                }
                try { nH = molecule.getAtom(i).getImplicitHydrogenCount(); }
                catch (Exception e) { nH = 0; }
                try { Charge = molecule.getAtom(i).getFormalCharge(); }
                catch (Exception e) { Charge = 0; }

                int Z = (int)ConnMatrix[i][i];
                int bo = TBO + nH + ((-1) * Charge);

                // C
                if (Z==6) {
                    if (bo != 4) throw new CDKException(StringSelectorCore.getString("tool_kekule_bond_order_error_cdk_ex"));
                }

                // Halogen
                else if ((Z==9)||(Z==17)||(Z==35)||(Z==53)) {
                    if (bo != 1) throw new CDKException(StringSelectorCore.getString("tool_kekule_bond_order_error_cdk_ex"));
                }

                // N or P
                else if ((Z==7)||(Z==15)) {
                    if (!((bo == 3)||(bo == 5))) throw new CDKException(StringSelectorCore.getString("tool_kekule_bond_order_error_cdk_ex"));
                }

                // O or Se
                else if ((Z==8)||(Z==34)) {
                    if (bo != 2) throw new CDKException(StringSelectorCore.getString("tool_kekule_bond_order_error_cdk_ex"));
                }

                // B
                else if (Z==5) {
                    if (bo != 3) throw new CDKException(StringSelectorCore.getString("tool_kekule_bond_order_error_cdk_ex"));
                }

                // S
                else if (Z==16) {
                    if (!((bo == 2)||(bo == 4)||(bo == 6))) throw new CDKException(StringSelectorCore.getString("tool_kekule_bond_order_error_cdk_ex"));
                }

            }
        }

    }


}
