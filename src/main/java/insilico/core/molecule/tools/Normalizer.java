package insilico.core.molecule.tools;

import insilico.core.exception.InitFailureException;
import insilico.core.exception.MoleculeConversionException;
import insilico.core.molecule.InsilicoMoleculeMessages;
import insilico.core.molecule.matrix.ConnectionAugMatrix;
import insilico.core.tools.utils.MoleculeUtilities;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Normalize a CDK Molecule object following the insilico defaults on
 * aromaticiy, presence of hydrogen atoms, mesomers representation etc.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class Normalizer {

    Logger logger = LoggerFactory.getLogger(Normalizer.class);

    private final AtomicNumber ZFinder;

    public Normalizer() throws InitFailureException{
        ZFinder = new AtomicNumber();
    }

    /**
     * Normalize a CDK Molecule object following the insilico defaults.<p>
     * Action performed: sets proper atom types, removes explicit hydrogen
     * atoms, adds lacking (implicit) hydrogens, sets aromaticity, normalizes
     * resonance/mesomeric forms.
     * Throws exception if it is unable to normalize the molecule. If no
     * exception is thrown, some warning messages could anyway be returned in the
     * Warnings object.<p>
     * The molecule returned is a clone of the input molecule (the original
     * molecule is not modified).
     *
     * @param mol CDK Molecule to be normalized
     * @param warnings List of warnings raised during normalization
     * @return The normalized CDK Molecule
     * @throws MoleculeConversionException
     */
    public IAtomContainer ConfigureMolecule(IAtomContainer mol, InsilicoMoleculeMessages warnings)
            throws MoleculeConversionException {

        final String ERR_HEADER = "Molecule Normalization: ";

        IAtomContainer newMol = null;

        try {
            newMol =  mol.clone();
        } catch (CloneNotSupportedException ex){
            String err = ERR_HEADER + "unable to clone molecule";
            throw new MoleculeConversionException(ex);

        }

        // Configures Atoms
        for (IAtom atom: newMol.atoms())
            ConfigureAtom(newMol, atom);

        // Removes explicit Hydrogens and set them as implicit
        try{
            newMol = Manipulator.RemoveHydrogens(newMol);
        } catch (CDKException ex) {
            String err = ERR_HEADER + "unable to make hydrogens implicit";
            throw new MoleculeConversionException(err);
        }

        // Adds implicits H where they are lacking
        try {
            if (Manipulator.AddLackingImplicitHydrogens(newMol) > 0)
                warnings.AddMessage("Some lacking hydrogen atoms ahve been added to original structures");
        } catch (Exception ex){
            String err = ERR_HEADER + "unable to add lacking H atoms";
            throw new MoleculeConversionException(err);
        }


        // METHOD DEPRECATED - Find the smallest set of smallest rings (SSSR)
        // Old Method Commented
        IRingSet singleRings = null;
        try {
            Cycles cycles = Cycles.sssr(newMol);
            singleRings = cycles.toRingSet();
//            singleRings = new SSSRFinder(NewMol).findSSSR();
        }
        catch (Exception e) {
            String err = ERR_HEADER + "unable to recognize rings";throw new MoleculeConversionException(err);
        }

        // Aromaticity
        // Tries to remove some wrongly set aromaticity
        try {

            Iterator<IAtomContainer> ringsIterator = singleRings.atomContainers().iterator();
            while(ringsIterator.hasNext()){
                IRing ring = (IRing) ringsIterator.next();

                // Fixes 5-membered all carbon rings wrongly set as aromatic
                // This often comes when they are fused between to other aromatic cycles
                if(ring.getAtomCount() == 5){
                    if(MoleculeUtilities.IsRingAromatic(ring)){
                        boolean heteroCycle = false;
                        for (IAtom atCycle: ring.atoms())
                            if(!atCycle.getSymbol().equalsIgnoreCase("C")){
                                heteroCycle = true;
                                break;
                            }
                        // Is an aromatic 5-membered all carbon ring
                        // Stores atoms and bonds to be unset
                        // only those that don't belong to other fused aromatic rings
                        if (!heteroCycle){
                            ArrayList<IAtom> markedAtoms = new ArrayList<>();
                            for (IAtom atCycle : ring.atoms()){
                                IRingSet fusedRings = singleRings.getRings(atCycle);
                                if (fusedRings.getAtomContainerCount() == 1){
                                    markedAtoms.add(atCycle);
                                } else {
                                    int aromaticRings = 0;
                                    Iterator<IAtomContainer> RIterator = fusedRings.atomContainers().iterator();
                                    while (RIterator.hasNext()){
                                        IRing r = (IRing) RIterator.next();
                                        if (MoleculeUtilities.IsRingAromatic(r))
                                            aromaticRings++;
                                    }
                                    if (aromaticRings == 1)
                                        markedAtoms.add(atCycle);
                                }
                            }

                            ArrayList<IBond> markedBonds = new ArrayList<>();
                            for (IBond bndCycle : ring.bonds()){
                                IRingSet fusedRings = singleRings.getRings(bndCycle);
                                if (fusedRings.getAtomContainerCount() == 1){
                                    markedBonds.add(bndCycle);
                                } else {
                                    int aromaticRings = 0;
                                    Iterator<IAtomContainer> RIterator = fusedRings.atomContainers().iterator();
                                    while (RIterator.hasNext()){
                                        IRing r = (IRing) RIterator.next();
                                        if (MoleculeUtilities.IsRingAromatic(r))
                                            aromaticRings++;
                                    }
                                    if (aromaticRings == 1)
                                        markedBonds.add(bndCycle);
                                }
                            }

                            // Clears marked atoms and relative bonds
                            if ( (!markedAtoms.isEmpty()) || (!markedBonds.isEmpty())){
                                logger.debug("Aromaticity has been removed from a five membered carbon cycle");
                                warnings.AddMessage("Some five membered carbon ccles were wrongly set as aromatic and have been changed");
                                for (IAtom mAt : markedAtoms){
                                    mAt.setFlag(CDKConstants.ISAROMATIC, false);
                                }
                                for (IBond mBnd: markedBonds){
                                    mBnd.setFlag(CDKConstants.ISAROMATIC, false);
                                }
                            }
                        }
                    }

                    // Fixes 4-membered all carbon rings wrongly set as aromatic
                    if (ring.getAtomCount() == 4)
                        if (MoleculeUtilities.IsRingAromatic(ring)){
                            for (IBond bnd : ring.bonds()) {
                                IRingSet bondRings = singleRings.getRings(bnd);
                                if (bondRings.getAtomContainerCount() == 1)
                                    bnd.setFlag(CDKConstants.ISAROMATIC, false);
                            }
                        }
                }

            }

            // Fixes bridge bonds between two aromatic rings - starting from
            // a SMILES, such bonds are wrongly set as aromatic

            for (IBond bond : newMol.bonds()){
                if (bond.getFlag(CDKConstants.ISAROMATIC)) {
                    IRingSet bondRings = singleRings.getRings(bond);
                    if (bondRings.getAtomContainerCount() == 0)
                        bond.setFlag(CDKConstants.ISAROMATIC, false);
                    else {
                        boolean inOneOrMoreAromaticRing = false;
                        Iterator<IAtomContainer> RIterator = bondRings.atomContainers().iterator();
                        while (RIterator.hasNext()){
                            IRing r = (IRing) RIterator.next();
                            if (MoleculeUtilities.IsRingAromatic(r)){
                                inOneOrMoreAromaticRing = true;
                                break;
                            }
                        }
                        if(!inOneOrMoreAromaticRing)
                            bond.setFlag(CDKConstants.ISAROMATIC, false);

                    }
                }
            }
        } catch (Exception e) {
            String err = ERR_HEADER + "unable to remove improper fixed aromaticity";
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }

        // Fixes aromatic rings derived from original molecule format that
        // should not be considered aromatic
        FixAromaticRings(newMol, warnings);

        // some rings derived from original molecule could have
        // not all atoms set as aromatic even if they are in an aromatic ring
        try {
            Iterator<IAtomContainer> ArRingsIterator = singleRings.atomContainers().iterator();
            while (ArRingsIterator.hasNext()) {
                IRing ring = (IRing) ArRingsIterator.next();
                if (MoleculeUtilities.IsRingAromatic(ring)) {
                    for (IAtom a : ring.atoms())
                        a.setFlag(CDKConstants.ISAROMATIC, true);
                }
            }
        } catch (Exception e) {
            String err = ERR_HEADER + "unable to set aromaticity";
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }

        // Calculates aromaticity
        try {

            boolean NewAromaticRingFound = false;
            do {
                // Aromaticity can be checked more than one time, to be sure
                // that some fused ring are marked as aromatic, as their aromaticity
                // could depend on previously calculated aromaticity of their
                // fused rings.
                NewAromaticRingFound = false;
                Iterator<IAtomContainer> ArRingsIterator = singleRings.atomContainers().iterator();
                while (ArRingsIterator.hasNext()) {
                    IRing ring = (IRing) ArRingsIterator.next();
                    if (!(MoleculeUtilities.IsRingAromatic(ring)))
                        if (Aromaticity.ConfigureRing(ring, singleRings, newMol))
                            NewAromaticRingFound = true;
                }
            } while (NewAromaticRingFound);

        } catch (Exception e) {
            String err = ERR_HEADER + "unable to calculate aromaticity";
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }

        // Normalizes resonance forms
        newMol = CheckResonance(newMol);


        // Return normalized molecule
        return newMol;
    }


    /**
     * Set proper atom types
     * @param mol
     * @param atom
     */
    private void ConfigureAtom(IAtomContainer mol, IAtom atom) throws MoleculeConversionException {

        final String ERR_HEADER = "Atom Configurator:";

        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(mol.getBuilder());
        try {
            if (!(atom instanceof IPseudoAtom)) {
                IAtomType atomType = matcher.findMatchingAtomType(mol, atom);
                if (atomType != null){
                    atom.setAtomTypeName(atomType.getAtomTypeName());
                    atom.setSymbol(atomType.getSymbol());
                    atom.setMaxBondOrder(atomType.getMaxBondOrder());
                    atom.setBondOrderSum(atomType.getBondOrderSum());
                    atom.setCovalentRadius(atomType.getCovalentRadius());
                    atom.setValency(atomType.getValency());
                    atom.setFormalCharge(atomType.getFormalCharge());
                    atom.setHybridization(atomType.getHybridization());
                    atom.setFormalNeighbourCount(atomType.getFormalNeighbourCount());
                    atom.setFlag(CDKConstants.IS_HYDROGENBOND_ACCEPTOR, atomType.getFlag(CDKConstants.IS_HYDROGENBOND_ACCEPTOR));
                    atom.setFlag(CDKConstants.IS_HYDROGENBOND_DONOR, atomType.getFlag(CDKConstants.IS_HYDROGENBOND_DONOR));
                    Object constant = atomType.getProperty(CDKConstants.CHEMICAL_GROUP_CONSTANT);
                    if (constant != null){
                        atom.setProperty(CDKConstants.CHEMICAL_GROUP_CONSTANT, constant);
                    }

                    Object color = atomType.getProperty("org.openscience.cdk.renderer.color");
                    if (color!=null){
                        atom.setProperty("org.openscience.cdk.renderer.color", color);
                    }
                    System.out.println(atom.getSymbol());
                    atom.setAtomicNumber(ZFinder.GetAtomicNumber(atom.getSymbol()));
                    if(atomType.getExactMass() != CDKConstants.UNSET)
                        atom.setExactMass(atomType.getExactMass());
                }
            }

        } catch (CDKException ex){
            String err = ERR_HEADER + "unable to configure atom n° " + mol.indexOf(atom);
            if ((atom.getSymbol()!=null)&&(!atom.getSymbol().equalsIgnoreCase("")))
                err += " (" + atom.getSymbol() + ")";
            throw new MoleculeConversionException(err);
        }
    }

    /**
     * Check all aromatic rings, remove aromaticity where it has been wrongly set and tries to add hydronges.
     * To be used with molecules where aromaticity has been explicitly set in the orignial format, e.g SMILES)
     * @param mol AtomContainer (Molecule) object
     * @param messages
     * @throws MoleculeConversionException
     */
    private void FixAromaticRings (IAtomContainer mol, InsilicoMoleculeMessages messages) throws MoleculeConversionException {

        final String ERR_HEADER = "Aromaticity Normalizaztion: ";

        Cycles cycles = Cycles.sssr(mol);
        IRingSet singleRings = cycles.toRingSet();
        double[][] connMatrix = ConnectionAugMatrix.getMatrix(mol);

        boolean conversionError = false;
        boolean ringFixed = false;
        boolean isInMultipleAtomRings[] = new boolean[mol.getAtomCount()];

        // check for atoms shared in multiple aromatic rings
        for (int i=0; i< mol.getAtomCount(); i++){
            isInMultipleAtomRings[i] = false;
            IAtom at = mol.getAtom(i);
            IRingSet curRings = singleRings.getRings(at);
            int ringCount = curRings.getAtomContainerCount();
            if (ringCount > 1) {
                int atomCount = 0;
                for (int r=0; r < curRings.getAtomContainerCount(); r++){
                    IRing ring = (IRing) curRings.getAtomContainer(r);
                    if (MoleculeUtilities.IsRingAromatic(ring))
                        atomCount++;
                }
                if (atomCount > 1)
                    isInMultipleAtomRings[i] = true;
            }
        }

        Iterator<IAtomContainer> ringsIterator = singleRings.atomContainers().iterator();
        while (ringsIterator.hasNext()){

            IRing ring = (IRing) ringsIterator.next();
            if(!MoleculeUtilities.IsRingAromatic(ring))
                continue;

            boolean aromCorrect = true;
            int PIelectrons = 0;

            // heteroatoms with uncertain H count
            ArrayList<IAtom> uncertainAtoms = new ArrayList<>();
            // heteroatoms shared by more aromatic rings
            ArrayList<IAtom> sharedAtoms = new ArrayList<>();

            for (IAtom at: ring.atoms()){

                int atNum = mol.indexOf(at);
                int Z = (int) connMatrix[atNum][atNum];
                int H;
                try {
                    H = at.getImplicitHydrogenCount();
                } catch (Exception e) {
                    H = 0;
                }
                boolean ExoDoubleElNeg = false;

                double bufBondOrd = 0;
                int vertexDegree = 0;
                for (int j = 0; j < mol.getAtomCount(); j++)
                    if (connMatrix[atNum][j] > 0)
                        if ( j != atNum) {
                            bufBondOrd += connMatrix[atNum][j];
                            vertexDegree++;

                            if (!ring.contains(mol.getAtom(j)))
                                if(connMatrix[atNum][j] == 2)
                                    if((connMatrix[j][j] == 7) || (connMatrix[j][j] == 8) || (connMatrix[j][j] == 15) || (connMatrix[j][j] == 16))
                                        ExoDoubleElNeg = true;
                        }

                int bondOrder = (int) bufBondOrd;
                int formalCharge = 0;
                try {
                    formalCharge = at.getFormalCharge();
                } catch (Exception e){
                    System.out.println(e.toString());
                }

                // For Atom C
                if (Z==6){

                    if ((ExoDoubleElNeg) || (formalCharge != 0)) {
                        aromCorrect = false;
                        break;
                    }

                    // ..C.. is always -C= so 1 H is added
                    if (vertexDegree == 2)
                        H = 1;
                    else if (vertexDegree > 2)
                        H = 0;

                    PIelectrons++;
                }

                // O
                else if (Z==8) {
                    // Oxygen in aromatic is always -O- with no H
                    if (formalCharge == 0) {
                        PIelectrons += 2;
                    } else {
                        PIelectrons++;
                    }
                    H = 0;
                }

                // N or P
                if ((Z==7)||(Z==15)) {

                    // check for P with valence 5
                    if ( (Z==15) && (vertexDegree > 3)) {
                        aromCorrect = false;
                        break;
                    }

                    if (vertexDegree == 2) {
                        if (H == 1) {
                            PIelectrons += 2;
                        } else {
                            H = 0;
                            uncertainAtoms.add(at);
                            PIelectrons++; // here adds just one PI e
                        }
                    }
                    else {
                        if (formalCharge == 1) {
                            H = 0;
                            PIelectrons += 1;
                        } else {
                            H = 0;
                            if (isInMultipleAtomRings[atNum]) {
                                sharedAtoms.add(at);
                                PIelectrons++; // here adds just one PI e
                            } else
                                PIelectrons +=2;
                        }
                    }
                }

                // S
                else if (Z==16) {
                    // If is ..S.. with vertex degree 2 (equal to a
                    // tot bond order of 3 i.e. 1.5 + 1.5), no H added
                    if (vertexDegree == 2) {
                        H = 0;
                        if (formalCharge == 0)
                            PIelectrons += 2;
                        else
                            PIelectrons++;
                    }
                    // else ..S.. is treated as -S=
                    else if (bondOrder <= 4) {
                        H = 4 - bondOrder;
                        PIelectrons++;
                    }
                    else if (bondOrder <= 6) {
                        H = 6 - bondOrder;
                        PIelectrons++;
                    }
                }

                at.setImplicitHydrogenCount(H);
            }

            // CHECKING THE RESULTING PI ELECTRON - PI BOND
            if ( (uncertainAtoms.size() > 0) || (sharedAtoms.size() > 0)) {

                // Calculates the supposed correct number of
                // PI electrons that the ring should have to be aromatic
                int aromPiElectrons = 0;
                for (int i = 0; i < 5; i++) {
                    aromPiElectrons = (4 * i) +2;
                    if (aromPiElectrons >= PIelectrons)
                        break;
                }

                // Calculates for how many atoms could be assigned
                // more than 1 PI electron
                int MorePIelectrons = (aromPiElectrons - PIelectrons);

                // First tries to assign atoms shared by multiple aromatic rings
                for (int i=0; i<sharedAtoms.size(); i++) {
                    if (MorePIelectrons > 0) {
                        MorePIelectrons--;
                        PIelectrons++;
                        // no H are assigned to heteroatom with connectivy > 2
                    }
                }

                // then tries uncertain atoms
                for (int i=0; i<uncertainAtoms.size(); i++) {
                    if (MorePIelectrons > 0) {
                        MorePIelectrons--;
                        PIelectrons++;
                        uncertainAtoms.get(i).setImplicitHydrogenCount(1);
                    } else {
                        uncertainAtoms.get(i).setImplicitHydrogenCount(0);
                    }
                }

                if (MorePIelectrons > 0)
                    aromCorrect = false;

            }

            // final check for hueckel rule

            if (((PIelectrons-2) % 4)  != 0)
                aromCorrect = false;

            // Tries to remove aromaticity if needed
            if (!aromCorrect) {
                logger.debug("Wrong no. of electrons for Hueckel rule - PI electrons = " + PIelectrons);
                KekuleForm Kekule = new KekuleForm();
                try {
                    Kekule.Convert(mol, ring);
                    ringFixed = true;
                } catch (MoleculeConversionException e) {
                    System.out.println(e.toString());
                    conversionError = true;

                }
            }

        }


        if (conversionError) {
            String err = ERR_HEADER + "some aromatic rings can not be not correctly recognized";
            logger.error(err);
            throw new MoleculeConversionException(err);
        }

        if (ringFixed)
            messages.AddMessage("Some aromatic rings are not correctly recognized, and transformed to kekule form.");
    }

    /**
     *
     * @param Mol CDK Atom Container object
     * @return CDK Atom Container object
     */
    private IAtomContainer CheckResonance(IAtomContainer Mol) {

        double[][] ConnMatrix = ConnectionAugMatrix.getMatrix(Mol);

        int idxN;
        int idxDoubleO, idxDoubleN, idxTripleN, idxDoubleC, idxTripleC;

        for (int i=0; i<Mol.getAtomCount(); i++) {

            // Checks for N-based groups

            if (ConnMatrix[i][i] == 7) {

                idxN = i;
                idxDoubleO = -1; idxDoubleN = -1;
                idxTripleN = -1; idxDoubleC = -1; idxTripleC = -1;
                int VD=0, Odbl=0, Ndbl=0, Ntriple=0;
                int Cdbl=0, Ctriple=0;

                for (int j=0; j<Mol.getAtomCount(); j++) {
                    if (j == i)
                        continue;
                    if (ConnMatrix[i][j] != 0) {
                        VD++;
                        // Oxygen
                        if (ConnMatrix[j][j] == 8) {
                            if (ConnMatrix[i][j] == 2) {
                                Odbl++;
                                idxDoubleO = j;
                            }
                        }

                        // Carbon
                        if (ConnMatrix[j][j] == 6) {
                            if (ConnMatrix[i][j] == 2) {
                                Cdbl++;
                                idxDoubleC = j;
                            }
                            if (ConnMatrix[i][j] == 3) {
                                Ctriple++;
                                idxTripleC = j;
                            }
                        }

                        // Nitrogen
                        if (ConnMatrix[j][j] == 7) {
                            if (ConnMatrix[i][j] == 2) {
                                Ndbl++;
                                idxDoubleN = j;
                            }
                            if (ConnMatrix[i][j] == 3) {
                                Ntriple++;
                                idxTripleN = j;
                            }
                        }
                    }
                }


                // NO2 in O=N=O form, to be changed into O=[N+][O-]
                if ((Odbl==2)) {
                    int NCharge;
                    try {
                        NCharge = Mol.getAtom(idxN).getFormalCharge();
                    } catch (Exception e) {
                        NCharge = 0;
                    }

                    Mol.getAtom(idxN).setFormalCharge(NCharge + 1);
                    Mol.getAtom(idxDoubleO).setFormalCharge(-1);
                    Mol.getBond(Mol.getAtom(idxN), Mol.getAtom(idxDoubleO)).setOrder(IBond.Order.SINGLE);

                    logger.debug("Normalized a NO2 group");
                    continue;
                }

                // N=N#N, to be changed into N=[N+]=[N-]
                // C=N#N, to be changed into C=[N+]=[N-]
                if ( ((Ndbl==1)&&(Ntriple==1)) || ((Cdbl==1)&&(Ntriple==1))) {
                    int NCharge, NTripleCharge;
                    try {
                        NCharge = Mol.getAtom(idxN).getFormalCharge();
                    } catch (Exception e) {
                        NCharge = 0;
                    }
                    try {
                        NTripleCharge = Mol.getAtom(idxTripleN).getFormalCharge();
                    } catch (Exception e) {
                        NTripleCharge = 0;
                    }

                    Mol.getAtom(idxN).setFormalCharge(NCharge + 1);
                    Mol.getAtom(idxTripleN).setFormalCharge(NTripleCharge -1);
                    Mol.getBond(Mol.getAtom(idxN), Mol.getAtom(idxTripleN)).setOrder(IBond.Order.DOUBLE);

                    logger.debug("Normalized a N=N#N / C=N#N group");
                    continue;
                }

                // C#N=O, to be changed into C#[N+][O-]
                // C=N=O, to be changed into C=[N+][O-]
                // N=N=O, to be changed into N=[N+][O-]
                if ( ((Ctriple==1)&&(Odbl==1)) || ((Cdbl==1)&&(Odbl==1)) ||
                        ((Ndbl==1)&&(Odbl==1)) ){
                    int NCharge;
                    try {
                        NCharge = Mol.getAtom(idxN).getFormalCharge();
                    } catch (Exception e) {
                        NCharge = 0;
                    }

                    Mol.getAtom(idxN).setFormalCharge(NCharge + 1);
                    Mol.getAtom(idxDoubleO).setFormalCharge(-1);
                    Mol.getBond(Mol.getAtom(idxN), Mol.getAtom(idxDoubleO)).setOrder(IBond.Order.SINGLE);

                    logger.debug("Normalized a C#N=O / C=N=O / N=N=O group");
                    // continue;   not needed, last block
                }


            }
        }

        return Mol;
    }





}

