package insilico.core.auxiliary.protoqsar.filters;

import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRingSet;

import java.util.List;

/**
 *
 * @author Alberto
 */
public class BiocideFilter {
    
    public static final String POS = "Biocide-like";
    public static final String NEG = "NON Biocide-like";
    public static final String UNK = "Not predicted";
    
    //
    // Biocide - like compound ≤ 2 Sum of sulphur and phosphorus(P + S)  
    // Biocide - like compound ≥ 1 Number de heteroatoms(heteroat)
    // Biocide - like compound ≥ 1 Number of rigid bonds (rigidb)
    // Biocide - like compound ≤ 0 Number phosphorus (nP)
    // Biocide - like compound ≤ 3 Number of chlorines(nCl)
    //
    public static String ApplyFilter(InsilicoMolecule mol) {
        try {
            boolean res = Calculate(mol);
            return res ? POS : NEG;
        } catch (Exception e) {
            return UNK;
        }
    }
    
    private static boolean Calculate(InsilicoMolecule mol) throws Exception {
        
        int nPS=0, nHet=0, nRotatableBonds=0, nRigidBonds=0, nP=0, nCl=0;
                
        IAtomContainer curMol = mol.GetStructure();
        int nSK = curMol.getAtomCount();
        int nBO = curMol.getBondCount();
        
        for (int i=0; i<nSK; i++) {

                IAtom CurAt = curMol.getAtom(i);

                if (CurAt.getSymbol().equalsIgnoreCase("Cl")) 
                    nCl++;
                if (CurAt.getSymbol().equalsIgnoreCase("P")) {
                    nP++;
                    nPS++;
                }
                if (CurAt.getSymbol().equalsIgnoreCase("S")) 
                    nPS++;
                if ( (!CurAt.getSymbol().equalsIgnoreCase("C")) && (!CurAt.getSymbol().equalsIgnoreCase("H")) )
                    nHet++;
                
        }        
        
        for (int i=0; i<nBO; i++) {

            IBond CurBo = curMol.getBond(i);
            
            boolean isInRing = false;
            boolean isHBond = false;
            boolean isAmide = false;
            boolean isNplusOminus = false; // for NO2 groups
            
            boolean isSingle = (CurBo.getOrder() == IBond.Order.SINGLE);
            
            IRingSet rings = mol.GetSSSR().getRings(CurBo);
            if ( rings.getAtomContainerCount() != 0 )
                isInRing = true;

            if ( (CurBo.getAtom(0).getSymbol().equalsIgnoreCase("H")) || (CurBo.getAtom(1).getSymbol().equalsIgnoreCase("H"))) 
                isHBond = true;

            if ( (!isInRing) && (isSingle) && ( !( (mol.GetSSSR().contains(CurBo.getAtom(0))) || (mol.GetSSSR().contains(CurBo.getAtom(1))) ) ) ) {
                
                if ( ( (CurBo.getAtom(0).getSymbol().equalsIgnoreCase("N")) && (CurBo.getAtom(1).getSymbol().equalsIgnoreCase("O"))) ) {
                    if ( (CurBo.getAtom(0).getFormalCharge() == +1) && (CurBo.getAtom(1).getFormalCharge() == -1) )
                        isNplusOminus = true;
                }                
                if ( ( (CurBo.getAtom(1).getSymbol().equalsIgnoreCase("N")) && (CurBo.getAtom(0).getSymbol().equalsIgnoreCase("O"))) ) {
                    if ( (CurBo.getAtom(1).getFormalCharge() == +1) && (CurBo.getAtom(0).getFormalCharge() == -1) )
                        isNplusOminus = true;
                }                
                
                if ( (CurBo.getAtom(0).getFormalCharge()==0) && (CurBo.getAtom(1).getFormalCharge()==0) ) {

                    if ( ( (CurBo.getAtom(0).getSymbol().equalsIgnoreCase("C")) && (CurBo.getAtom(1).getSymbol().equalsIgnoreCase("N"))) ) {
                        List<IBond> bonds = curMol.getConnectedBondsList(CurBo.getAtom(0));
                        for (IBond b : bonds) {
                            if (b.getOrder() == IBond.Order.DOUBLE) {
                                if ( (b.getAtom(0).getSymbol().equalsIgnoreCase("O")) || (b.getAtom(1).getSymbol().equalsIgnoreCase("O")) ) {
                                    isAmide = true; 
                                    break;
                                }
                            }
                        }
                    }
                    if ( ( (CurBo.getAtom(1).getSymbol().equalsIgnoreCase("C")) && (CurBo.getAtom(0).getSymbol().equalsIgnoreCase("N"))) ) {
                        List<IBond> bonds = curMol.getConnectedBondsList(CurBo.getAtom(1));
                        for (IBond b : bonds) {
                            if (b.getOrder() == IBond.Order.DOUBLE) {
                                if ( (b.getAtom(0).getSymbol().equalsIgnoreCase("O")) || (b.getAtom(1).getSymbol().equalsIgnoreCase("O")) ) {
                                    isAmide = true; 
                                    break;
                                }
                            }
                        }
                    } 
                }
            }
            
            if ( (!isInRing) && (!isHBond) && (!isAmide) && (!isNplusOminus) && (isSingle) )
                nRotatableBonds++;
        } 
        
        nRigidBonds = nBO - nRotatableBonds;

        if ((nPS <= 2) &&
            (nHet >= 1) &&
            (nRigidBonds >= 1) &&
            (nP <= 0) &&
            (nCl <= 3)) {
            return true;
        } else {
            return false;
        }
        
    }
    
}
