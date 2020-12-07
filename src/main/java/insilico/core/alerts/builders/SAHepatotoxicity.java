package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
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
public class SAHepatotoxicity extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    private int nRules;

    private final static String[] SMARTSActive = {
        "C(=CC(C)C)CCCCCCC",
        "O=CC(NC)CO",
        "O(CCCC)CC(NC)CC",
        "O=C(NCCCC)c1ccccc1",
        "OCC(Oc1ccccc1)(C)C",
        "c1ccc(cc1)SCC",
        "c1ccc(cc1)CCCc2ccccc2",
        "O=COC(CC(O)C)C(C)C",
        "O=CCc1ccccc1(N)",
        "N(CC)CCCCNCC", // 10
        "O=C(CO)C(O)CCO",
        "n2cnc1c(ncn1CCOC)c2N",
        "O(c1cccc(c1)CNCCCC)C",
        "O=C(OCCc1ccccc1)C",
        "OCC(O)C(O)CCCNC",
        "O=[N+]([O-])c1cccc(O)c1",
        "c1c(cc(cc1C)CC)C",
        "O=CNC(CC=O)CCC",
        "O=CNC(C(=O)O)CCC",
        "O=SCCNCCC", // 20
        "O=C(N)N(N)CC",
        "FC(F)CCl",
        "O=C(NC)CCc1ccccc1",
        "O=COC(C)COC",
        "Oc1ccc(c(OC)c1)C",
        "N(C)C(C)CNCCCC",
        "NCCCCCNC",
        "O=CC(N)Cc1ccccc1",
        "c1ccc(c(c1)C=CC)C",
        "n1cc[nH]c1", // 30
        "NCNc1ccccc1",
        "O=CC(c1ccccc1)CN",
        "FC(F)(F)c1ccccc1",
        "n1ccccc1Cc2ccccc2",
        "N(CC)CCCl",
        "c1cc(C)sc1",
        "C(=C(Cl))",
        "O=S(=O)(N)c1ccccc1",
        "Nc1ccc(cc1)S(=O)=O",
        "[n,o]1n[c,n][c,n,s,nH][c,n]1", // 40
        "O=C1CCCCCCCCCCCCO1",
        "[n,c]1ccn[n,c]c1", // 42
        "CNC(=O)N(CCCl)N=O", // 43
        "OC(=O)C1[C,S][S,O,C]C2CC(=O)N12",
        "Nc1[n,c]cc2C(=O)C(=CNc2[c,n]1)C(O)=O", // 45
        "O=C1N~CC=C[N,C]1C2C~[S,C]CO2", // 46
        "NS(=O)(=O)c1ccccc1", // 47
        "C1[S,C,N,O]c2ccccc2[N,C,S,O]c3ccccc13", // 48
        "*N(*)CCC(c1cccc[n,c]1)c2cccc[n,c]2", // 49
        "CC=C(C)C=CC=C(C)C=C[R,a]", // 50
        "Nc1[n,c]cnc2[n,c]cccc12" // 51
    };

    private final static String[] SMARTSInactive = {
        "OC(c1ccccc1)c2ccccc2",
        "O=C(O)CCc1ccc(OC)cc1",
        "OC1OC(CN)CCC1(N)",
        "OCCNC(C)(C)C",
        "O=C(OC2CCC3C4CCc1cc(O)ccc1C4(CCC23(C)))CC",
        "O=C(N(c1ccccc1)CC)C",
        "O=C(Nc1ccccc1C)CC",
        "O=COCC(C)(C)COC=O",
        "Oc4ccc1c2c4(OC3CCCC(O)(C(N(C)CC)C1)C23(C))", // 60
        "O=C(c1ccccc1)c2ccccc2(O)",
        "c1ccc(cc1)Cc2ccccc2Cl",
        "O=C(NC)CNC(=O)C(CC)CCC",
        "OC(c1cccc(OC)c1)CNC",
        "O(c1ccccc1)CCN(C)CC",
        "Oc1ccc(cc1)NCCCC",
        "OCCC1(C)(CCCCC1)",
        "c1ccc(c(c1)CCC)Cl",
        "C1C2CC3CC1CC(C2)C3",
        "N1CN(C)CC(C)C1",  // 70
        "OCCSCCC",
        "O=S(=O)(NC)C",
        "O=Cc1cccc(c1)NCC",
        "OCCC(CO)CCO",
        "O=C(N(c1ccccc1C)C)C",
        "O=CC(C)(C)CN",
        "CC[N+](C)(C)C",
        "O=C1CCCCC1",
        "c1ccc(cc1)I",
        "O=C(N)c1cccc(c1)S(=O)=O",  // 80
        "Nc1ccc2ccccc2(c1)",
        "O(c1ccc(cc1)C)CCC",
        "OC1OCC(O)C(O)C1(N)",
        "c1cc(c(cc1Cl)Cl)C",
        "NN=C",
        "CC(=O)Nc1ccccc1C",
        "CC(=O)NC1C2[S,O]CC=C(N2C1=O)C(O)=O", // 87
        "C1CC2CCC3C(CC[C,c]4[C,c][C,c][C,c][C,c][C,c]34)C2C1" // 88
    };

    private final static String[] SMARTSActiveDesc = {
        "Rule for hepatotocixity defined by the SMARTS C(=CC(C)C)CCCCCCC, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=CC(NC)CO, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O(CCCC)CC(NC)CC, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=C(NCCCC)c1ccccc1, with an accuracy on the training set of 89%",
        "Rule for hepatotocixity defined by the SMARTS OCC(Oc1ccccc1)(C)C, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS c1ccc(cc1)SCC, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS c1ccc(cc1)CCCc2ccccc2, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=COC(CC(O)C)C(C)C, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=CCc1ccccc1(N), with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS N(CC)CCCCNCC, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=C(CO)C(O)CCO, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS n2cnc1c(ncn1CCOC)c2N, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O(c1cccc(c1)CNCCCC)C, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=C(OCCc1ccccc1)C, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS OCC(O)C(O)CCCNC, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=[N+]([O-])c1cccc(O)c1, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS c1c(cc(cc1C)CC)C, with an accuracy on the training set of 80%",
        "Rule for hepatotocixity defined by the SMARTS O=CNC(CC=O)CCC, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=CNC(C(=O)O)CCC, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=SCCNCCC, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=C(N)N(N)CC, with an accuracy on the training set of 80%",
        "Rule for hepatotocixity defined by the SMARTS FC(F)CCl, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS O=C(NC)CCc1ccccc1, with an accuracy on the training set of 92%",
        "Rule for hepatotocixity defined by the SMARTS O=COC(C)COC, with an accuracy on the training set of 91%",
        "Rule for hepatotocixity defined by the SMARTS Oc1ccc(c(OC)c1)C, with an accuracy on the training set of 75%",
        "Rule for hepatotocixity defined by the SMARTS N(C)C(C)CNCCCC, with an accuracy on the training set of 90%",
        "Rule for hepatotocixity defined by the SMARTS NCCCCCNC, with an accuracy on the training set of 85%",
        "Rule for hepatotocixity defined by the SMARTS O=CC(N)Cc1ccccc1, with an accuracy on the training set of 82%",
        "Rule for hepatotocixity defined by the SMARTS c1ccc(c(c1)C=CC)C, with an accuracy on the training set of 77%",
        "Rule for hepatotocixity defined by the SMARTS n1cc[nH]c1, with an accuracy on the training set of 85%",
        "Rule for hepatotocixity defined by the SMARTS NCNc1ccccc1, with an accuracy on the training set of 75%",
        "Rule for hepatotocixity defined by the SMARTS O=CC(c1ccccc1)CN, with an accuracy on the training set of 83%",
        "Rule for hepatotocixity defined by the SMARTS FC(F)(F)c1ccccc1, with an accuracy on the training set of 86%",
        "Rule for hepatotocixity defined by the SMARTS n1ccccc1Cc2ccccc2, with an accuracy on the training set of 75%",
        "Rule for hepatotocixity defined by the SMARTS N(CC)CCCl, with an accuracy on the training set of 83%",
        "Rule for hepatotocixity defined by the SMARTS c1cc(C)sc1, with an accuracy on the training set of 71%",
        "Rule for hepatotocixity defined by the SMARTS C(=C(Cl)), with an accuracy on the training set of 75%",
        "Rule for hepatotocixity defined by the SMARTS O=S(=O)(N)c1ccccc1, with an accuracy on the training set of 71%",
        "Rule for hepatotocixity defined by the SMARTS Nc1ccc(cc1)S(=O)=O, with an accuracy on the training set of 75%",
        "Rule for hepatotocixity defined by the SMARTS [n,o]1n[c,n][c,n,s,nH][c,n]1, with an accuracy on the training set of 71%",
        "Rule for hepatotocixity defined by the SMARTS O=C1CCCCCCCCCCCCO1, with an accuracy on the training set of 71%",
        "Rule for hepatotocixity defined by the SMARTS [n,c]1ccn[n,c]c1, with an accuracy on the training set of 72%",
        "Rule for hepatotocixity defined by the SMARTS CNC(=O)N(CCCl)N=O, with an accuracy on the training set of 100%",
        "Rule for hepatotocixity defined by the SMARTS OC(=O)C1[C,S][S,O,C]C2CC(=O)N12, with an accuracy on the training set of 67%",
        "Rule for hepatotocixity defined by the SMARTS Nc1[n,c]cc2C(=O)C(=CNc2[c,n]1)C(O)=O, with an accuracy on the training set of 67%",
        "Rule for hepatotocixity defined by the SMARTS O=C1N~CC=C[N,C]1C2C~[S,C]CO2, with an accuracy on the training set of 82%",
        "Rule for hepatotocixity defined by the SMARTS NS(=O)(=O)c1ccccc1, with an accuracy on the training set of 71%",
        "Rule for hepatotocixity defined by the SMARTS C1[S,C,N,O]c2ccccc2[N,C,S,O]c3ccccc13, with an accuracy on the training set of 82%",
        "Rule for hepatotocixity defined by the SMARTS *N(*)CCC(c1cccc[n,c]1)c2cccc[n,c]2, with an accuracy on the training set of 83%",
        "Rule for hepatotocixity defined by the SMARTS CC=C(C)C=CC=C(C)C=C[R,a], with an accuracy on the training set of 75%",
        "Rule for hepatotocixity defined by the SMARTS Nc1[n,c]cnc2[n,c]cccc12, with an accuracy on the training set of 60%"
    };

    private final static String[] SMARTSInactiveDesc = {
        "Rule for NON hepatotocixity defined by the SMARTS OC(c1ccccc1)c2ccccc2, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C(O)CCc1ccc(OC)cc1, with an accuracy on the training set of 86%",
        "Rule for NON hepatotocixity defined by the SMARTS OC1OC(CN)CCC1(N), with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS OCCNC(C)(C)C, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C(OC2CCC3C4CCc1cc(O)ccc1C4(CCC23(C)))CC, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C(N(c1ccccc1)CC)C, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C(Nc1ccccc1C)CC, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=COCC(C)(C)COC=O, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS Oc4ccc1c2c4(OC3CCCC(O)(C(N(C)CC)C1)C23(C)), with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C(c1ccccc1)c2ccccc2(O), with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS c1ccc(cc1)Cc2ccccc2Cl, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C(NC)CNC(=O)C(CC)CCC, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS OC(c1cccc(OC)c1)CNC, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O(c1ccccc1)CCN(C)CC, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS Oc1ccc(cc1)NCCCC, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS OCCC1(C)(CCCCC1), with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS c1ccc(c(c1)CCC)Cl, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS C1C2CC3CC1CC(C2)C3, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS N1CN(C)CC(C)C1, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS OCCSCCC, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=S(=O)(NC)C, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=Cc1cccc(c1)NCC, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS OCCC(CO)CCO, with an accuracy on the training set of 100%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C(N(c1ccccc1C)C)C, with an accuracy on the training set of 88%",
        "Rule for NON hepatotocixity defined by the SMARTS O=CC(C)(C)CN, with an accuracy on the training set of 83%",
        "Rule for NON hepatotocixity defined by the SMARTS CC[N+](C)(C)C, with an accuracy on the training set of 83%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C1CCCCC1, with an accuracy on the training set of 80%",
        "Rule for NON hepatotocixity defined by the SMARTS c1ccc(cc1)I, with an accuracy on the training set of 87%",
        "Rule for NON hepatotocixity defined by the SMARTS O=C(N)c1cccc(c1)S(=O)=O, with an accuracy on the training set of 75%",
        "Rule for NON hepatotocixity defined by the SMARTS Nc1ccc2ccccc2(c1), with an accuracy on the training set of 75%",
        "Rule for NON hepatotocixity defined by the SMARTS O(c1ccc(cc1)C)CCC, with an accuracy on the training set of 70%",
        "Rule for NON hepatotocixity defined by the SMARTS OC1OCC(O)C(O)C1(N), with an accuracy on the training set of 78%",
        "Rule for NON hepatotocixity defined by the SMARTS c1cc(c(cc1Cl)Cl)C, with an accuracy on the training set of 78%",
        "Rule for NON hepatotocixity defined by the SMARTS NN=C, with an accuracy on the training set of 70%",
        "Rule for NON hepatotocixity defined by the SMARTS CC(=O)Nc1ccccc1C, with an accuracy on the training set of 78%",
        "Rule for NON hepatotocixity defined by the SMARTS CC(=O)NC1C2[S,O]CC=C(N2C1=O)C(O)=O, with an accuracy on the training set of 69%",
        "Rule for NON hepatotocixity defined by the SMARTS C1CC2CCC3C(CC[C,c]4[C,c][C,c][C,c][C,c][C,c]34)C2C1, with an accuracy on the training set of 70%"
    };


    
    
    
    public SAHepatotoxicity() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_HEPATOTOXICITY, "Rules for hepatotoxicity (IRFMN)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        nRules = SMARTSActive.length + SMARTSInactive.length;
        
        int idx = 0;
        Alert curSA;

        for (int i=0; i<SMARTSActive.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Hepatotoxicity toxic alert no. " + (i+1));
            curSA.setDescription(SMARTSActiveDesc[i]);
            curSA.setImageURL("/insilico/core/alerts/png/hepatotoxicity/HEPA_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_HEPA_TOXIC, true);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTSInactive.length; i++) {
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Hepatotoxicity NON-toxic alert no. " + (i+1));
            curSA.setDescription(SMARTSInactiveDesc[i]);
            curSA.setImageURL("/insilico/core/alerts/png/hepatotoxicity/HEPA_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_HEPA_NONTOXIC, true);
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

            for (String s : SMARTSInactive) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
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

        for (int i=0; i<SMARTSActive.length; i++) {
            String s = SMARTSActive[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "HEPA_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

        for (int i=0; i<SMARTSInactive.length; i++) {
            String s = SMARTSInactive[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "HEPA_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }
    
}