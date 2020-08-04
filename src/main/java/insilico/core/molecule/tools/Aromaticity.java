package insilico.core.molecule.tools;

import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.*;

import java.util.Iterator;

/**
 * Detection of Aromaticity with Huckel rule https://en.wikipedia.org/wiki/H%C3%BCckel%27s_rule
 */
public class Aromaticity {

    /**
     * Configures aromaticity in a molecule with Huckel's rule.
     * @param mol CDK Molecule Object to be configured
     * @return True if some atoms have been set to aromatic
     * @throws InvalidMoleculeException
     */
    public static boolean ConfigureMolecule(InsilicoMolecule mol) throws InvalidMoleculeException{
        boolean retVal = false;
        Iterator<IAtomContainer> ringsIterator = mol.GetSSSR().atomContainers().iterator();
        while (ringsIterator.hasNext()) {
            IRing ring = (IRing) ringsIterator.next();
            boolean curRetVal = ConfigureRing(ring, mol.GetSSSR(), mol.GetStructure());
            if (curRetVal)
                retVal = true;
        }

        return retVal;
    }


    /**
     * Configures aromaticity in a single ring with Huckel's rule
     * @param ring CDK IRing object to be configured
     * @param allRings Set of rings (SSSR) of the molecule
     * @param mol CDK Molecule Object where the ring belongs
     * @return True if some atoms have been set to aromatic
     */
    public static boolean ConfigureRing(IRing ring, IRingSet allRings, IAtomContainer mol){

        boolean basicAromaticityDetection = true;

        int PIElectrons = 0;
        boolean notAllowedAtoms = false;
        boolean exoDoubleBond = false;

        // Calculate PI Electrons for eatch atom in ring
        for (IAtom atom : ring.atoms()){

            if (notAllowedAtoms)
                break;

            // General information on atom
            String atomSymbol = atom.getSymbol();
            int formalCharge = 0;
            try {
                formalCharge = atom.getFormalCharge();
            } catch (Exception e){
                e.printStackTrace();
            }

            boolean isAromaticAtom = false;
            int nBondSingle = 0, nBondDouble = 0;
            boolean exoDoubleBondToElectronegative = false;

            Iterator<IBond> bondIterator = mol.getConnectedBondsList(atom).iterator();
            while (bondIterator.hasNext()){
                IBond bnd = bondIterator.next();
                boolean isBondInCurrentRing = ring.contains(bnd);

                if (bnd.getFlag(CDKConstants.ISAROMATIC))
                    isAromaticAtom = true;
                else {
                    if (bnd.getOrder() == IBond.Order.SINGLE) {
                        if (isBondInCurrentRing)
                            nBondSingle++;
                    } else if (bnd.getOrder() == IBond.Order.DOUBLE) {
                        if (isBondInCurrentRing)
                            nBondDouble++;
                    }
                }

                // checks exocyclic bonds
                if (!isBondInCurrentRing)
                    if (bnd.getOrder() == IBond.Order.DOUBLE) {
                        IAtom at1 = bnd.getAtom(0);
                        IAtom at2 = bnd.getAtom(1);
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
                            exoDoubleBond = true;

                            String atExtSymbol = atExt.getSymbol();
                            if ((atExtSymbol.equalsIgnoreCase("O"))||
                                    (atExtSymbol.equalsIgnoreCase("S"))||
                                    (atExtSymbol.equalsIgnoreCase("N"))||
                                    (atExtSymbol.equalsIgnoreCase("P"))||
                                    (atExtSymbol.equalsIgnoreCase("Se"))) {

                                // Exocyclic bond to an electronegative atom
                                exoDoubleBondToElectronegative = true;
                            }
                        }

                    }
            }

            int nTotSingle=0, nTotDbl=0, nTotTriple=0, nTotArom=0;
            bondIterator = mol.getConnectedBondsList(atom).iterator();
            while (bondIterator.hasNext()) {
                IBond bnd = bondIterator.next();

                if (bnd.getFlag(CDKConstants.ISAROMATIC)) {
                    nTotArom++;
                }
                else {
                    if (bnd.getOrder() == IBond.Order.SINGLE) {
                        nTotSingle++;
                    } else if (bnd.getOrder() == IBond.Order.DOUBLE) {
                        nTotDbl++;
                    } else if (bnd.getOrder() == IBond.Order.TRIPLE) {
                        nTotTriple++;
                    }
                }
            }

            // Calculates PI electrons by atom type

            // C atom
            if (atomSymbol.equalsIgnoreCase("C")) {

                // Charged C can not be aromatic
                if (formalCharge!=0) {
                    notAllowedAtoms = true;
                    continue;
                }

                // sp2 hybridized form (or belongs to another fused aromatic ring)
                if ((nBondDouble == 1) || isAromaticAtom) {
                    PIElectrons += 1;
                    continue;
                }

                // exocyclic double bond to el. neg.
                if ((exoDoubleBondToElectronegative)&&(!basicAromaticityDetection)) {
                    PIElectrons += 0;
                    continue;
                }

                notAllowedAtoms = true;
                continue;
            }

            // N or P atom
            if ((atomSymbol.equalsIgnoreCase("N"))||(atomSymbol.equalsIgnoreCase("P"))) {

                // for P atom, check if valence is over 3
                // in this case it is not aromatic by defauls (to be checked?)
                if (atomSymbol.equalsIgnoreCase("P")) {
                    if (!isAromaticAtom) {
                        int Valence = nTotSingle + 2*nTotDbl + 3*nTotTriple;
                        if (Valence > 3) {
                            notAllowedAtoms = true;
                            continue;
                        }
                    } else {
                        int Valence = nTotSingle + 2*nTotDbl + 3*nTotTriple + nTotArom;
                        if (Valence > 3) {
                            notAllowedAtoms = true;
                            continue;
                        }
                    }
                }

                // sp2 hybridized form because already set as aromatic
                // (i.e. already in a fused aromatic ring)
                if (isAromaticAtom) {
                    PIElectrons += 1;
                    continue;
                }

                // sp2 hybridized form: like C=N-C
                if ((nBondDouble == 1) && (formalCharge == 0)) {
                    PIElectrons += 1;
                    continue;
                }

                // sp2 hybridized form with charge +1: like C=[N+](-C)-C
                if ((nBondDouble == 1) && (formalCharge == 1)) {
                    PIElectrons += 1;
                    continue;
                }

                // sp3 hybridized form: like C-N(-C)-C
                if ((nBondSingle == 2) && (formalCharge == 0)){
                    PIElectrons += 2;
                    continue;
                }

                notAllowedAtoms = true;
                continue;
            }

            // O or S atom
            if ((atomSymbol.equalsIgnoreCase("O"))||(atomSymbol.equalsIgnoreCase("S"))) {

                if (nBondSingle == 2) {
                    PIElectrons += 2;
                    continue;
                }

                // =[O+]- or =[S+]-
                if (formalCharge == 1) {
                    PIElectrons += 1;
                    continue;
                }

                notAllowedAtoms = true;
                continue;
            }


            // Se atom
            if (atomSymbol.equalsIgnoreCase("Se")) {

                if (nBondSingle == 2) {
                    PIElectrons += 2;
                    continue;
                }

                notAllowedAtoms = true;
                continue;
            }

            notAllowedAtoms = true;

        }


        // Checks aromaticity by 4n+2 rule

        if ((notAllowedAtoms)||( (basicAromaticityDetection)&&(exoDoubleBond) ))

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
