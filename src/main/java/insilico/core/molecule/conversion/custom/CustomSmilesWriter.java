/**
 * Original code taken from CDK 1.4.4
 * Does NOT check for aromaticity, as it relies on the aromaticity already
 * calculated when the molecule was imported from SDF / SMILES
 *
 * It is used instead of the original class taken from CDK 1.2.3 as it fixes
 * some bugs (error in interpreting benzenes fully substituted)
 *
 *
 *  Copyright (C) 2002-2007  Oliver Horlacher
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package insilico.core.molecule.conversion.custom;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.BondTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.invariant.MorganNumbersTools;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.ringsearch.AllRingsFinder;

import java.io.IOException;
import java.util.*;


/**
 * Generates SMILES strings {@cdk.cite WEI88, WEI89}. It takes into account the
 * isotope and formal charge information of the atoms. In addition to this it
 * takes stereochemistry in account for both Bond's and Atom's. Via the flag
 * useAromaticity it can be set if only SP2-hybridized atoms shall be set to
 * lower case (default) or atoms, which are SP2 or aromatic.
 *
 * <p>Some example code:
 * <pre>
 * IMolecule benzene; // single/aromatic bonds between 6 carbons
 * SmilesGenerator sg = new SmilesGenerator();
 * String smiles = sg.createSMILES(benzene); // C1CCCCC1
 * sg.setUseAromaticityFlag(true);
 * smiles = sg.createSMILES(benzene); // c1ccccc1
 * IMolecule benzene2; // one of the two kekule structures with explicit double bond orders
 * String smiles2 = sg.createSMILES(benzene2); // C1=CC=CC=C1
 * </pre>
 * <b>Note</b>Due to the way the initial atom labeling is constructed, ensure
 * that the input molecule is appropriately configured.
 * In absence of such configuration it is possible that different forms
 * of the same molecule will not result in the same canonical SMILES.
 */
@SuppressWarnings("unchecked")
public class CustomSmilesWriter {

    /**
     * Number of rings that have been opened
     */
    private int ringMarker = 0;


    /**
     * Collection of all the bonds that were broken
     */
    private List<BrokenBond> brokenBonds = new ArrayList<>();

    /**
     * Isotope factory which is used to write the mass is needed
     */
    private IsotopeFactory isotopeFactory;

    AllRingsFinder ringsFinder;

    // RingSet hold all rings of the molecule
    private IRingSet rings = null;

    // labeler
    private CustomCanonicalLabeler labeler = new CustomCanonicalLabeler();
    private final String RING_CONFIG = "stereoconfig";
    private final String UP = "up";
    private final String DOWN = "down";
    private boolean useAromaticityFlag=false;

    // Create the smiles generator
    public CustomSmilesWriter(){}

    /**
     * Tells if a certain bond is center of a valid double bond configuration
     * @param container IAtomContainer object
     * @param bond IBond object
     * @return
     */
    public boolean isValidDoubleBondConfiguration(IAtomContainer container, IBond bond) {
        IAtom atom0 = bond.getAtom(0);
        IAtom atom1 = bond.getAtom(1);
        List<IAtom> connectedAtoms = container.getConnectedAtomsList(atom0);
        IAtom from = null;
        for(IAtom connectedAtom : connectedAtoms){
            if (connectedAtom != atom1)
                from = connectedAtom;
        }
        boolean[] array = new boolean[container.getBondCount()];
        Arrays.fill(array, true);
        return isStartOfDoubleBond(container, atom0, from, array) && isEndOfDoubleBond(container, atom1, atom0, array) && !bond.getFlag(CDKConstants.ISAROMATIC);
    }

    /**
     * Provide a reference to a RingSet that holds ALL rings of the molecule.<BR>
     * During creation of a SMILES the aromaticity of the molecule has to be detected.
     * So requires the determination of all rings of the molecule.
     *
     * @param  rings  RingSet that holds ALL rings of the molecule
     * @return        reference to the SmilesGenerator object this method was called for
     */
    public CustomSmilesWriter setRings(IRingSet rings){
        this.rings = rings;
        return this;
    }


