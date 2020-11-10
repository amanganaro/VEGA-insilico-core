package insilico.core.molecule.tools;

import insilico.core.descriptor.Descriptor;
import insilico.core.tools.utils.MoleculeUtilities;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.RingSet;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

/**
 *
 * @author Alberto
 */
@Slf4j
public class InsilicoMoleculeNormalization {

    // If true, nitro groups are normalized as in Dragon7: N(=O)=O
    // Otherwise, nitro groups are normalized as [N+]([O-])=O
    public static boolean DRAGON7_COMPLIANT_NORMALIZATION = false;

    // molecole con DISCORDANZE fra vecchio e nuovo
    //
    // O=C1C=C2C=CC=CC2(=NN1)
    // n1ccn(c1)C(OC(COc2ccccc2)C(C)(C)C)=S
    // C=1C=C(C2=CC(=CC=C(C=12)C)C(C)C)C
    // O=C(O)C(N)CCN2C=NC(NCC=C(C)C)=C1N=CN=C12
    // o2cc(c3C=C1C(=CC=C1C)C(=Cc23)C)C


    public static IAtomContainer Normalize(IAtomContainer mol) throws Exception {

        // Matches atom types
        TypeMatching(mol);

        // Hydrogen: explicit H are removed and set to implicit
        mol = AtomContainerManipulator.removeHydrogens(mol);

        // Check and normalize resonance forms (if done, check again types)
        if (Resonance(mol))
            TypeMatching(mol);

        // Set aromaticity using the CDK routine
        // Uses CDK algorithm for calculation of e donation
        // With Daylight algorithm it seems to have a looser definition of
        // aromaticity - not compliant with the one used in the previous
        // VEGA libraries
        // Uses Cycles.all() to find SSSR (CHECK: aggiungere un timeout?)
        ElectronDonation ElModel = ElectronDonation.cdk();
        CycleFinder RingFinder = Cycles.all();

        Aromaticity Arom = new Aromaticity(ElModel, RingFinder);
        Arom.apply(mol);

        // Finally check and fix some known problems with aromaticity
        FixAromaticityProblems(mol);

        // Marks rings (needed by SMARTS matching)
        Cycles.markRingAtomsAndBonds(mol);

        return(mol);
    }




    ///////// Internal methods /////////////////////////////////////////////////


    /**
     * Uses CDK Atom Type matcher to match atom types.
     * CHECK: cosa fare se non matcha un atomo (per ora eccezione?)
     *
     * @param Mol
     * @throws Exception
     */
    private static void TypeMatching(IAtomContainer Mol) throws Exception {

        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(DefaultChemObjectBuilder.getInstance());
        for (IAtom a : Mol.atoms()) {
            try {
                IAtomType type = matcher.findMatchingAtomType(Mol, a);
                AtomTypeManipulator.configure(a, type);
            } catch (Exception ex) {
                log.warn(ex.getMessage());
            }
        }

        // Some rings containing N atoms are wrongly seen as NOT aromatic, it happens when the N is seen
        // as part of a thioamide group (which is set in the AtomType property), example compound:
        // COC(=S)n1ccnc1
        // This atom type is hence removed and replaced with a normal PLANAR3 type
        for (IAtom a : Mol.atoms())
            if (a.getAtomTypeName().equalsIgnoreCase("N.thioamide")) {
                a.setAtomTypeName("N.planar3");
                a.setHybridization(IAtomType.Hybridization.PLANAR3);
            }

    }


    /**
     * Check for resonance forms and normalizes them.
     *
     * Check the following groups:
     * NO2 in O=N=O form, to be changed into O=[N+][O-]
     * N=N#N, to be changed into N=[N+]=[N-]
     * C=N#N, to be changed into C=[N+]=[N-]
     * C#N=O, to be changed into C#[N+][O-]
     * C=N=O, to be changed into C=[N+][O-]
     * N=N=O, to be changed into N=[N+][O-]
     *
     * @param Mol
     * @return
     */
    private static boolean Resonance(IAtomContainer Mol) {

        // To do : loggare normalizzazione se succedono

        boolean HasModified = false;

        // Calculate augmented connection matrix of the molecule
        int nSK = Mol.getAtomCount();
        double[][] ConnMatrix = new double[nSK][nSK];
        int[][] AdjMat = AdjacencyMatrix.getMatrix(Mol);

        for (int i=0; i<(nSK-1); i++) {
            for (int j=(i+1); j<nSK; j++) {
                double CellVal = 0;
                if (AdjMat[i][j]!=0)
                    CellVal = MoleculeUtilities.Bond2Double( Mol.getBond(Mol.getAtom(i), Mol.getAtom(j)) );
                ConnMatrix[i][j] = CellVal;
                ConnMatrix[j][i] = CellVal;
            }
        }
        for (int i=0; i<nSK; i++)
            ConnMatrix[i][i] = Mol.getAtom(i).getAtomicNumber();


        int idxN;
        int idxDoubleO, idxSingleOminus, idxTripleN;

        for (int i=0; i<Mol.getAtomCount(); i++) {

            // Checks for N-based groups

            if (ConnMatrix[i][i] == 7) {

                idxN = i;
                idxDoubleO = -1;
                idxSingleOminus = -1;
                idxTripleN = -1;
                int VD=0, Odbl=0, Ominusng=0, Ndbl=0, Ntriple=0;
                int Cdbl=0, Ctriple=0;

                int NCharge;
                try {
                    NCharge = Mol.getAtom(idxN).getFormalCharge();
                } catch (Exception e) {
                    NCharge = 0;
                }

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
                            if  ( (ConnMatrix[i][j] == 1) && (Mol.getAtom(j).getFormalCharge() == -1) ) {
                                Ominusng++;
                                idxSingleOminus = j;
                            }
                        }

                        // Carbon
                        if (ConnMatrix[j][j] == 6) {
                            if (ConnMatrix[i][j] == 2) {
                                Cdbl++;
                            }
                            if (ConnMatrix[i][j] == 3) {
                                Ctriple++;
                            }
                        }

                        // Nitrogen
                        if (ConnMatrix[j][j] == 7) {
                            if (ConnMatrix[i][j] == 2) {
                                Ndbl++;
                            }
                            if (ConnMatrix[i][j] == 3) {
                                Ntriple++;
                                idxTripleN = j;
                            }
                        }
                    }
                }


