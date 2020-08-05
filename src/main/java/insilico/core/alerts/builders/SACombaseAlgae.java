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
public class SACombaseAlgae extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static String[] SMARTS_LET_1 = {
        // EC50 <= 1 mg/L
        "c1cc(c(c(c1Br)Br)Br)Br",
        "c1ccc(cc1)Nc2ccccc2"
    };

    private final static String[] SMARTS_LET_100 = {
        // EC50 <= 100 mg/L
        "c1ccc(cc1)Cl",
        "CCCCCCCCCC",
        "c1ccc2ccccc2(c1)",
        "c1ccc(cc1)Cc2ccccc2",
        "N(C)(C)CC",
        "c1ccc(cc1)c2ccccc2",
        "n1ccccc1",
        "c1ccc(cc1)Br",
        "C=Cc1ccccc1",
        "n1csc2ccccc12",
        "c1cc(cc(c1)C)C",
        "ON",
        "c1cc(ccc1C)C",
        "C(Cl)Cl",
        "N#CC=C",
        "NN",
        "Nc1ccc(cc1)C",
        "C=CCCCC=CC",
        "Nc1ccc(N)cc1",
        "O=CC(C)C",
        "OPO",
        "SS",
        "Oc1ccccc1C(C)C",
        "O=C(OCCC)C=C",
        "c1ccc(c(c1)CC)CC",
        "CCCCCS",
        "O=CCO",
        "C=CC=C",
        "O1CC1"
    };

    private final static String[] SMARTS_GT_100 = {
        // EC50 > 100 mg/L
        "O(C)C(C)(C)C",
        "OCCOCC"
    };

    private final static double[] ACCURACY = { // ordering based on previous arrays
        1.00,
        0.80,
        1.00,
        0.97,
        1.00,
        1.00,
        0.90,
        1.00,
        1.00,
        1.00,
        1.00,
        1.00,
        0.93,
        1.00,
        1.00,
        1.00,
        1.00,
        0.85,
        1.00,
        1.00,
        1.00,
        0.90,
        0.83,
        1.00,
        1.00,
        1.00,
        1.00,
        1.00,
        1.00,
        1.00,
        1.00,
        1.00,
        0.71        
    };
    
    
    public SACombaseAlgae() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_ALGAE_COMBASE, "Rules for algae toxicity (EC50) from COMBASE project");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_LET_1.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 <= 1 mg/L alert no. " + (i+1));
            curSA.setDescription("Structural alert for algae toxicity defined by the SMARTS: " + SMARTS_LET_1[i] + ". It is related to toxicity values (EC50) less or equal than 1 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasealgae/COMB_ALGAE_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_ALGAE_TOX_LESS_1, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 1.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTS_LET_100.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 <= 100 mg/L alert no. " + (i+1));
            curSA.setDescription("Structural alert for algae toxicity defined by the SMARTS: " + SMARTS_LET_100[i] + ". It is related to toxicity values (EC50) less or equal than 100 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasealgae/COMB_ALGAE_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_ALGAE_TOX_LESS_100, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 100.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTS_GT_100.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 > 100 mg/L alert no. " + (i+1));
            curSA.setDescription("Structural alert for algae toxicity defined by the SMARTS: " + SMARTS_GT_100[i] + ". It is related to toxicity values (EC50) greater than 100 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasealgae/COMB_ALGAE_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_ALGAE_TOX_GREATER_100, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 100.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_LOWER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTS_LET_1.length + SMARTS_LET_100.length + SMARTS_GT_100.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (String s : SMARTS_LET_1) {
                SA[idx] = SMARTSParser.parse(s, SilentChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTS_LET_100) {
                SA[idx] = SMARTSParser.parse(s, SilentChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTS_GT_100) {
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

            int nFragments = SMARTS_LET_1.length + SMARTS_LET_100.length + SMARTS_GT_100.length;
            
            for (int i=0; i<nFragments; i++) 
                if (Matches(SA[i])) 
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    public void SaveSmartsPNG() {

        ArrayList<String> list = new ArrayList<>();
        for (String s : SMARTS_LET_1) 
            list.add(s);
        for (String s : SMARTS_LET_100) 
            list.add(s);
        for (String s : SMARTS_GT_100) 
            list.add(s);
                
        int idx = 1;
        for (String s : list) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "COMB_ALGAE_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }    
}