    public synchronized String createSMILES(IAtomContainer molecule) throws CDKException{
        return(createSMILES(molecule, false, new boolean[molecule.getBondCount()]));
    }

    /**
     *  Generate canonical SMILES from the <code>molecule</code>. This method canonicaly lables the molecule but dose not
     *  perform any checks on the chemical validity of the molecule. This method also takes care of multiple molecules.
     *  IMPORTANT: A precomputed Set of All Rings (SAR) can be passed to this
     *  SmilesGenerator in order to avoid recomputing it. Use setRings() to
     *  assign the SAR.
     *
     * @param  molecule                 The molecule to evaluate.
     * @param  chiral                   true=SMILES will be chiral, false=SMILES will not be chiral.
     * @param  doubleBondConfiguration  Should E/Z configurations be read at these positions? If the flag at position X is set to true,
     *                                  an E/Z configuration will be written from coordinates around bond X, if false, it will be ignored.
     *                                  If flag is true for a bond which does not constitute a valid double bond configuration, it will be
     *                                  ignored (meaning setting all to true will create E/Z indication will be pu in the smiles wherever
     *                                  possible, but note the coordinates might be arbitrary).
     * @exception CDKException          At least one atom has no Point2D;
     *      coordinates are needed for crating the chiral smiles. This excpetion
     *      can only be thrown if chiral smiles is created, ignore it if you want a
     *      non-chiral smiles (createSMILES(AtomContainer) does not throw an
     *      exception).
     * @see                             org.openscience.cdk.graph.invariant.CanonicalLabeler#canonLabel(IAtomContainer)
     * @return the SMILES representation of the molecule
     */
    public synchronized String createSMILES(IAtomContainer molecule, boolean chiral, boolean[] doubleBondConfiguration)
            throws CDKException {
        IAtomContainerSet moleculeSet = ConnectivityChecker.partitionIntoMolecules(molecule);
        if(moleculeSet.getAtomContainerCount() > 1) {
            StringBuilder fullSMILES = new StringBuilder();
            for(int i = 0; i< moleculeSet.getAtomContainerCount(); i++){
                IAtomContainer molPart = moleculeSet.getAtomContainer(i);
                fullSMILES.append(createSMILESWithoutCheckForMultipleMolecules(molPart, chiral, doubleBondConfiguration));
                if (i < (moleculeSet.getAtomContainerCount() - 1))
                    fullSMILES.append('.');

            }
            return fullSMILES.toString();
        } else
            return createSMILESWithoutCheckForMultipleMolecules(molecule, chiral, doubleBondConfiguration);
    }

    /**
     *  Performes a DFS search on the <code>atomContainer</code>. Then parses the
     *  resulting tree to create the SMILES string.
     *
     *@param  a                        the atom to start the search at.
     *@param  line                     the StringBuffer that the SMILES is to be
     *      appended to.
     *@param  chiral                   true=SMILES will be chiral, false=SMILES
     *      will not be chiral.
     *@param  doubleBondConfiguration  Should E/Z configurations be read at these positions? If the flag at position X is set to true,
     *                                 an E/Z configuration will be written from coordinates around bond X, if false, it will be ignored.
     *                                 If flag is true for a bond which does not constitute a valid double bond configuration, it will be
     *                                 ignored (meaning setting all to true will create E/Z indication will be pu in the smiles wherever
     *                                 possible, but note the coordinates might be arbitrary).
     *@param  atomContainer            the AtomContainer that the SMILES string is
     *      generated for.
     *@param useAromaticity				true=aromaticity or sp2 will trigger lower case letters, wrong=only sp2
     */
    private void createSMILES(IAtom a, StringBuffer line, IAtomContainer atomContainer, boolean chiral, boolean[] doubleBondConfiguration, boolean useAromaticity)
    {
        List tree = new Vector();

        // set all ISVISITED labels to FALSE
        Iterator atoms = atomContainer.atoms().iterator();
        while (atoms.hasNext()) ((IAtom)atoms.next()).setFlag(CDKConstants.VISITED, false);

        createDFSTree(a, tree, null, atomContainer);
        //logger.debug("Done with tree");

        parseChain(tree, line, atomContainer, null, chiral, doubleBondConfiguration, new Vector(), useAromaticity);
    }

