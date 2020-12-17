package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
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
public class SAPersistenceSoil extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    private boolean[] Multiple;
    private boolean[] Single;
    private int nRules;

    private final static String[] SMARTSFragsNP = {
        "O=C(OCCC)",
        "O=C(CC)C",
        "C(O)c1ccccc1C",
        "Cc1c(ccc(c1))OCC",
        "COC(=O)CO",
        "C(O)C=C",
        "C(=NOC(=O)NC)C",
        "N(C)CCCl",
        "CCC(=O)Nc1cc(c(cc1))",
        "CSCc1ccccc1",
        "O(C)CCCl",
        "[P]",
        "C(CN(C)C)S",
        "C(=S)N(C)"
    };

    private final static String[] SMARTSChemClassNP = {
        "C[CH2]O", // Primary alcohols	m   nP
        "AC(=O)O*", // Esters (aromatic)	m   nP
        "AC(A)=NO*", // Oximes (aliphatic)	s   nP
        "AC(=O)O[*;!H]", // Esters (aliphatic)	nP
        "A[CH1](=O)", // Aldehydes (aliphatic)	s   nP
        "AC(=O)O", // Carboxylic acids (aliphatic)	s   nP
        "A[O,S]C(=[O,S])N(A)A", // (Thio-) carbamates (aliphatic)	s   nP
        "aC(=O)*", // Ketones (aromatic)	s   nP
        "*[O,S]=P([O,S]*)([O,S])[O,S]*", // Phosphates/thiophosphates	s   nP
    };    
    
    private final static String[] SMARTSFragsVP = {
        "c1ccc(c(c1)c2ccccc2Cl)",
        "c1c(Cl)cc2Oc3cc(Cl)cc(Cl)c3Oc2c1"

    };

    
 
    
    
    public SAPersistenceSoil() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_PERSISTENCE_SOIL_IRFMN, "Rules for persistence in soil (IRFMN)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        nRules = SMARTSFragsNP.length + SMARTSFragsVP.length + SMARTSChemClassNP.length;
        Multiple = new boolean[nRules];
        for (boolean b : Multiple) b = false;
        Single = new boolean[nRules];
        for (boolean b : Single) b = false;
        
        int idx = 0;
        int pngidx = 0;
        Alert curSA;

        for (int i=0; i<SMARTSFragsNP.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("nP (soil) alert no. " + (idx+1));
            curSA.setDescription("Fragment related to nP compounds (soil), defined by the SMARTS: " + SMARTSFragsNP[i]);
            curSA.setImageURL("/insilico/core/alerts/png/perssoil/PER_SOIL_" + (pngidx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
            Alerts.add(curSA);
            idx++; pngidx++;
        }
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of multiple primary alcohols");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of multiple esters (aromatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of a single oxime (aliphatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of esters (aliphatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of a single aldehyde (aliphatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of a single carboxylic acid (aliphatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of a single (thio-) carbamate (aliphatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of a single ketone (aromatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (soil) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (soil), defined by the presence of a single phosphate/thiophosphate");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        
        int vPidx = 0;        
        
        for (int i=0; i<SMARTSFragsVP.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("vP (soil) alert no. " + (vPidx+1));
            curSA.setDescription("Fragment related to vP compounds (soil), defined by the SMARTS: " + SMARTSFragsVP[i]);
            curSA.setImageURL("/insilico/core/alerts/png/perssoil/PER_SOIL_" + (pngidx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_VP, true);
            Alerts.add(curSA);
            idx++; vPidx++; pngidx++;
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new Pattern[nRules];
            
            int idx = 0;
            for (String s : SMARTSFragsNP) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : SMARTSChemClassNP) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }

            for (String s : SMARTSFragsVP) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
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

            for (int i=0; i<nRules; i++) {
                int matches = SA[i].matchAll(CurMol.GetStructure()).countUnique();
                if (Single[i]) {
                    if (matches == 1)
                        Res.add((Alert)Alerts.get(i).clone());
                } else if (Multiple[i]) {
                    if (matches > 1) 
                        Res.add((Alert)Alerts.get(i).clone());
                } else if (matches > 0)
                    Res.add((Alert)Alerts.get(i).clone());
            }
            
    } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            return null;
        }
        
        return Res; 
    }

    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<SMARTSFragsNP.length; i++) {
            String s = SMARTSFragsNP[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_SOIL_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

        for (int i=0; i<SMARTSFragsVP.length; i++) {
            String s = SMARTSFragsVP[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_SOIL_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }      
    
}