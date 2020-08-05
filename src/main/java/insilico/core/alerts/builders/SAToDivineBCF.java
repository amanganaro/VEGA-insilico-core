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
public class SAToDivineBCF extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    // SMARTS, pred. class
    private final static String[][] SMARTS = {
        {"O(-C(-c1:c:c:c:c:c:1)-C)-C-C", "A"},
        {"N#C-C-c1:c:c:c:c:c:1", "A"},
        {"N-C-C(-C)-C", "A"},
        {"F-C(-F)(-F)-c1:c:c(:c:c:c:1)-Cl", "A"},
        {"O-C(-C1-C-C-C-C-C-1)=O", "A"},
        {"N(-C-C-C)(-C)-C", "A"},
        {"N(-C-C-C)(-C-C)-C", "A"},
        {"N(-c1:c:c:c:c:c:1)(-C=O)-C", "A"},
        {"C1-C-C=C-C=C-1", "A"},
        {"O=C(-C)-C", "A"},
        {"N(-C=O)(-C)-C", "A"},
        {"N(-C-C)(-C=O)-C", "A"},
        {"S-C-N", "A"},
        {"S(=O)=O", "A"},
        {"S-N", "A"},
        {"S(-N)(=O)=O", "A"},
        {"S-N-C", "A"},
        {"S(-N-C)(=O)=O", "A"},
        {"S-N-C=O", "A"},
        {"N(-C-N-C)-C", "A"},
        {"N(-C(-N-C)=O)-C", "A"},
        {"Cl-c1:c:c(:c:c(:c:1)-N)-Cl", "A"},
        {"O(-C(-C)-C)-C-C", "A"},
        {"O(-C(-C)-C)-C(-C)=O", "A"},
        {"S(-C)=O", "A"},
        {"S(-C)(-C)=O", "A"},
        {"N(-C-C-C-C)-C", "A"},
        {"O=C-c1:c(:c:c:c:c:1)-C", "A"},
        {"P(-S)-O-C", "A"},
        {"P(-S-C)-O-C", "A"},
        {"P(-S)-O-C-C", "A"},
        {"P(-S-C)-O-C-C", "A"},
        {"P(-S)(-O-C)-O-C", "A"},
        {"P(-S-C)(-O-C)-O-C", "A"},
        {"P(-S-C)(-O-C-C)-O-C", "A"},
        {"P(-S)(-O-C-C)-O-C", "A"},
        {"S(-c1:c:c:c:c:c:1)=O", "A"},
        {"S(-c1:c:c:c:c:c:1)(=O)=O", "A"},
        {"S(-N)(-c1:c:c:c:c:c:1)(=O)=O", "A"},
        {"P(-S-C-C)-O-C", "A"},
        {"P(-S-C-C)-O-C-C", "A"},
        {"P(-S-C-C)(-O-C)-O-C", "A"},
        {"P(-S)(-O-C)(-O-C)=S", "A"},
        {"P(-S-C)(-O-C)(-O-C)=S", "A"},
        {"P(-O-C-C)(-O-C)=S", "A"},
        {"N(-O)-C", "A"},
        {"n1:c(:c:c:c:c:1)-O", "A"},
        {"n1:c(:c:c:c:c:1)-O-C", "A"},
        {"O(-C-C(-O)-C)-C-C", "A"},
        {"O(-C-C(-O)-C)-C", "A"},
        {"O-C(-C-O)=O", "A"},
        {"O(-c1:c:c:c:c:c:1)-C-C(-O)=O", "A"},
        {"O(-c1:c:c:c:c:c:1)-C(-C=O)-C", "A"},
        {"O-C(-C(-O)=O)-C", "A"},
        {"O(-c1:c:c:c:c:c:1)-C(-C(-O)=O)-C", "A"},
        {"P-O-C-C", "A"},
        {"P(-O-C-C)-O", "A"},
        {"P(-O-C-C)-O-C", "A"},
        {"P(-O-C)=O", "A"},
        {"P(-O-C)(-O)=O", "A"},
        {"P(-O-C-C)=O", "A"},
        {"P(-O-C-C)(-O)=O", "A"},
        {"P(-O-C)(-O-C)=O", "A"},
        {"P(-O-C-C)(-O-C)=O", "A"},
        {"P(-O-C-C)(-O)-O", "A"},
        {"P(-O-C)(-O)(-O)=O", "A"},
        {"P(-O-C-C)(-O)(-O)=O", "A"},
        {"P(-O-C-C)(-O-C)-O", "A"},
        {"P(-O-C)(-O-C)(-O)=O", "A"},
        {"P(-O-C-C)(-O-C)(-O)=O", "A"},
        {"P(-O-C)(-O-C)-O-C", "A"},
        {"P(-O-C-C)(-O-C)-O-C", "A"},
        {"P(-O-C)(-O-C)(-O-C)=O", "A"},
        {"P(-O-C-C)(-O-C)(-O-C)=O", "A"},
        {"P(-O)=O", "A"},
        {"P(-O)(-O)=O", "A"},
        {"Cl-c1:c:n:c:c:c:1", "A"},
        {"O(-C(-C-O)=O)-C", "A"},
        {"O(-C(-C-O)-C)-C", "A"},
        {"O(-C(-C-O-C)-C)-C", "A"},
        {"N-O-C", "A"},
        {"N-O-C-C", "A"},
        {"N(-O)=C", "A"},
        {"N(-O)=C-C", "A"},
        {"N(-O-C)=C", "A"},
        {"N(-O-C)=C-C", "A"},
        {"N(-O)=C(-C)-C", "A"},
        {"Cl-c1:c(:c:c:c:c:1-N)-Cl", "A"},
        {"S(-C)(=O)=O", "A"},
        {"S(-O)(=O)=O", "A"},
        {"S(-O)(-c1:c:c:c:c:c:1)(=O)=O", "A"},
        {"n1:c(:n:c:n:c:1)-N", "A"},
        {"n1:c(:n:c:n:c:1-N)-N", "A"},
        {"S(-c1:c:c(:c:c:c:1)-N)(=O)=O", "A"},
        {"n1:c(:n:c:n:c:1)-N-C", "A"},
        {"n1:c(:n:c:n:c:1-N)-N-C", "A"},
        {"S(-c1:c(:c:c:c:c:1)-C)=O", "A"},
        {"S(-c1:c(:c:c:c:c:1)-C)(=O)=O", "A"},
        {"N(-C(-C)=O)(-C)-C", "A"},
        {"N(-C-C-O)(-C-C)-C", "A"},
        {"N(-C-C-O)-C-C", "A"},
        {"N(-C-C-O-C)-C", "A"},
        {"N(-C)(-C)-C", "A"},
        {"N(-C-C)(-C)-C", "A"},
        {"N(-C-C)(-C-C)-C", "A"},
        {"N(-C-C)(-C-C)-C-C", "A"},
        {"N(-C-C-O-C)-C-C", "A"},
        {"S(-O)(-C)(=O)=O", "A"},
        {"S=C", "A"},
        {"Cl-c1:c(:c:c:c(:c:1)-N)-Cl", "A"},
        {"N-C-O-c1:c:c:c:c:c:1", "A"},
        {"N(-C(-O-c1:c:c:c:c:c:1)=O)-C", "A"},
        {"O-C-c1:c(:c:c:c:c:1)-C", "A"},
        {"O-C(-c1:c(:c:c:c:c:1)-C)=O", "A"},
        {"P(-O-c1:c:c:c:c:c:1)-O-C-C", "A"},
        {"P(-O-c1:c:c:c:c:c:1)(-O-C)=O", "A"},
        {"S(-N)-C", "A"},
        {"S(-N-C)-C", "A"},
        {"O(-C-c1:c(:c:c:c:c:1)-C)-C", "A"},
        {"n1:c(:n:c:n:c:1-N-C)-N-C", "A"},
        {"N-C-C-C-C-C", "A"},
        {"N-C-C-C-C", "A"},
        {"O-C-C=C", "A"},
        {"O(-C-C=C)-C", "A"},
        {"O-C(-C=C)=O", "A"},
        {"Cl-c1:c(:c:c:c:c:1)-N(-O)=O", "A"},
        {"O(-C-C-O)-C-C", "A"},
        {"O(-C-C-O-C)-C", "A"},
        {"N#C-C", "A"},
        {"n1:c:n:c:c:c:1", "A"},
        {"N#C-C-C", "A"},
        {"N-C-C=O", "A"},
        {"N(-c1:c:c:c:c:c:1)=N-C", "A"},
        {"N(-c1:c:c:c:c:c:1)-C(-C)=O", "A"},
        {"O(-C(-C-C)=O)-C-C", "A"},
        {"O(-C(-C-C-C)=O)-C-C", "A"},
        {"O(-C(-C)=O)-C-C", "A"},
        {"N(-c1:c(:c:c:c:c:1)-C)-C", "A"},
        {"O=C-C1-C-C-C-C-C-1", "A"},
        {"O-C-C1-C-C-C-C-C-1", "A"},
        {"O-C-C1-C-C(-C-C-C-1)-C", "A"},
        {"C(-C-C-C-C-C-C-C)-C-C-C-C-C-C-C", "A"},
        {"O(-C(-C)=O)-C-C-C", "A"},
        {"O(-C-C-C)-C=O", "A"},
        {"N-c1:c:c:c(:c:c:1)-N", "A"},
        {"Cl-c1:c(:c(:c:c:c:1)-Cl)-N", "A"},
        {"O(-c1:c:c:c:c:c:1)-C-C=O", "A"},
        {"Cl-c1:c:c:c(:c:c:1)-N(-O)=O", "A"},
        {"N(-c1:c:c:c:c:c:1)-C-O-C", "A"},
        {"Cl-c1:c(:c:c:c(:c:1)-Cl)-N", "A"},
        {"O-c1:c:c:c(:c:c:1)-C-C-C", "A"},
        {"N(-C(-C)-C)-C", "A"},
        {"N(-C(-C)-C)-C=O", "A"},
        {"N(-C(-C)-C)-C-C", "A"},
        {"N(-C(-C)=O)-C-C", "A"},
        {"Cl-C-C=O", "A"},
        {"S(-C)-C", "A"},
        {"S(-C-C)-C", "A"},
        {"Cl-C12-C3-C(-C(-C-2(-Cl)-Cl)(-C(=C-1-Cl)-Cl)-Cl)-C-C-C-3", "C"},
        {"Cl-C12-C(-C(-C(=C-2-Cl)-Cl)(-C-C-C-C-C-1)-Cl)(-Cl)-Cl", "C"},
        {"Cl-C12-C-C(-C(=C-2-Cl)-Cl)(-C=C-1)-Cl", "C"},
        {"Cl-C12-C(-C(-C(=C-2-Cl)-Cl)(-C-C-C-C(-C-1)-Cl)-Cl)(-Cl)-Cl", "C"},
        {"F-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C(-F)(-F)-C", "C"}        
    };
     
   
    
    public SAToDivineBCF() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_TODIVINE_BCF, "Rules for BCF (toDivine)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("BCF alert (class " + SMARTS[i][1] + ") no. " + (idx+1));
            curSA.setDescription("Structural alert for BCF values in class  " + SMARTS[i][1]
                    + "defined by the SMARTS: " + SMARTS[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/bcftodivine/BCFTD_" + (idx+1) + ".png");
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "BCFTD_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}