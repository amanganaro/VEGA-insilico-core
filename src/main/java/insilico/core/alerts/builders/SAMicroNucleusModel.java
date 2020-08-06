package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 * Alerts extended from the SAMicroNucleusVermeer (used in ToxRead) as they
 * are used for the new sarpy-based model.
 * 
 * @author User
 */
public class SAMicroNucleusModel extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // number, SMARTS, activity
    private final static Object[][] SMARTS = {
        {1, "C(=O)NC(CO)C", 1},
        {2, "Nc1ccc2c3ccccc3Cc2c1", 1},
        {3, "O=[N+]([O-])c1cc(ccc1NCCO)", 1},
        {4, "C(C(=C)Cl)", 1},
        {5, "O=S(=O)(OCCC)", 1},
        {6, "Oc1cc(N)c(cc1C)", 1},
        {7, "Oc1ccc(cc1C)Cl", 1},
        {8, "c3ccc4cc2c(ccc1ccccc12)cc4(c3)", 1},
        {9, "CC(O)C(C)N", 1},
        {10, "CCC(CC)C(=O)O", 1},
        {11, "C(=O)CC(CCO)C", 1},
        {12, "[N+]([O-])c1cccc(c1)C", 1},
        {13, "c1cc(N)c(cc1C)Cl", 1},
        {14, "CCN(c1ccccc1)CC", 1},
        {15, "ClCC(Cl)C", 1},
        {16, "n1c(nc(c(c1N)))N", 1},
        {17, "O=[N+]([O-])c1cc(O)ccc1", 1},
        {18, "O(CCN)C", 1},
        {19, "c1cc(ccc1)N(CC)C", 1},
        {20, "Nc1ccc(cc1)Nc2ccccc2", 1},
        {21, "COS(=O)(=O)C", 1},
        {22, "c1ccc2cc3ccccc3cc2c1", 1},
        {23, "c1nc2ccccc2[nH]1", 1},
        {24, "c1ncc(cc1C)", 1},
        {25, "c3cc(O)cc(O)c3", 1},
        {26, "O=C(O)CCCC(=O)O", 1},
        {27, "O=NN(CC)CC", 1},
        {28, "O=S(=O)(OCC)", 1},
        {29, "c1cncn1", 1},
        {30, "O=C(O)C=CC", 1},
        {31, "O=C1OCC(=C1)", 1},
        {32, "O=C(O)CCCCCCCC", 0},
        {33, "O=C(OCC)CCCCC", 0},
        {34, "O=COC1CCCCC1C(C)(C)", 0},
        {35, "O=C(OCCCCCCCCCCOCC=C)C=C", 0},
        {36, "C(OCCc1ccccc1)C", 0},
        {37, "O(C)C(O)C(C)(C)", 0},
        {38, "O(COCC(CC)CCCC)", 0},
        {39, "O=S(=O)(O)CCC", 0},
        {40, "C(F)F", 0},
        {41, "C(F)(F)F", 0},
        {42, "C(OCC=C)CCCCC", 0},
        {43, "C(O)C(C)N", 1},
        {44, "COC1CCCCC1C(C)(C)", 0},
        {45, "O(CO)CC(C)C", 0},
        {46, "C(=O)OCC(CC)CCCC", 0},
        {47, "O=C(OCC(CC)CCCC)C", 0},
        {48, "C(=O)C=CC", 1},
        {49, "O=Cc1ccccc1O", 1},
        {50, "O=S(=O)(OC)", 1},
        {51, "CCNc1cccc(N)c1", 1},
        {52, "C=CC=C", 1},
        {53, "CN(c1ccccc1)C", 1},
        {54, "C1CCCCC1C(C)(C)", 0},
        {55, "O(C)CC(CC)CCC", 0},
        {56, "O=C(O)CCCCC", 0},
        {57, "C(CC)CCCC", 0},
        {58, "C(O)(CO)CCO", 1},
        {59, "O=NN(C)C", 1},
        {60, "CCCCOC(=O)CC", 0},
        {61, "CCCCCC", 0},
        {62, "c1cc(ccc1N)NCCO", 1},
        {63, "C(O)C=CC", 1},
        {64, "Cc1cccc(c1)Cl", 1},
        {65, "N=NC(C)C", 0},
        {66, "NCCCCCCN", 0},
        {67, "O=C(OC)Cc1ccccc1", 0},
        {68, "O=C(N)CCC", 0},
        {69, "Nc1ccc(OC)cc1", 0},
        {70, "C(OCCC)O", 0},
        {71, "O(C)CCCCCC", 0},
        {72, "CCCCCCCCCC", 0},
        {73, "Oc1cccc(c1)C", 1},
        {74, "C1CCCCC1", 0},
        {75, "ClCC(Cl)", 1},
        {76, "C(C(=O)O)NC", 1},
        {77, "O=C(OCCC(C))CC(C)", 0},
        {78, "O=C(OCc1ccccc1)C", 0},
        {79, "O=C(OCCCCCC)C", 0},
        {80, "OCCCCCC", 0},
        {81, "C(O)CCO", 1},
        {82, "C(C=C)(C)CC", 1},
        {83, "OCCCCO", 1},
        {84, "CC=C(C)", 1},
        {85, "P(O)O", 1},
        {86, "C(Cl)(Cl)Cl", 1},
        {87, "NC(C)C", 1},
        {88, "C(=O)CCCCC", 0},
        {89, "CCCCCO", 0},
        {90, "Oc1ccccc1C", 1},
        {91, "[N+]c1ccccc1", 1},
        {92, "NCCO", 1},
        {93, "CCCCCCN", 0},
        {94, "[N+](=O)[O-]", 1},
        {95, "NCCNC", 1},
        {96, "C(C=C)(C)CCC", 1},
        {97, "O=[N+]([O-])c1cc(ccc1N)", 1},
        {98, "O=C(N)CC", 0},
        {99, "O=[N+]([O-])c1ccccc1", 1},
        {100, "CCCCC", 0},
        {101, "O=S(=O)(O)c1ccccc1", 0},
        {102, "CCC(C)C", 0},
        {103, "c1ccc(OCCO)cc1", 0},
        {104, "n1ccccc1", 1},
        {105, "C(C)(C)C", 0},
        {106, "P(=O)(O)O", 1},
        {107, "CN(CC)CC", 1},
        {108, "C(C(=O))NC", 1},
        {109, "N(C)(C)CCCCCC", 0},
        {110, "c1ccc(cc1)Cc2ccccc2", 1},
        {111, "C(O)C=CC=CC", 1},
        {112, "C(O)CC(=O)", 1},
        {113, "C(O)N", 1},
        {114, "C(OCC1OC1)", 1},
        {115, "c1ccc(cc1OC)C", 1},
        {116, "n1ccc(N)c2ccc(cc12)", 1},
        {117, "Nc1ccc(cc1)N", 1},
        {118, "O=[N+]([O-])c1cc(ccc1)NCC", 1},
        {119, "O=C(NCCO)", 1},
        {120, "O=C(OCCO)C(=C)", 1},
        {121, "O=C1c3ccccc3(C(=O)c2ccccc12)", 1},
        {122, "Oc1ccc(cc1)CC", 1},
        {123, "Oc1ccc(cc1)CCC", 1},
        {124, "OCCN(c1ccc(N)cc1)CC", 1},
        {125, "C(C)(C)CCCC(C)C", 0},
        {126, "C(C)CNCCO", 0},
        {127, "C(N)C(N=N)(C)", 0},
        {128, "C(OC1CC(C)CC(C)(C)C1)C", 0},
        {129, "C(OCC=C)CC", 0},
        {130, "C1(O)CCCCC1", 0},
        {131, "CC(N)(C)C", 0},
        {132, "CC=CCCC", 1},
        {133, "COC1CCCCC1", 0},
        {134, "N=Nc3ccccc3", 0},
        {135, "O(C)CC(CC)", 0},
        {136, "O(C)CCCCC", 0},
        {137, "O=C(N(C))CC", 0},
        {138, "c1ccc(F)cc1", 1}
    };        
        
 
    
    public SAMicroNucleusModel() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MICRONUCLEUS_INVITRO_MODEL, "Rules for in vitro micronucleus assay (IRFMN)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            
            String SM = (String) SMARTS[i][1];
            int Activity = (Integer) SMARTS[i][2];
            
            curSA.setImageURL("/insilico/core/alerts/png/mnmodel/MN_MOD_" + (idx+1) + ".png");
            if (Activity == 1) {
                curSA.setName("Micronucleus active alert no. " + (i+1));
                curSA.setDescription("Structural alert for micronucleus activity defined by the SMARTS: " + SM);
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_ACTIVE, true);
            } else {
                curSA.setName("Micronucleus inactive alert no. " + (i+1));
                curSA.setDescription("Structural alert for micronucleus inactivity defined by the SMARTS: " + SM);
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_INACTIVE, true);
            }
            Alerts.add(curSA);
            idx++;
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new QueryAtomContainer[SMARTS.length];
            
            int idx = 0;
            for (Object[] arr : SMARTS) {
                SA[idx] = SMARTSParser.parse((String)arr[1], DefaultChemObjectBuilder.getInstance());
                idx++;
            }
            
        } catch (Exception e) {
            throw new InitFailureException("Unable to initialize SMARTS");
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {
            
            for (int i=0; i<SMARTS.length; i++) {
                
                if (Matches(SA[i])) 
                    Res.add((Alert)Alerts.get(i).clone());
            }
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    
    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<SMARTS.length; i++) {
            String s = (String)SMARTS[i][1];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "MN_MOD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }      
}