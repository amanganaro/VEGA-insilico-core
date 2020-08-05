package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 *
 * @author User
 */
public class SASkinVermeer extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static Object[][] SMARTS_NON_SENS = {
        {"C(O)NC(C)C", 1.000},
        {"O=C(c1ccc(OC)cc1)C", 1.000},
        {"c1ccc2cc(O)ccc2c1", 1.000},
        {"O=S(=O)(N)c1cc(ccc1C)", 1.000},
        {"C(O)C(O)C(O)C(O)CO", 1.000},
        {"O=C(O)c1ccc(N)cc1", 1.000},
        {"C(c1cccc(c1)C)NC", 1.000},
        {"CC(O)(C(=O)O)C", 1.000},
        {"CCC1CCCC1CCCCC", 0.889},
        {"Cc1cccc(OCCC)c1", 0.769}      
    };
    
    private final static Object[][] SMARTS_SENS = {
        {"Nc1ccc(c(c1))Cl", 1.000},
        {"CCCCCCCCCCCBr", 1.000},
        {"O=[N+]([O-])c1ccccc1N", 1.000},
        {"O=CC(=O)C", 1.000},
        {"O=CCl", 1.000},
        {"Oc1cc(cc(c1O)C)", 1.000},
        {"c1ccc(cc1Cl)Cl", 1.000},
        {"C(OCCCC)C=C", 1.000},
        {"C#CC", 1.000},
        {"O=C(O)CCCCCCCCCCCCCCC", 1.000},
        {"C(OCCCCC)c1ccccc1", 1.000},
        {"O=[N+]([O-])c1ccc(c(c1)[N+](=O)[O-])", 1.000},
        {"N(c1ccc(N)c(c1)C)", 1.000},
        {"c1ccc(cc1)CBr", 1.000},
        {"O=C1NSC=C1", 1.000},
        {"C(=S)S", 1.000},
        {"O=C(OCCc1ccccc1)", 1.000},
        {"O=C(Oc1ccccc1)C(C)", 1.000},
        {"Oc1nc(c(cc1Cl)Cl)", 1.000},
        {"n1ccccc1C", 1.000},
        {"n1c(nc(nc1))", 1.000},
        {"CCCC(C)Br", 1.000},
        {"C=C(C)CCC=C(C)", 0.950},
        {"O=CC(=C)C(C)", 0.938},
        {"Nc1ccc(cc1)N", 0.929},
        {"O=C(OC)C=C", 0.895},
        {"Oc1ccc(cc1)[N+](=O)[O-]", 0.875},
        {"O=C(c1ccccc1)CC(=O)C", 0.875},
        {"c1cc(O)c(O)c(O)c1", 0.857},
        {"CC=CC=CC", 0.857},
        {"O=CC(C)CCC=C", 0.833},
        {"c1ccc(N=C)cc1", 0.833},
        {"NC1CCCCC1", 0.833},
        {"C(OCC)COc1ccccc1", 0.800},
        {"Cc1ccc(cc1)C", 0.786},
        {"CC(c1ccccc1)CC", 0.769}
    };
     
    
    public SASkinVermeer() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_SKIN_SENS_VERMEER, "Rules for Skin Sensitization (LLNA) classification (IRFMN/VERMEER)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_SENS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Skin sensitizer alert no. " + (i+1));
            curSA.setDescription("Structural alert for Skin sensitization defined by the SMARTS: " + SMARTS_SENS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/skinvermeer/SKIN_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_SENS, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS_SENS[i][1]);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTS_NON_SENS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Skin NON sensitizer alert no. " + (i+1));
            curSA.setDescription("Structural alert for Skin NON sensitization defined by the SMARTS: " + SMARTS_NON_SENS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/skinvermeer/SKIN_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_NON_SENS, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS_NON_SENS[i][1]);
            Alerts.add(curSA);
            idx++;
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTS_SENS.length + SMARTS_NON_SENS.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (Object[] arr : SMARTS_SENS) {
                SA[idx] = SMARTSParser.parse((String)arr[0], SilentChemObjectBuilder.getInstance());
                idx++;
            }
            for (Object[] arr : SMARTS_NON_SENS) {
                SA[idx] = SMARTSParser.parse((String)arr[0], SilentChemObjectBuilder.getInstance());
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

            int nFragments = SMARTS_SENS.length + SMARTS_NON_SENS.length;
            
            for (int i=0; i<nFragments; i++) 
                if (Matches(SA[i])) 
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<SMARTS_SENS.length; i++) {
            String s = (String)SMARTS_SENS[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "SKIN_VER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

        for (int i=0; i<SMARTS_NON_SENS.length; i++) {
            String s = (String)SMARTS_NON_SENS[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "SKIN_VER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }    
}