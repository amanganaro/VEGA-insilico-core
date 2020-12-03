package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
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
        super(InsilicoConstants.SA_BLOCK_BCF_CAESAR, "Rules for outliers and moieties for Caesar BCF model");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        // Definition of SAs

        FragNames = new String[nFragments];
        FragDescription = new String[nFragments];
        FragPNGNames = new String[nFragments];
        FragSMARTS = new String[nFragments];

        //// Fragments for outlier

        FragNames[0] = "6 Cl atoms in the molecule (SO 01)";
        FragDescription[0] = "Compounds with six or more Cl atoms fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[0] = "sa1.png";
        FragSMARTS[0] = "[Cl]";   

        FragNames[1] = "2 t-butyl linked to aromatic (SO 02)";
        FragDescription[1] = "Compounds with two or more t-butyl linked to aromatic ring fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[1] = "sa2.png";
        FragSMARTS[1] = "aC([CH3])([CH3])[CH3]";   

        FragNames[2] = "Si atom in the molecule (SO 03)";
        FragDescription[2] = "Compounds with Si atoms fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[2] = "sa3.png";
        FragSMARTS[2] = "[Si]";   

        FragNames[3] = "Sn atom in the molecule  (SO 04)";
        FragDescription[3] = "Compounds with Sn atoms fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[3] = "sa4.png";
        FragSMARTS[3] = "[Sn]";   

        FragNames[4] = "O linked to aromatic and 3 Br/Cl linked to aromatic  (SO 05)";
        FragDescription[4] = "Compounds with one or more O atoms linked to aromatic ring and three or more Br or Cl atoms linked to aromatic ring fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[4] = "sa5.png";
        FragSMARTS[4] = "[O,o][a]";
        FragSMARTS_5_add = "[Cl,Br]a";

        FragNames[5] = "Azo group liked to aromatic (SO 06)";
        FragDescription[5] = "Compounds with one or more azo groups liked to aromatic rings fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[5] = "sa6.png";
        FragSMARTS[5] = "[a]N=N[a]";   

        FragNames[6] = "3 Nitro-groups linked to aromatic (SO 07)";
        FragDescription[6] = "Compounds with three or more nitro-groups linked to Ar fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[6] = "sa7.png";
        FragSMARTS[6] = "[N+](=O)([O-])a";   

        FragNames[7] = "Peroxide (SO 08)";
        FragDescription[7] = "Compounds with one or more peroxide fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[7] = "sa8.png";
        FragSMARTS[7] = "*O-O*";   

        FragNames[8] = "Phosphinothioyl-oxy-imino (SO 09)";
        FragDescription[8] = "Compounds with one or more phosphinothioyl-oxy-imino fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[8] = "sa9.png";
        FragSMARTS[8] = "*OP(=S)(O*)O[C,c][N,n]*";   

        FragNames[9] = "10 F atoms in the molecule (SO 10)";
        FragDescription[9] = "Compounds with ten or more F atoms fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[9] = "sa10.png";
        FragSMARTS[9] = "[F]";   

        FragNames[10] = "Phosphorodithioate (SO 11)";
        FragDescription[10] = "Compounds with one or more phosphorodithioate fall into a chemical category that results out of the applicability domain of the model.";
        FragPNGNames[10] = "sa11.png";
        FragSMARTS[10] = "*OP(=S)(O*)S";   

        //// Fragments only for reasoning

        FragNames[11] = "Moiety (SMILES: O=Cc1ccccc1) (SR 01)";
        FragDescription[11] = "This chemical contains a moiety defined by SMILES O=Cc1ccccc1, which has been found only in non-bioaccumulative compounds (24 chemicals), even when the logP value was higher than 3.";
        FragPNGNames[11] = "o12.png";
        FragSMARTS[11] = "O=Cc1ccccc1";   

        FragNames[12] = "Carbonyl residue (SR 02)";
        FragDescription[12] = "This chemical contains a carbonyl residue. This residue has been found to be present in a very large (112) number of non-bioaccumulative compounds, even when the logP value was higher than 3.";
        FragPNGNames[12] = "o13.png";
        FragSMARTS[12] = "C=O";   

        FragNames[13] = "PO2 residue (SR 03)";
        FragDescription[13] = "This chemical contains the O-P=O residue, which has been found only in non-bioaccumulative compounds (45 chemicals), even when the logP value was higher than 3.";
        FragPNGNames[13] = "o14.png";
        FragSMARTS[13] = "OP(O)";   

        FragNames[14] = "Thiobenzene residue (SR 04)";
        FragDescription[14] = "This chemical contains the thiobenzene residue, which has been found only in non-bioaccumulative compounds (39 chemicals), even when the logP value was higher than 3.";
        FragPNGNames[14] = "o15.png";
        FragSMARTS[14] = "[S]c1ccccc1";   

        FragNames[15] = "Tertiary amine (SR 05)";
        FragDescription[15] = "This chemical contains a tertiary amine. This residue has been found to be present in a large numberof non-bioaccumulative compounds (28), even when the logP value was higher than 3.";
        FragPNGNames[15] = "o16.png";
        FragSMARTS[15] = "CN(C)C";   

        FragNames[16] = "Triazole ring (SR 06)";
        FragDescription[16] = "This chemical contains a triazole ring. This residue has been found to be present in a number of non- bioaccumulative compounds (16), even when the logP value was higher than 3.";
        FragPNGNames[16] = "o17.png";
        FragSMARTS[16] = "c1ncncn1";   

        FragNames[17] = "Moiety (SMILES: Clc1ccccc1c1ccc(Cl)cc1) (SR 07)";
        FragDescription[17] = "This chemical contains a moiety defined by SMILES Clc1ccccc1c1ccc(Cl)cc1), which has been found only in bioaccumulative compounds (15 chemicals). The high lipophylicity of this moiety increases the bioaccumulative behavior.";
        FragPNGNames[17] = "o18.png";
        FragSMARTS[17] = "Clc1ccccc1c1ccc(Cl)cc1";   

        FragNames[18] = "Moiert (SMILES: C1cc(Oc2ccccc2)ccc1Cl) (SR 08)";
        FragDescription[18] = "This chemical contains a moiety defined by SMILES C1cc(Oc2ccccc2)ccc1Cl, which has been found only in bioaccumulative compounds (9 chemicals). The high lipophylicity of this moiety increases the bioaccumulative behavior.";
        FragPNGNames[18] = "o19.png";
        FragSMARTS[18] = "C1cc(Oc2ccccc2)ccc1Cl";   

        FragNames[19] = "Moiety (SMILES: Clc1cc(c2ccccc2)c(Cl)cc1) (SR 09)";
        FragDescription[19] = "This chemical contains a moiety defined by SMILES Clc1cc(c2ccccc2)c(Cl)cc1, which has been found only in bioaccumulative compounds (15 chemicals). The high lipophylicity of this moiety increases the bioaccumulative behavior.";
        FragPNGNames[19] = "o20.png";
        FragSMARTS[19] = "Clc1cc(c2ccccc2)c(Cl)cc1";  

        //// Only for reasoning: polar fragments

        String PolarGroupExpl = "The presence of polar groups increases hydrophilicity, related to lower values of BCF.";

        // First group (20-24)

        FragNames[20] = "COOH group (PG 01)";
        FragDescription[20] = "This chemical contains a COOH polar group. " + PolarGroupExpl;
        FragPNGNames[20] = "g21.png";
        FragSMARTS[20] = "*-C(=O)[OH]";  

        FragNames[21] = "SO3H group (PG 02)";
        FragDescription[21] = "This chemical contains a SO3H polar group. " + PolarGroupExpl;
        FragPNGNames[21] = "g22.png";
        FragSMARTS[21] = "*-S(=O)(=O)[OH]";  

        FragNames[22] = "PO3 group (PG 03)";
        FragDescription[22] = "This chemical contains a PO3 polar group. " + PolarGroupExpl;
        FragPNGNames[22] = "g23.png";
        FragSMARTS[22] = "*-P(=O)([OH])[OH]";  

        FragNames[23] = "PO2S group (PG 04)";
        FragDescription[23] = "This chemical contains a PO2S polar group. " + PolarGroupExpl;
        FragPNGNames[23] = "g24.png";
        FragSMARTS[23] = "*-P(=S)([OH])[OH]";  

        FragNames[24] = "POS2 group (PG 05)";
        FragDescription[24] = "This chemical contains a POS2 polar group. " + PolarGroupExpl;
        FragPNGNames[24] = "g25.png";
        FragSMARTS[24] = "*-P(=S)(-S)[OH]";  

        // Second group (25-27)

        FragNames[25] = "OH group (PG 06)";
        FragDescription[25] = "This chemical contains a OH polar group. " + PolarGroupExpl;
        FragPNGNames[25] = "g26.png";
        FragSMARTS[25] = "*-[OH]";  

        FragNames[26] = "NH2 group (PG 07)";
        FragDescription[26] = "This chemical contains a NH2 polar group. " + PolarGroupExpl;
        FragPNGNames[26] = "g27.png";
        FragSMARTS[26] = "*-[N;D1]";  

        FragNames[27] = "CS2 group (PG 08)";
        FragDescription[27] = "This chemical contains a CS2 polar group. " + PolarGroupExpl;
        FragPNGNames[27] = "g28.png";
        FragSMARTS[27] = "*-C(=S)[S;D1]";  

        // Third group (28)

        FragNames[28] = ">C=O group (PG 09)";
        FragDescription[28] = "This chemical contains a >C=O polar group. " + PolarGroupExpl;
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
            throw new InitFailureException("Unable to initialize SMARTS");
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