    /**
     *  Generate canonical SMILES from the <code>molecule</code>. This method canonicaly lables the molecule but dose not perform any checks on the
     *  chemical validity of the molecule. Does not care about multiple molecules.
     *  IMPORTANT: A precomputed Set of All Rings (SAR) can be passed to this SmilesGenerator in order to avoid recomputing it. Use setRings() to assign the SAR.
     *
     * @param  molecule The molecule to evaluate.
     * @param  chiral true=SMILES will be chiral, false=SMILES will not be chiral.
     * @param  doubleBondConfiguration  Should E/Z configurations be read at these positions? If the flag at position X is set to true,
     *                                  an E/Z configuration will be written from coordinates around bond X, if false, it will be ignored.
     *                                  If flag is true for a bond which does not constitute a valid double bond configuration, it will be
     *                                  ignored (meaning setting all to true will create E/Z indication will be pu in the smiles wherever
     *                                  possible, but note the coordinates might be arbitrary).
     * @exception  CDKException         At least one atom has no Point2D;
     *      coordinates are needed for creating the chiral smiles. This excpetion
     *      can only be thrown if chiral smiles is created, ignore it if you want a
     *      non-chiral smiles (createSMILES(AtomContainer) does not throw an
     *      exception).
     * @return the SMILES representation of the molecule
     */
    public synchronized String createSMILESWithoutCheckForMultipleMolecules(IAtomContainer molecule, boolean chiral, boolean doubleBondConfiguration[])
            throws CDKException {

        if (molecule.getAtomCount() == 0)
            return "";
        try {
            labeler.canonLabel(molecule);
        } catch (Throwable e) {
            throw new CDKException("Unable to run canonical labelelr for the SMILES [" + e.getMessage() + "]");
        }
        brokenBonds.clear();
        ringMarker = 0;
        IAtom start = null;
        for (int i = 0; i < molecule.getAtomCount(); i++){
            IAtom atom = molecule.getAtom(i);
            atom.setFlag(CDKConstants.VISITED, false);
            if((Long) atom.getProperty("CanonicalLabel") == 1)
                start = atom;
        }

        StringBuffer buffer = new StringBuffer();
        createSMILES(start, buffer, molecule, chiral, doubleBondConfiguration,useAromaticityFlag);
        rings = null;

        // Remove all Canonical Label and InvariancePair props
        for (int i = 0; i < molecule.getAtomCount(); i++){
            molecule.getAtom(i).removeProperty("CanonicalLabel");
            molecule.getAtom(i).removeProperty("InvariancePair");
        }

        return buffer.toString();
    }

