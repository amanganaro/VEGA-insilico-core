package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

import static insilico.core.constant.InsilicoConstants.KEY_ALERT_SKIN_NON_SENS;
import static insilico.core.constant.InsilicoConstants.KEY_ALERT_SKIN_SENS;

/**
 *
 * @author User
 */
public class SASkinNcstox extends AlertBlockFromSMARTS implements iAlertBlock {

    private Pattern[] SA;
    
    private final static String[][] SMARTS = {
        {"C(C)CCC=C(C)CC",KEY_ALERT_SKIN_SENS},
        {"C(O)CC(O)C",KEY_ALERT_SKIN_NON_SENS},
        {"O=CC=C(c1ccccc1)",KEY_ALERT_SKIN_SENS},
        {"CCCCCCCCCCCBr",KEY_ALERT_SKIN_SENS},
        {"Nc1ccccc1N",KEY_ALERT_SKIN_SENS},
        {"O=C(Cl)",KEY_ALERT_SKIN_SENS},
        {"C(C=O)c1ccccc1",KEY_ALERT_SKIN_SENS},
        {"Nc1ccc(Cl)cc1",KEY_ALERT_SKIN_SENS},
        {"CC(OCC)(C)C",KEY_ALERT_SKIN_SENS},
        {"CC(=O)C(=O)",KEY_ALERT_SKIN_SENS},
        {"COc1ccc(C)cc1OC",KEY_ALERT_SKIN_NON_SENS},
        {"Nc1ccc(C(=O))cc1",KEY_ALERT_SKIN_NON_SENS},
        {"CCOC(=O)c1ccccc1C(=O)OC",KEY_ALERT_SKIN_NON_SENS},
        {"NCCCCCC",KEY_ALERT_SKIN_NON_SENS},
        {"C(c1cc(ccc1)C)CCC",KEY_ALERT_SKIN_SENS},
        {"C1c2ccccc2C(=O)O1",KEY_ALERT_SKIN_SENS},
        {"Oc1ccc(c(c1O)C)",KEY_ALERT_SKIN_SENS},
        {"Nc1ccc(N)cc1",KEY_ALERT_SKIN_SENS},
        {"O=C(Oc1ccccc1)C(C)",KEY_ALERT_SKIN_SENS},
        {"c1ccc(N)cc1O",KEY_ALERT_SKIN_SENS},
        {"c1cc(O)ccc1N",KEY_ALERT_SKIN_SENS},
        {"CCCCCCCCCCCCCCOC",KEY_ALERT_SKIN_SENS},
        {"CCCOC(=O)c1cc(O)c(O)c(O)c1",KEY_ALERT_SKIN_SENS},
        {"CCCCCCCCCCCCCCCl",KEY_ALERT_SKIN_SENS},
        {"C(c1ccccc1)c1ccccc1",KEY_ALERT_SKIN_SENS},
        {"CC(Cc1ccccc1)C=O",KEY_ALERT_SKIN_SENS},
        {"Cc1cc(O)c(C)c(O)c1",KEY_ALERT_SKIN_SENS},
        {"c1cc(Cl)c(Cl)cc1Cl",KEY_ALERT_SKIN_SENS},
        {"c1ccc(CBr)cc1",KEY_ALERT_SKIN_SENS},
        {"n1c(=O)ccs1",KEY_ALERT_SKIN_SENS},
        {"NCCN",KEY_ALERT_SKIN_SENS},
        {"CC#C",KEY_ALERT_SKIN_SENS},
        {"CC=CCCCCC",KEY_ALERT_SKIN_SENS},
        {"C(OCCCC)C(C)",KEY_ALERT_SKIN_SENS},
        {"O(c1ccc(cc1)C=CC)",KEY_ALERT_SKIN_SENS},
        {"CN(CN)",KEY_ALERT_SKIN_SENS},
        {"OCCCCCCCCCCCCC",KEY_ALERT_SKIN_NON_SENS},
        {"O=CCCCCCC(C)C",KEY_ALERT_SKIN_SENS},
        {"CNc1ccccc1",KEY_ALERT_SKIN_SENS},
        {"c1ccccc1N(=O)=O",KEY_ALERT_SKIN_NON_SENS},
        {"O=C(c1ccccc1)CC(=O)C",KEY_ALERT_SKIN_SENS},
        {"O=CC=CCC",KEY_ALERT_SKIN_SENS},        
    };
    