                if (DRAGON7_COMPLIANT_NORMALIZATION) {

                    // As in Dragon: NO2 in O=[N+][O-] form changed to O=N=O
                    if ((Odbl == 1) && (Ominusng == 1) && (NCharge > 0)) {
                        Mol.getAtom(idxN).setFormalCharge(NCharge - 1);
                        Mol.getAtom(idxSingleOminus).setFormalCharge(0);
                        Mol.getBond(Mol.getAtom(idxN), Mol.getAtom(idxSingleOminus)).setOrder(IBond.Order.DOUBLE);

                        HasModified = true;
                        log.info("Normalized a NO2 group to O=N=O form");
                        continue;
                    }

                } else {

                    // NO2 in O=N=O form, to be changed into O=[N+][O-]
                    if ((Odbl == 2)) {
                        Mol.getAtom(idxN).setFormalCharge(NCharge + 1);
                        Mol.getAtom(idxDoubleO).setFormalCharge(-1);
                        Mol.getBond(Mol.getAtom(idxN), Mol.getAtom(idxDoubleO)).setOrder(IBond.Order.SINGLE);

                        HasModified = true;
                        log.info("Normalized a NO2 group to O=[N+][O-] form");
                        continue;
                    }

                }

                // N=N#N, to be changed into N=[N+]=[N-]
                // C=N#N, to be changed into C=[N+]=[N-]
                if ( ((Ndbl==1)&&(Ntriple==1)) || ((Cdbl==1)&&(Ntriple==1))) {
                    int NTripleCharge;
                    try {
                        NTripleCharge = Mol.getAtom(idxTripleN).getFormalCharge();
                    } catch (Exception e) {
                        NTripleCharge = 0;
                    }

                    Mol.getAtom(idxN).setFormalCharge(NCharge + 1);
                    Mol.getAtom(idxTripleN).setFormalCharge(NTripleCharge -1);
                    Mol.getBond(Mol.getAtom(idxN), Mol.getAtom(idxTripleN)).setOrder(IBond.Order.DOUBLE);

                    HasModified = true;
                    log.info("Normalized a N=N#N / C=N#N group");
                    continue;
                }

                // C#N=O, to be changed into C#[N+][O-]
                // C=N=O, to be changed into C=[N+][O-]
                // N=N=O, to be changed into N=[N+][O-]
                if ( ((Ctriple==1)&&(Odbl==1)) || ((Cdbl==1)&&(Odbl==1)) ||
                        ((Ndbl==1)&&(Odbl==1)) ){

                    Mol.getAtom(idxN).setFormalCharge(NCharge + 1);
                    Mol.getAtom(idxDoubleO).setFormalCharge(-1);
                    Mol.getBond(Mol.getAtom(idxN), Mol.getAtom(idxDoubleO)).setOrder(IBond.Order.SINGLE);

                    HasModified = true;
                    log.info("Normalized a C#N=O / C=N=O / N=N=O group");
                }


            }
        }

        return HasModified;
    }


    /**
     * Fix some known problems on aromatic detection / setting.
     *
     * - Make aromatic the bond shared between two aromatic fused structures like in the example mol:
     * CN1C=NC(N)=C2N=CN=C12
     * for unknown reason, in CDK the bond is not set as aromatic
     *
     * - Set as aromatic all bonds in a ring that has all aromatic atoms, to fix problems like in the following PAH:
     * c1ccc2c(c1)c4cccc3cccc2c34
     * where a 5-member ring is fused to benzene rings
     *
     * @param Mol
     */
    private static void FixAromaticityProblems(IAtomContainer Mol) {

        Cycles cycles = Cycles.sssr(Mol);
        RingSet SSSR = (RingSet) cycles.toRingSet();

        for (IBond b : Mol.bonds()) {

            // if the bond is already aromatic, skip
            if (b.getFlag(CDKConstants.ISAROMATIC))
                continue;

            // check all NON aromatic bonds shared between two rings
            // if both rings are aromatic (all atoms with the aromatic flag), the bond is set as aromatic

            IRingSet rings = SSSR.getRings(b);

            if (rings.getAtomContainerCount() != 2)
                continue;

            boolean AllAromatic = true;
            for (IAtomContainer curRing : rings.atomContainers())
                for (IAtom a : curRing.atoms())
                    if (!a.getFlag(CDKConstants.ISAROMATIC)) {
                        AllAromatic = false;
                        break;
                    }

            if (AllAromatic) {
                b.setFlag(CDKConstants.ISAROMATIC, true);
                log.info("Fixed an aromatic bond between fused aromatic rings");
            }

        }


        for (IAtomContainer curRing : SSSR.atomContainers()) {
            boolean AllAromatic = true;
            for (IAtom a : curRing.atoms())
                if (!a.getFlag(CDKConstants.ISAROMATIC)) {
                    AllAromatic = false;
                    break;
                }
            if (AllAromatic) {
                for (IBond b : curRing.bonds())
                    b.setFlag(CDKConstants.ISAROMATIC, true);
            }

        }

    }

}
