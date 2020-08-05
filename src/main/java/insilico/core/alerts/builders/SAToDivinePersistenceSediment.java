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
public class SAToDivinePersistenceSediment extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"O=Cc1ccccc1", "nP"},
        {"O=[$([C;D2]),$([C;D3]C)][O;D2][C,c]", "nP"},
        {"[$([C;D2](=O)C)]", "nP"},
        {"[C;D3](=O)([C])[C]", "nP"},
        {"[N;D1][$([C,c]);!$(C=[O,S])]", "nP"},
        {"[O;D1;!-]A", "nP"},
        {"Cl-c1:c(:c:c:c:c:1)-C", "nP"},
        {"Cl-C-C-O-C", "nP"},
        {"O(-c1:c:c:c:c:c:1)-C(-C)-C", "nP"},
        {"O=C-c1:c:c:c:c:c:1", "nP"},
        {"c12c(cccc1)Oc3c(cccc3)O2", "vP"},
        {"[Cl,Br,F,I][$(C(@[*])@[*]);!$(C=*)]", "vP"},
        {"c1(:c:c:c(:c:c:1)-C)-C", "vP"},
        {"o1:c2:c(:c3:c:1:c:c:c:c:3):c:c:c:c:2", "vP"},
        {"Br-C", "vP"},
        {"Cl-C(-Cl)-C", "vP"},
        {"O-c1:c(:c(:c(:c:c:1)-Cl)-Cl)-Cl", "vP"},
        {"N(-c1:c:c:c:c:c:1)-C", "vP"}
    };
     
   
    
    public SAToDivinePersistenceSediment() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_PERSISTENCE_SEDIMENT, "Rules for Persistence - Sediment (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Persistence in sediment alert (class " + SMARTS[i][1] + ") no. " + (idx+1));
            curSA.setDescription("Structural alert for Persistence in sediment, values in class " + SMARTS[i][1]
                    + ", defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/perssoiltodivine/PERSSEDTD_" + (idx+1) + ".png");
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PERSSEDTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}