    /**
     * Check if an atom is the end of a double bond configuration
     * @param container the AtomContainer the atom is in
     * @param atom the atom which is the end of configuration
     * @param parent the atom we came from
     * @param doubleBondConfiguration the array which indicates where dobule bound configurations are spcified
     * @return false=not the end of configuration, true=end of configuration
     */
    private boolean isEndOfDoubleBond(IAtomContainer container, IAtom atom, IAtom parent, boolean[] doubleBondConfiguration){

        IBond bond = container.getBond(atom, parent);
        int bondOrder = container.indexOf(bond);

        if(bondOrder == -1 || doubleBondConfiguration.length <= bondOrder || !doubleBondConfiguration[bondOrder])
            return false;

        // DEPRECATED MODE
//        if(container.getBondNumber(atom, parent) == -1 || doubleBondConfiguration.length <= container.getBondNumber(atom, parent) || !doubleBondConfiguration[container.getBondNumber(atom,parent)])
//            return false;
        int lengthAtom = container.getConnectedBondsCount(atom) + ((atom.getImplicitHydrogenCount() == CDKConstants.UNSET) ? 0 : atom.getImplicitHydrogenCount());
        int lengthParent = container.getConnectedBondsCount(parent) + ((parent.getImplicitHydrogenCount() == CDKConstants.UNSET) ? 0 : parent.getImplicitHydrogenCount());
        if (container.getBond(atom, parent) != null)
        {

            // CDKConstants.BONDORDER_DOUBLE =>
            if (container.getBond(atom, parent).getOrder() == IBond.Order.DOUBLE && (lengthAtom == 3 || (lengthAtom == 2 && atom.getSymbol().equals("N"))) && (lengthParent == 3 || (lengthParent == 2 && parent.getSymbol().equals("N"))))
            {
                List<IAtom> atoms = container.getConnectedAtomsList(atom);
                IAtom one = null;
                IAtom two = null;
                IAtom atm = null;
                for (int i = 0; i < atoms.size(); i++)
                {
                    atm = (IAtom)container.getAtom(i);
                    if (atm != parent && one == null)
                    {
                        one = atm;
                    } else if (atm != parent)
                    {
                        two = atm;
                    }
                }
                String[] morganNumbers = MorganNumbersTools.getMorganNumbersWithElementSymbol(container);
                return (one != null && two == null && atom.getSymbol().equals("N")
                        && Math.abs(BondTools.giveAngleBothMethods(parent, atom, one, true)) > Math.PI / 10)
                        || (!atom.getSymbol().equals("N") && one != null && two != null
                        && !morganNumbers[container.indexOf(one)].equals(morganNumbers[container.indexOf(two)]));
            }
        }
        return (false);
    }

    /**
     *  Says if an atom is the start of a double bond configuration
     * @param container the atom container in which <code>atom</code> is in - IAtom Object
     * @param atom The atom which is the start of configuration - IAtomContainer Object
     * @param parent The atom we came from
     * @param doubleBondConfiguration The array indicating where double bond configurations are specified
     *                                The method ensures that there is actually a chance of a double bond configuration
     * @return true = is a start of configuration
     *         false = is not a start of configuration
     */
    private boolean isStartOfDoubleBond(IAtomContainer container, IAtom atom, IAtom parent, boolean[] doubleBondConfiguration){
        int lengthAtom = container.getConnectedBondsCount(atom) + ((atom.getImplicitHydrogenCount() == CDKConstants.UNSET) ? 0 : atom.getImplicitHydrogenCount());
        if (lengthAtom != 3 && (lengthAtom != 2 && !atom.getSymbol().equals("N")))
            return false;
        List<IAtom> atoms = container.getConnectedAtomsList(atom);
        IAtom one = null;
        IAtom two = null;
        IAtom nextAtom = null;
        boolean doubleBond = false;
        for (IAtom atm: atoms){
            if (atm != parent && container.getBond(atm, atom).getOrder() == IBond.Order.DOUBLE && isEndOfDoubleBond(container, atm, atom, doubleBondConfiguration)) {
                doubleBond = true;
                nextAtom = atm;
            }
            if (atm != nextAtom && one == null)
                one = atm;
            else if (atm != nextAtom)
                two = atm;
        }
        String[] morganNumbers = MorganNumbersTools.getMorganNumbersWithElementSymbol(container);
        // REFACTOR .indexOf and .getBondNumber are deprecated
        //
        IBond bond = container.getBond(atom, nextAtom);
        int bondOrder = container.indexOf(bond);
        if (one != null && ((!atom.getSymbol().equals("N") &&
                two != null && !morganNumbers[container.indexOf(one)].equals(morganNumbers[container.indexOf(two)]) &&
                doubleBond && doubleBondConfiguration[bondOrder]) ||
                (doubleBond && atom.getSymbol().equals("N") && Math.abs(BondTools.giveAngleBothMethods(nextAtom, atom, parent, true)) > Math.PI / 10)))
            return true;
        else return false;
    }

    /**
     *  Gets the bondBroken attribute of the SmilesGenerator object
     */
    private boolean isBondBroken(IAtom atom1, IAtom atom2)
    {
        for (BrokenBond bond : brokenBonds) {
            if ((bond.getAtom1().equals(atom1) || bond.getAtom1().equals(atom2)) && (bond.getAtom2().equals(atom1) || bond.getAtom2().equals(atom2)))
                return (true);
        }
        return false;
    }

