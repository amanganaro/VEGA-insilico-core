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
 *
 * @author User
 */
public class SAMicroNucleusVermeer extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static Object[][] SMARTS_TOX = {
        {"C(=O)NC(CO)C", 1.000},
        {"Nc1ccc2c3ccccc3Cc2c1", 1.000},
        {"O=[N+]([O-])c1cc(ccc1NCCO)", 1.000},
        {"C(C(=C)Cl)", 1.000},
        {"O=S(=O)(OCCC)", 1.000},
        {"Oc1cc(N)c(cc1C)", 1.000},
        {"Oc1ccc(cc1C)Cl", 1.000},
        {"c3ccc4cc2c(ccc1ccccc12)cc4(c3)", 1.000},
        {"CC(O)C(C)N", 1.000},
        {"CCC(CC)C(=O)O", 1.000},
        {"C(=O)CC(CCO)C", 1.000},
        {"[N+]([O-])c1cccc(c1)C", 1.000},
        {"c1cc(N)c(cc1C)Cl", 1.000},
        {"CCN(c1ccccc1)CC", 1.000},
        {"ClCC(Cl)C", 1.000},
        {"n1c(nc(c(c1N)))N", 1.000},
        {"O=[N+]([O-])c1cc(O)ccc1", 1.000},
        {"O(CCN)C", 1.000},
        {"c1cc(ccc1)N(CC)C", 1.000},
        {"Nc1ccc(cc1)Nc2ccccc2", 1.000},
        {"COS(=O)(=O)C", 1.000},
        {"c1ccc2cc3ccccc3cc2c1", 1.000},
        {"c1nc2ccccc2n1", 1.000}, // REMOVE - index = 23
        {"c1ncc(cc1C)", 1.000},
        {"c3cc(O)cc(O)c3", 1.000},
        {"O=C(O)CCCC(=O)O", 1.000},
        {"O=NN(CC)CC", 1.000},
        {"O=S(=O)(OCC)", 1.000},
        {"c1cncn1", 0.952},
        {"O=C(O)C=CC", 0.889},
        {"O=C1OCC(=C1)", 0.833},
        {"C(O)C(C)N", 0.882},
        {"C(=O)C=CC", 0.808},
        {"O=Cc1ccccc1O", 0.929},
        {"O=S(=O)(OC)", 0.923},
        {"CCNc1cccc(N)c1", 0.929},
        {"C=CC=C", 0.900},
        {"CN(c1ccccc1)C", 0.882},
        {"C(O)(CO)CCO", 0.909},
        {"O=NN(C)C", 0.889},
        {"c1cc(ccc1N)NCCO", 0.875},
        {"C(O)C=CC", 0.824},
        {"Oc1cccc(c1)C", 0.889},
        {"C(O)CCO", 0.840},
        {"OCCCCO", 0.857},
        {"P(O)O", 0.800},
        {"Oc1ccccc1C", 0.821},
        {"C(O)C=CC=CC", 1.000},
        {"C(O)CC(=O)", 1.000},
        {"C(O)N", 0.842},
        {"C(OCC1OC1)", 1.000},
        {"c1ccc(cc1OC)C", 1.000},
        {"c1ncccc1C", 1.000}, // REMOVE - index = 53
        {"CC=CC(=O)", 0.808}, // REMOVE - index = 54
        {"n1ccc(N)c2ccc(cc12)", 1.000},
        {"n1ccnc1", 0.952}, // REMOVE - index = 56
        {"O=[N+]([O-])c1cc(ccc1)NCC", 0.875},
        {"O=C(NCCO)", 0.889},
        {"O=C(OCCO)C(=C)", 0.857},
        {"O=C1c3ccccc3(C(=O)c2ccccc12)", 1.000},
        {"Oc1ccc(cc1)CC", 0.889},
        {"Oc1ccc(cc1)CCC", 0.917},
        {"OCCN(c1ccc(N)cc1)CC", 1.000},
        {"c1ccc(cc1O)C", 0.889}, // REMOVE - index = 64
        {"c1ccc(F)cc1", 0.909},
        {"c1nc2ccccc2[nH]1", 1.000}
    };
    
    private final static Object[][] SMARTS_NON_TOX = {
        {"O=C(O)CCCCCCCC", 1.000},
        {"O=C(OCC)CCCCC", 1.000},
        {"O=COC1CCCCC1C(C)(C)", 1.000},
        {"O=C(OCCCCCCCCCCOCC=C)C=C", 1.000},
        {"C(OCCc1ccccc1)C", 1.000},
        {"O(C)C(O)C(C)(C)", 1.000},
        {"O(COCC(CC)CCCC)", 1.000},
        {"O=S(=O)(O)CCC", 1.000},
        {"C(F)F", 0.875},
        {"C(F)(F)F", 0.857},
        {"C(OCC=C)CCCCC", 0.900},
        {"COC1CCCCC1C(C)(C)", 0.833},
        {"O(CO)CC(C)C", 0.833},
        {"C1CCCCC1C(C)(C)", 0.800},
        {"O=C(O)CCCCC", 0.833},
        {"N=NC(C)C", 0.800},
        {"C(N)C(N=N)(C)", 1.000},
        {"C(OC1CC(C)CC(C)(C)C1)C", 1.000},
        {"CC(N)(C)C", 1.000},
        {"FCF", 0.875} // REMOVE - index = 20 (overall 86)
    };
     
    
    public SAMicroNucleusVermeer() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MICRONUCLEUS_VERMEER, "Rules for in vitro micro nucleus assay (IRFMN/VERMEER)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_TOX.length; i++) {
            
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Micro Nucleus active alert no. " + (i+1));
            curSA.setDescription("Structural alert for micro nucleus activity defined by the SMARTS: " + SMARTS_TOX[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/mnvermeer/MN_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_ACTIVE, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS_TOX[i][1]);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTS_NON_TOX.length; i++) {
            
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Micro Nucleus inactive alert no. " + (i+1));
            curSA.setDescription("Structural alert for micro nucleus inactivity defined by the SMARTS: " + SMARTS_NON_TOX[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/mnvermeer/MN_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_INACTIVE, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS_NON_TOX[i][1]);
            Alerts.add(curSA);
            idx++;
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTS_TOX.length + SMARTS_NON_TOX.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (Object[] arr : SMARTS_TOX) {
                SA[idx] = SMARTSParser.parse((String)arr[0], DefaultChemObjectBuilder.getInstance());
                idx++;
            }
            for (Object[] arr : SMARTS_NON_TOX) {
                SA[idx] = SMARTSParser.parse((String)arr[0], DefaultChemObjectBuilder.getInstance());
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

            int nFragments = SMARTS_TOX.length + SMARTS_NON_TOX.length;
            
            for (int i=0; i<nFragments; i++) {
                
                // skip SAs that have been removed because they were duplicated
                // they are available in the list of alerts, but never matched
                // it's ok for now, this block only used in ToxRead
                if ( (i==(23-1)) || (i==(53-1)) || (i==(54-1)) || (i==(56-1)) || (i==(64-1)) || (i==(86-1)) ) 
                    continue;
                
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

        for (int i=0; i<SMARTS_TOX.length; i++) {
            String s = (String)SMARTS_TOX[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "MN_VER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

        for (int i=0; i<SMARTS_NON_TOX.length; i++) {
            String s = (String)SMARTS_NON_TOX[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "MN_VER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }      
}