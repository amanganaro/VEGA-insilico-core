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

/**
 *
 * @author User
 */
public class SAToDivineDaphniaAcute extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"N(-C-C-C-C-C-C-C-C-C=C-C-C-C-C-C-C-C-C)-C-C", "A"},
        {"S-C-C-C-C-C-C-C-C", "A"},
        {"S(-N)-C", "B"},
        {"O(-C-O)-C-C-C-C", "B"},
        {"c1(:c:c:c:c:c:1)-C=C", "B"},
        {"S(-N(-C)-C)-C", "B"},
        {"F-C(-F)(-F)-C-C-C", "B"},
        {"Cl-C-Cl", "C"},
        {"N-C-C1-C-C(-C-C-C-1)-C", "C"},
        {"O(-C1-C(-C-C-C-1)-C)-C(-C)=O", "C"},
        {"Br-C(-C=O)(-C)-C", "C"},
        {"Br-C(-C)-C", "C"},
        {"N-C-C(-C)(-C)-C", "C"},
        {"O-C1(-C2(-C(-C-C-1)-C-C-C-C-2)-C)-C", "C"},
        {"O-C-C=C(-C)-C", "C"},
        {"O=C1-C-C-C(-C=C-1)-C", "C"},
        {"O(-C-C=C-C)-C-C", "C"},
        {"O(-C-C=C-C)-C", "C"},
        {"O(-C(-C)=O)-C-C=C(-C)-C", "C"},
        {"O(-C(-C(-O)-C)=O)-C-C-C", "D"},
        {"N-C-C-C-C-C-C(-O)=O", "D"},
        {"N-C-C-C-C-C-C=O", "D"},
        {"N-c1:c(:c:c:c:c:1)-O", "D"},
        {"S(-c1:c:c(:c:c:c:1)-N)(=O)=O", "D"},
        {"S(-O)(-c1:c:c(:c:c:c:1)-N)(=O)=O", "D"},
        {"N(-C-C-C-C-C)-C=O", "D"},
        {"O-C(-C-C-O)=O", "D"},
        {"O-C(-C-C-O)(-C)-C", "D"},
        {"S(-O-C-C)(-O)(=O)=O", "D"},
        {"O1-C(-c2:c(-C-1=O):c:c:c:c:2)=O", "D"},
        {"O-C(-C(-O)=O)-C", "D"}
    };
     
    
    
    public SAToDivineDaphniaAcute() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_DAPHNIA_ACUTE, "Rules for Daphnia acute toxicity (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Daphnia acute toxicity alert (class " + SMARTS[i][1] + ") no. " + (idx+1));
            curSA.setDescription("Structural alert for Daphnia acute toxicity values in class  " + SMARTS[i][1]
                    + "defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/daphniaacutetodivine/DACTD_" + (idx+1) + ".png");
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
                SA[idx] = SMARTSParser.parse((String)arr[0], DefaultChemObjectBuilder.getInstance());
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "DACTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}