    /**
     * Determines if the atom <code>atom1</code>
     * @param atom1
     * @param v
     * @return
     */
    private boolean isRingOpening(IAtom atom1, List v) {
        for (BrokenBond bond : brokenBonds) {
            for (Object aV : v) {
                if ((bond.getAtom1().equals(atom1) && bond.getAtom2().equals((IAtom) aV)) || (bond.getAtom1().equals((IAtom) aV) && bond.getAtom2().equals(atom1)))
                    return true;
            }
        }
        return false;
    }

    /**
     *  Return the neighbours of atom <code>atom</code> in canonical order with the
     *  atoms that have high bond order at the front.
     *
     *@param  atom          the atom whose neighbours are to be found.
     *@param  container  the AtomContainer that is being parsed.
     *@return            Vector of atoms in canonical order.
     */
    private List getCanNeigh(final IAtom atom, final IAtomContainer container){
        List<IAtom> list = container.getConnectedAtomsList(atom);
        if (list.size() > 1)
            list.sort(new Comparator() {
                public int compare(Object o1, Object o2) {
                    return (int) ((Long) ((IAtom) o1).getProperty("CanonicalLabel") - (Long) ((IAtom) o2).getProperty("CanonicalLabel"));
                }
            });
        return list;
    }

    /**
     * Gets the ringOpenings attribute of the SmilesGenerator object
     * @param atom
     * @param vbonds
     * @return
     */
    private List getRingOpenings(IAtom atom, List vbonds){
        Iterator it = brokenBonds.iterator();
        List list = new Vector(10);
        while(it.hasNext()){
            BrokenBond bond = (BrokenBond) it.next();
            if(bond.getAtom1().equals(atom) || bond.getAtom2().equals(atom)){
                list.add(bond.getMarker());
                if (vbonds != null)
                    vbonds.add(bond.getAtom1().equals(atom) ? bond.getAtom2() : bond.getAtom1());
            }
        }
        Collections.sort(list);
        return list;
    }

    /**
     * Gets the last atom object in a vector as created by createDSFree method
     * @param list vector
     * @param result feature to be added to the atoms attribute
     */
    private void addAtoms(List list, List result){
        for(Object item: list){
            if(item instanceof IAtom)
                result.add((IAtom) item);
            else addAtoms((List) item, result);
        }
    }

    /**
     *  Recursively perform a DFS search on the <code>container</code> placing atoms and branches in vector <code>tree</code>
     * @param atom atom being visited
     * @param tree  vector holding the tree
     * @param parent the atom we came from
     * @param container the AtomContainer we are parsing
     */
    private void createDFSTree(IAtom atom, List tree, IAtom parent, IAtomContainer container){
        tree.add(atom);
        List neighbours = new ArrayList(getCanNeigh(atom, container));
        neighbours.remove(parent);
        IAtom next;
        atom.setFlag(CDKConstants.VISITED, true);
        Iterator iter = neighbours.iterator();
        while (iter.hasNext()) {
            next = (IAtom)iter.next();
            if (!next.getFlag(CDKConstants.VISITED))
            {
                if (!iter.hasNext())
                    createDFSTree(next, tree, atom, container);
                else {
                    List branch = new Vector();
                    tree.add(branch);
                    createDFSTree(next, branch, atom, container);
                }
            } else {
                ringMarker++;
                BrokenBond bond = new BrokenBond(atom, next, ringMarker);
                if (!brokenBonds.contains(bond))
                    brokenBonds.add(bond);
                else
                    ringMarker--;
            }
        }
    }

