package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
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
public class SAPersistenceWater extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    private boolean[] Multiple;
    private boolean[] Single;
    private int nRules;

    private final static String[] SMARTSFragsNP = {
        "O=CC",
        "Nc1ccc(cc1)",
        "c1ccc(OC)cc1",
        "Cc1ccccc1C",
        "c1(C)c2c(cccc2)ccc1",
        "c1(O)c(C)cccc1",
        "P(OC)OC",
        "C(O)C",
        "c1ccccc1O",
        "O=C",
        "C(=C)C",
        "NC"
    };

    private final static String[] SMARTSChemClassNP = {
        "C(=O)([O;D1;!-])[A]", // carboxyl acids (aliphatic) - single - nP
        "O=[$([C;D2]),$([C;D3]C)][O;D2][C,c]", // esters (aliphatic) - single - nP
        "O=C([a])[O;D2][C,c]", // esters (aromatic) - multiple - nP
        "[$(C(=[O,S])([O,S][a])N),$(C(=[O,S])([O,S])N[a])]", // (thio)-carbamates (aromatic) - single - nP
        "[$([C;D2](=O)C)]", // aldehydes (aliphatic) - single - nP
        "[C;D3](=O)([C])[C]", // ketones (aliphatic) - single - nP
        "[N;D1][$([C;A]);!$(C=[O,S])]", // primary amines (aliphatci) - single - nP
        "[N;D1][a]", // primary amines (aromatic)		nP
        "O=NN([C,c])[C,c]", // N-nitroso groups (aliphatic) - single - nP
        "[O;D1;!-]A", // hydroxyl groups - nP
        "[O;D1;!-]a", // aromatic hydroxyl - single - nP
        "[O;D1;!-][C;D2;H2][C,c]" // primary alcohol		nP        
    };    

    final static String[] SMARTSFragsVP = {
        "c1ccc(c(c1)c2ccccc2Cl)",
        "C1(=C(C2(C3C(CC(C3))C1(C2(Cl)Cl)Cl)Cl)Cl)Cl",
        "c1cc(c(c(c1Cl)Cl)Cl)",
        "c1(c2ccccc2)ccccc1",
    };
    
    
 
    
    
    public SAPersistenceWater() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_PERSISTENCE_WATER_IRFMN, StringSelectorCore.getString("sa_persistence_water_initialization"));
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
            curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
            curSA.setDescription(String.format(StringSelectorCore.getString("sa_persistence_water_np_description"), SMARTSFragsNP[i]));
            curSA.setImageURL("/insilico/core/alerts/png/perswater/PER_WATER_" + (pngidx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
            Alerts.add(curSA);
            idx++; pngidx++;
        }
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_carboxyl"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_singester"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
    
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_multiester"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_carbamate"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_aldehyde"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_ketone"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
                
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_amine_aliphatic"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
                
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_amine_aromatic"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        idx++;
                
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_amine_nitroso"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
                
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_amine_hydroxyl"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        idx++;
                
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_amine_hydroxyl_single"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        Single[idx] = true;
        idx++;
                
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_np"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_water_np_description_amine_alcohols"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_NP, true);
        Alerts.add(curSA);
        idx++;

        int vPidx = 0;        
        
        for (int i=0; i<SMARTSFragsVP.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_water_name_vp"), idx+1));
            curSA.setDescription(String.format(StringSelectorCore.getString("sa_persistence_water_vp_description"), SMARTSFragsVP[i]));
            curSA.setImageURL("/insilico/core/alerts/png/perswater/PER_WATER_" + (pngidx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_WATER_VP, true);
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
            throw new InitFailureException(StringSelectorCore.getString("sa_exception_smarts_initialization"));
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
            
        } catch (InvalidMoleculeException | CloneNotSupportedException e) {
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_WATER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (int i=0; i<SMARTSFragsVP.length; i++) {
            String s = SMARTSFragsVP[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_WATER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

    }       
}