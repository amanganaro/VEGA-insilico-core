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
public class SAToDivineAndrogen extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"n1(:n:c:n:c:1)-C-C(-O)(-C)-C", "Active"},
        {"Cl-c1:c(:c:c:c:c:1)-C(-C)-C", "Active"},
        {"O-C1-C2(-C(-C-C-1)-C-C-C-C-2)-C", "Active"},
        {"O-c1:c:c:c(:c:c:1)-C-C(-C-C)-C", "Active"},
        {"Cl-c1:c:c:c(:c:c:1)-C=C-C", "Active"},
        {"n1(:n:c:n:c:1)-C-C(-O)(-C-C-C)-C", "Active"},
        {"F-C(-F)(-F)-c1:c(:c:c:c(:c:1)-N)-N(-O)=O", "Active"},
        {"F-C(-F)(-F)-c1:c(:c:c:c(:c:1)-N)-N", "Active"},
        {"F-C(-F)(-F)-c1:c:c(:c:c:c:1)-N-C(-C(-C)-C)=O", "Active"},
        {"P(-O-c1:c:c:c(:c:c:1)-N(-O)=O)(-O-C)(-O-C)=S", "Active"},
        {"n1(:c:n:c:c:1)-C-C-O-C-C", "Active"},
        {"O-C1(-C2(-C(-C-C-1)-C-C-C-C-2)-C)-C", "Active"},
        {"O(-C1(-C2(-C(-C-C-1)-C-C-C-C-2)-C)-C(-C)=O)-C(-C)=O", "Active"},
        {"O-C1(-C2(-C(-C-C-1)-C-C-C-C-2)-C)-C(-C)=O", "Active"},
        {"O=C1-C-C-C(-C=C-1)-C", "Active"},
        {"O(-C1-C2(-C(-C-C-1)-C-C-C-C-2)-C)-C(-C)=O", "Active"},
        {"N-C-C(-O)(-C)-C", "Active"},
        {"O=C1-C2(-C(-C-C-1)-C-C-C-C-2)-C", "Active"},
        {"O-C1(-C2(-C(-C-C-1)-C-C-C-C-2)-C)-C#C", "Active"},
        {"n1:c:n:c:n:c:1", "Inactive"},
        {"n1:c(:n:c:n:c:1)-N", "Inactive"},
        {"N-c1:c:c:c(:c:c:1)-C-C", "Inactive"},
        {"[Si](-O)-C", "Inactive"},
        {"[Si]-O", "Inactive"},
        {"O(-C(-C)=O)-C-C-O-C", "Inactive"},
        {"O(-C(-C)=O)-C-C-O", "Inactive"},
        {"N(-O)=C-C", "Inactive"},
        {"O(-C(-c1:c(:c:c:c:c:1)-C(-O-C-C)=O)=O)-C-C", "Inactive"},
        {"c1(:c:c:c(:c:c:1)-C)-C-C", "Inactive"},
        {"Cl-c1:c:c:c(:c:c:1)-Cl", "Inactive"},
        {"O(-C(-C)=O)-C-c1:c:c:c:c:c:1", "Inactive"},
        {"n1:c(:n:c:n:c:1)-N-C", "Inactive"},
        {"n1:c(:c:c:c:c:1)-C", "Inactive"},
        {"N(-C(-C)-C)-C(-C)=O", "Inactive"},
        {"n1:c(:c:c:c:c:1)-O", "Inactive"},
        {"N(-C)=N", "Inactive"},
        {"O(-C-C-O-C-C)-C-C-O", "Inactive"},
        {"[Si](-O)(-O)-C", "Inactive"},
        {"[Si](-O)-O", "Inactive"},
        {"n1:c(:c:c:c:c:1)-O-C", "Inactive"},
        {"N(-C)=N-C", "Inactive"},
        {"O(-c1:c:c:c:c:c:1)-C-C(-O-C-C)=O", "Inactive"},
        {"O(-c1:c:c:c:c:c:1)-C-C(-O-C)=O", "Inactive"},
        {"F-C(-F)(-F)-C(-F)-C(-F)-F", "Inactive"},
        {"F-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)-F", "Inactive"},
        {"S(-N)(-C)(=O)=O", "Inactive"},
        {"S(-N-C)(-C)(=O)=O", "Inactive"},
        {"O(-C(-C-C-C-C-C-C)=O)-C-C", "Inactive"},
        {"O-C(-C-C-C-C-C-C-C-C-C)=O", "Inactive"},
        {"O(-C-C-O-C-C)-C-C", "Inactive"},
        {"O(-C(-C-O-C)=O)-C-C", "Inactive"},
        {"O(-C(-C-O-C)=O)-C", "Inactive"},
        {"O(-C(-C(-O)=O)-C)-C", "Inactive"},
        {"S(-c1:c:c:c(:c:c:1)-N)(=O)=O", "Inactive"},
        {"S-c1:c:c:c(:c:c:1)-N", "Inactive"},
        {"O(-C(-c1:c(:c:c:c:c:1)-C)=O)-C", "Inactive"},
        {"O(-C(-c1:c(:c:c:c:c:1)-C)=O)-C-C", "Inactive"},
        {"O(-C-c1:c(:c:c:c:c:1)-C)-C", "Inactive"},
        {"O(-C-c1:c(:c:c:c:c:1)-C)-C-C", "Inactive"},
        {"O(-C(-c1:c(:c:c:c:c:1)-C(-O-C)=O)=O)-C", "Inactive"},
        {"O(-C(-c1:c(:c:c:c:c:1)-C(-O)=O)=O)-C", "Inactive"},
        {"O(-C(-c1:c(:c:c:c:c:1)-C(-O)=O)=O)-C-C", "Inactive"},
        {"n1(:n:c:c:c:1)-C", "Inactive"},
        {"O-C(-C=C)=O", "Inactive"},
        {"c1(:c:c:c(:c:c:1)-C)-C(-C)-C", "Inactive"},
        {"n1:c:c(:c:c:c:1)-C=O", "Inactive"},
    };
     
   
    
    public SAToDivineAndrogen() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_ANDROGEN, "Rules for Androgen Receptor Binding (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Androgen receptor binding alert (" + SMARTS[i][1] + ") no. " + (idx+1));
            curSA.setDescription("Structural alert for Androgen receptor binding, related to " + SMARTS[i][1]
                    + " compounds, defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/androgentodivine/ANDROGENTD_" + (idx+1) + ".png");
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "ANDROGENTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}