    private void parseChain(List list, StringBuffer buffer, IAtomContainer container, IAtom parent, boolean chiral, boolean[] doubleBondConfiguration,
                            List atomsInOrderOfSmiles, boolean useAromaticity) {
        int positionInVector = 0;
        IAtom atom;
        for (int h = 0; h < list.size(); h++){
            Object o = list.get(h);
            if (o instanceof IAtom)
            {
                atom = (IAtom) o;
                if (parent != null)
                    parseBond(buffer, atom, parent, container, useAromaticity);
                else
                if (chiral && BondTools.isStereo(container, atom))
                    parent = (IAtom) ((List) list.get(1)).get(0);
                parseAtom(atom, buffer, container, chiral, doubleBondConfiguration, parent, atomsInOrderOfSmiles, list, useAromaticity);

                parent = atom;
            } else
            {
                boolean brackets = true;
                List result = new Vector();
                addAtoms((List) o, result);
                IAtom prevAtom;
                /*
                 * Got to find last atom that was processed.
                 * This is to check the relative position of the current atom/chain with respect to its parent
                 */
                prevAtom = (IAtom)((Vector) atomsInOrderOfSmiles).lastElement();
                int maxConnectedBondCount = 4;
                /**
                 * If the parent atom of this new chain is the very first atom in the SMILES string and this chain is placed
                 * immediately after the parent atom then the max connected bond count for the parent should be 3 instead of 4.
                 */
                if (atomsInOrderOfSmiles.indexOf(parent) == 0 && prevAtom == parent){
                    maxConnectedBondCount = 3;
                }
                if (isRingOpening(parent, result) && container.getConnectedBondsCount(parent) < maxConnectedBondCount)
                    brackets = false;
                if (brackets)
                    buffer.append('(');
                parseChain((List) o, buffer, container, parent, chiral, doubleBondConfiguration, atomsInOrderOfSmiles, useAromaticity);
                if (brackets)
                    buffer.append(')');
            }
            positionInVector++;
        }
    }

    /**
     *  Append the symbol for the bond order between <code>a1</code> and <code>a2</code>
     *  to the <code>line</code>.
     *
     *@param  line           the StringBuffer that the bond symbol is appended to.
     *@param  atom1             Atom participating in the bond.
     *@param  atom2             Atom participating in the bond.
     *@param  atomContainer  the AtomContainer that the SMILES string is generated
     *      for.
     *@param useAromaticity				true=aromaticity or sp2 will trigger lower case letters, wrong=only sp2
     */
    private void parseBond(StringBuffer line, IAtom atom1, IAtom atom2, IAtomContainer atomContainer, boolean useAromaticity){
        if(useAromaticity && atom1.getFlag(CDKConstants.ISAROMATIC) && atom2.getFlag(CDKConstants.ISAROMATIC))
            return;
        if(atomContainer.getBond(atom1, atom2) == null)
            return;
        IBond.Order type = atomContainer.getBond(atom1, atom2).getOrder();
        if (type == IBond.Order.SINGLE){}
        else if (type == IBond.Order.DOUBLE)
            line.append("=");
        else if (type == IBond.Order.TRIPLE)
            line.append("#");
        else {}
    }


