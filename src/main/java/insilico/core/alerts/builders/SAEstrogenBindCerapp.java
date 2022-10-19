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
public class SAEstrogenBindCerapp extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    private int nRules;

    private final static String[] SMARTSActive = {
        "C(CC)(c1ccc(O)cc1)C(CC)",
        "Oc1ccc2C(=O)C=COc2c1",
        "C(=Cc1ccccc1)(c2ccccc2)c3ccccc3",
        "C12(C)CCC3C4CCC(=O)C=C4CCC3C1CCC2(O)C#C",
        "c1(ccc(C(c2ccc(O)cc2)c3ccc(O)cc3)cc1)",
        "ClC(Cl)(Cl)C(c1ccc(O)cc1)c2ccc(O)cc2",
        "c1c(C(c2ccc(Cl)cc2)C(Cl)(Cl))c(Cl)ccc1",
        "C(C)(c1ccc(O)cc1)(c2ccc(O)cc2)CC",
        "C(Oc1ccccc1)c2ccc(O)cc2",
    };

    private final static String[] SMARTSActiveProb = {
        "c1(O)ccc(CCCC)cc1",
        "C(=O)(OCC)c1ccc(O)cc1",
        "C(c1ccc(O)cc1)c2ccc(O)cc2",
        "C(=O)CCCCCCCCCCC(CCCC)O",
        "C(=Cc1ccccc1)(c2ccccc2)",
        "CC(C)(c1ccc(O)cc1)",
        "C(=O)c1ccc(O)cc1",
    };

    private final static String[] SMARTSInactive = {
        "C(=O)(O)CCCCC",
        "N=O",
        "n1ccccc1",
        "CCNc1ccccc1",
        "C(=O)NCCC",
        "C(N)O",
        "n1cncn1",
        "c1(O)ccccc1C(C)C",
        "n1cncnc1",
        "C(N)Nc1ccccc1",
        "c1ncncc1",
        "[Si](O)",
        "N=N",
        "C(CCC=C)O",
        "c1(Cl)c(Cl)cccc1",
        "C(=C)(CCC=C(C))",
        "c1(ccccc1)CCO",
        "c1cc(Cl)ccc1O",
        "C(=O)(N(C)C)",
        "ClCCO",
        "S(=O)(=O)N",
        "N#CCC",
        "c1(ccccc1C(=O)OCCCCCCCC)C(=O)OCCCCC",
        "c1c(C)cc(C)cc1C",
        "N(CCN)CC",
        "C(CCl)Cl",
        "c12c3ccccc3ccc1cccc2",
        "c1ccc(Br)cc1",
        "OCCCO",
        "n1cccc1",
        "c1cc2c(C)cccc2cc1",
        "CCC(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)F",
        "NC(C)(C)C",
        "[Si](C)C",
    };

    private final static String[] SMARTSInactiveProb = {
        "CCOCC",
        "C(=O)(O)CCC",
        "C(O)CO",
        "Nc1ccccc1",
        "SC",
        "NCc1ccccc1",
        "Nc1ccc(C)cc1",
        "c1ccccc1Cl",
        "C(=O)",
    };

    
    
    public SAEstrogenBindCerapp() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_ESTROGEN_BIND_CERAPP, StringSelectorCore.getString("sa_estrogen_cerapp_initialization"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        nRules = SMARTSActive.length + SMARTSActiveProb.length + SMARTSInactive.length + SMARTSInactiveProb.length;
        
        int idx = 0;
        Alert curSA;

        for (int i=0; i<SMARTSActive.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(StringSelectorCore.getString("sa_estrogen_cerapp_active_name") + (i+1));
            curSA.setDescription(StringSelectorCore.getString("sa_estrogen_cerapp_active_description") + SMARTSActive[i]);
            curSA.setImageURL("/insilico/core/alerts/png/estrogenbindcerapp/CERAPP_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_ER_ACTIVE, true);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTSActiveProb.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(StringSelectorCore.getString("sa_estrogen_cerapp_active_prob_name") + (i+1));
            curSA.setDescription(StringSelectorCore.getString("sa_estrogen_cerapp_active_prob_description") + SMARTSActiveProb[i]);
            curSA.setImageURL("/insilico/core/alerts/png/estrogenbindcerapp/CERAPP_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_ER_ACTIVE_POSSIBLE, true);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTSInactive.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(StringSelectorCore.getString("sa_estrogen_cerapp_inactive_name") + (i+1));
            curSA.setDescription(StringSelectorCore.getString("sa_estrogen_cerapp_inactive_description") + SMARTSInactive[i]);
            curSA.setImageURL("/insilico/core/alerts/png/estrogenbindcerapp/CERAPP_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_ER_INACTIVE, true);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTSInactiveProb.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(StringSelectorCore.getString("sa_estrogen_cerapp_inactive_prob_name") + (i+1));
            curSA.setDescription(StringSelectorCore.getString("sa_estrogen_cerapp_inactive_prob_description") + SMARTSInactiveProb[i]);
            curSA.setImageURL("/insilico/core/alerts/png/estrogenbindcerapp/CERAPP_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_ER_INACTIVE_POSSIBLE, true);
            Alerts.add(curSA);
            idx++;
        }
      

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new Pattern[nRules];
            
            int idx = 0;
            for (String s : SMARTSActive) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : SMARTSActiveProb) {
                SA[idx] =  SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }

            for (String s : SMARTSInactive) {
                SA[idx] =  SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : SMARTSInactiveProb) {
                SA[idx] =  SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false
                );
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
                if (matches > 0)
                    Res.add((Alert)Alerts.get(i).clone());
            }
                        
        } catch (InvalidMoleculeException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (String s : SMARTSActive) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "CERAPP_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (String s : SMARTSActiveProb) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "CERAPP_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (String s : SMARTSInactive) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "CERAPP_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (String s : SMARTSInactiveProb) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "CERAPP_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

    }
    
}