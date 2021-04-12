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
public class SAPersistenceSediment extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    private boolean[] Multiple;
    private boolean[] Single;
    private int nRules;

    private final static String[] SMARTSFragsNP = {
        "O=CC",
        "O=Cc1ccccc1",
        "C(N)C",
        "OC(C)"        
    };

    private final static String[] SMARTSChemClassNP = {
        "O=[$([C;D2]),$([C;D3]C)][O;D2][C,c]", // esters (aliphatic) - nP
        "[$([C;D2](=O)C)]", // aldehydes (aliphatic) - nP
        "[C;D3](=O)([C])[C]", // ketones (aliphatic) - nP
        "[N;D1][$([C,c]);!$(C=[O,S])]", // primary amines - nP
        "[O;D1;!-]A", // hydroxyl groups - multiple - nP        
    };    
    
    private final static String[] SMARTSFragsVP = {
        "c1(c2ccccc2)ccccc1",
        "c1cc(c(cc1)Cl)Cl",
        "ClCC",
        "c12c(cccc1)Oc3c(cccc3)O2"
    };

    private final static String[] SMARTSChemClassVP = {
        "[#6;!$(C=O);!$(C#N)]O[a]", // ethers (aromatic) - multiple - vP
        "[Cl,Br,F,I][$(C(@[*])@[*]);!$(C=*)]", // X on ring C (sp3) - multiple - vP
    };    
    
    
 
    
    
    public SAPersistenceSediment() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_PERSISTENCE_SEDIMENT_IRFMN, StringSelectorCore.getString("sa_persistence_sediment_initialization"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        nRules = SMARTSFragsNP.length + SMARTSFragsVP.length + SMARTSChemClassNP.length + SMARTSChemClassVP.length;
        Multiple = new boolean[nRules];
        for (boolean b : Multiple) b = false;
        Single = new boolean[nRules];
        for (boolean b : Single) b = false;
        
        int idx = 0;
        int pngidx = 0;
        Alert curSA;

        for (int i=0; i<SMARTSFragsNP.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_name"), idx+1));
            curSA.setDescription(String.format(StringSelectorCore.getString("sa_persistence_sediment_description"), SMARTSFragsNP[i]));
            curSA.setImageURL("/insilico/core/alerts/png/perssed/PER_SED_" + (pngidx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
            Alerts.add(curSA);
            idx++; pngidx++;
        }
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_name"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_sediment_description_esters"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_name"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_sediment_description_aldehydes"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_name"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_sediment_description_ketones"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_name"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persitence_sediment_description_amines"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_name"), idx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persitence_sediment_description_hydroxyl"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++;        

        
        int vPidx = 0;
        
        for (int i=0; i<SMARTSFragsVP.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_vp_name"), vPidx+1));
            curSA.setDescription(String.format(StringSelectorCore.getString("sa_persistence_sediment_vp_description"), SMARTSFragsVP[i]));
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_VP, true);
            curSA.setImageURL("/insilico/core/alerts/png/perssed/PER_SED_" + (pngidx+1) + ".png");
            Alerts.add(curSA);
            idx++; vPidx++; pngidx++;
        }
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_vp_name"), vPidx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_sediment_vp_description_ether"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_VP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++; vPidx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName(String.format(StringSelectorCore.getString("sa_persistence_sediment_vp_name"), vPidx+1));
        curSA.setDescription(StringSelectorCore.getString("sa_persistence_sediment_vp_description_halogens"));
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_VP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++; vPidx++;

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
            for (String s : SMARTSChemClassVP) {
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_SED_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (int i=0; i<SMARTSFragsVP.length; i++) {
            String s = SMARTSFragsVP[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_SED_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

    }         
}