    /**
     *  Generates the SMILES string for the atom
     *
     *@param  atom                        the atom to generate the SMILES for.
     *@param  buffer                   the string buffer that the atom is to be
     *      apended to.
     *@param  container                the AtomContainer to analyze.
     *@param  chiral                   is a chiral smiles wished?
     *@param  parent                   the atom we came from.
     *@param  atomsInOrderOfSmiles     a vector containing the atoms in the order
     *      they are in the smiles.
     *@param  currentChain             The chain we currently deal with.
     *@param useAromaticity				true=aromaticity or sp2 will trigger lower case letters, wrong=only sp2
     */
    private void parseAtom(IAtom atom, StringBuffer buffer, IAtomContainer container, boolean chiral, boolean[] doubleBondConfiguration, IAtom parent, List atomsInOrderOfSmiles, List currentChain, boolean useAromaticity)
    {
        String symbol = atom.getSymbol();
        if (atom instanceof PseudoAtom) symbol = "*";

        boolean stereo = false;
        if (chiral)
            stereo = BondTools.isStereo(container, atom);
        boolean brackets = symbol.equals("B") || symbol.equals("C") || symbol.equals("N") || symbol.equals("O") || symbol.equals("P") || symbol.equals("S") || symbol.equals("F") || symbol.equals("Br") || symbol.equals("I") || symbol.equals("Cl");
        brackets = !brackets;
        //Deal with the start of atom double bond configuration
        if (chiral && isStartOfDoubleBond(container, atom, parent, doubleBondConfiguration))
        {
            buffer.append('/');
        }

        String mass = generateMassString(atom);
        brackets = brackets | !mass.equals("");

        String charge = generateChargeString(atom);
        brackets = brackets | !charge.equals("");

        if (chiral && stereo && (BondTools.isTrigonalBipyramidalOrOctahedral(container, atom)!=0 || BondTools.isSquarePlanar(container, atom) || BondTools.isTetrahedral(container, atom,false) != 0 || BondTools.isSquarePlanar(container, atom)))
        {
            brackets = true;
        }
        if (brackets)
        {
            buffer.append('[');
        }
        buffer.append(mass);
        if ((useAromaticity && atom.getFlag(CDKConstants.ISAROMATIC)))
        {
//          we put in atom special check for N.planar3 cases such
//          as for indole and pyrrole, which require an explicit
//          H on the nitrogen. However this only makes sense when
//          the connectivity is not 3 - so for atom case such as n1ncn(c1)CC
//          the PLANAR3 N already has 3 bonds, so don't add atom H for this case.
//          All aromatic N with valence = 3 and one hydrogen are written with explicit H


            int nH = 0;
            if (atom.getImplicitHydrogenCount() != null)
                nH = atom.getImplicitHydrogenCount();
            if (atom.getSymbol().equals("N") && nH == 1 && container.getConnectedAtomsList(atom).size() != 3)
                buffer.append("[").append(atom.getSymbol().toLowerCase()).append("H]");
            else buffer.append(atom.getSymbol().toLowerCase());

        } else {
            buffer.append(symbol);
            if (symbol.equals("*") && atom.getImplicitHydrogenCount() != null && atom.getImplicitHydrogenCount() > 0)
                buffer.append("H").append(atom.getImplicitHydrogenCount());
            else {
//              if heteroatoms where atom bracket is opened have hydrogens, they are explicitly set
                if (brackets)
                    if (atom.getImplicitHydrogenCount() != null && atom.getImplicitHydrogenCount() > 0) {
                        buffer.append("H");
                        if (atom.getImplicitHydrogenCount() > 1)
                            buffer.append(atom.getImplicitHydrogenCount());
                    }
            }

        }

        if (atom.getProperty(RING_CONFIG) != null && atom.getProperty(RING_CONFIG).equals(UP))
            buffer.append('/');
        if (atom.getProperty(RING_CONFIG) != null && atom.getProperty(RING_CONFIG).equals(DOWN))
            buffer.append('\\');
        if (chiral && stereo && (BondTools.isTrigonalBipyramidalOrOctahedral(container, atom)!=0 || BondTools.isSquarePlanar(container, atom) || BondTools.isTetrahedral(container, atom,false) != 0))
            buffer.append('@');
        if (chiral && stereo && BondTools.isSquarePlanar(container, atom))
            buffer.append("SP1");
        //chiral
        //hcount
        buffer.append(charge);
        if (brackets)
            buffer.append(']');

        //Deal with the end of atom double bond configuration
        if (chiral && isEndOfDoubleBond(container, atom, parent, doubleBondConfiguration))
        {
            IAtom viewFrom = null;
            for (int i = 0; i < currentChain.size(); i++)
            {
                if (currentChain.get(i) == parent)
                {
                    int k = i - 1;
                    while (k > -1)
                    {
                        if (currentChain.get(k) instanceof IAtom)
                        {
                            viewFrom = (IAtom) currentChain.get(k);
                            break;
                        }
                        k--;
                    }
                }
            }
            if (viewFrom == null)
            {
                for (int i = 0; i < atomsInOrderOfSmiles.size(); i++)
                {
                    if (atomsInOrderOfSmiles.get(i) == parent)
                    {
                        viewFrom = (IAtom) atomsInOrderOfSmiles.get(i - 1);
                    }
                }
            }
            boolean afterThisAtom = false;
            IAtom viewTo = null;
            for (int i = 0; i < currentChain.size(); i++)
            {
                if (afterThisAtom && currentChain.get(i) instanceof IAtom)
                {
                    viewTo = (IAtom) currentChain.get(i);
                    break;
                }
                if (afterThisAtom && currentChain.get(i) instanceof List)
                {
                    viewTo = (IAtom) ((List) currentChain.get(i)).get(0);
                    break;
                }
                if (atom == currentChain.get(i))
                {
                    afterThisAtom = true;
                }
            }

            try{
                if (BondTools.isCisTrans(viewFrom,atom,parent,viewTo,container))
                {
                    buffer.append('\\');
                } else
                {
                    buffer.append('/');
                }
            }catch(CDKException ex){
                //If the user wants atom double bond configuration, where there is none, we ignore this.
            }
        }

        List v = new Vector();
        Iterator it = getRingOpenings(atom, v).iterator();
        Iterator it2 = v.iterator();
        //logger.debug("in parseAtom() after checking for Ring openings");
        while (it.hasNext())
        {
            Integer integer = (Integer) it.next();
            IAtom a2=(IAtom) it2.next();
            IBond b = container.getBond(a2, atom);
            IBond.Order type = b.getOrder();
            if (!(useAromaticity && atom.getFlag(CDKConstants.ISAROMATIC) && a2.getFlag(CDKConstants.ISAROMATIC))){
                if (type == IBond.Order.DOUBLE) {
                    buffer.append("=");
                } else if (type == IBond.Order.TRIPLE) {
                    buffer.append("#");
                }
            }
            if (integer >= 10) buffer.append("%"+integer);
            else buffer.append(integer);
        }
        atomsInOrderOfSmiles.add(atom);
        //logger.debug("End of parseAtom()");
    }

