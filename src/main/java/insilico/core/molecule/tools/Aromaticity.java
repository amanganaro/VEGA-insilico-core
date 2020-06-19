package insilico.core.molecule.tools;

import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.*;

import java.util.Iterator;

public class Aromaticity {
    /**
     * Configures aromaticity in a molecule with Huckel rule.
     * @param mol CDK Molecule object to be configured
     * @return True if some atoms have been set to aromatic
     */
    public static boolean ConfigureMolecule(InsilicoMolecule mol) throws InvalidMoleculeException {

        boolean RetVal = false;

        Iterator<IAtomContainer> RingsIterator = mol.GetSSSR().atomContainers().iterator();
        while (RingsIterator.hasNext()) {
            IRing ring = (IRing)RingsIterator.next();
            boolean CurRetVal = ConfigureRing(ring, mol.GetSSSR(), mol.GetStructure());
            if (CurRetVal)
                RetVal = true;
        }

        return RetVal;
    }


    /**
     * Configures aromaticity in a single ring with Huckel rule.
     * @param ring CDK IRing object to be configured
     * @param allRings Set of rings (SSSR) of the molecule
     * @param mol CDK Molecule object where the ring belongs
     * @return True if some atoms have been set to aromatic
     */
    public static boolean ConfigureRing(IRing ring, IRingSet allRings, IAtomContainer mol) {

        boolean BasicAromaticityDetection = true;

        int PIElectrons = 0;

        boolean NotAllowedAtom = false;
        boolean ExoDoubleBond = false;


        // Calculates PI Electrons for each atom in ring

        for (IAtom at : ring.atoms()) {

            // Checks if loop should be continued
            if (NotAllowedAtom)
                break;


            // General info on atom

            String atomSymbol = at.getSymbol();

            int FormalCharge = 0;
            try {
                FormalCharge = at.getFormalCharge();
            } catch (Exception e) {}

            boolean IsAromaticAtom = false;
            int nBondSingle=0, nBondDouble=0;
            boolean ExoDoubleBondToElectroNegative = false;
            Iterator<IBond> BndIterator = mol.getConnectedBondsList(at).iterator();
            while (BndIterator.hasNext()) {
                IBond b = BndIterator.next();
                boolean IsBondInCurRing = ring.contains(b);

                if (b.getFlag(CDKConstants.ISAROMATIC)) {
                    IsAromaticAtom = true;
                }
                else {
                    if (b.getOrder() == IBond.Order.SINGLE) {
                        if (IsBondInCurRing)
                            nBondSingle++;
                    } else if (b.getOrder() == IBond.Order.DOUBLE) {
                        if (IsBondInCurRing)
                            nBondDouble++;
                    }
                }

                // checks exocyclic bonds
                if (!IsBondInCurRing)
                    if (b.getOrder() == IBond.Order.DOUBLE) {
                        IAtom at1 = b.getAtom(0);
                        IAtom at2 = b.getAtom(1);
                        IAtom atExt = null;
                        if (ring.contains(at1))
                            atExt = at2;
                        else
                            atExt = at1;
                        IRingSet r = allRings.getRings(atExt);
                        if (r.getAtomContainerCount()>0) {
                            // The external atom is in another ring
                            // that is fused with the actual one.
                        } else {

                            // Exocyclic double bond
                            ExoDoubleBond = true;

                            String atExtSymbol = atExt.getSymbol();
                            if ((atExtSymbol.equalsIgnoreCase("O"))||
                                    (atExtSymbol.equalsIgnoreCase("S"))||
                                    (atExtSymbol.equalsIgnoreCase("N"))||
                                    (atExtSymbol.equalsIgnoreCase("P"))||
                                    (atExtSymbol.equalsIgnoreCase("Se"))) {

                                // Exocyclic bond to an electronegative atom
                                ExoDoubleBondToElectroNegative = true;
                            }
                        }

                    }

            }

            int nTotSingle=0, nTotDbl=0, nTotTriple=0, nTotArom=0;
            BndIterator = mol.getConnectedBondsList(at).iterator();
            while (BndIterator.hasNext()) {
                IBond b = BndIterator.next();

                if (b.getFlag(CDKConstants.ISAROMATIC)) {
                    nTotArom++;
                }
                else {
                    if (b.getOrder() == IBond.Order.SINGLE) {
                        nTotSingle++;
                    } else if (b.getOrder() == IBond.Order.DOUBLE) {
                        nTotDbl++;
                    } else if (b.getOrder() == IBond.Order.TRIPLE) {
                        nTotTriple++;
                    }
                }
            }


            // Calculates PI electrons by atom type

            // C atom
            if (atomSymbol.equalsIgnoreCase("C")) {

                // Charged C can not be aromatic
                if (FormalCharge!=0) {
                    NotAllowedAtom = true;
                    continue;
                }

                // sp2 hybridized form (or belongs to another fused aromatic ring)
                if ((nBondDouble == 1) || IsAromaticAtom) {
                    PIElectrons += 1;
                    continue;
                }

                // exocyclic double bond to el. neg.
                if ((ExoDoubleBondToElectroNegative)&&(!BasicAromaticityDetection)) {
                    PIElectrons += 0;
                    continue;
                }

                NotAllowedAtom = true;
                continue;
            }


            // N or P atom
            if ((atomSymbol.equalsIgnoreCase("N"))||(atomSymbol.equalsIgnoreCase("P"))) {

                // for P atom, check if valence is over 3
                // in this case it is not aromatic by defauls (to be checked?)
                if (atomSymbol.equalsIgnoreCase("P")) {
                    if (!IsAromaticAtom) {
                        int Valence = nTotSingle + 2*nTotDbl + 3*nTotTriple;
                        if (Valence > 3) {
                            NotAllowedAtom = true;
                            continue;
                        }
                    } else {
                        int Valence = nTotSingle + 2*nTotDbl + 3*nTotTriple + nTotArom;
                        if (Valence > 3) {
                            NotAllowedAtom = true;
                            continue;
                        }
                    }
                }

                // sp2 hybridized form because already set as aromatic
                // (i.e. already in a fused aromatic ring)
                if (IsAromaticAtom) {
                    PIElectrons += 1;
                    continue;
                }

                // sp2 hybridized form: like C=N-C
                if ((nBondDouble == 1) && (FormalCharge == 0)) {
                    PIElectrons += 1;
                    continue;
                }

                // sp2 hybridized form with charge +1: like C=[N+](-C)-C
                if ((nBondDouble == 1) && (FormalCharge == 1)) {
                    PIElectrons += 1;
                    continue;
                }

                // sp3 hybridized form: like C-N(-C)-C
                if ((nBondSingle == 2) && (FormalCharge == 0)){
                    PIElectrons += 2;
                    continue;
                }

                NotAllowedAtom = true;
                continue;
            }


            // O or S atom
            if ((atomSymbol.equalsIgnoreCase("O"))||(atomSymbol.equalsIgnoreCase("S"))) {

                if (nBondSingle == 2) {
                    PIElectrons += 2;
                    continue;
                }

                // =[O+]- or =[S+]-
                if (FormalCharge == 1) {
                    PIElectrons += 1;
                    continue;
                }

                NotAllowedAtom = true;
                continue;
            }


            // Se atom
            if (atomSymbol.equalsIgnoreCase("Se")) {

                if (nBondSingle == 2) {
                    PIElectrons += 2;
                    continue;
                }

                NotAllowedAtom = true;
                continue;
            }

            NotAllowedAtom = true;

        }


        // Checks aromaticity by 4n+2 rule

        if ((NotAllowedAtom)||( (BasicAromaticityDetection)&&(ExoDoubleBond) ))

            return false;

        else {

            int Hueckel = (PIElectrons - 2) % 4;
            if (Hueckel == 0) {

                for (IAtom at: ring.atoms())
                    at.setFlag(CDKConstants.ISAROMATIC, true);
                for (IBond bnd: ring.bonds())
                    bnd.setFlag(CDKConstants.ISAROMATIC, true);

                return true;

            } else {

                return false;

            }

        }
    }
}
