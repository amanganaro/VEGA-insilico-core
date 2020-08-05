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
public class SACombaseFish extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static String[] SMARTS_LET_1 = {
        // EC50 <= 1 mg/L
        "O=C(OC)C1C(C=C)C1(C)C",
        "[#7]-1-[#16]-c2ccccc2-[#6]-1=O",
        "N#Cc1cn(c(c1))"
    };

    private final static double[] ACCURACY = {
        1,
        1,
        1
    };
    
    
    
    public SACombaseFish() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_FISH_COMBASE, "Rules for fish toxicity (EC50) from COMBASE project");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_LET_1.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("EC50 <= 1 mg/L alert no. " + (i+1));
            curSA.setDescription("Structural alert for fish toxicity defined by the SMARTS: " + SMARTS_LET_1[i] + ". It is related to toxicity values (EC50) less or equal than 1 mg/l.");
            curSA.setImageURL("/insilico/core/alerts/png/combasefish/COMB_FISH_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_FISH_TOX_LESS_1, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 1.0);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, ACCURACY[idx]);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTS_LET_1.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (String s : SMARTS_LET_1) {
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

            int nFragments = SMARTS_LET_1.length;
            
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
                
        int idx = 1;
        for (String s : list) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "COMB_FISH_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }    
    
}