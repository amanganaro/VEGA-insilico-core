package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Benigni/Bossa alerts, new set (SA over 31) taken from ToxTree 2.6.13
 * implementation, both carcinogenic and mutagenic alerts are implemented.
 *
 * @author User
 */
public class SABenigniBossaAdditional extends AlertBlockFromSMARTS implements iAlertBlock {

    Logger logger = LoggerFactory.getLogger(SABenigniBossaAdditional.class);
    
    public static String KEY_BBSA_IS_MUTAGEN = "bb_muta";
    public static String KEY_BBSA_IS_CARCINOGEN = "bb_carc";

    public class BBAlert {
        private final ArrayList<String> SMARTS;
        private final ArrayList<String> PreSMARTS;
        private final ArrayList<QueryAtomContainer> ParsedSMARTS;
        private final ArrayList<QueryAtomContainer> ParsedPreSMARTS;
        private String Id;
        private String Name;
        private String Description;
        private boolean Mutagen;
        private boolean Carcinogen;
        
        public BBAlert() {
            SMARTS = new ArrayList<>();
            ParsedSMARTS = new ArrayList<>();
            PreSMARTS = new ArrayList<>();
            ParsedPreSMARTS = new ArrayList<>();
            Mutagen = false;
            Carcinogen = false;
        }

        public void InitSMARTS() throws InitFailureException {
            for (String curSMARTS : SMARTS) {
                try {
                    ParsedSMARTS.add(SMARTSParser.parse(curSMARTS, DefaultChemObjectBuilder.getInstance()));
                } catch (Exception ex) {
                    InsilicoLogger.getLogger().warn("unable to initialize " + Id + ": " + curSMARTS);
                    throw new InitFailureException("unable to initialize " + Id + ": " + curSMARTS);
                }
            }
            for (String curSMARTS : PreSMARTS) {
                try {
                    ParsedPreSMARTS.add(SMARTSParser.parse(curSMARTS, DefaultChemObjectBuilder.getInstance()));
                } catch (Exception ex) {
                    InsilicoLogger.getLogger().warn("unable to initialize " + Id + ": " + curSMARTS);
                    throw new InitFailureException("unable to initialize " + Id + ": " + curSMARTS);
                }
            }            
        }
        
        public void addSMARTS(String value) {
            this.SMARTS.add(value);
        }
        
        public ArrayList<String> getSMARTS() {
            return SMARTS;
        }

        public ArrayList<QueryAtomContainer> getParsedSMARTS() {
            return ParsedSMARTS;
        }

        public void addPreSMARTS(String value) {
            this.PreSMARTS.add(value);
        }
        
        public ArrayList<String> getPreSMARTS() {
            return PreSMARTS;
        }

        public ArrayList<QueryAtomContainer> getParsedPreSMARTS() {
            return ParsedPreSMARTS;
        }

        public String getId() {
            return Id;
        }

        public void setId(String Id) {
            this.Id = Id;
        }

        public String getName() {
            return Name;
        }

        public void setName(String Name) {
            this.Name = Name;
        }

        public String getDescription() {
            return Description;
        }

        public void setDescription(String Description) {
            this.Description = Description;
        }

        public boolean isMutagen() {
            return Mutagen;
        }

        public void setMutagen(boolean Mutagen) {
            this.Mutagen = Mutagen;
        }

        public boolean isCarcinogen() {
            return Carcinogen;
        }

