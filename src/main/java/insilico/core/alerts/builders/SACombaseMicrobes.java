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

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author User
 */
public class SACombaseMicrobes extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static String[] SMARTS_LT_100 = {
        // EC50 < 100 mg/L
        "CCCCNC"
    };

    private final static String[] SMARTS_LET_1000 = {
        // EC50 <= 1000 mg/L
        "CCCCCC",
        "OCNC",
        "O=C1Oc3ccccc3(C(O)=C1C(c2ccccc2)C)",
        "C(CO)O"
    };

    private final static String[] SMARTS_GT_1000 = {
        // EC50 > 1000 mg/L
        "c1ccc(cc1)C(=O)" 
    };

    private final static double[] ACCURACY = {
        1,
        1,
        1,
        1,
        0.75,
        1
    };
    
    
    
    public SACombaseMicrobes() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MICROBES_COMBASE, "Rules for microbes toxicity (EC50) from COMBASE project");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_LT_100.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 < 100 mg/L alert no. " + (i+1));
            curSA.setDescription("Structural alert for microbes toxicity defined by the SMARTS: " + SMARTS_LT_100[i] + ". It is related to toxicity values (EC50) less than 100 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasemicro/COMB_MICRO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICROBES_TOX_LESS_100, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 100.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTS_LET_1000.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 <= 1000 mg/L alert no. " + (i+1));
            curSA.setDescription("Structural alert for microbes toxicity defined by the SMARTS: " + SMARTS_LET_1000[i] + ". It is related to toxicity values (EC50) less or equal than 1000 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasemicro/COMB_MICRO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICROBES_TOX_LESS_1000, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 1000.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTS_GT_1000.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 > 1000 mg/L alert no. " + (i+1));
            curSA.setDescription("Structural alert for microbes toxicity defined by the SMARTS: " + SMARTS_GT_1000[i] + ". It is related to toxicity values (EC50) greater than 1000 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasemicro/COMB_MICRO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICROBES_TOX_GREATER_1000, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 1000.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_LOWER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTS_LT_100.length + SMARTS_LET_1000.length + SMARTS_GT_1000.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (String s : SMARTS_LT_100) {
                SA[idx] = SMARTSParser.parse(s, DefaultChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTS_LET_1000) {
                SA[idx] = SMARTSParser.parse(s, DefaultChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTS_GT_1000) {
                SA[idx] = SMARTSParser.parse(s, DefaultChemObjectBuilder.getInstance());
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

            int nFragments = SMARTS_LT_100.length + SMARTS_LET_1000.length + SMARTS_GT_1000.length;
            
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
        list.addAll(Arrays.asList(SMARTS_LT_100));
        list.addAll(Arrays.asList(SMARTS_LET_1000));
        list.addAll(Arrays.asList(SMARTS_GT_1000));
                
        int idx = 1;
        for (String s : list) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "COMB_MICRO_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }    
    
}