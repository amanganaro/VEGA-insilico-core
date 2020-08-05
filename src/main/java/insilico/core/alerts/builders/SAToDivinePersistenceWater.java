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
public class SAToDivinePersistenceWater extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"c1ccc(OC)cc1", "nP"},
        {"Cc1ccccc1C", "nP"},
        {"c1(C)c2c(cccc2)ccc1", "nP"},
        {"c1(O)c(C)cccc1", "nP"},
        {"C(=O)([O;D1;!-])[A]", "nP"},
        {"O=[$([C;D2]),$([C;D3]C)][O;D2][C,c]", "nP"},
        {"O=C([a])[O;D2][C,c]", "nP"},
        {"[$(C(=[O,S])([O,S][a])N),$(C(=[O,S])([O,S])N[a])]", "nP"},
        {"[$([C;D2](=O)C)]", "nP"},
        {"[C;D3](=O)([C])[C]", "nP"},
        {"[N;D1][a]", "nP"},
        {"O=NN([C,c])[C,c]", "nP"},
        {"[O;D1;!-]A", "nP"},
        {"[O;D1;!-][C;D2;H2][C,c]", "nP"},
        {"Cl-C-C-O-C", "nP"},
        {"O=C-C-C", "nP"},
        {"N(-C-C-C)-C=O", "nP"},
        {"O-C=O", "nP"},
        {"O(-C(-C)=O)-C-C-C", "nP"},
        {"Cl-c1:c:c:c(:c:c:1)-O-C", "nP"},
        {"O(-c1:c:c:c:c:c:1)-C(-C)-C", "nP"},
        {"Cl-c1:c:c(:c:c:c:1)-C", "nP"},
        {"c1(:c(:c:c:c:c:1)-C)-C", "nP"},
        {"c12:c(:c:c:c:c:2-C):c:c:c:c:1", "nP"},
        {"c1(:c:c:c(:c:c:1)-C)-C", "nP"},
        {"N(-C(-N)=O)-C", "nP"},
        {"N-C(-C)-C", "vP"},
        {"c12:c3:c:c:c:c:2:c:c:c:c:1:c:c:c:3", "vP"},
        {"Cl-C(-Cl)-C(-Cl)-Cl", "vP"},
        {"Cl-C12-C3-C(-C(-C-2(-Cl)-Cl)(-C(=C-1-Cl)-Cl)-Cl)-C-C-C-3", "vP"}       
    };
     
   
    
    public SAToDivinePersistenceWater() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_PERSISTENCE_WATER, "Rules for Persistence - Water (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Persistence in water alert (class " + SMARTS[i][1] + ") no. " + (idx+1));
            curSA.setDescription("Structural alert for Persistence in water, values in class " + SMARTS[i][1]
                    + ", defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/perswatertodivine/PERSWATTD_" + (idx+1) + ".png");
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PERSWATTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}