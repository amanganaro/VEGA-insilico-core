package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

/**
 *
 * @author User
 */
public class SASkinSensitizationConcert extends AlertBlockFromSMARTS implements iAlertBlock {

    private final static String ACTIVE = "active";
    private final static String INACTIVE = "inactive";

    private Pattern[] SA;

    // SMARTS with inf stats from SarPy
    private final static String[][] SMARTS_MAX = {
            {"O=C(c1ccccc1)CC(=O)c1ccc(cc1)",INACTIVE},
            {"OCCOc1ccc(cc1)C(C)(C)",ACTIVE},
            {"O=CC(C)Cc1ccc(cc1)C(C)",ACTIVE},
            {"CCCCCCCCCCCBr",ACTIVE},
            {"c1ccc(cc1[N+](=O)[O-])[N+](=O)[O-]",ACTIVE},
            {"O=C(c1ccccc1)C(C(=O)C)",ACTIVE},
            {"O(c1ccc(C=CC)cc1OC)",ACTIVE},
            {"O=Cc1cccc(OCC)c1",INACTIVE},
            {"c2c(O)ccc3ccc(cc23)",INACTIVE},
            {"N#CCc1cc(OC)c(cc1)",INACTIVE},
            {"C(C)C(c1ccc(F)cc1)",INACTIVE},
            {"Oc1ccc(cc1)C(O)C",INACTIVE},
            {"OCCOCCOCCO",INACTIVE},
            {"C=C(C)CCC=C(C)CC",ACTIVE},
            {"O(c1ccc(cc1)CC=C)",ACTIVE},
            {"C(C=Cc1ccccc1)C",ACTIVE},
            {"C(OC)c1ccccc1O",ACTIVE},
            {"O=[N+]([O-])c1cc(O)ccc1",ACTIVE},
            {"O=C(O)c1ccc(cc1)C",ACTIVE},
            {"CC1C(=CCCC1(C)C)C",ACTIVE},
            {"O=C(OCC(C)(C)C)C",INACTIVE},
            {"Oc1ccc(c(c1O)C)",ACTIVE},
            {"O=C(Oc1ccccc1)",ACTIVE},
            {"c1cc(N)ccc1NC",ACTIVE},
            {"c1cc(O)c(O)c(O)c1",ACTIVE},
            {"NCCCCCC(C)C",ACTIVE},
            {"Oc1ccc(N)cc1N",ACTIVE},
            {"OCCCCCCNC",ACTIVE},
            {"Nc1ccc(c(c1))Cl",ACTIVE},
            {"c1ccc(c(c1)Cl)Cl",ACTIVE},
            {"C(OCCCC)C=C",ACTIVE},
            {"c1ccc(N=C)cc1",ACTIVE},
            {"CCCCC(C)C(N)",ACTIVE},
            {"c1ccc(cc1)CCl",ACTIVE},
            {"Cc1ccc(cc1)Cl",ACTIVE},
            {"NC(C(=O))CCC",INACTIVE},
            {"n1ccc(nc1)N",INACTIVE},
            {"C(O)NC(C)C",INACTIVE},
            {"O=CC(O)(C)C",INACTIVE},
            {"O=CC(=C)C(C)",ACTIVE},
            {"O=CCCC=C",ACTIVE},
            {"O(C)CC1OC1",ACTIVE},
            {"O=CC=CC(=O)",ACTIVE},
            {"O=NN(CN)C",ACTIVE},
            {"O=c1ccsn1",ACTIVE},
            {"C(=NC(C)C)",ACTIVE},
            {"O=C(C(=O)C)",ACTIVE},
            {"C1CC1",ACTIVE},
            {"O=CCl",ACTIVE},
            {"C(N)S",ACTIVE},
            {"C#CC",ACTIVE},
            {"C(=S)S",ACTIVE},
    };

