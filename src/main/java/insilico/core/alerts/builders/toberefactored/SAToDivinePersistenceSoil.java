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
public class SAToDivinePersistenceSoil extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"O=C(OCCC)", "nP"},
        {"O=C(CC)C", "nP"},
        {"C(O)c1ccccc1C", "nP"},
        {"O(C)CCCl", "nP"},
        {"[P]", "nP"},
        {"AC(=O)O*", "nP"},
        {"AC(=O)O[*;!H]", "nP"},
        {"AC(=O)O", "nP"},
        {"A[O,S]C(=[O,S])N(A)A", "nP"},
        {"aC(=O)*", "nP"},
        {"Cl-c1:c(:c(:c:c:c:1)-Cl)-O", "nP"},
        {"n1:c(:n:c:n:c:1-N-C-C)-N-C-C", "nP"},
        {"n1:c:c:c:c:c:1", "nP"},
        {"c1(:c:c:c:c:c:1)-C-C-C", "nP"},
        {"O-c1:c(:c:c:c:c:1)-C", "nP"},
        {"O=C-C-C", "nP"},
        {"c1(:c:c:c(:c:c:1)-C)-C", "nP"},
        {"c1(:c:c:c:c:c:1)-C(-C)-C", "nP"},
        {"O-c1:c:c:c(:c:c:1)-C", "nP"},
        {"N-c1:c:c:c:c:c:1", "nP"},
        {"P(-O-C)(-O-C)=S", "nP"},
        {"Cl-C(-Cl)=C-Cl", "nP"},
        {"Cl-C-C-O-C", "nP"},
        {"O(-C=O)-C", "nP"},
        {"c1ccc(c(c1)c2ccccc2Cl)", "vP"},
        {"c1c(Cl)cc2Oc3cc(Cl)cc(Cl)c3Oc2c1", "vP"},
        {"o1:c2:c(:c3:c:1:c:c:c(:c:3)-Cl):c:c(:c:c:2)-Cl", "vP"},
        {"c12:c3:c(:c:c:c:2:c:c:c:c:1):c:c:c:c:3", "vP"}   
    };
     
   
    
    public SAToDivinePersistenceSoil() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_PERSISTENCE_SOIL, "Rules for Persistence - Soil (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Persistence in soil alert (class " + SMARTS[i][1] + ") no. " + (idx+1));
            curSA.setDescription("Structural alert for Persistence in soil, values in class " + SMARTS[i][1]
                    + ", defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/perssoiltodivine/PERSSOLTD_" + (idx+1) + ".png");
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PERSSOLTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}