    /**
     * Creates a string for the charge of atom <code>atom</code>
     * @param atom IAtom object
     * @return String. "+" if the value returned is +1, "-" if the value returned is -\
     */
    private String generateChargeString(IAtom atom){
        int charge = atom.getFormalCharge() == CDKConstants.UNSET ? 0 : atom.getFormalCharge();
        StringBuffer buffer = new StringBuffer(3);
        if(charge > 0){
            buffer.append("+");
            if (charge > 1)
                buffer.append(charge);
        } else if (charge < 0) {
            if (charge == -1)
                buffer.append('-');
            else
                buffer.append(charge);
        }
        return buffer.toString();
    }

    /**
     * Create a string containing the mass of the atom <code>atom</code>.
     * If the mass is the same as the major isotope an empty string returned;
     * @param atom IAtom object to create the mass
     * @return
     */
    private String generateMassString(IAtom atom){
        if(isotopeFactory == null)
            setupIsotopeFactory();
        if(atom instanceof IPseudoAtom){
            if (atom.getMassNumber() != null) return Integer.toString(atom.getMassNumber());
            else return "";
        }

        IIsotope majorIsotope = isotopeFactory.getMajorIsotope(atom.getSymbol());
        if (majorIsotope == null || majorIsotope.getMassNumber() == atom.getMassNumber())
            return "";
        else if (atom.getMassNumber() == null) return "";
        else return Integer.toString(atom.getMassNumber());
    }


    // REFACTORING : IsotopeFactory.getInstance(builder) deprecated.
    private void setupIsotopeFactory(){
        try {
            isotopeFactory = Isotopes.getInstance();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public AllRingsFinder getRingsFinder() {
        return ringsFinder;
    }

    /**
     * Sets the current AllRingsFinder instance
     * Use this if you want to customize the timeout for
     * the AllRingsFinder. AllRingsFinder is stopping its
     * quest to find all rings after a default of 5 seconds.
     *
     * @param ringsFinder value to assing to ringFinder
     */
    public void setRingsFinder(AllRingsFinder ringsFinder) {
        this.ringsFinder = ringsFinder;
    }

    /**
     * Indicates whether output should be an aromatic SMILES.
     * @param useAromaticityFlag if false only SP2-hybridized atoms will be lower case (default),
     *      * true=SP2 or aromaticity trigger lower case
     */
    public void setUseAromaticityFlag(boolean useAromaticityFlag){
        this.useAromaticityFlag = useAromaticityFlag;
    }









}
