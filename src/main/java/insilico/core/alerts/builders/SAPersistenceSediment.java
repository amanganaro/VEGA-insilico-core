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
public class SAPersistenceSediment extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
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
        super(InsilicoConstants.SA_BLOCK_PERSISTENCE_SEDIMENT_IRFMN, "Rules for persistence in sediment (IRFMN)");
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
            curSA.setName("nP (sediment) alert no. " + (idx+1));
            curSA.setDescription("Fragment related to nP compounds (sediment), defined by the SMARTS: " + SMARTSFragsNP[i]);
            curSA.setImageURL("/insilico/core/alerts/png/perssed/PER_SED_" + (pngidx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
            Alerts.add(curSA);
            idx++; pngidx++;
        }
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (sediment) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (sediment), defined by the presence of esters (aliphatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (sediment) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (sediment), defined by the presence of aldehydes (aliphatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (sediment) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (sediment), defined by the presence of ketones (aliphatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (sediment) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (sediment), defined by the presence of primary amines");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        idx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("nP (sediment) alert no. " + (idx+1));
        curSA.setDescription("Chemical class related to nP compounds (sediment), defined by the presence of multiple hydroxyl groups");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_NP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++;        

        
        int vPidx = 0;
        
        for (int i=0; i<SMARTSFragsVP.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("vP (sediment) alert no. " + (vPidx+1));
            curSA.setDescription("Fragment related to vP compounds (sediment), defined by the SMARTS: " + SMARTSFragsVP[i]);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_VP, true);
            curSA.setImageURL("/insilico/core/alerts/png/perssed/PER_SED_" + (pngidx+1) + ".png");
            Alerts.add(curSA);
            idx++; vPidx++; pngidx++;
        }
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("vP (sediment) alert no. " + (vPidx+1));
        curSA.setDescription("Chemical class related to vP compounds (sediment), defined by the presence of multiple ethers (aromatic)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_VP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++; vPidx++;

        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
        curSA.setName("vP (sediment) alert no. " + (vPidx+1));
        curSA.setDescription("Chemical class related to vP compounds (sediment), defined by the presence of multiple halogens on ring C (sp3)");
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_PERS_SEDIMENT_VP, true);
        Alerts.add(curSA);
        Multiple[idx] = true;
        idx++; vPidx++;

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new QueryAtomContainer[nRules];
            
            int idx = 0;
            for (String s : SMARTSFragsNP) {
                SA[idx] = SMARTSParser.parse(s, SilentChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTSChemClassNP) {
                SA[idx] = SMARTSParser.parse(s, SilentChemObjectBuilder.getInstance());
                idx++;
            }

            for (String s : SMARTSFragsVP) {
                SA[idx] = SMARTSParser.parse(s, SilentChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTSChemClassVP) {
                SA[idx] = SMARTSParser.parse(s, SilentChemObjectBuilder.getInstance());
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
                int matches = MatchesNumber(SA[i]);
                if (Single[i]) {
                    if (matches == 1)
                        Res.add((Alert)Alerts.get(i).clone());
                } else if (Multiple[i]) {
                    if (matches > 1) 
                        Res.add((Alert)Alerts.get(i).clone());
                } else if (matches > 0)
                    Res.add((Alert)Alerts.get(i).clone());
            }
                        
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<SMARTSFragsNP.length; i++) {
            String s = (String)SMARTSFragsNP[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_SED_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

        for (int i=0; i<SMARTSFragsVP.length; i++) {
            String s = (String)SMARTSFragsVP[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "PER_SED_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }         
}