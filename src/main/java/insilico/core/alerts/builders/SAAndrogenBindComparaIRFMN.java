package insilico.core.alerts.builders;

import insilico.core.exception.InitFailureException;
import insilico.core.alerts.AlertBlockFromSMARTS;
import insilico.core.alerts.Alert;
import insilico.core.alerts.AlertList;
import insilico.core.alerts.AlertEncoding;
import insilico.core.alerts.iAlertBlock;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelector;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

/**
 *
 * @author User
 */
public class SAAndrogenBindComparaIRFMN extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    private int nRules;

    private final static String[] All_15_act_inf ={
        "CC(C)(C)C(C)CCc1ccccc1",
        "C(C)c1ccc(Cl)cc1Cl",
        "c1ccc(Cl)cc1Cc2ccccc2",
        "c1cc(ccc1[N+](=O)[O-])OP(=S)(OC)OC",
        "CCCCCCCCCCCC[N+](C)(C)C",
        "SN(CCCC)CCCC",       
    };
    
    private final static String[] All_15_act_noninf ={
        "C(C(c1ccc(Cl)cc1)c2ccccc2)",
        "CCNc1cc(c(cc1))C(F)(F)F",
        "c1ccc(cc1)OCCN(C)C",
        "CC=C3CCCCC3",
        "CC1CCCC1(O)",
        "CC(C)(c1ccc(O)cc1)c2ccccc2",
        "CC=Cc1ccc(cc1)O",
        "CC(=O)Nc1cc(Cl)ccc1",
        "Oc1ccc(cc1)C(c2ccc(O)cc2)",
        "C(=O)C(O)(CC)CC",
        "CCCCN(CC)CCCCC"
    };
    
    private final static String[] All_15_inact_inf ={
        "CCOCCOCC",
        "Cc1ccccc1COC",
        "CNN",
        "Cc1ccccn1",
        "c1ncncn1",
        "CC(O)C(=O)O",
        "O[Si]",
        "OCCCCCCOCC",
        "Clc1ccc(Cl)cc1",
        "CCOS(=O)",
        "Cc1cccc(c1)Oc2ccccc2",
        "Cc1cc(nc(n1))",
        "C(=CCCC(=C))",
        "Nc1ccc(cc1)S(=O)(=O)",
        "Cc1ccc(C)cc1C",
        "C=CC(=O)OC",
        "ON=C",
        "c1cccc2cccnc12",
        "CN(C)CSC",
        "CCCCCCC(=O)N(C)",
        "Oc1ccccn1",
        "Oc1ccccc1C(=O)O",
        "S(=O)(=O)NC(=O)",
        "CSP(OCC)",
        "c1ccc(F)cc1F",
        "OCC1(CCCC1)",
        "C(Cc1ccccc1)NCCCC",
        "Nc1cccc2c1cccc2",
        "N(CCCl)CC",
        "Cc1ccc(cc1)[N+](=O)[O-]",
        "Cc1cc(ccc1)CNC",
        "C(=O)CCCCC(=O)",
        "c1cnn(C)c1",
        "c1ccc(cc1)[S]c2ccccc2",
        "O=P(Oc1ccccc1)O",
        "N=CN(C)C",
        "Cc1ccc(cc1)Oc2ccc(cc2)",
        "CC(C)(C)c1cc(O)ccc1",
        "Cc1ccc(cc1)S(=O)(=O)O",
        "CC(=CCCC(C)(O)C)C",
        "c1ncnc2c1ncn2",
        "OCC(C)(C)CO",
        "CN1CCCC1=O",
        "NC(CO)(CO)",
        "CC(C)(OO)",
        "OC(=O)C(Cl)",
        "ClC(=C(Cl))",
        "P(=O)SC",
        "C1CO1",
        "CCCCCCCOC(=O)c1ccc(O)cc1",
        "c1cccc2Cc3ccccc3C(=O)c12",
        "CC1=C(C(CCC1)(C)C)C=CCC",
        "C(CC(C)N(C))(c1ccccc1)",
        "CN(C)C(=O)Nc1ccccc1",
        "CCCCCOc1ccccc1",
        "c1ccn(n1)c2ccccc2",
        "Oc1ccc(cc1N)[N+](=O)[O-]",
        "CC(C)(C)C1CCC(O)CC1",
        "COc1cc(ccc1)NC(=O)",
        "C(=O)N(Cc1ccccc1)C",
        "OCCN(CCO)CCO",
        "OC(=O)c1ccc(N)cc1",
        "C(=O)c1cc(ccc1)C(=O)",
        "CCCc1ccc(N)cc1",
        "CC(C)c1ccc(N)cc1",
        "CCCN1CCOCC1",
        "C(=O)c1cccnc1",
        "c1nnc(s1)",
        "OP(=O)(O)C",
        "CCCN=O",
        "N(N=O)",
        "CCCCCCC=CCCCCCCCC(=O)O",
        "c1ccc(cc1)S(=O)(=O)NCCCC",
        "OC(=O)CCCCCCCC(=O)O",
        "C(c1ccccc1)n2cncc2",
        "OC(=O)c1ccc(cc1)C(=O)O",
        "CCc1cccc2ccccc12",
        "CCCCC(=O)c1ccccc1",
        "C(O)COc1ccc(C)cc1",
        "O=C(OCCc1ccccc1)",
        "CC(NC(=O))c1ccccc1",
        "Cc1cc(c(C)cc1)S(=O)(=O)",
        "O=C(C)OCc1ccccc1",
        "n1c(cnc1)c2ccccc2",
        "OC(=O)c1ccccc1N",
        "Oc1ccccc1C(=O)N",
        "CC1C(=CCCC1(C)C)C",
        "c1nc2ccccc2s1",
        "Nc1c(Cl)cccc1Cl",
        "ClC(Cl)c1ccccc1",
        "n1nc2ccccc2n1",
        "Brc1ccc(cc1)O",
        "c1ccccc1P",
        "CCCN=C",
    };
    
    private final static String[] All_15_inact_noninf ={
        "OC=C",
        "COc1ccc(Cl)cc1",
        "CCCC(F)(F)",
        "CCCNC(=O)N",
        "C(=O)CC(=O)",
        "CCSC",
        "CCCCCCCCCC(=O)O",
        "c1ccc(CC=C)cc1",
        "NC(=S)N",
        "C(=O)CCCC(=O)",
        "CC(C)(C)c1cccc(c1)C(C)(C)C",
    };
    
    private final static String[] Act6 = {
        "CC(C)(C)C(C)CCc1ccccc1",
        "C(C)c1ccc(Cl)cc1Cl",
        "C(C(c1ccc(Cl)cc1)c2ccccc2)",
        "CC1(C)CCCC1(O)C",
        "CCNc1cc(c(cc1))C(F)(F)F",
        "c1ccc(cc1)OCCN(C)C",
        "Oc1ccccc1Cc1ccccc1O",
        "CC=C3CCCCC3",
        "CC(C)(c1ccc(O)cc1)c2ccccc2",
        "CC(=Cc1ccc(O)cc1)",
        "CC(=O)Nc1cc(Cl)ccc1",
        "Oc1ccc(cc1)C(c2ccc(O)cc2)",
        "CCCCCCCCCCCN(C)C",
        "Cc1ccccc1N(C)C",
        "CC(C)C1CCCC1C",
        "COc1ccc(cc1OC)",
        "c1ccc(cc1)c2ccccc2",
        "c1ccc(cc1)CNC(CC)",
        "Nc1ccc(Cl)cc1",
        "CC(=Cc1ccccc1)",
        "CCC(C)c1ccccc1",
        "COP(=S)(Oc1ccc(cc1))OC"       
    };
    
    
    public SAAndrogenBindComparaIRFMN() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_ANDROGEN_BIND_COMPARA_SARPY, StringSelector.getString("sa_androgen_bind_compara_rules_intro"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        nRules = All_15_act_inf.length + All_15_act_noninf.length + All_15_inact_inf.length + 
                All_15_inact_noninf.length + Act6.length;
        
        int idx = 0;
        Alert curSA;

        for (String s : All_15_act_inf) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx + 1)));
            curSA.setName(String.format(StringSelector.getString("sa_androgen_bind_compara_name_active"), idx+1));
            curSA.setDescription(StringSelector.getString("sa_androgen_bind_compara_description_activity_high_rel") + s);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_AR_COMPARA_SARPY_ALL15_ACT_INF, true);
            Alerts.add(curSA);
            idx++;
        }

        for (String s : All_15_act_noninf) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx + 1)));
            curSA.setName(String.format(StringSelector.getString("sa_androgen_bind_compara_name_active"), idx+1));
            curSA.setDescription(StringSelector.getString("sa_androgen_bind_compara_description_activity_mod_rel") + s);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_AR_COMPARA_SARPY_ALL15_ACT_NONINF, true);
            Alerts.add(curSA);
            idx++;
        }

        for (String s : All_15_inact_inf) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx + 1)));
            curSA.setName(String.format(StringSelector.getString("sa_androgen_bind_compara_name_inactive"), idx+1));
            curSA.setDescription(StringSelector.getString("sa_androgen_bind_compara_description_inactivity_high_rel") + s);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_AR_COMPARA_SARPY_ALL15_INACT_INF, true);
            Alerts.add(curSA);
            idx++;
        }

        for (String s : All_15_inact_noninf) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx + 1)));
            curSA.setName(String.format(StringSelector.getString("sa_androgen_bind_compara_name_inactive"), idx+1));
            curSA.setDescription(StringSelector.getString("sa_androgen_bind_compara_description_inactivity_mod_rel") + s);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_AR_COMPARA_SARPY_ALL15_INACT_NONINF, true);
            Alerts.add(curSA);
            idx++;
        }

        for (String s : Act6) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx + 1)));
            curSA.setName(String.format(StringSelector.getString("sa_androgen_bind_compara_name_active"), idx+1));
            curSA.setDescription(StringSelector.getString("sa_androgen_bind_compara_description_activity_mod_rel") + s);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_AR_COMPARA_SARPY_ACT6, true);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new Pattern[nRules];
            
            int idx = 0;
            for (String s : All_15_act_inf) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : All_15_act_noninf) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }

            for (String s : All_15_inact_inf) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : All_15_inact_noninf) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : Act6) {
                SA[idx] =  SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
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
                int matches = (SA[i].matchAll(CurMol.GetStructure()).countUnique());
                if (matches > 0)
                    Res.add((Alert)Alerts.get(i).clone());
            }
                        
        } catch (InvalidMoleculeException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    
}