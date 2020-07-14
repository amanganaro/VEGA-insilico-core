package insilico.core.alerts.builders;

import insilico.core.alerts.*;
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
public class SADevToxPGRoman extends AlertBlockFromSMARTS implements iAlertBlock {
    

    private QueryAtomContainer s1_1, s1_2, s1_3a, s1_3b, s2, s3, s4, s5_1, s5_2, s5_3, s6;
    private QueryAtomContainer[] s4_chelate;
    
    private static final String[] AlertNames = {
        "I", // 0
        "II", // 1
        "III", // 2
        "IV", // 3
        "V", // 4
        "VI"  // 5
    };

    private static final String[] AlertDescriptions = {
        "",
        "",
        "",
        "",
        "",
        ""
    };
    

    
    
    public SADevToxPGRoman() throws InitFailureException {
        super(-1, "DevTox P&G Roman Rules");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {
        
        Alert curSA;
        
        for (int i=0; i<AlertNames.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName(AlertNames[i]);
            curSA.setDescription(AlertDescriptions[i]);
            
            Alerts.add(curSA);
            
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            // I
            s1_1 = SMARTSParser.parse("[#5,#13,#14,#21,#22,#23,#24,#25,#26,#27,#28,#29,#30,#31,#32,#33,#39,#40,#41,#42,#43,#44,#45,#46,#47,#48,#49,#50,#51,#52,#80,#81,#82,#83,#84]", DefaultChemObjectBuilder.getInstance());
            s1_2 = SMARTSParser.parse("OP(O)([*])=O", DefaultChemObjectBuilder.getInstance()); // org. phosphorus
            s1_3a = SMARTSParser.parse("[Si]O[Si]", DefaultChemObjectBuilder.getInstance()); // Siloxane
            s1_3b = SMARTSParser.parse("[C,c]", DefaultChemObjectBuilder.getInstance());

            // II
            s2 = SMARTSParser.parse("[R1,R2,R3,R4]", DefaultChemObjectBuilder.getInstance());

            // III
            s3 = SMARTSParser.parse("[a]", DefaultChemObjectBuilder.getInstance());
            
            // IV
            s4 = SMARTSParser.parse("[!$([C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R])][C;!R][N,S,O,Cl,Br,F,I]", DefaultChemObjectBuilder.getInstance());
            s4_chelate= new QueryAtomContainer[9];
            s4_chelate[0] = SMARTSParser.parse("OC(=O)CN(CCN(CC(O)=O)CC(O)=O)CC(O)=O", DefaultChemObjectBuilder.getInstance());
            s4_chelate[1] = SMARTSParser.parse("CC(=O)CC(C)=O", DefaultChemObjectBuilder.getInstance());
            s4_chelate[2] = SMARTSParser.parse("[N;!$([N+])]", DefaultChemObjectBuilder.getInstance());
            s4_chelate[3] = SMARTSParser.parse("OC(=O)C(O)=O", DefaultChemObjectBuilder.getInstance());
            s4_chelate[4] = SMARTSParser.parse("[*]OC(=O)O[*]", DefaultChemObjectBuilder.getInstance());
            s4_chelate[5] = SMARTSParser.parse("C1C[O]2CC[O]3CC[O]4CC[O]5CC[O]6CC[O]1[Cu]23456", DefaultChemObjectBuilder.getInstance());
            s4_chelate[6] = SMARTSParser.parse("C1=CN=C(C=C1)C1=CC=CC=N1", DefaultChemObjectBuilder.getInstance());
            s4_chelate[7] = SMARTSParser.parse("NCC(O)=O", DefaultChemObjectBuilder.getInstance());
            s4_chelate[8] = SMARTSParser.parse("OC(=O)CN(CC(O)=O)CC(O)=O", DefaultChemObjectBuilder.getInstance());
            
            // V
            //carboxilic acid and derivates (esters, amides, ureas, thioureas, carbamates) with C <= 9:
            s5_1 = SMARTSParser.parse("[!$([C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R]~[C;!R])][$([C;!R](=O)O),$([C;!R](=O)[N;!+]),$([N;!R]C(=[O,S])N),$([N;!R]C(=O)[O;D2])]", DefaultChemObjectBuilder.getInstance());
            //vinyl amides:
            s5_2 = SMARTSParser.parse("[C;D1]=CC(=O)N", DefaultChemObjectBuilder.getInstance());
            //vinyl aldehydes and esters C<4:
            s5_3 = SMARTSParser.parse("[!$([C;!R]~[C;!R]~[C;!R]~[C;!R])][C;!R][$(OC=[C;D1]),$(C(=O)C=[C;D1])]", DefaultChemObjectBuilder.getInstance());
            
            // VI
            s6 = SMARTSParser.parse("[!$([C;!R]-[C;!R]-[C;!R]-[C;!R]-[C;!R]-[C;!R]-[C;!R]-[C;!R]-[C;!R])][C;!R](=O)O", DefaultChemObjectBuilder.getInstance());
            
        } catch (Exception e) {
            throw new InitFailureException("Unable to initialize SMARTS");
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {
            
            if ( (Matcher.matches(s1_1)) || (Matcher.matches(s1_2)) || ( (Matcher.matches(s1_3a)) && (Matcher.matches(s1_3b)) ) )
                Res.add((Alert)Alerts.get(0).clone());

            if ( (Matcher.matches(s2)) )
                Res.add((Alert)Alerts.get(1).clone());
            
            if ( (Matcher.matches(s3)) )
                Res.add((Alert)Alerts.get(2).clone());
            
            boolean found_chelate = false;
            for (QueryAtomContainer q : s4_chelate) {
                if (Matcher.matches(q)) {
                    found_chelate = true;
                    break;
                }
            }
            if ( (Matcher.matches(s4)) || (found_chelate) )
                Res.add((Alert)Alerts.get(3).clone());

            if ( (Matcher.matches(s5_1)) || (Matcher.matches(s5_2)) || (Matcher.matches(s5_3)) )
                Res.add((Alert)Alerts.get(4).clone());
            
            if ( (Matcher.matches(s6)) )
                Res.add((Alert)Alerts.get(5).clone());
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }


}