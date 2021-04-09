package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelector;
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
public class SABCFCaesar extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private final int nFragments = 29;
    private String[] FragNames;
    private String[] FragDescription;
    private String[] FragPNGNames;
    private String[] FragSMARTS;
    private String FragSMARTS_5_add;
    
    private Pattern[] SA;
    private Pattern SA_5_add;
    
    
    
    public SABCFCaesar() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_BCF_CAESAR, StringSelector.getString("sa_bcf_caesar_rules"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        // Definition of SAs

        FragNames = new String[nFragments];
        FragDescription = new String[nFragments];
        FragPNGNames = new String[nFragments];
        FragSMARTS = new String[nFragments];

        //// Fragments for outlier

        FragNames[0] = StringSelector.getString("sa_bcf_caesar_frag_name_0");
        FragDescription[0] = StringSelector.getString("sa_bcf_caesar_frag_description_0");
        FragPNGNames[0] = "sa1.png";
        FragSMARTS[0] = "[Cl]";   

        FragNames[1] = StringSelector.getString("sa_bcf_caesar_frag_name_1");
        FragDescription[1] = StringSelector.getString("sa_bcf_caesar_frag_description_1");
        FragPNGNames[1] = "sa2.png";
        FragSMARTS[1] = "aC([CH3])([CH3])[CH3]";   

        FragNames[2] = StringSelector.getString("sa_bcf_caesar_frag_name_2");
        FragDescription[2] = StringSelector.getString("sa_bcf_caesar_frag_description_2");
        FragPNGNames[2] = "sa3.png";
        FragSMARTS[2] = "[Si]";   

        FragNames[3] = StringSelector.getString("sa_bcf_caesar_frag_name_3");
        FragDescription[3] = StringSelector.getString("sa_bcf_caesar_frag_description_3");
        FragPNGNames[3] = "sa4.png";
        FragSMARTS[3] = "[Sn]";   

        FragNames[4] = StringSelector.getString("sa_bcf_caesar_frag_name_4");
        FragDescription[4] = StringSelector.getString("sa_bcf_caesar_frag_description_4");
        FragPNGNames[4] = "sa5.png";
        FragSMARTS[4] = "[O,o][a]";
        FragSMARTS_5_add = "[Cl,Br]a";

        FragNames[5] = StringSelector.getString("sa_bcf_caesar_frag_name_5");
        FragDescription[5] = StringSelector.getString("sa_bcf_caesar_frag_description_5");
        FragPNGNames[5] = "sa6.png";
        FragSMARTS[5] = "[a]N=N[a]";

        FragNames[6] = StringSelector.getString("sa_bcf_caesar_frag_name_6");
        FragDescription[6] = StringSelector.getString("sa_bcf_caesar_frag_description_6");
        FragPNGNames[6] = "sa7.png";
        FragSMARTS[6] = "[N+](=O)([O-])a";

        FragNames[7] = StringSelector.getString("sa_bcf_caesar_frag_name_7");
        FragDescription[7] = StringSelector.getString("sa_bcf_caesar_frag_description_7");
        FragPNGNames[7] = "sa8.png";
        FragSMARTS[7] = "*O-O*";

        FragNames[8] = StringSelector.getString("sa_bcf_caesar_frag_name_8");
        FragDescription[8] = StringSelector.getString("sa_bcf_caesar_frag_description_8");
        FragPNGNames[8] = "sa9.png";
        FragSMARTS[8] = "*OP(=S)(O*)O[C,c][N,n]*";

        FragNames[9] = StringSelector.getString("sa_bcf_caesar_frag_name_9");
        FragDescription[9] = StringSelector.getString("sa_bcf_caesar_frag_description_9");
        FragPNGNames[9] = "sa10.png";
        FragSMARTS[9] = "[F]";

        FragNames[10] = StringSelector.getString("sa_bcf_caesar_frag_name_10");
        FragDescription[10] = StringSelector.getString("sa_bcf_caesar_frag_description_10");
        FragPNGNames[10] = "sa11.png";
        FragSMARTS[10] = "*OP(=S)(O*)S";   

        //// Fragments only for reasoning

        FragNames[11] = StringSelector.getString("sa_bcf_caesar_frag_name_11");
        FragDescription[11] = StringSelector.getString("sa_bcf_caesar_frag_description_11");
        FragPNGNames[11] = "o12.png";
        FragSMARTS[11] = "O=Cc1ccccc1";

        FragNames[12] = StringSelector.getString("sa_bcf_caesar_frag_name_12");
        FragDescription[12] = StringSelector.getString("sa_bcf_caesar_frag_description_12");
        FragPNGNames[12] = "o13.png";
        FragSMARTS[12] = "C=O";

        FragNames[13] = StringSelector.getString("sa_bcf_caesar_frag_name_13");
        FragDescription[13] = StringSelector.getString("sa_bcf_caesar_frag_description_13");
        FragPNGNames[13] = "o14.png";
        FragSMARTS[13] = "OP(O)";

        FragNames[14] = StringSelector.getString("sa_bcf_caesar_frag_name_14");
        FragDescription[14] = StringSelector.getString("sa_bcf_caesar_frag_description_14");
        FragPNGNames[14] = "o15.png";
        FragSMARTS[14] = "[S]c1ccccc1";

        FragNames[15] = StringSelector.getString("sa_bcf_caesar_frag_name_15");
        FragDescription[15] = StringSelector.getString("sa_bcf_caesar_frag_description_15");
        FragPNGNames[15] = "o16.png";
        FragSMARTS[15] = "CN(C)C";

        FragNames[16] = StringSelector.getString("sa_bcf_caesar_frag_name_16");
        FragDescription[16] = StringSelector.getString("sa_bcf_caesar_frag_description_16");
        FragPNGNames[16] = "o17.png";
        FragSMARTS[16] = "c1ncncn1";

        FragNames[17] = StringSelector.getString("sa_bcf_caesar_frag_name_17");
        FragDescription[17] = StringSelector.getString("sa_bcf_caesar_frag_description_17");
        FragPNGNames[17] = "o18.png";
        FragSMARTS[17] = "Clc1ccccc1c1ccc(Cl)cc1";

        FragNames[18] = StringSelector.getString("sa_bcf_caesar_frag_name_18");
        FragDescription[18] = StringSelector.getString("sa_bcf_caesar_frag_description_18");
        FragPNGNames[18] = "o19.png";
        FragSMARTS[18] = "C1cc(Oc2ccccc2)ccc1Cl";

        FragNames[19] = StringSelector.getString("sa_bcf_caesar_frag_name_19");
        FragDescription[19] = StringSelector.getString("sa_bcf_caesar_frag_description_19");
        FragPNGNames[19] = "o20.png";
        FragSMARTS[19] = "Clc1cc(c2ccccc2)c(Cl)cc1";  

        //// Only for reasoning: polar fragments

        String PolarGroupExpl = StringSelector.getString("sa_bcf_caesar_polar_group");

        // First group (20-24)

        FragNames[20] = StringSelector.getString("sa_bcf_caesar_frag_name_20");
        FragDescription[20] = StringSelector.getString("sa_bcf_caesar_frag_description_20") + PolarGroupExpl;
        FragPNGNames[20] = "g21.png";
        FragSMARTS[20] = "*-C(=O)[OH]";

        FragNames[21] = StringSelector.getString("sa_bcf_caesar_frag_name_21");
        FragDescription[21] = StringSelector.getString("sa_bcf_caesar_frag_description_21") + PolarGroupExpl;
        FragPNGNames[21] = "g22.png";
        FragSMARTS[21] = "*-S(=O)(=O)[OH]";

        FragNames[22] = StringSelector.getString("sa_bcf_caesar_frag_name_22");
        FragDescription[22] = StringSelector.getString("sa_bcf_caesar_frag_description_22") + PolarGroupExpl;
        FragPNGNames[22] = "g23.png";
        FragSMARTS[22] = "*-P(=O)([OH])[OH]";

        FragNames[23] = StringSelector.getString("sa_bcf_caesar_frag_name_23");
        FragDescription[23] = StringSelector.getString("sa_bcf_caesar_frag_description_23") + PolarGroupExpl;
        FragPNGNames[23] = "g24.png";
        FragSMARTS[23] = "*-P(=S)([OH])[OH]";

        FragNames[24] = StringSelector.getString("sa_bcf_caesar_frag_name_24");
        FragDescription[24] = StringSelector.getString("sa_bcf_caesar_frag_description_24") + PolarGroupExpl;
        FragPNGNames[24] = "g25.png";
        FragSMARTS[24] = "*-P(=S)(-S)[OH]";  

        // Second group (25-27)

        FragNames[25] = StringSelector.getString("sa_bcf_caesar_frag_name_25");
        FragDescription[25] = StringSelector.getString("sa_bcf_caesar_frag_description_25") + PolarGroupExpl;
        FragPNGNames[25] = "g26.png";
        FragSMARTS[25] = "*-[OH]";

        FragNames[26] = StringSelector.getString("sa_bcf_caesar_frag_name_26");
        FragDescription[26] = StringSelector.getString("sa_bcf_caesar_frag_description_26") + PolarGroupExpl;
        FragPNGNames[26] = "g27.png";
        FragSMARTS[26] = "*-[N;D1]";

        FragNames[27] = StringSelector.getString("sa_bcf_caesar_frag_name_27");
        FragDescription[27] = StringSelector.getString("sa_bcf_caesar_frag_description_27") + PolarGroupExpl;
        FragPNGNames[27] = "g28.png";
        FragSMARTS[27] = "*-C(=S)[S;D1]";  

        // Third group (28)

        FragNames[28] = StringSelector.getString("sa_bcf_caesar_frag_name_28");
        FragDescription[28] = StringSelector.getString("sa_bcf_caesar_frag_description_28") + PolarGroupExpl;
        FragPNGNames[28] = "g29.png";
        FragSMARTS[28] = "[C;D3](=O)";     
        
        
        for (int i=0; i<nFragments; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName(FragNames[i]);
            curSA.setDescription(FragDescription[i]);
            curSA.setImageURL("/insilico/core/alerts/png/bcfcaesar/" + FragPNGNames[i]);

            // Flag for outliers
            if ( (i>=0) && (i<=10) )
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_BCF_CAESAR_OUTLIER, true);

            Alerts.add(curSA);
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

//            SA = new QueryAtomContainer[nFragments];
            SA = new Pattern[nFragments];


            for (int i=0; i<nFragments; i++) 
                SA[i] = SmartsPattern.create(FragSMARTS[i], DefaultChemObjectBuilder.getInstance()).setPrepare(false);

            SA_5_add = SmartsPattern.create(FragSMARTS_5_add, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
            
        } catch (Exception e) {
            throw new InitFailureException(StringSelector.getString("sa_exception_smarts_initialization"));
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {

        AlertList Res = new AlertList();
        
        try {

            // First group of alerts
            for (int i=0; i<20; i++) {
                
                if (i==0) {
                    // SA 1
                    if ((SA[i].matchAll(CurMol.GetStructure()).countUnique()) >= 6)
                        Res.add((Alert)Alerts.get(i).clone());
                } else if (i==1) {
                    // SA 2
                    if ((SA[i].matchAll(CurMol.GetStructure()).countUnique()) >= 2)
                        Res.add((Alert)Alerts.get(i).clone());
                } else if (i==4) {
                    // SA 5 has two separate SMARTS
                    if ((SA[i].matches(CurMol.GetStructure())))
                        if ((SA_5_add.matchAll(CurMol.GetStructure()).countUnique()) >= 3)
                            Res.add((Alert)Alerts.get(i).clone());
                } else if (i==6) {
                    // SA 7
                    if ((SA[i].matchAll(CurMol.GetStructure()).countUnique()) >= 3)
                        Res.add((Alert)Alerts.get(i).clone());
                } else if (i==9) {
                    // SA 10
                    if ((SA[i].matchAll(CurMol.GetStructure()).countUnique()) >= 10)
                        Res.add((Alert)Alerts.get(i).clone());
                } else {
                    
                    // All other rules are normal matches
                    if ((SA[i].matches(CurMol.GetStructure())))
                        Res.add((Alert)Alerts.get(i).clone());
                    
                }
                
            }

            // Polar groups (3 blocks to be checked hierarchically)
            boolean PolarGroupFound = false;
            for (int i=20; i<25; i++) {
                if ((SA[i].matches(CurMol.GetStructure()))) {
                    Res.add((Alert)Alerts.get(i).clone());
                    PolarGroupFound = true;
                }
            }

            if (!PolarGroupFound)
                for (int i=25; i<28; i++) {
                    if ((SA[i].matches(CurMol.GetStructure()))) {
                        Res.add((Alert)Alerts.get(i).clone());
                        PolarGroupFound = true;
                    }
                }
            
            if (!PolarGroupFound)
            if ((SA[28].matches(CurMol.GetStructure())))
                Res.add((Alert)Alerts.get(28).clone());
            
        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            return null;
        }
        
        return Res; 
    }

    
}