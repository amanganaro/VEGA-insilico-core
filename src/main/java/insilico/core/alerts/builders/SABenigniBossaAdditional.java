package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.localization.StringSelectorCore;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

import java.util.ArrayList;

/**
 * Benigni/Bossa alerts, new set (SA over 31) taken from ToxTree 2.6.13
 * implementation, both carcinogenic and mutagenic alerts are implemented.
 *
 * @author User
 */
@Slf4j
public class SABenigniBossaAdditional extends AlertBlockFromSMARTS implements iAlertBlock {
    
    public static String KEY_BBSA_IS_MUTAGEN = "bb_muta";
    public static String KEY_BBSA_IS_CARCINOGEN = "bb_carc";

    public class BBAlert {
        private final ArrayList<String> SMARTS;
        private final ArrayList<String> PreSMARTS;
        private final ArrayList<Pattern> ParsedSMARTS;
        private final ArrayList<Pattern> ParsedPreSMARTS;
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
                    ParsedSMARTS.add(SmartsPattern.create(curSMARTS).setPrepare(false));
                } catch (Exception ex) {
                    log.warn(String.format(StringSelectorCore.getString("sa_exception_smarts_initialization_with_index"), Id, curSMARTS));
                    throw new InitFailureException(String.format(StringSelectorCore.getString("sa_exception_smarts_initialization_with_index"), Id, curSMARTS));
                }
            }
            for (String curSMARTS : PreSMARTS) {
                try {
                    ParsedPreSMARTS.add(SmartsPattern.create(curSMARTS).setPrepare(false));
                } catch (Exception ex) {
                    log.warn(String.format(StringSelectorCore.getString("sa_exception_smarts_initialization_with_index"), Id, curSMARTS));
                    throw new InitFailureException(String.format(StringSelectorCore.getString("sa_exception_smarts_initialization_with_index"), Id, curSMARTS));
                }
            }            
        }
        
        public void addSMARTS(String value) {
            this.SMARTS.add(value);
        }
        
        public ArrayList<String> getSMARTS() {
            return SMARTS;
        }

        public ArrayList<Pattern> getParsedSMARTS() {
            return ParsedSMARTS;
        }

        public void addPreSMARTS(String value) {
            this.PreSMARTS.add(value);
        }
        
        public ArrayList<String> getPreSMARTS() {
            return PreSMARTS;
        }

        public ArrayList<Pattern> getParsedPreSMARTS() {
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
        super(InsilicoConstants.SA_BLOCK_MUTAGEN_BENIGNI_BOSSA_ADDITIONAL, StringSelectorCore.getString("sa_benigni_bossa_additional_init"));
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
                    for (Pattern q : BB.getParsedPreSMARTS())
                        if (q.matches(CurMol.GetStructure())) {
                            PreScreen = true;
                            break;
                        }
                
                if (PreScreen)
                    for (Pattern q : BB.getParsedSMARTS())
                        if (q.matches(CurMol.GetStructure())) {
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
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa37_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa37_name"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("C12CCCN1CC=C2");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 38 (idx 1)
        
        curAlert = new BBAlert();
        curAlert.setId("SA38");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa38_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa38_name"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("c1ccccc1C[C;!R]=C");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 39 (idx 2)
        
        curAlert = new BBAlert();
        curAlert.setId("SA39");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa39_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa39_name"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);

        //      Original SMARTS modified, it seems to have problems in matching
        //        curAlert.addSMARTS("C[C@@]12CCC3c4c(CCC3C1CC[C@H]2O)cc(O)cc4");

        curAlert.addSMARTS("CC12CCC3c4c(CCC3C1CCC2O)cc(O)cc4");

        BBAlerts.add(curAlert);
        
        
        //// SA 40 (idx 3)
        
        curAlert = new BBAlert();
        curAlert.setId("SA40");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa40_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa40_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("c1(OC(C)(C)C(=O)O)ccc([#6,#17])cc1");
        curAlert.addSMARTS("c1(OCC(=O)[O;H0])cc(Cl)c(Cl)cc1");
        curAlert.addSMARTS("c1(OCC(=O)[O;H0])c(Cl)cc(Cl)cc1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 41 (idx 4)
        
        curAlert = new BBAlert();
        curAlert.setId("SA41");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa41_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa41_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[C;!R&$(C([C;!R])([C;!R])[C;!R][C;!R])&!$(CCCCCCCCCCCC)][C;!R&$(C[OX2;!R]),$(C(=O)[OX2;!R])]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 42 (idx 5)
        
        curAlert = new BBAlert();
        curAlert.setId("SA42");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa42_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa42_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("O=C(O)c1ccccc1C(=O)O");
        curAlert.addSMARTS("O=C(O)[CX4;!R][CX4;!R][CX4;!R][CX4;!R]C(=O)O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 43 (idx 6
        
        curAlert = new BBAlert();
        curAlert.setId("SA43");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa43_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa43_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("CC(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)F");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 44 (idx 7)
        
        curAlert = new BBAlert();
        curAlert.setId("SA44");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa44_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa44_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[Cl,F][C;!$(Cc)]=C([Cl,F])[Cl,F]");
        curAlert.addSMARTS("[Cl,F]C#C[Cl,F]");
        curAlert.addSMARTS("Cl[C;!$(Cc)]=C(Cl)Cl");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 45 (idx 8)
        
        curAlert = new BBAlert();
        curAlert.setId("SA45");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa45_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa45_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);

        // Original SMARTS modified
        // curAlert.addSMARTS("OCc1c[nH]c2ccccc12");
        curAlert.addSMARTS("OCc1c[n+0;D2]c2ccccc12");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 46 (idx 9)
        
        curAlert = new BBAlert();
        curAlert.setId("SA46");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa46_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa46_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("Clc1c(Cl)c(Cl)c(Cl)c(Cl)c1O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 47 (idx 10)
        
        curAlert = new BBAlert();
        curAlert.setId("SA47");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa47_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa47_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("Oc2ccccc2c1ccccc1");
        curAlert.addSMARTS("Oc1c(c2ccccc2)cccc1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 48 (idx 11)
        
        curAlert = new BBAlert();
        curAlert.setId("SA48");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa48_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa48_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("Oc1cc(O)c2C(=O)C(O)=C(Oc2c1)c3ccc(O)c(O)c3");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 49 (idx 12)
        
        curAlert = new BBAlert();
        curAlert.setId("SA49");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa49_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa49_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("n1c[nH]cc1");
        curAlert.addSMARTS("n2c1ccccc1nc2");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 50 (idx 13)
        
        curAlert = new BBAlert();
        curAlert.setId("SA50");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa50_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa50_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[#6]1[#6](=O)[#7][#6](=O)[#6]1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 51 (idx 14)
        
        curAlert = new BBAlert();
        curAlert.setId("SA51");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa51_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa51_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[CX4H3]c1cccc([CX4H3])n1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 52 (idx 15)
        
        curAlert = new BBAlert();
        curAlert.setId("SA52");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa52_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa52_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[As,Cu,Cr,Hg,Co]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 53 (idx 16)
        
        curAlert = new BBAlert();
        curAlert.setId("SA53");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa53_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa53_name"));
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
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa54_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa54_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[C;!$(C(C)(C))]1Oc2ccccc2O1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 55 (idx 18)
        
        curAlert = new BBAlert();
        curAlert.setId("SA55");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa55_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa55_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("c1cc(O)ccc1OC(C)C(=O)[O;!$(OCCCC)]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 56 (idx 19)
        
        curAlert = new BBAlert();
        curAlert.setId("SA56");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa56_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa56_name"));
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[C;H1,!$(CCOP)&!$(CCP)&!$(CF)&!$(CN)&!$(C[CH3])](Cl)(Cl)(Cl)");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 57 (idx 20)
        
        curAlert = new BBAlert();
        curAlert.setId("SA57");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa57_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa57_description"));
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
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa58_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa58_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("O=C(O)C(N)CS[CX3;!R]=[CX3;!R]Cl");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 59 (idx 22)
        
        curAlert = new BBAlert();
        curAlert.setId("SA59");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa59_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa59_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("C1(=O)c3ccccc3([S,N,O]c2ccccc12)");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 60 (idx 23)
        
        curAlert = new BBAlert();
        curAlert.setId("SA60");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa60_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa60_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("C1(=O)c2c(O)cc(O)cc2OC(c3cc(O)ccc3)=C1O");
        curAlert.addSMARTS("c1(=O)c2c(O)cc(O)cc2oc(c3cc(O)ccc3)c1O");
        curAlert.addSMARTS("c1(=O)c2c(O)cc(O)cc2Oc(c3cc(O)ccc3)c1O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 61 (idx 24)
        
        curAlert = new BBAlert();
        curAlert.setId("SA61");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa61_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa61_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("[O;H1;D1]OC");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 62 (idx 25)
        
        curAlert = new BBAlert();
        curAlert.setId("SA62");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa62_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa62_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("c1ccccc1C(=O)[N;H1,$(NOC(=O)[C,c])][O;H1,$(O[CX4])]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 63 (idx 26)
        
        curAlert = new BBAlert();
        curAlert.setId("SA63");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa63_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa63_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("c1ccccc1N(OC(=O)[CX4])C(=O)[CX4]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 64 (idx 27)
        
        curAlert = new BBAlert();
        curAlert.setId("SA64");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa64_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa64_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("O=[C;$(CN),$(C[CX4;!R]),$(C[OCX4;!R])][NH][OH]");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 65 (idx 28)
        
        curAlert = new BBAlert();
        curAlert.setId("SA65");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa65_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa65_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("[F,Br,I,Cl]C1=CCOC1=O");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 66 (idx 29)
        
        curAlert = new BBAlert();
        curAlert.setId("SA66");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa66_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa66_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("O=C1c2ccccc2[CX4;H1,H2]c2ccccc12");
        curAlert.addSMARTS("O=C1c2ccccc2Cc3c1cccc3");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 67 (idx 30)
        
        curAlert = new BBAlert();
        curAlert.setId("SA67");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa67_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa67_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("c2ccccc2C1[N;H1]C(c3ccccc3)=C(c4ccccc4)[S,O]=1");
        curAlert.addSMARTS("c1(C2=C(c3ccccc3)N=C(c3ccccc3)N2)ccccc1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 68 (idx 31)
        
        curAlert = new BBAlert();
        curAlert.setId("SA68");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa68_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa68_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("c1c2c3ccccc3CCc2ccc1");
        
        BBAlerts.add(curAlert);
        
        
        //// SA 69 (idx 32)
        
        curAlert = new BBAlert();
        curAlert.setId("SA69");
        curAlert.setName(StringSelectorCore.getString("sa_benigni_bossa_additional_sa69_name"));
        curAlert.setDescription(StringSelectorCore.getString("sa_benigni_bossa_additional_sa69_description"));
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(false);
        curAlert.addSMARTS("[c;H1,$(cF)]1c2[c;H1,$(cF)][c;H1,$(cF)][c;H1,$(cF)]nc2[c;H1,$(cF)][c;H1,$(cF)][c;H1,$(cF)]1");
        
        BBAlerts.add(curAlert);
        
        
       
        return BBAlerts;
        
    }    

}