    private final static String[][] SMARTS_OLD = {
        {"C(C)CCC=C(C)CC",KEY_ALERT_SKIN_SENS},
        {"O=CC=C(c1ccccc1)",KEY_ALERT_SKIN_SENS},
        {"CCCCCCCCCCCBr",KEY_ALERT_SKIN_SENS},
        {"Nc1ccccc1N",KEY_ALERT_SKIN_SENS},
        {"O=C(Cl)",KEY_ALERT_SKIN_SENS},
        {"C(C=O)c1ccccc1",KEY_ALERT_SKIN_SENS},
        {"Nc1ccc(Cl)cc1",KEY_ALERT_SKIN_SENS},
        {"CC(OCC)(C)C",KEY_ALERT_SKIN_SENS},
        {"CC(=O)C(=O)",KEY_ALERT_SKIN_SENS},
        {"C(c1cc(ccc1)C)CCC",KEY_ALERT_SKIN_SENS},
        {"C1c2ccccc2C(=O)O1",KEY_ALERT_SKIN_SENS},
        {"Oc1ccc(c(c1O)C)",KEY_ALERT_SKIN_SENS},
        {"Nc1ccc(N)cc1",KEY_ALERT_SKIN_SENS},
        {"O=C(Oc1ccccc1)C(C)",KEY_ALERT_SKIN_SENS},
        {"c1ccc(N)cc1O",KEY_ALERT_SKIN_SENS},
        {"c1cc(O)ccc1N",KEY_ALERT_SKIN_SENS},
        {"CCCCCCCCCCCCCCOC",KEY_ALERT_SKIN_SENS},
        {"CCCOC(=O)c1cc(O)c(O)c(O)c1",KEY_ALERT_SKIN_SENS},
        {"CCCCCCCCCCCCCCCl",KEY_ALERT_SKIN_SENS},
        {"C(c1ccccc1)c2ccccc2",KEY_ALERT_SKIN_SENS},
        {"CC(Cc1ccccc1)C=O",KEY_ALERT_SKIN_SENS},
        {"Cc1cc(O)c(C)c(O)c1",KEY_ALERT_SKIN_SENS},
        {"c1cc(Cl)c(Cl)cc1Cl",KEY_ALERT_SKIN_SENS},
        {"c1ccc(CBr)cc1",KEY_ALERT_SKIN_SENS},
        {"n1c(=O)ccs1",KEY_ALERT_SKIN_SENS},
        {"NCCN",KEY_ALERT_SKIN_SENS},
        {"CC#C",KEY_ALERT_SKIN_SENS},
        {"CC=CCCCCC",KEY_ALERT_SKIN_SENS},
        {"C(OCCCC)C(C)",KEY_ALERT_SKIN_SENS},
        {"O(c1ccc(cc1)C=CC)",KEY_ALERT_SKIN_SENS},
        {"CN(CN)",KEY_ALERT_SKIN_SENS},
        {"O=CCCCCCC(C)C",KEY_ALERT_SKIN_SENS},
        {"CNc1ccccc1",KEY_ALERT_SKIN_SENS},
        {"O=C(c1ccccc1)CC(=O)C",KEY_ALERT_SKIN_SENS},
        {"O=CC=CCC",KEY_ALERT_SKIN_SENS},
        {"C(O)CC(O)C",KEY_ALERT_SKIN_NON_SENS}, 
        {"COc1ccc(C)cc1OC",KEY_ALERT_SKIN_NON_SENS},
        {"Nc1ccc(C(=O))cc1",KEY_ALERT_SKIN_NON_SENS},
        {"CCOC(=O)c1ccccc1C(=O)OC",KEY_ALERT_SKIN_NON_SENS},
        {"NCCCCCC",KEY_ALERT_SKIN_NON_SENS},
        {"OCCCCCCCCCCCCC",KEY_ALERT_SKIN_NON_SENS},
        {"c1ccccc1N(=O)=O",KEY_ALERT_SKIN_NON_SENS}
    };
    
    
    
    public SASkinNcstox() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_SKIN_SENS_NCSTOX, StringSelectorCore.getString("sa_skin_ncstox_initialization"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelectorCore.getString("sa_skin_ncstox_name"), i+1));
            curSA.setDescription("Structural alert for Skin sensitization related to " + 
                    (SMARTS[i][1].equalsIgnoreCase(KEY_ALERT_SKIN_SENS)?"active":"inactive") + " compounds defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setDescription(String.format(StringSelectorCore.getString("sa_skin_ncstox_description"),
                    SMARTS[i][1].equalsIgnoreCase(KEY_ALERT_SKIN_SENS)? StringSelectorCore.getString("sa_skin_ncstox_active"): StringSelectorCore.getString("sa_skin_ncstox_nonactive"),
                    SMARTS[i][0]));

            curSA.setImageURL("/insilico/core/alerts/png/skinvermeer/SKIN_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(SMARTS[i][1], true);
            Alerts.add(curSA);
            idx++;
        }        
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTS.length;
            SA = new Pattern[nFragments];
            
            int idx = 0;
            for (String[] arr : SMARTS) {
                SA[idx] = SmartsPattern.create(arr[0], DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            
        } catch (Exception e) {
            throw new InitFailureException(StringSelectorCore.getString("sa_exception_smarts_initialization"));
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() {
        AlertList Res = new AlertList();
        
        try {

            int nFragments = SMARTS.length;
            
            for (int i=0; i<nFragments; i++) 
                if ((SA[i].matches(CurMol.GetStructure())))
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (InvalidMoleculeException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<SMARTS.length; i++) {
            String s = SMARTS[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "SKIN_NCSTOX_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

    }    
}