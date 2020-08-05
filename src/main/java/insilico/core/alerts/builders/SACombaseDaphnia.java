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

import java.util.ArrayList;

/**
 *
 * @author User
 */
public class SACombaseDaphnia extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private final static String USE_BENZENE = "usebenzene";
    private QueryAtomContainer[] SA;
    private QueryAtomContainer SA_Benzene;
    
    private final static String[] SMARTS_LT_1_BENZENE = {
        // EC50 < 1 mg/L with benzene matching
        "CC(=O)O",
        "CCO",
        "CCOc1ccccc1",
        "COc1ccccc1",
        "CS",
        "Nc1ccccc1",
        "O=COc1ccccc1",
        "Oc1ccccc1"
    };

    private final static String[] SMARTS_LT_1 = {
        // EC50 < 1 mg/L
        "CCS",
        "CS"
    };
    
    private final static double[] ACCURACY = {
        0.82,	
        0.80,
        0.79,
        0.81,
        1.00,
        0.91,
        0.92,
        0.83,
        1.00,
        0.95
    };

    
    
    
    public SACombaseDaphnia() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_DAPHNIA_COMBASE, "Rules for daphnia toxicity (EC50) from COMBASE project");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_LT_1_BENZENE.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 < 1 mg/L alert no. " + (idx+1));
            curSA.setDescription("Structural alert for daphnia toxicity defined by the SMARTS (for molecules with benzene): " + SMARTS_LT_1_BENZENE[i] + ". It is related to toxicity values (EC50) less than 1 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasedaphnia/COMB_DAPHNIA_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_DAPHNIA_TOX_LESS_1, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 1.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            curSA.setBoolProperty(USE_BENZENE, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTS_LT_1.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 < 1 mg/L alert no. " + (idx+1));
            curSA.setDescription("Structural alert for daphnia toxicity defined by the SMARTS: " + SMARTS_LT_1[i] + ". It is related to toxicity values (EC50) less than 1 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasedaphnia/COMB_DAPHNIA_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_DAPHNIA_TOX_LESS_1, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 1.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            curSA.setBoolProperty(USE_BENZENE, false);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA_Benzene = SMARTSParser.parse("c1ccccc1", SilentChemObjectBuilder.getInstance());
            
            int nFragments = SMARTS_LT_1_BENZENE.length + SMARTS_LT_1.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (String s : SMARTS_LT_1_BENZENE) {
                SA[idx] = SMARTSParser.parse(s, SilentChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTS_LT_1) {
                SA[idx] = SMARTSParser.parse(s, SilentChemObjectBuilder.getInstance());
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

            int nFragments = SMARTS_LT_1_BENZENE.length + SMARTS_LT_1.length;
            
            for (int i=0; i<nFragments; i++) {
                if (Alerts.get(i).getBoolProperty(USE_BENZENE)) {
                    if (!Matches(SA_Benzene))
                        continue;
                }
                if (Matches(SA[i])) 
                    Res.add((Alert)Alerts.get(i).clone());
            }
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    public void SaveSmartsPNG() {

        ArrayList<String> list = new ArrayList<>();
        for (String s : SMARTS_LT_1_BENZENE) 
            list.add(s);
        for (String s : SMARTS_LT_1) 
            list.add(s);
                
        int idx = 1;
        for (String s : list) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "COMB_DAPHNIA_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }    
    
}