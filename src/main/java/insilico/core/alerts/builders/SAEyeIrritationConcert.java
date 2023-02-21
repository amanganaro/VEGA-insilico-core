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
public class SAEyeIrritationConcert extends AlertBlockFromSMARTS implements iAlertBlock {

    private final static String ACTIVE = "active";
    private final static String INACTIVE = "inactive";

    private Pattern[] SA;

    // SMARTS with inf stats from SarPy
    private final static String[][] SMARTS_MAX = {
            {"CC(=O)Nc1ccccc1",INACTIVE},
            {"CCCC(CCC)CCC",INACTIVE},
            {"CCCC(C)COCC",INACTIVE},
            {"CCCCOC(=O)c1ccc(cc1)",INACTIVE},
            {"CCc2cc(C)cc(c2)C(C)",INACTIVE},
            {"Nc1c2ccccc2ccc1",INACTIVE},
            {"c2nc(N)nc(N)n2",INACTIVE},
            {"CC(CC)S(=O)(=O)O",ACTIVE},
            {"Cc1ccc(NC)cc1",INACTIVE},
            {"COOC(C)(C)C",INACTIVE},
            {"CCC(=CC)C",INACTIVE},
            {"CCCCCCCCCCCCC=C",INACTIVE},
            {"CO[Si]",INACTIVE},
            {"CC(C)(c1ccccc1)C(C)",INACTIVE},
            {"CC(=C)C(=O)OCCO",INACTIVE},
            {"NN",INACTIVE},
            {"O[Si](C)(C)O[Si](C)(C)O[Si](C)(C)",INACTIVE},
            {"n3c[nH]c4c3cccc4",INACTIVE},
            {"CCC(C)(O)C#C",ACTIVE},
            {"c1ccc(NC(=O)N)cc1",INACTIVE},
            {"CCOP(OCC)OCC",INACTIVE},
            {"c1ccc(cc1)C(=O)C(C)",INACTIVE},
            {"P(c1ccccc1)",INACTIVE},
            {"CCC(=O)C(C)C",INACTIVE},
    };

    // SMARTS with less than inf stats from SarPy, ordered by descending value (first the best rules)
    private final static String[][] SMARTS_MIN = {
            {"CCCCCCCCC(=O)N(C)",ACTIVE},
            {"CCCCCCCCN(C)C",ACTIVE},
            {"COC(=O)CCO",ACTIVE},
            {"c1c(cccc1)c1ccccc1",INACTIVE},
            {"Cc1ccccc1C",INACTIVE},
            {"Nc1c(C)cccc1",INACTIVE},
            {"C(=O)OCCCCCCCC",INACTIVE},
            {"CC(C)CCCCCCCC",INACTIVE},
            {"P(Oc1ccccc1)",INACTIVE},
            {"OC=C",INACTIVE},
            {"c1c(Cl)cccc1",INACTIVE},
            {"C(C)OCC(C)OCCO",ACTIVE},
            {"CC(N)C(=O)",INACTIVE},
            {"c1cccnc1",INACTIVE},
            {"OCCC(CC)C(=O)O",INACTIVE},
            {"C(CS)C",ACTIVE},
            {"CCCOCC=C",INACTIVE},
            {"CCCCCCCCCCCCCCC",INACTIVE},
            {"CCCOC(=O)CCCCC",INACTIVE},
            {"CNCC=C",INACTIVE},
            {"CCCCCCCCCCCCCCC(=O)",INACTIVE},
            {"Nc1ccc(O)cc1",ACTIVE},
            {"CC(COCCCO)",INACTIVE},
            {"c1ccncn1",INACTIVE},
            {"OCCCO",INACTIVE},
            {"OC(=O)C=C",ACTIVE},
            {"CC=CC=O",ACTIVE},
    };


    public SAEyeIrritationConcert() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_EYE_IRRITATION_CONCERT, "Rules for Eye Irritation classification (CONCERT REACH B.3)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;

        for (int i=0; i<SMARTS_MAX.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Eye Irritation " + SMARTS_MAX[i][1] + " alert no. " + (i+1));
            curSA.setDescription("Structural alert for Eye Irritation "+ SMARTS_MAX[i][1] + " compounds with good reliability, defined by the SMARTS: " + SMARTS_MAX[i][0]);
            if (SMARTS_MAX[i][1].equalsIgnoreCase(ACTIVE))
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_EYE_IRR, true);
            else
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_EYE_NON_IRR, true);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SARPY_STATS_INF, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTS_MIN.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Eye Irritation " + SMARTS_MIN[i][1] + " alert no. " + (i+1));
            curSA.setDescription("Structural alert for Eye Irritation "+ SMARTS_MIN[i][1] + " compounds with moderate reliability, defined by the SMARTS: " + SMARTS_MIN[i][0]);
            if (SMARTS_MIN[i][1].equalsIgnoreCase(ACTIVE))
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_EYE_IRR, true);
            else
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_EYE_NON_IRR, true);
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