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
public class SASkinIrritationConcert extends AlertBlockFromSMARTS implements iAlertBlock {

    private final static String ACTIVE = "active";
    private final static String INACTIVE = "inactive";

    private Pattern[] SA;

    // SMARTS with inf stats from SarPy
    private final static String[][] SMARTS_MAX = {
            {"O=C(N)",INACTIVE},
            {"OCCOCCC",INACTIVE},
            {"C1(C)CC=C(C)CC1",ACTIVE},
            {"CCCCCCCCCCCCCCCCC",INACTIVE},
            {"O=Cc1ccccc1C",INACTIVE},
            {"Nc1ccccc1C",INACTIVE},
            {"c1ncncn1",INACTIVE},
            {"OCCCOC",INACTIVE},
            {"O=C(O)C(C)C",INACTIVE},
            {"OCCOCCOCC",INACTIVE},
            {"Fc1ccc(cc1)",INACTIVE},
            {"P(O)O",INACTIVE},
    };

    // SMARTS with less than inf stats from SarPy, ordered by descending value (first the best rules)
    private final static String[][] SMARTS_MIN = {
            {"c1ccc(cc1)N",INACTIVE},
            {"Cc1ccc(cc1)C(C)C",ACTIVE},
            {"C(C)C1CCCCC1",ACTIVE},
            {"O=Cc1ccc(cc1)",INACTIVE},
            {"C(Cl)Cl",ACTIVE},
            {"OC(C)CO",INACTIVE},
            {"CCC=C(C)C",ACTIVE},
            {"C(=CCC)C",ACTIVE},
            {"CC(=O)C",INACTIVE},
            {"C=CCCCCC",ACTIVE},
            {"c1ccc(cc1)C(C)C",ACTIVE},
            {"C(CBr)C",ACTIVE},
            {"CC(CC)CCCC",ACTIVE},
            {"CCl",ACTIVE},
            {"O=S",INACTIVE},
            {"O=CC=C",ACTIVE},
            {"CCCCCCC",ACTIVE},
            {"O=C(OCC(C))C",INACTIVE},
            {"C(OC)CCCCCC",ACTIVE},
            {"O=C(O)CC",INACTIVE},
            {"CCN(C)C",ACTIVE},
    };


    public SASkinIrritationConcert() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_SKIN_IRRITATION_CONCERT_B3, "Rules for Skin Irritation classification (CONCERT REACH B.3)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;

        for (int i=0; i<SMARTS_MAX.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Skin Irritation " + SMARTS_MAX[i][1] + " alert no. " + (i+1));
            curSA.setDescription("Structural alert for Skin Irritation "+ SMARTS_MAX[i][1] + " compounds with good reliability, defined by the SMARTS: " + SMARTS_MAX[i][0]);
            curSA.setSMARTS(SMARTS_MAX[i][0]);
            if (SMARTS_MAX[i][1].equalsIgnoreCase(ACTIVE))
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_IRR, true);
            else
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_NON_IRR, true);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SARPY_STATS_INF, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTS_MIN.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Skin Irritation " + SMARTS_MIN[i][1] + " alert no. " + (i+1));
            curSA.setDescription("Structural alert for Skin Irritation "+ SMARTS_MIN[i][1] + " compounds with moderate reliability, defined by the SMARTS: " + SMARTS_MIN[i][0]);
            curSA.setSMARTS(SMARTS_MIN[i][0]);
            if (SMARTS_MIN[i][1].equalsIgnoreCase(ACTIVE))
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_IRR, true);
            else
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_NON_IRR, true);
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