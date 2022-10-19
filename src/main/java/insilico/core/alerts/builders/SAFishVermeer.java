package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smarts.SmartsPattern;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 *
 * @author User
 */
@Slf4j
public class SAFishVermeer extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA_Set1_Scaffold;
    private Pattern[][] SA_Set2_Scaffold_Alerts;
    
    // SMARTS, pred. class, accuracy
    private final static Object[][] Set1_Scaffold = {
        {"O=C(OCc1cccc(Oc2ccccc2)c1)C3CC3", "A", 1.0},
        {"O=C1NC=CC=C1", "D", 1.0}, 
        {"C1=CC2CC1C3CCCC23", "A", 1.0},
        {"O=C(OCc1ccccc1)C2CC2", "A", 1.0}
    };
     
    // SMARTS scaffold, SMARTS Alert, pred. class, accuracy
    private final static Object[][] Set2_Scaffold_Alerts = {
        {"c1ccncc1", "O-c1:c:n:c:c:c:1", "D", 1.0 },
        {"c1ccncc1", "O=C1-N-C=C-C=C-1", "D", 1.0 },
        {"c1ccncc1", "n1:c:c:c(:c:c:1)-C-C", "D", 1.0 },
        {"c1ccc(Cc2ccccc2)cc1", "Cl-C(-Cl)(-Cl)-C(-c1:c:c:c:c:c:1)-c2:c:c:c:c:c:2", "A", 1.0 },
        {"c1ccc(Cc2ccccc2)cc1", "O-c1:c(:c:c(:c:c:1)-Cl)-C-c2:c:c:c:c:c:2", "A", 1.0 },
        {"c1ccc2ccccc2c1", "O", "B", 1.0 },
        {"c1ccc2ccccc2c1", "O-c1:c2:c(:c:c:c:1):c:c:c:c:2", "B", 1.0 },
        {"c1ccc2ccccc2c1", "O-c1:c:c:c:c:c:1", "B", 1.0 },
        {"C1CCCCC1", "N(-C1-C-C-C-C-C-1)-C", "C", 1.0 },
        {"C1CCCCC1", "N-C1-C-C-C(-C-C-1)-C-C", "C", 1.0 },
        {"C1CCCCC1", "O=C1-C(-C-C-C-C-1)-C", "C", 1.0 },
        {"c1ccc(Oc2ccccc2)cc1", "N#C-C(-O-C(-C)=O)-c1:c:c(:c:c:c:1)-O-c2:c:c:c:c:c:2", "A", 1.0 },
        {"c1ccc(Oc2ccccc2)cc1", "N#C-C(-O-C(-C1-C(-C-1-C)(-C)-C)=O)-c2:c:c(:c:c:c:2)-O-c3:c:c:c:c:c:3", "A", 1.0 },
        {"c1ccc(Oc2ccccc2)cc1", "N#C-C(-O-C(-C1-C(-C-1-C=C)(-C)-C)=O)-c2:c:c(:c:c:c:2)-O-c3:c:c:c:c:c:3", "A", 1.0 },
        {"c1ccc(Oc2ccccc2)cc1", "N-C", "B", 1.0 },
        {"c1ccc(Oc2ccccc2)cc1", "N-c1:c:c:c:c:c:1", "B", 1.0 },
        {"c1ccc(Oc2ccccc2)cc1", "O(-c1:c:c:c(:c:c:1)-O)-c2:c:c:c:c:c:2", "B", 1.0 },
        {"O=C1NC(=O)c2ccccc21", "N1(-C(-c2:c(-C-1=O):c:c:c:c:2)=O)-C", "B", 1.0 },
        {"O=C(Nc1ccccc1)c2ccccc2", "Cl-c1:c:c(:c:c:c:1)-N", "A", 1.0 },
        {"C1=CCCCC1", "Cl-C12-C3-C(-C(-C-2(-Cl)-Cl)(-C(=C-1-Cl)-Cl)-Cl)-C-C-C-3", "A", 1.0 },
        {"c1cncnc1", "O=C", "D", 1.0 },
        {"C1=CC2CCC1C2", "Cl-C12-C3-C(-C(-C-2(-Cl)-Cl)(-C(=C-1-Cl)-Cl)-Cl)-C-C-C-3", "A", 1.0 },
        {"O=C1CCCCC1", "O=C1-C(-C-C-C-C-1)-C", "C", 1.0 }
    };
     
   
    
    public SAFishVermeer() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_FISH_ACUTE_VERMEER, "Rules for fish acute toxicity (IRFMN/VERMEER)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<Set1_Scaffold.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Fish acute tox alert no. " + (idx+1));
            curSA.setDescription("Structural alert for fish acute toxicity values "
                    + ConvertExpValueToClass((String)Set1_Scaffold[i][1]) + ", applied to compounds having at least one "
                    + "ring structure and matching with the scaffold defined by the SMARTS: " + Set1_Scaffold[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/fishvermeer/FISHVERMEER_" + (idx+1) + ".png");
            curSA.setBoolProperty(ConvertExpValueToKey((String)Set1_Scaffold[i][1]), true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)Set1_Scaffold[i][2]);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<Set2_Scaffold_Alerts.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Fish acute tox alert no. " + (idx+1));
            curSA.setDescription("Structural alert for fish acute toxicity values "
                    + ConvertExpValueToClass((String)Set2_Scaffold_Alerts[i][2]) + ", applied to compounds having at least one "
                    + "ring structure, matching with the scaffold defined by the SMARTS: " + Set2_Scaffold_Alerts[i][0] + 
                    " and with the alert defined by the SMARTS: " + Set2_Scaffold_Alerts[i][1]);
            curSA.setImageURL("/insilico/core/alerts/png/fishvermeer/FISHVERMEER_" + (idx+1) + ".png");
            curSA.setBoolProperty(ConvertExpValueToKey((String)Set2_Scaffold_Alerts[i][2]), true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)Set2_Scaffold_Alerts[i][3]);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    private String ConvertExpValueToKey(String ExpValue) {
        switch (ExpValue) {
            case "A":
                return InsilicoConstants.KEY_ALERT_FISH_TOX_LESS_1;
            case "B":
                return InsilicoConstants.KEY_ALERT_FISH_TOX_1_10;
            case "C":
                return InsilicoConstants.KEY_ALERT_FISH_TOX_10_100;
            case "D":
                return InsilicoConstants.KEY_ALERT_FISH_TOX_OVER_100;
            default:
                return "";
        }
    }
    
    
    private String ConvertExpValueToClass(String ExpValue) {
        switch (ExpValue) {
            case "A":
                return "less than 1 mg/l";
            case "B":
                return "beetween 1 and 10 mg/l";
            case "C":
                return "beetween 10 and 100 mg/l";
            case "D":
                return "over 100 mg/l";
            default:
                return "";
        }
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA_Set1_Scaffold = new Pattern[Set1_Scaffold.length];
            SA_Set2_Scaffold_Alerts = new Pattern[Set2_Scaffold_Alerts.length][2];
            
            int idx = 0;
            for (Object[] arr : Set1_Scaffold) {
                SA_Set1_Scaffold[idx] = SmartsPattern.create((String)arr[0], DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            
            idx = 0;
            for (Object[] arr : Set2_Scaffold_Alerts) {
                SA_Set2_Scaffold_Alerts[idx][0] = SmartsPattern.create((String)arr[0], DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                SA_Set2_Scaffold_Alerts[idx][1] = SmartsPattern.create((String)arr[1], DefaultChemObjectBuilder.getInstance()).setPrepare(false);
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

            // Check rings - set 1 and 2 apply only to molecules
            // with at least one ring structure
            if (CurMol.GetSSSR().getAtomContainerCount() > 0) {
                
                int idx = 0;
                
                // Set 1
                for (int i=0; i<SA_Set1_Scaffold.length; i++) {
                    if ((SA_Set1_Scaffold[i].matches(CurMol.GetStructure())))
                        Res.add((Alert)Alerts.get(idx).clone());
                    idx++;
                }
                
                // Set 2
                for (int i=0; i<SA_Set2_Scaffold_Alerts.length; i++) {
                    if ((SA_Set2_Scaffold_Alerts[i][0].matches(CurMol.GetStructure())))
                        if ((SA_Set2_Scaffold_Alerts[i][1].matches(CurMol.GetStructure())))
                            Res.add((Alert)Alerts.get(idx).clone());
                    idx++;
                }
                
            }
            
        } catch (InvalidMoleculeException  | CloneNotSupportedException e ) {
            log.warn(e.getClass() + ": " + e.getMessage());
            return null;
        } 
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<Set1_Scaffold.length; i++) {
            String s = (String)Set1_Scaffold[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "FISHVERMEER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

        for (int i=0; i<Set2_Scaffold_Alerts.length; i++) {
            String s1 = (String)Set2_Scaffold_Alerts[i][0];
            String s2 = (String)Set2_Scaffold_Alerts[i][1];
            try {
                InsilicoMolecule mol1 = SmilesMolecule.Convert(s1);
                InsilicoMolecule mol2 = SmilesMolecule.Convert(s2);
                Depiction.SaveImageAsPNG(Depiction.DepictDoubleMolecule(mol1, mol2, 350, 200), "FISHVERMEER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s1 + " - " + e.getMessage());
            }
            idx++;
        }
    }     
    
}