        public void setCarcinogen(boolean Carcinogen) {
            this.Carcinogen = Carcinogen;
        }
    }    

    
    private ArrayList<BBAlert> BBAlertList;
    
    
    public SABenigniBossaAdditional() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MUTAGEN_BENIGNI_BOSSA_ADDITIONAL, "Benigni/Bossa (from ToxTree 2.6) additional (over SA31) rules");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {
        
        BBAlertList = CreateAlerts();
        
        Alert curSA;
        
        for (int i=0; i<BBAlertList.size(); i++) {
            BBAlert BB = BBAlertList.get(i);
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName(BB.getId() + " " + BB.getName());
            curSA.setDescription(BB.getDescription());
            curSA.setImageURL("/insilico/core/alerts/png/benignibossa/" + BB.getId() + ".png");

            curSA.setBoolProperty(KEY_BBSA_IS_CARCINOGEN, BB.isCarcinogen());
            curSA.setBoolProperty(KEY_BBSA_IS_MUTAGEN, BB.isMutagen());
            
            // All alerts are toxic
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_TOXIC, true);

            Alerts.add(curSA);
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        for (BBAlert BB : BBAlertList)
            BB.InitSMARTS();

    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        
        AlertList Res = new AlertList();
        
        try {
            
            // Cycle on all alerts
            
            for (int i=0; i<BBAlertList.size(); i++) {
                
                // Normal matching
                
                BBAlert BB = BBAlertList.get(i);

                boolean PreScreen = false;
                if (BB.getParsedPreSMARTS().isEmpty())
                    PreScreen = true;
                else
                    for (QueryAtomContainer q : BB.getParsedPreSMARTS())
                        if (Matcher.matches(q)) {
                            PreScreen = true;
                            break;
                        }
                
                if (PreScreen)
                    for (QueryAtomContainer q : BB.getParsedSMARTS())
                        if (Matcher.matches(q)) {
                            Res.add((Alert)Alerts.get(i).clone());
                            break;
                        }
                
            }
            
        } catch (Throwable e) {
            return null;
        }
        
        return Res; 
    }

    
    
    
    public ArrayList<BBAlert> CreateAlerts() {
        
        ArrayList<BBAlert> BBAlerts = new ArrayList<>();
        BBAlert curAlert;
        
        //// SA 37 (idx 0)
        
        curAlert = new BBAlert();
        curAlert.setId("SA37");
        curAlert.setName("Pyrrolizidine Alkaloids");
        curAlert.setDescription("Pyrrolizidine Alkaloids");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("C12CCCN1CC=C2");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 38 (idx 1)
        
        curAlert = new BBAlert();
        curAlert.setId("SA38");
        curAlert.setName("Alkenylbenzenes");
        curAlert.setDescription("Alkenylbenzenes");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("c1ccccc1C[C;!R]=C");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 39 (idx 2)
        
        curAlert = new BBAlert();
        curAlert.setId("SA39");
        curAlert.setName("Steroidal estrogens");
        curAlert.setDescription("Steroidal estrogens");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("C[C@@]12CCC3c4c(CCC3C1CC[C@H]2O)cc(O)cc4");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 40 (idx 3)
        
        curAlert = new BBAlert();
        curAlert.setId("SA40");
        curAlert.setName("Substituted phenoxyacid");
        curAlert.setDescription("Substituted phenoxyacid");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("c1(OC(C)(C)C(=O)O)ccc([#6,#17])cc1");
        curAlert.addSMARTS("c1(OCC(=O)[O;H0])cc(Cl)c(Cl)cc1");
        curAlert.addSMARTS("c1(OCC(=O)[O;H0])c(Cl)cc(Cl)cc1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 41 (idx 4)
        
        curAlert = new BBAlert();
        curAlert.setId("SA41");
        curAlert.setName("Substituted n-alkylcarboxylic acids");
        curAlert.setDescription("Substituted n-alkylcarboxylic acids");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[C;!R&$(C([C;!R])([C;!R])[C;!R][C;!R])&!$(CCCCCCCCCCCC)][C;!R&$(C[OX2;!R]),$(C(=O)[OX2;!R])]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 42 (idx 5)
        
        curAlert = new BBAlert();
        curAlert.setId("SA42");
        curAlert.setName("Phthalate diesters and monoesters");
        curAlert.setDescription("Phthalate diesters and monoesters");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("O=C(O)c1ccccc1C(=O)O");
        curAlert.addSMARTS("O=C(O)[CX4;!R][CX4;!R][CX4;!R][CX4;!R]C(=O)O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 43 (idx 6
        
        curAlert = new BBAlert();
        curAlert.setId("SA43");
        curAlert.setName("Perfluorooctanoic acid (PFOA)");
        curAlert.setDescription("Perfluorooctanoic acid (PFOA)");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("CC(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)F");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 44 (idx 7)
        
        curAlert = new BBAlert();
        curAlert.setId("SA44");
        curAlert.setName("Trichloro (or fluoro) ethylene and Tetrachloro (or fluoro) ethylene");
        curAlert.setDescription("Trichloro (or fluoro) ethylene and Tetrachloro (or fluoro) ethylene");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[Cl,F][C;!$(Cc)]=C([Cl,F])[Cl,F]");
        curAlert.addSMARTS("[Cl,F]C#C[Cl,F]");
        curAlert.addSMARTS("Cl[C;!$(Cc)]=C(Cl)Cl");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 45 (idx 8)
        
        curAlert = new BBAlert();
        curAlert.setId("SA45");
        curAlert.setName("Indole-3-carbinol");
        curAlert.setDescription("Indole-3-carbinol");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);

        // Original SMARTS modified
        // curAlert.addSMARTS("OCc1c[nH]c2ccccc12");
        curAlert.addSMARTS("OCc1c[n+0;D2]c2ccccc12");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 46 (idx 9)
        
        curAlert = new BBAlert();
        curAlert.setId("SA46");
        curAlert.setName("Pentachlorophenol");
        curAlert.setDescription("Pentachlorophenol");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("Clc1c(Cl)c(Cl)c(Cl)c(Cl)c1O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 47 (idx 10)
        
        curAlert = new BBAlert();
        curAlert.setId("SA47");
        curAlert.setName("O-phenylphenol");
        curAlert.setDescription("O-phenylphenol");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("Oc2ccccc2c1ccccc1");
        curAlert.addSMARTS("Oc1c(c2ccccc2)cccc1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 48 (idx 11)
        
        curAlert = new BBAlert();
        curAlert.setId("SA48");
        curAlert.setName("Quercetin-type flavonoids");
        curAlert.setDescription("Quercetin-type flavonoids");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("Oc1cc(O)c2C(=O)C(O)=C(Oc2c1)c3ccc(O)c(O)c3");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 49 (idx 12)
        
        curAlert = new BBAlert();
        curAlert.setId("SA49");
        curAlert.setName("Imidazole and benzimidazole");
        curAlert.setDescription("Imidazole and benzimidazole");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("n1c[nH]cc1");
        curAlert.addSMARTS("n2c1ccccc1nc2");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 50 (idx 13)
        
        curAlert = new BBAlert();
        curAlert.setId("SA50");
        curAlert.setName("Dicarboximide");
        curAlert.setDescription("Dicarboximide");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[#6]1[#6](=O)[#7][#6](=O)[#6]1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 51 (idx 14)
        
        curAlert = new BBAlert();
        curAlert.setId("SA51");
        curAlert.setName("Dimethylpyridine");
        curAlert.setDescription("Dimethylpyridine");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[CX4H3]c1cccc([CX4H3])n1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 52 (idx 15)
        
        curAlert = new BBAlert();
        curAlert.setId("SA52");
        curAlert.setName("Metals, oxidative stress");
        curAlert.setDescription("Metals, oxidative stress");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[As,Cu,Cr,Hg,Co]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 53 (idx 16)
        
        curAlert = new BBAlert();
        curAlert.setId("SA53");
        curAlert.setName("Benzensulfonic ethers");
        curAlert.setDescription("Benzensulfonic ethers");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("c1cc(N)ccc1S(=O)(=O)N");
        curAlert.addSMARTS("c1cc(S)ccc1S(=O)(=O)N");
        curAlert.addSMARTS("c1ccccc1S(=O)(=O)[N;-1]");
        curAlert.addSMARTS("c1cc(C)ccc1S(=O)(=O)O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 54 (idx 17)
        
        curAlert = new BBAlert();
        curAlert.setId("SA54");
        curAlert.setName("1,3-Benzodioxoles");
        curAlert.setDescription("1,3-Benzodioxoles");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[C;!$(C(C)(C))]1Oc2ccccc2O1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 55 (idx 18)
        
        curAlert = new BBAlert();
        curAlert.setId("SA55");
        curAlert.setName("Phenoxy herbicides");
        curAlert.setDescription("Phenoxy herbicides");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("c1cc(O)ccc1OC(C)C(=O)[O;!$(OCCCC)]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 56 (idx 19)
        
        curAlert = new BBAlert();
        curAlert.setId("SA56");
        curAlert.setName("Alkyl halides");
        curAlert.setDescription("Alkyl halides");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[C;H1,!$(CCOP)&!$(CCP)&!$(CF)&!$(CN)&!$(C[CH3])](Cl)(Cl)(Cl)");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 57 (idx 20)
        
        curAlert = new BBAlert();
        curAlert.setId("SA57");
        curAlert.setName("DNA Intercalating Agents with a basic side chain");
        curAlert.setDescription("DNA intercalating agents are defined as those compounds that are able to insert partially or completely between adjacent DNA base pairs.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        
        // Original SMARTS has been modified
        curAlert.addPreSMARTS("NCCN");
        String[] sa57_subs = {
            "Cc1c2aaaac2aaa1",
            "Cc1c2[A]=[A][A]c2aaa1",
            "Cc1c2[A][A]=[A][A]c2aaa1",
            "Cc1c2[A][A]=[A]c2aaa1",
            "Cc1c2Cc3aaaac3c2aaa1",
            "Cc1c2c3aaaac3Cc2aaa1",
        };
        String s57_base = "[N+0;!R;!$(N=*);!$(N#*)][C+0;!R;!$(C=*);!$(C#*)][C+0;!R;!$(C=*);!$(C#*)][N+0;!R;!$(N=*);!$(N#*)]";
        for (String s : sa57_subs) {
            String s57 = s57_base + s;
            curAlert.addSMARTS(s57);
        }
        
        BBAlerts.add(curAlert);
        
        
        //// SA 58 (idx 21)
        
        curAlert = new BBAlert();
        curAlert.setId("SA58");
        curAlert.setName("Haloalkene cysteine S-conjugates");
        curAlert.setDescription("Haloalkene cysteine S-conjugates. These substances are reactive metabolites generated in the course of  the activation process of toxic and carcinogenic haloalkenes.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("O=C(O)C(N)CS[CX3;!R]=[CX3;!R]Cl");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 59 (idx 22)
        
        curAlert = new BBAlert();
        curAlert.setId("SA59");
        curAlert.setName("Xanthones, Thioxanthones, Acridones");
        curAlert.setDescription("Xanthones, Thioxanthones, Acridones. DNA intercalating agents are defined as those compounds that are able to insert partially or completely between adjacent DNA base pairs. Fused polycyclic chemicals are classical members of this class of compounds.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("C1(=O)c3ccccc3([S,N,O]c2ccccc12)");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 60 (idx 23)
        
        curAlert = new BBAlert();
        curAlert.setId("SA60");
        curAlert.setName("Flavonoids");
        curAlert.setDescription("Flavonoids. Quercetin-type flavonoids are recognized as a class of epigenetic carcinogens acting via induction of oxidative stress.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("C1(=O)c2c(O)cc(O)cc2OC(c3cc(O)ccc3)=C1O");
        curAlert.addSMARTS("c1(=O)c2c(O)cc(O)cc2oc(c3cc(O)ccc3)c1O");
        curAlert.addSMARTS("c1(=O)c2c(O)cc(O)cc2Oc(c3cc(O)ccc3)c1O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 61 (idx 24)
        
        curAlert = new BBAlert();
        curAlert.setId("SA61");
        curAlert.setName("Alkyl hydroperoxides");
        curAlert.setDescription("Alkyl hydroperoxides. Hydroperoxides are able to promote extensive biomolecular damage.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("[O;H1;D1]OC");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 62 (idx 25)
        
        curAlert = new BBAlert();
        curAlert.setId("SA62");
        curAlert.setName("N-acyloxy-N -alkoxybenzamides");
        curAlert.setDescription("N-Acyloxy-N-alkoxybenzamides have been shown to be mutagenic in Salmonella typhimurium without the need of metabolic activation.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("c1ccccc1C(=O)[N;H1,$(NOC(=O)[C,c])][O;H1,$(O[CX4])]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 63 (idx 26)
        
        curAlert = new BBAlert();
        curAlert.setId("SA63");
        curAlert.setName("N-aryl-N-acetoxyacetamides");
        curAlert.setDescription("N-aryl-N-acetoxyacetamides are reactive molecules generated in the metabolic activation process of aromatic amines in vivo.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("c1ccccc1N(OC(=O)[CX4])C(=O)[CX4]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 64 (idx 27)
        
        curAlert = new BBAlert();
        curAlert.setId("SA64");
        curAlert.setName("Hydroxamic acid derivatives");
        curAlert.setDescription("Hydroxamic acid derivatives are potential DNA acylating agents after metabolic transformation.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("O=[C;$(CN),$(C[CX4;!R]),$(C[OCX4;!R])][NH][OH]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 65 (idx 28)
        
        curAlert = new BBAlert();
        curAlert.setId("SA65");
        curAlert.setName("Halofuranones");
        curAlert.setDescription("Halofuranones are direct-acting bacterial mutagens that are found - together with other class of halo compounds- as disinfection by-products in drinking water.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("[F,Br,I,Cl]C1=CCOC1=O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 66 (idx 29)
        
        curAlert = new BBAlert();
        curAlert.setId("SA66");
        curAlert.setName("Anthrones");
        curAlert.setDescription("Anthrones are supposed to act as DNA intercalating agents. These are defined as those compounds that are able to insert partially or completely between adjacent DNA base pairs.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("O=C1c2ccccc2[CX4;H1,H2]c2ccccc12");
        curAlert.addSMARTS("O=C1c2ccccc2Cc3c1cccc3");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 67 (idx 30)
        
        curAlert = new BBAlert();
        curAlert.setId("SA67");
        curAlert.setName("Triphenylimidazole and related");
        curAlert.setDescription("Triphenylimidazole and related. DNA intercalating agents are defined as those compounds that are able to insert partially or completely between adjacent DNA base pairs.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("c2ccccc2C1[N;H1]C(c3ccccc3)=C(c4ccccc4)[S,O]=1");
        curAlert.addSMARTS("c1(C2=C(c3ccccc3)N=C(c3ccccc3)N2)ccccc1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 68 (idx 31)
        
        curAlert = new BBAlert();
        curAlert.setId("SA68");
        curAlert.setName("9,10 - dihydrophenanthrenes");
        curAlert.setDescription("9,10 - dihydrophenanthrenes. DNA intercalating agents are defined as those compounds that are able to insert partially or completely between adjacent DNA base pairs.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("c1c2c3ccccc3CCc2ccc1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 69 (idx 32)
        
        curAlert = new BBAlert();
        curAlert.setId("SA69");
        curAlert.setName("Fluorinated quinolines");
        curAlert.setDescription("Fluorinated quinolines. Quinoline and many substituted quinolines have been reported to be mutagenic.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("[c;H1,$(cF)]1c2[c;H1,$(cF)][c;H1,$(cF)][c;H1,$(cF)]nc2[c;H1,$(cF)][c;H1,$(cF)][c;H1,$(cF)]1");
        
        BBAlerts.add(curAlert);
        
        
       
        return BBAlerts;
        
    }    

}