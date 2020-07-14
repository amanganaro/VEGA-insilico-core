package insilico.core.molecule.tools;

import insilico.core.exception.InitFailureException;
import insilico.core.exception.MoleculeConversionException;
import insilico.core.molecule.InsilicoMoleculeMessages;
import insilico.core.molecule.matrix.ConnectionAugMatrix;
import insilico.core.tools.utils.MoleculeUtilities;
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

    /**
     * Constructor. Throws exception if it is not able to init its internal
     * {@link AtomicNumber} object.
     *
     * @throws InitFailureException
     */
    public Normalizer() throws InitFailureException {
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
     * @param molecule CDK Molecule to be normalized
     * @param Warnings List of warnings raised during normalization
     * @return The normalized CDK Molecule
     * @throws MoleculeConversionException
     */
    public IAtomContainer ConfigureMolecule (IAtomContainer molecule, InsilicoMoleculeMessages Warnings)
            throws MoleculeConversionException {

        final String ERR_HEADER = "Molecule normalization: ";

        IAtomContainer newMol = null;

        try {
            newMol = molecule.clone();
        } catch (CloneNotSupportedException e) {
            String err = ERR_HEADER + "unable to clone molecule";
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }


        //// Configures atoms ////
        for (IAtom atom : newMol.atoms())
            ConfigureAtom(newMol, atom);


        //// Hydrogens ////

        // Removes explicit H and sets them as implicit
        try {
            newMol = Manipulator.RemoveHydrogens(newMol);
        } catch (CDKException e) {
            String err = ERR_HEADER + "unable to make hydrogens implicit";
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }

        // Adds (implict) hydrogens where they are lacking
        try {
            if (Manipulator.AddLackingImplicitHydrogens(newMol) > 0)
                Warnings.AddMessage("Some lacking hydrogen atoms have been added to original structure");
        } catch (CDKException e) {
            String err = ERR_HEADER + "unable to add lacking hydrogen atoms";
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }


        IRingSet singleRings = null;
        try {
            Cycles cycles = Cycles.sssr(molecule);
            singleRings =  cycles.toRingSet();
        } catch (Exception e) {
            String err = ERR_HEADER + "unable to recognize rings";
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }


        //// Aromaticity ////

        // tries to remove some wrongly set aromaticity (expecially needed if
        // molecule comes from SMILES format)
        try {

            Iterator<IAtomContainer> RingsIterator = singleRings.atomContainers().iterator();

            while (RingsIterator.hasNext()) {
                IRing ring = (IRing) RingsIterator.next();

                // Fixes 5-membered all carbon rings wrongly set as aromatic
                // this often comes when they are fused between to other
                // aromatic cycles
                if (ring.getAtomCount() == 5)
                    if (MoleculeUtilities.IsRingAromatic(ring)) {
                        boolean HeteroCycle = false;
                        for (IAtom atCycle : ring.atoms())
                            if (!atCycle.getSymbol().equalsIgnoreCase("C")) {
                                HeteroCycle = true;
                                break;
                            }
                        if (!HeteroCycle) {
                            // Is an aromatic 5-membered all carbon ring

                            // Stores atoms and bonds to be unset
                            // only those that don't belong to other fused aromatic rings

                            ArrayList<IAtom> MarkedAtoms = new ArrayList<>();
                            for (IAtom atCycle : ring.atoms())  {
                                IRingSet FusedRings = singleRings.getRings(atCycle);
                                if (FusedRings.getAtomContainerCount() == 1) {
                                    MarkedAtoms.add(atCycle);
                                } else {
                                    int AromaticRings = 0;
                                    Iterator<IAtomContainer> RIterator = FusedRings.atomContainers().iterator();
                                    while (RIterator.hasNext()) {
                                        IRing r = (IRing) RIterator.next();
                                        if (MoleculeUtilities.IsRingAromatic(r))
                                            AromaticRings++;
                                    }
                                    if (AromaticRings == 1)
                                        MarkedAtoms.add(atCycle);
                                }
                            }

                            ArrayList<IBond> MarkedBonds = new ArrayList<>();
                            for (IBond bndCycle : ring.bonds())  {
                                IRingSet FusedRings = singleRings.getRings(bndCycle);
                                if (FusedRings.getAtomContainerCount() == 1) {
                                    MarkedBonds.add(bndCycle);
                                } else {
                                    int AromaticRings = 0;
                                    Iterator<IAtomContainer> RIterator = FusedRings.atomContainers().iterator();
                                    while (RIterator.hasNext()) {
                                        IRing r = (IRing) RIterator.next();
                                        if (MoleculeUtilities.IsRingAromatic(r))
                                            AromaticRings++;
                                    }
                                    if (AromaticRings == 1)
                                        MarkedBonds.add(bndCycle);
                                }
                            }

                            // Clears marked atoms and relative bonds
                            if ( (!MarkedAtoms.isEmpty()) || (!MarkedBonds.isEmpty()) ) {
                                logger.debug("Aromaticity has been removed from a five membered carbon cycle");
                                Warnings.AddMessage("Some five membered carbon cycles were wrongly set as aromatic and have been changed");
                                for (IAtom mAt : MarkedAtoms) {
                                    mAt.setFlag(CDKConstants.ISAROMATIC, false);
                                }
                                for (IBond mBnd : MarkedBonds) {
                                    mBnd.setFlag(CDKConstants.ISAROMATIC, false);
                                }
                            }
                        }
                    }

                // Fixes 4-membered all carbon rings wrongly set as aromatic
                if (ring.getAtomCount() == 4)
                    if (MoleculeUtilities.IsRingAromatic(ring)) {
                        for (IBond bnd : ring.bonds())  {
                            IRingSet BondRings = singleRings.getRings(bnd);
                            if (BondRings.getAtomContainerCount() == 1)
                                bnd.setFlag(CDKConstants.ISAROMATIC, false);
                        }
                    }

            }

            // Fixes bridge bonds between two aromatic rings - starting from
            // a SMILES, such bonds are wrongly set as aromatic
            for (IBond b : newMol.bonds()) {
                if (b.getFlag(CDKConstants.ISAROMATIC)) {
                    IRingSet BondRings = singleRings.getRings(b);
                    if (BondRings.getAtomContainerCount() == 0)
                        b.setFlag(CDKConstants.ISAROMATIC, false);
                    else {
                        boolean InOneOrMoreAromaticRing = false;
                        for (IAtomContainer atomContainer : BondRings.atomContainers()) {
                            IRing r = (IRing) atomContainer;
                            if (MoleculeUtilities.IsRingAromatic(r)) {
                                InOneOrMoreAromaticRing = true;
                                break;
                            }
                        }
                        if (!InOneOrMoreAromaticRing)
                            b.setFlag(CDKConstants.ISAROMATIC, false);
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

        FixAromaticRings(newMol, Warnings);


        // Final fixing: some rings derived from original molecule could have
        // not all atoms set as aromatic even if they are in an aromatic ring

        try {
            for (IAtomContainer atomContainer : singleRings.atomContainers()) {
                IRing ring = (IRing) atomContainer;
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
     * Check Resonance for given molecule
     * @param molecule
     * @return
     * @throws CDKException
     */
    private IAtomContainer CheckResonance(IAtomContainer molecule) {

        double[][] ConnMatrix = ConnectionAugMatrix.getMatrix(molecule);

        int idxN;
        int idxDoubleO, idxDoubleN, idxTripleN, idxDoubleC, idxTripleC;

        for (int i=0; i<molecule.getAtomCount(); i++) {

            // Checks for N-based groups

            if (ConnMatrix[i][i] == 7) {

                idxN = i;
                idxDoubleO = -1; idxDoubleN = -1;
                idxTripleN = -1; idxDoubleC = -1; idxTripleC = -1;
                int VD=0, Odbl=0, Ndbl=0, Ntriple=0;
                int Cdbl=0, Ctriple=0;

                for (int j=0; j<molecule.getAtomCount(); j++) {
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
                        NCharge = molecule.getAtom(idxN).getFormalCharge();
                    } catch (Exception e) {
                        NCharge = 0;
                    }

                    molecule.getAtom(idxN).setFormalCharge(NCharge + 1);
                    molecule.getAtom(idxDoubleO).setFormalCharge(-1);
                    molecule.getBond(molecule.getAtom(idxN), molecule.getAtom(idxDoubleO)).setOrder(IBond.Order.SINGLE);

                    logger.debug("Normalized a NO2 group");
                    continue;
                }

                // N=N#N, to be changed into N=[N+]=[N-]
                // C=N#N, to be changed into C=[N+]=[N-]
                if ( ((Ndbl==1)&&(Ntriple==1)) || ((Cdbl==1)&&(Ntriple==1))) {
                    int NCharge, NTripleCharge;
                    try {
                        NCharge = molecule.getAtom(idxN).getFormalCharge();
                    } catch (Exception e) {
                        NCharge = 0;
                    }
                    try {
                        NTripleCharge = molecule.getAtom(idxTripleN).getFormalCharge();
                    } catch (Exception e) {
                        NTripleCharge = 0;
                    }

                    molecule.getAtom(idxN).setFormalCharge(NCharge + 1);
                    molecule.getAtom(idxTripleN).setFormalCharge(NTripleCharge -1);
                    molecule.getBond(molecule.getAtom(idxN), molecule.getAtom(idxTripleN)).setOrder(IBond.Order.DOUBLE);

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
                        NCharge = molecule.getAtom(idxN).getFormalCharge();
                    } catch (Exception e) {
                        NCharge = 0;
                    }

                    molecule.getAtom(idxN).setFormalCharge(NCharge + 1);
                    molecule.getAtom(idxDoubleO).setFormalCharge(-1);
                    molecule.getBond(molecule.getAtom(idxN), molecule.getAtom(idxDoubleO)).setOrder(IBond.Order.SINGLE);

                    logger.debug("Normalized a C#N=O / C=N=O / N=N=O group");
                    // continue;   not needed, last block
                }


            }
        }

        return molecule;
    }

    /**
     *
     * @param molecule
     * @param atom
     * @throws MoleculeConversionException
     */
    private void ConfigureAtom(IAtomContainer molecule, IAtom atom)
            throws MoleculeConversionException {

        final String ERR_HEADER = "Atom Configurator: ";

        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(molecule.getBuilder());
        try {
            if (!(atom instanceof IPseudoAtom)) {
                IAtomType atomType = matcher.findMatchingAtomType(molecule, atom);
                if (atomType != null) {
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
                    if (constant != null) {
                        atom.setProperty(CDKConstants.CHEMICAL_GROUP_CONSTANT, constant);
                    }

                    Object color = atomType.getProperty("org.openscience.cdk.renderer.color");
                    if (color != null) {
                        atom.setProperty("org.openscience.cdk.renderer.color", color);
                    }
                    //if (atomType.getAtomicNumber() != CDKConstants.UNSET) atom.setAtomicNumber(atomType.getAtomicNumber());
                    atom.setAtomicNumber(ZFinder.GetAtomicNumber(atom.getSymbol()));
                    if (atomType.getExactMass() != CDKConstants.UNSET) atom.setExactMass(atomType.getExactMass());
                }
            }
        } catch (CDKException e) {

            String err = ERR_HEADER + "unable to configure atom no. " + molecule.indexOf(atom);
            if ((atom.getSymbol()!=null)&&(!atom.getSymbol().equalsIgnoreCase("")))
                err += " (" + atom.getSymbol() + ")";
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }

    }


    /**
     * Checks all aromatic rings, and remove aromaticity where it has been
     * wrongly set (to be used with molecules where aromaticity has been
     * explicitly set in the original format, e.g. SMILES) and tries
     * to add hydrogens
     *
     * @param molecule
     */
    private void FixAromaticRings (IAtomContainer molecule, InsilicoMoleculeMessages Warnings)
            throws MoleculeConversionException {

        final String ERR_HEADER = "Aromaticity normalization: ";

        Cycles cycles = Cycles.sssr(molecule);
        IRingSet singleRings =  cycles.toRingSet();

        double[][] ConnMatrix = ConnectionAugMatrix.getMatrix(molecule);

        boolean ConversionError = false;
        boolean RingFixed = false;
        boolean[] IsInMultipleAromRings = new boolean[molecule.getAtomCount()];


        // check for atoms shared in multiple aromatic rings
        for (int i=0; i<molecule.getAtomCount(); i++) {
            IsInMultipleAromRings[i] = false;
            IAtom at = molecule.getAtom(i);
            IRingSet curRings = singleRings.getRings(at);
            int ringCount = curRings.getAtomContainerCount();
            if (ringCount > 1) {
                int aromCount = 0;
                for (int r=0; r<curRings.getAtomContainerCount(); r++) {
                    IRing ring = (IRing) curRings.getAtomContainer(r);
                    if (MoleculeUtilities.IsRingAromatic(ring))
                        aromCount++;
                }
                if (aromCount > 1)
                    IsInMultipleAromRings[i] = true;
            }
        }

        for (IAtomContainer atomContainer : singleRings.atomContainers()) {

            IRing ring = (IRing) atomContainer;
            if (!MoleculeUtilities.IsRingAromatic(ring))
                continue;

            boolean AromCorrect = true;

            int PIelectrons = 0;
            ArrayList<IAtom> UncertainAtoms = new ArrayList<>(); // heteroatoms with uncertain H count
            ArrayList<IAtom> SharedAtoms = new ArrayList<>(); // heteroatoms shared by more aromatic rings

            for (IAtom at : ring.atoms()) {

                int atNum = molecule.indexOf(at);
                int Z = (int) ConnMatrix[atNum][atNum];
                int H;
                try {
                    H = at.getImplicitHydrogenCount();
                } catch (Exception e) {
                    H = 0;
                }
                boolean ExoDoubleElNeg = false;

                double bufBondOrd = 0;
                int VertexDegree = 0;
                for (int j = 0; j < molecule.getAtomCount(); j++)
                    if (ConnMatrix[atNum][j] > 0)
                        if (j != atNum) {
                            bufBondOrd += ConnMatrix[atNum][j];
                            VertexDegree++;

                            // check for exocyclic double bonds to el. neg.
                            if (!ring.contains(molecule.getAtom(j)))
                                if (ConnMatrix[atNum][j] == 2)
                                    if ((ConnMatrix[j][j] == 7) ||
                                            (ConnMatrix[j][j] == 8) ||
                                            (ConnMatrix[j][j] == 15) ||
                                            (ConnMatrix[j][j] == 16))
                                        ExoDoubleElNeg = true;
                        }
                int BondOrder = (int) bufBondOrd;

                int FormalCharge = 0;
                try {
                    FormalCharge = at.getFormalCharge();
                } catch (Exception e) {
                }


                // C atom
                if (Z == 6) {

                    if ((ExoDoubleElNeg) || (FormalCharge != 0)) {
                        AromCorrect = false;
                        break;
                    }

                    // ..C.. is always -C= so 1 H is added
                    if (VertexDegree == 2)
                        H = 1;
                    else if (VertexDegree > 2)
                        H = 0;

                    PIelectrons++;
                }

                // O
                else if (Z == 8) {
                    // Oxygen in aromatic is always -O- with no H
                    if (FormalCharge == 0) {
                        PIelectrons += 2;
                    } else {
                        PIelectrons++;
                    }
                    H = 0;
                }

                // N or P
                if ((Z == 7) || (Z == 15)) {

                    // check for P with valence 5
                    if ((Z == 15) && (VertexDegree > 3)) {
                        AromCorrect = false;
                        break;
                    }

                    if (VertexDegree == 2) {
                        if (H == 1) {
                            PIelectrons += 2;
                        } else {
                            H = 0;
                            UncertainAtoms.add(at);
                            PIelectrons++; // here adds just one PI e
                        }
                    } else {
                        if (FormalCharge == 1) {
                            H = 0;
                            PIelectrons += 1;
                        } else {
                            H = 0;
                            if (IsInMultipleAromRings[atNum]) {
                                SharedAtoms.add(at);
                                PIelectrons++; // here adds just one PI e
                            } else
                                PIelectrons += 2;
                        }
                    }
                }

                // S
                else if (Z == 16) {
                    // If is ..S.. with vertex degree 2 (equal to a
                    // tot bond order of 3 i.e. 1.5 + 1.5), no H added
                    if (VertexDegree == 2) {
                        H = 0;
                        if (FormalCharge == 0)
                            PIelectrons += 2;
                        else
                            PIelectrons++;
                    }
                    // else ..S.. is treated as -S=
                    else if (BondOrder <= 4) {
                        H = 4 - BondOrder;
                        PIelectrons++;
                    } else if (BondOrder <= 6) {
                        H = 6 - BondOrder;
                        PIelectrons++;
                    }
                }

                at.setImplicitHydrogenCount(H);
            }


            // Checks the resulting PI electron

            if ((UncertainAtoms.size() > 0) || (SharedAtoms.size() > 0)) {

                // Calculates the supposed correct number of
                // PI electrons that the ring should have to be aromatic
                int AromPIelectrons = 0;
                for (int i = 0; i < 5; i++) {
                    AromPIelectrons = (4 * i) + 2;
                    if (AromPIelectrons >= PIelectrons)
                        break;
                }

                // Calculates for how many atoms could be assigned
                // more than 1 PI electron
                int MorePIelectrons = (AromPIelectrons - PIelectrons);

                // First tries to assign atoms shared by multiple aromatic rings
                for (int i = 0; i < SharedAtoms.size(); i++) {
                    if (MorePIelectrons > 0) {
                        MorePIelectrons--;
                        PIelectrons++;
                        // no H are assigned to heteroatom with connectivy > 2
                    }
                }

                // then tries uncertain atoms
                for (IAtom uncertainAtom : UncertainAtoms) {
                    if (MorePIelectrons > 0) {
                        MorePIelectrons--;
                        PIelectrons++;
                        uncertainAtom.setImplicitHydrogenCount(1);
                    } else {
                        uncertainAtom.setImplicitHydrogenCount(0);
                    }
                }

                if (MorePIelectrons > 0)
                    AromCorrect = false;
            }


            // final check for hueckel rule

            if (((PIelectrons - 2) % 4) != 0)
                AromCorrect = false;


            // Tries to remove aromaticity if needed
            if (!AromCorrect) {
                logger.debug("Wrong no. of electrons for Hueckel rule - PI electrons = " + PIelectrons);
                KekuleForm Kekule = new KekuleForm();
                try {
                    Kekule.Convert(molecule, ring);
                    RingFixed = true;
                } catch (MoleculeConversionException e) {

                    ConversionError = true;

                }
            }
        }

        if (ConversionError) {
            String err = ERR_HEADER + "some aromatic rings can not be not correctly recognized";
            logger.error(err);
            throw new MoleculeConversionException(err);
        }

        if (RingFixed)
            Warnings.AddMessage("Some aromatic rings are not correctly recognized, and transformed to kekule form.");

    }






}