    // SMARTS with less than inf stats from SarPy, ordered by descending value (first the best rules)
    private final static String[][] SMARTS_MIN = {
            {"NC(c1ccccc1)C",INACTIVE},
            {"c1ccc(OC)cc1O",INACTIVE},
            {"C(O)CCCCCO",INACTIVE},
            {"Cc1cccc(OCCC)c1",INACTIVE},
            {"N#CCC",INACTIVE},
            {"Cc1cccc(OCC)c1",INACTIVE},
            {"n1c2ccccc2c(cc1)C",INACTIVE},
            {"O=S(=O)(N)",INACTIVE},
            {"N(C)C(C)C",INACTIVE},
            {"CCN=C",ACTIVE},
            {"Nc1ccc(c(N)c1)",ACTIVE},
            {"CC=Cc1ccccc1",ACTIVE},
            {"O=[N+]([O-])",ACTIVE},
            {"O=CC(=O)",ACTIVE},
            {"O=C(O)C(=C)",ACTIVE},
            {"O=CC(c1ccccc1)",ACTIVE},
            {"C(O)CC(=O)",INACTIVE},
            {"O=CN(C)",INACTIVE},
            {"Oc1ccc(cc1)Cl",ACTIVE},
            {"OCCCCCCCCCCCCCC",INACTIVE},
            {"c1ccc(cc1)Cl",ACTIVE},
            {"CCCC(=CCCC)C",ACTIVE},
            {"O=C(O)CC(C)C",ACTIVE},
            {"O=Cc1ccc(O)cc1",INACTIVE},
            {"C(C(=C))CCC=C",ACTIVE},
            {"CCCCI",ACTIVE},
            {"NCCCCC(C)",ACTIVE},
            {"OC(C)C",INACTIVE},
            {"O=S(=O)",INACTIVE},
            {"O=C(c1cccc(c1)C)",INACTIVE},
            {"C3CCCCC3",INACTIVE},
            {"C(=CC)CC",ACTIVE},
            {"OCCO",INACTIVE},
            {"OCC(O)C",INACTIVE},
            {"O=C(c1ccccc1)CC",INACTIVE},
            {"Nc1ccccc1N",ACTIVE},
            {"NNC",ACTIVE},
    };


    public SASkinSensitizationConcert() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_SKIN_SENS_CONCERT, "Rules for Skin Sensitization classification (CONCERT REACH)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;

        for (int i=0; i<SMARTS_MAX.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Skin Sensitization " + SMARTS_MAX[i][1] + " alert no. " + (i+1));
            curSA.setDescription("Structural alert for Skin Sensitization "+ SMARTS_MAX[i][1] + " compounds with good reliability, defined by the SMARTS: " + SMARTS_MAX[i][0]);
            if (SMARTS_MAX[i][1].equalsIgnoreCase(ACTIVE))
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_SENS, true);
            else
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_NON_SENS, true);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SARPY_STATS_INF, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTS_MIN.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Skin Sensitization " + SMARTS_MIN[i][1] + " alert no. " + (i+1));
            curSA.setDescription("Structural alert for Skin Sensitization "+ SMARTS_MIN[i][1] + " compounds with moderate reliability, defined by the SMARTS: " + SMARTS_MIN[i][0]);
            if (SMARTS_MIN[i][1].equalsIgnoreCase(ACTIVE))
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_SENS, true);
            else
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_NON_SENS, true);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SARPY_STATS_LESS_THAN_INF, true);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        int nFragments = SMARTS_MAX.length + SMARTS_MIN.length;
        SA = new Pattern[nFragments];

        int idx = 0;
        for (String[] s : SMARTS_MAX) {
            SA[idx] = SmartsPattern.create(s[0], DefaultChemObjectBuilder.getInstance()).setPrepare(false);
            idx++;
        }
        for (String[] s : SMARTS_MIN) {
            SA[idx] = SmartsPattern.create(s[0], DefaultChemObjectBuilder.getInstance()).setPrepare(false);
            idx++;
        }

    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            int nFragments = SMARTS_MAX.length + SMARTS_MIN.length;
            
            for (int i=0; i<nFragments; i++) 
                if (SA[i].matches(CurMol.GetStructure()))
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            return null;
        }
        
        return Res; 
    }


    public void SaveSmartsPNG() {
        //
    }    
}