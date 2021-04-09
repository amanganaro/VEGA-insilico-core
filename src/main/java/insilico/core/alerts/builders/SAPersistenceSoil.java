package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelector;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

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
        super(InsilicoConstants.SA_BLOCK_PERSISTENCE_SOIL_IRFMN, StringSelector.getString("sa_persistence_soil_irfmn_initialization"));
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
            curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
            curSA.setDescription(String.format(StringSelector.getString("sa_persistence_soil_irfmn_description_np"), SMARTSFragsNP[i]));
            curSA.setImageURL("/insilico/core/alerts/png/perssoil/PER_SOIL_" + (pngidx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
            Alerts.add(curSA);
            idx++; pngidx++;
        }
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_alcohols"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_esters_aromatic"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_oxime"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_esters_aliphatic"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_aldehyde"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_carboxylic"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_carbamate"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_ketone"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name"),idx+1));
        curSA.setDescription(StringSelector.getString("sa_persistence_soil_irfmn_description_phosph"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SOIL_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        
        int vPidx = 0;        
        
        for (int i=0; i<SMARTSFragsVP.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelector.getString("sa_persistence_soil_irfmn_name_vp"),idx+1));
            curSA.setDescription(String.format(StringSelector.getString("sa_persistence_soil_irfmn_description_vp"), SMARTSFragsVP[i]));
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
            throw new InitFailureException(StringSelector.getString("sa_exception_smarts_initialization"));
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
                System.out.println(String.format(StringSelector.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (int i=0; i<SMARTSFragsVP.length; i++) {
            String s = SMARTSFragsVP[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_SOIL_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelector.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

    }      
    
}