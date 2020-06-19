package insilico.core.molecule.conversion.custom;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.InvPair;
import org.openscience.cdk.tools.periodictable.PeriodicTable;

import java.util.*;

/**
 * Canonically labels an atom container implementing
 * the algorithm published in David Weininger et.al. {@cdk.cite WEI89}.
 * The Collections.sort() method uses a merge sort which is
 * stable and runs in n log(n).
 */
public class CustomCanonicalLabeler {

    public CustomCanonicalLabeler(){}

    /**
     * Canonically label the fragment.  The labels are set as atom property InvPair.CANONICAL_LABEL of type Integer, indicating the canonical order.
     * This is an implementation of the algorithm published in
     * David Weininger et.al. {@cdk.cite WEI89}.
     *
     * <p>The Collections.sort() method uses a merge sort which is
     * stable and runs in n log(n).
     *
     * <p>It is assumed that a chemicaly valid AtomContainer is provided:
     * this method does not check
     * the correctness of the AtomContainer. Negative H counts will
     * cause a NumberFormatException to be thrown.
     * @param atomContainer The molecule to label
     */
    public synchronized void canonLabel(IAtomContainer atomContainer) {
        if (atomContainer.getAtomCount() == 0)
            return;
        if (atomContainer.getAtomCount() == 1) {
            atomContainer.getAtom(0).setProperty(InvPair.CANONICAL_LABEL, 1);
        }

        ArrayList vect = createInvarLabel(atomContainer);
        step3(vect, atomContainer);
    }

    /**
     * @param v the invariance pair vector
     */
    private void step2(ArrayList v, IAtomContainer atoms) {
        primeProduct(v, atoms);
        step3(v, atoms);
    }

    /**
     * @param v the invariance pair vector
     */
    private void step3(ArrayList v, IAtomContainer atoms) {
        try {
            sortArrayList(v);
        } catch (Throwable e) {
            // try-catch added due to problems with JDK 1.7
        }
        rankArrayList(v);
        if (!isInvPart(v)) {
            step2(v, atoms);
        } else {
            //On first pass save, partitioning as symmetry classes.
            if (((InvPair) v.get(v.size()-1)).getCurr() < v.size()) {
                breakTies(v);
                step2(v, atoms);
            }
            // now apply the ranking
            for (Object aV : v) {
                ((InvPair) aV).commit();
            }
        }
    }

    /**
     * Create initial invariant labeling corresponds to step 1
     *
     * @return ArrayList containting the
     */
    private ArrayList createInvarLabel(IAtomContainer atomContainer) {
        java.util.Iterator atoms = atomContainer.atoms().iterator();
        IAtom a;
        StringBuffer inv;
        ArrayList vect = new ArrayList();
        while(atoms.hasNext()) {
            a = (IAtom)atoms.next();
            inv = new StringBuffer();
            inv.append(atomContainer.getConnectedAtomsList(a).size() +
                    (a.getImplicitHydrogenCount() == CDKConstants.UNSET ? 0 : a.getImplicitHydrogenCount())); //Num connections
            inv.append(atomContainer.getConnectedAtomsList(a).size());                        //Num of non H bonds
            inv.append(PeriodicTable.getAtomicNumber(a.getSymbol()));

            Double charge = a.getCharge();
            if (charge == CDKConstants.UNSET) charge = 0.0;
            if (charge < 0)                                                        //Sign of charge
                inv.append(1);
            else
                inv.append(0);                                                              //Absolute charge
            inv.append((int)Math.abs( (a.getFormalCharge() == CDKConstants.UNSET ? 0.0 : a.getFormalCharge())));                                     //Hydrogen count
            inv.append((a.getImplicitHydrogenCount() == CDKConstants.UNSET ? 0 : a.getImplicitHydrogenCount()));
            vect.add(new InvPair(Long.parseLong(inv.toString()), a));
        }
        return vect;
    }

    /**
     * Calculates the product of the neighbouring primes.
     *
     * @param v the invariance pair vector
     */
    private void primeProduct(ArrayList v, IAtomContainer atomContainer) {
        Iterator it = v.iterator();
        Iterator n;
        InvPair inv;
        IAtom a;
        long summ;
        while (it.hasNext()) {
            inv = (InvPair) it.next();
            List neighbour = atomContainer.getConnectedAtomsList(inv.getAtom());
            n = neighbour.iterator();
            summ = 1;
            while (n.hasNext()) {
                a = (IAtom) n.next();
                int next = ((InvPair)a.getProperty(InvPair.INVARIANCE_PAIR)).getPrime();
                summ = summ * next;
            }
            inv.setLast(inv.getCurr());
            inv.setCurr(summ);
        }
    }

    /**
     * Sorts the vector according to the current invariance, corresponds to step 3
     *
     * @param v the invariance pair vector
     * @cdk.todo    can this be done in one loop?
     */
    private void sortArrayList(ArrayList v) {
        v.sort((o1, o2) -> {
            try {
                return (int) (((InvPair) o1).getCurr() - ((InvPair) o2).getCurr());
            } catch (Throwable e) {
                return 0;
            }
        });
        v.sort((o1, o2) -> {
            try {
                return (int) (((InvPair) o1).getLast() - ((InvPair) o2).getLast());
            } catch (Throwable e) {
                return 0;
            }

        });
    }

    /**
     * Rank atomic vector, corresponds to step 4.
     *
     *  @param v the invariance pair vector
     */
    private void rankArrayList(ArrayList v) {
        int num = 1;
        int[] temp = new int[v.size()];
        InvPair last = (InvPair) v.get(0);
        Iterator it = v.iterator();
        InvPair curr;
        for (int x = 0; it.hasNext(); x++) {
            curr = (InvPair) it.next();
            if (!last.equals(curr)) {
                num++;
            }
            temp[x] = num;
            last = curr;
        }
        it = v.iterator();
        for (int x = 0; it.hasNext(); x++) {
            curr = (InvPair) it.next();
            curr.setCurr(temp[x]);
            curr.setPrime();
        }
    }

    /**
     * Checks to see if the vector is invariantely partitioned
     *
     * @param v the invariance pair vector
     * @return true if the vector is invariantely partitioned, false otherwise
     */
    private boolean isInvPart(ArrayList v) {
        if (((InvPair) v.get(v.size()-1)).getCurr() == v.size())
            return true;
        Iterator it = v.iterator();
        InvPair curr;
        while (it.hasNext()) {
            curr = (InvPair) it.next();
            if (curr.getCurr() != curr.getLast())
                return false;
        }
        return true;
    }

    /**
     * Break ties. Corresponds to step 7
     *
     * @param v the invariance pair vector
     */
    private void breakTies(ArrayList v) {
        Iterator it = v.iterator();
        InvPair curr;
        InvPair last = null;
        int tie = 0;
        boolean found = false;
        for (int x = 0; it.hasNext(); x++) {
            curr = (InvPair) it.next();
            curr.setCurr(curr.getCurr() * 2);
            curr.setPrime();
            if (x != 0 && !found && curr.getCurr() == last.getCurr()) {
                tie = x - 1;
                found = true;
            }
            last = curr;
        }
        curr = (InvPair) v.get(tie);
        curr.setCurr(curr.getCurr() - 1);
        curr.setPrime();
    }







}
