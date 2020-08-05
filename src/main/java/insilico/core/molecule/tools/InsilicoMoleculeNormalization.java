package insilico.core.molecule.tools;

import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alberto
 */
public class InsilicoMoleculeNormalization {

    static Logger logger = LoggerFactory.getLogger(InsilicoMoleculeNormalization.class);
    
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
        
        CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(SilentChemObjectBuilder.getInstance());
        for (IAtom a : Mol.atoms()) {
            try {
                IAtomType type = matcher.findMatchingAtomType(Mol, a);
                AtomTypeManipulator.configure(a, type);
            } catch (Exception ex) {
                logger.warn(ex.getMessage());
            }
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
        int idxDoubleO, idxTripleN;
        
        for (int i=0; i<Mol.getAtomCount(); i++) {
            
            // Checks for N-based groups 
            
            if (ConnMatrix[i][i] == 7) {
                
                idxN = i;
                idxDoubleO = -1; 
                idxTripleN = -1; 
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

                    HasModified = true;
                    logger.info("Normalized a NO2 group");
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

                    HasModified = true;
                    logger.info("Normalized a N=N#N / C=N#N group");
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

                    HasModified = true;
                    logger.info("Normalized a C#N=O / C=N=O / N=N=O group");
                }
                
                
            }
        }
        
        return HasModified;
    }    
    
}
