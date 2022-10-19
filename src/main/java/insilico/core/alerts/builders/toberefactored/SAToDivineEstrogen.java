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
public class SAToDivineEstrogen extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"O-C1(-C2(-C(-C-C-1)-C-C-C-C-2)-C)-C#C", "Active"},
        {"O1-c2:c(-C(-C=C-1)=O):c(:c:c(:c:2)-O)-O", "Active"},
        {"O1-c2:c(-C(-C=C-1)=O):c:c:c(:c:2)-O", "Active"},
        {"O1-c2:c(-C(-C=C-1)=O):c:c:c:c:2", "Active"},
        {"O1-C-C-C-c2:c-1:c:c(:c:c:2)-O", "Active"},
        {"N1-C-C-C-C-1", "Inactive"},
        {"Cl-c1:c:c:c(:c:c:1)-N", "Inactive"},
        {"N(-C-C-C)-C=O", "Inactive"},
        {"N(-C)=O", "Inactive"},
        {"N(-c1:c:c:c:c:c:1)-C(-N)=O", "Inactive"},
        {"O(-C(-C-C-C-C-C-C)=O)-C", "Inactive"},
        {"O(-C(-C-C-C-C-C-C)=O)-C-C", "Inactive"},
        {"O(-C(-C-C-C-C-C-C-C)=O)-C", "Inactive"},
        {"O(-C(-C-C-C-C-C-C-C-C)=O)-C", "Inactive"},
        {"O(-C(-C-C-C-C-C-C-C-C)=O)-C-C", "Inactive"},
        {"O(-C(-C-C-C-C-C-C-C)=O)-C-C", "Inactive"},
        {"O(-C(-C-C-C-C-C-C-C)=O)-C-C-C", "Inactive"},
        {"O(-C(-C)=O)-C-c1:c:c:c:c:c:1", "Inactive"},
        {"O-C(-C-C-C(-O)=O)=O", "Inactive"},
        {"O-C(-C-C-C-O)=O", "Inactive"},
        {"S(-c1:c:c:c(:c:c:1)-N)(=O)=O", "Inactive"},
        {"S-c1:c:c:c(:c:c:1)-N", "Inactive"},
        {"O(-C(-C-O-C)=O)-C-C", "Inactive"},
        {"O(-C(-C-O-C)=O)-C", "Inactive"},
        {"N(-C)=N", "Inactive"},
        {"O(-C(-C-C)=O)-C-C-O", "Inactive"},
        {"O(-C(-C-C-C)=O)-C-C-O", "Inactive"},
        {"N(-O)(-C)=O", "Inactive"},
        {"P(-O-C-C)(-O-C)(-O)=O", "Inactive"},
        {"P(-O-C-C)(-O-C)(-O-C)=O", "Inactive"},
        {"P(-O-C)(-O-C)(-O)=O", "Inactive"},
        {"P(-O-C)(-O-C)(-O-C)=O", "Inactive"},
        {"P(-O-C-C)(-O-C)=O", "Inactive"},
        {"P(-O-C-C)(-O-C-C)-O-C", "Inactive"},
        {"P(-O-C-C)(-O-C)-O-C", "Inactive"},
        {"P(-O-C)(-O-C)-O-C", "Inactive"},
        {"O(-C(-C-C-C)=O)-C-C-C-C", "Inactive"},
        {"O(-C(-C-C-C)=O)-C-C-C", "Inactive"},
        {"O(-C(-C-C-C-C-C)=O)-C", "Inactive"},
        {"O(-C(-C-C-C-C-C)=O)-C-C", "Inactive"},
        {"O(-C(-C-C-C-C)=O)-C-C", "Inactive"},
        {"O(-C(-C-C-C-C)=O)-C-C-C-C", "Inactive"},
        {"O(-C(-C-C-C-C)=O)-C", "Inactive"},
        {"O(-C-C=O)-C", "Inactive"},
        {"O(-C(-C=O)-C)-C", "Inactive"},
        {"O(-C(-C-C-C)=O)-C-C", "Inactive"},
        {"F-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)-F", "Inactive"},
        {"F-C(-F)(-F)-C(-F)-C(-F)-F", "Inactive"},
        {"S-C-N", "Inactive"},
        {"N(-c1:c:c:c:c:c:1)=N-C", "Inactive"},
        {"O(-C(-C=C)=O)-C", "Inactive"},
        {"n1:c:n:c:n:c:1", "Inactive"},
        {"n1:c(:n:c:n:c:1)-N-C", "Inactive"},
        {"N-C(-C)(-C)-C", "Inactive"},
        {"C1(-C-C-1)-C", "Inactive"},
        {"O=C-C1-C-C-1", "Inactive"},
        {"N-c1:c:c:c(:c:c:1)-O-C", "Inactive"},
        {"Cl-C-C-O", "Inactive"},
        {"O1-C(-C-C-C-1)-C", "Inactive"},
        {"S(-N)-C", "Inactive"},
        {"S(-N)(-C)(=O)=O", "Inactive"},
        {"O(-C(-C(-O)=O)-C)-C", "Inactive"},
        {"N(-O)-C", "Inactive"}
    };
     
   
    
    public SAToDivineEstrogen() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_ESTROGEN, "Rules for Estrogen Receptor Binding (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;

        for (String[] smart : SMARTS) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx + 1)));
            curSA.setName("Estrogen receptor binding alert (" + smart[1] + ") no. " + (idx + 1));
            curSA.setDescription("Structural alert for Estrogen receptor binding, related to " + smart[1]
                    + " compounds, defined by the SMARTS: " + smart[0]);
            curSA.setImageURL("/insilico/core/alerts/png/estrogentodivine/ESTROGENTD_" + (idx + 1) + ".png");
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "ESTROGENTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}