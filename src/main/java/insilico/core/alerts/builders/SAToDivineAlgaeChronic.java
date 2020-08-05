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
public class SAToDivineAlgaeChronic extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"O=C-c1:c(:c:c(:c:c:1)-C)-C", "A2"},
        {"O=C-C(-C)=C-c1:c:c:c:c:c:1", "A2"},
        {"O-c1:c(:c:c:c:c:1)-C=O", "A2"},
        {"O(-C(-C)=O)-C-C=C", "A2"},
        {"c1(:c:c:c:c:c:1)-C(-C-C-C-C)-C", "A2"},
        {"O-C(-C(-c1:c:c:c:c:c:1)=O)(-C)-C", "A2"},
        {"N(-c1:c:c:c(:c:c:1)-N)-C", "A2"},
        {"O(-C-C-C-C-C-C-C-C-C)-C", "A2"},
        {"C(-C-C-C=C)-C-C-C", "A2"},
        {"O(-C-C(-C)-C)-C-C=C", "A2"},
        {"O-C-C(-C-C-C-C-C-C)-C-C-C-C", "A2"},
        {"O(-C-C-C)-C=C", "B"},
        {"N-c1:c(:c:c:c:c:1)-O-C", "B"},
        {"O(-C-C-C(-O)-C-C)-C-C-C", "B"},
        {"O(-C-c1:c:c:c:c:c:1)-C-C", "A2"}
    };
     
   
    
    public SAToDivineAlgaeChronic() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_ALGAE_CHRONIC, "Rules for Algae chronic toxicity (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Algae chronic toxicity alert (class " + SMARTS[i][1] + ") no. " + (idx+1));
            curSA.setDescription("Structural alert for Algae chronic toxicity values in class  " + SMARTS[i][1]
                    + "defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/algaechronictodivine/ACRTD_" + (idx+1) + ".png");
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new QueryAtomContainer[SMARTS.length];
            
            int idx = 0;
            for (String[] arr : SMARTS) {
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

            for (int i=0; i<SA.length; i++) {
                if (Matches(SA[i])) 
                    Res.add((Alert)Alerts.get(i).clone());
            }            
            
        } catch (CloneNotSupportedException | CDKException e ) {
            return null;
        } 
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<SMARTS.length; i++) {
            String s = (String)SMARTS[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "ACRTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}