package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 *
 * @author User
 */
public class SAFishIRFMN extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static String[] SMARTSCategory1 = {
        
        // Category 1 (14 SAs)
        "C(OCCCC)c1cccc(c1)", // 1"
        "c1cc(c(O)c(c1)C(C)C)", // 2"
        "Oc1ccc(cc1C)Cl", // 3"
        "O=Cc1cccc(Oc2ccc(cc2))c1", // 4"
        "Nc1ccc(cc1)CCCCCCCC", // 5"
        "O=[N+]([O-])c1c(cc(c(c1)[N+](=O)[O-])Cl)Cl", // 6"
        "NCCCCCCCCCCCC", // 7"
        "O(CC)P(=S)(OCC)SCS", // 8"
        "CC[Sn](CC)(CC)CC", // 9"
        "c1cc(ccc1CCl)", // 10"
        "O=CC=CC(=O)", // 11"
        "N#CCC#N", // 12"
        "[c]([Cl,Br,F])[c]([Cl,Br,F])[c]([Cl,Br,F])", // 13"
        "CCC(=O)OC[a]" // 14"
    };

    private final static String[] SMARTSCategory2 = {
    
        // Category 2 (23 SAs)
        "Oc1ccc(cc1)Cl", // 15"
        "C(C)CCCCCCCCC", // 16"
        "c1ccc(Oc2ccccc2)cc1", // 17"
        "C(=O)Oc1ccccc1", // 18"
        "C(OCC=C)CC", // 19"
        "c1c(cccc1CC)CC", // 20"
        "O(CC)P(=S)(O)", // 21"
        "C=CC=C", // 22"
        "C(=O)OCCCC", // 23"
        "O=[N+]([O-])c1cc(cc(c1)C)", // 24"
        "c1cc(c(cc1C))C", // 25"
        "SCC", // 26"
        "O(c1ccccc1)CCCC", // 27"
        "c1cccc2ccccc12", // 28"
        "c1cc(ccc1O)Br", // 29"
        "c1ccccc1c2ccccc2", // 30"
        "O=Cc1c(F)cccc1", // 31"
        "I", // 32"
        "O=C(OC)CC", // 33"
        "[*;D1]#C[C;!D4][!C;D1]", // 34"
        "*[C;D2][C;D2][C;D2][C;D2][C;D2][C;D2]*", // 35"
        "[s;R]", // 36"
        "[$([c]([Cl,Br,F])[c]([Cl,Br,F])),$([c]([Cl,Br,F])[c][c]([Cl,Br,F]))]", // 37"
    };

    private final static String[] SMARTSCategory3 = {
        
        // Category 3 (24 SAs)
        "C(OC)c1ccc(cc1)", // 38"
        "NCCCCCC", // 39"
        "c1cc(c(cc1)C)C", // 40"
        "Fc1ccccc1", // 41"
        "O=C(OCCC)C", // 42"
        "c1ccc(cc1)Br", // 43"
        "O=[N+]([O-])c1cc(N)ccc1", // 44"
        "c1ccc(cc1)CCCC", // 45"
        "C=CCCC", // 46"
        "c1ccc(cc1)Cl", // 47"
        "Oc1ccc(cc1)C", // 48"
        "c1cc(ccc1N(C)C)", // 49"
        "c1cc(N)ccc1O", // 50"
        "CCCCCCCC", // 51"
        "C(C(Cl))Cl", // 52"
        "O=P(OCC)(OCC)OCC", // 53"
        "N(CCC)(CCC)C", // 54"
        "o1c(ccc1)", // 55"
        "C#CCC(O)", // 56"
        "C(CCCl)C", // 57"
        "OCC#CC", // 58"
        "O=[C;D2][C;D2]", // 59"
        "C=C", // 60"
        "c1ccccc1" // 61"        
    };    
    
    
    
    public SAFishIRFMN() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_FISH_IRFMN, "Rules for fish toxicity classification (IRFMN)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTSCategory1.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Toxicity class 1 alert no. " + (i+1));
            curSA.setDescription("Structural alert for fish toxicity defined by the SMARTS: " + SMARTSCategory1[i] + ". It is related to toxicity values less than 1 mg/l.");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_FISH_TOX_LESS_1, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTSCategory2.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Toxicity class 2 alert no. " + (i+1));
            curSA.setDescription("Structural alert for fish toxicity defined by the SMARTS: " + SMARTSCategory2[i] + ". It is related to toxicity values between 1 mg/l and 10 mg/l.");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_FISH_TOX_1_10, true);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTSCategory3.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Toxicity class 3 alert no. " + (i+1));
            curSA.setDescription("Structural alert for fish toxicity defined by the SMARTS: " + SMARTSCategory3[i] + ". It is related to toxicity values between 10 mg/l and 100 mg/l.");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_FISH_TOX_10_100, true);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTSCategory1.length + SMARTSCategory2.length + SMARTSCategory3.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (String s : SMARTSCategory1) {
                SA[idx] = SMARTSParser.parse(s, DefaultChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTSCategory2) {
                SA[idx] = SMARTSParser.parse(s, DefaultChemObjectBuilder.getInstance());
                idx++;
            }
            for (String s : SMARTSCategory3) {
                SA[idx] = SMARTSParser.parse(s, DefaultChemObjectBuilder.getInstance());
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

            int nFragments = SMARTSCategory1.length + SMARTSCategory2.length + SMARTSCategory3.length;
            
            for (int i=0; i<nFragments; i++) 
                if (Matches(SA[i])) 
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    
}