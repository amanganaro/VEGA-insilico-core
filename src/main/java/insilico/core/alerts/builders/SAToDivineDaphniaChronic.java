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
public class SAToDivineDaphniaChronic extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"O(-C-C-C-C-C-C-C-C-C-C)-C-C-C", "A"},
        {"O(-C-C-C-C-C-C-C-C-C-C)-C", "A"},
        {"O(-C(-C-C)=O)-C-C-C-C-C-C-C-C-C", "A"},
        {"O(-C(-C(-C)=C)=O)-C-C-C-C-C-C", "A"},
        {"O(-C(-C-C)=O)-C-C-C-C-C-C", "A"},
        {"O(-C-C-C-C-C-C-C-C-C)-C-C-C", "A"},
        {"O(-C-C-C-C-C-C-C-C)-C-C", "A"},
        {"O(-C-C-C-C-C-C-C-C-C)-C", "A"},
        {"O(-C(-C)=O)-C-C-C-C-C-C-C-C", "A"},
        {"O(-C(-C=C)=O)-C-C-C-C", "A"},
        {"O(-C(-C=C)=O)-C-C-C-C-C-C", "A"},
        {"O(-C(-C)=O)-C-C-C-C-C-C", "A"},
        {"Cl-c1:c:c:c:c:c:1", "A"},
        {"C(-C-C-C-C-C-C)-C-C-C-C-C", "A"},
        {"C(-C-C-C-C-C)(-C-C-C)-C", "A"},
        {"O(-C(-C-C-C-C-C-C-C-C)=O)-C", "A"},
        {"C(-C-C-C-C-C)(-C)-C", "A"},
        {"C(-C-C-C-C-C-C-C)(-C)-C", "A"},
        {"C(-C-C-C-C)(-C)(-C)-C", "A"},
        {"O(-C-C(-C-C-C-C)-C-C)-C=O", "A"},
        {"O(-C-C(-C-C)-C)-C=O", "A"},
        {"O(-C(-C)=O)-C-C(-C-C)-C", "A"},
        {"O(-C(-C)=O)-C-C(-C-C-C-C)-C-C", "A"},
        {"O(-C-C(-C-C-C-C)-C-C)-C", "A"},
        {"O(-C-C(-C-C)-C)-C", "A"},
        {"N(-C-C-C-C-C-C)-C-C-C", "A"},
        {"C1(-C-C-C-C-C-1)-C", "A"},
        {"O(-C-C-C-C-C-C-C-C-C-C-C-C-C)-C-C", "A"},
        {"S(-N)(-c1:c:c:c:c:c:1)(=O)=O", "C"},
        {"S(-N)(-c1:c:c:c(:c:c:1)-C)(=O)=O", "C"},
        {"O(-C-C-C-C)-C-C", "A"},
        {"O(-C-C-C-C-C-C)-C-C", "A"},
        {"O(-C(-C)=O)-C-C-C-C", "A"}
    };
     
    
    
    public SAToDivineDaphniaChronic() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_DAPHNIA_CHRONIC, "Rules for Daphnia chronic toxicity (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Daphnia chronic toxicity alert (class " + SMARTS[i][1] + ") no. " + (idx+1));
            curSA.setDescription("Structural alert for Daphnia chronic toxicity values in class  " + SMARTS[i][1]
                    + "defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/daphniachronictodivine/DCHTD_" + (idx+1) + ".png");
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "DCHTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}