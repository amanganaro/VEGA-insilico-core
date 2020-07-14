package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 *
 * @author User
 */
public class SAReprotoxVermeer extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static Object[][] SMARTS_TOX = {
        {"C(c1cc(ccc1)O)CO", 1.000}, 
        {"C(CO)(Cl)", 0.813}, 
        {"c1c(oc(c1))", 1.000}, 
        {"c1cc(c(c(c1)OC)O)OC", 1.000}, 
        {"c1ccc(n1)", 1.000}, 
        {"c1ccccc1OCNC", 1.000}, 
        {"CC=CC1C(CCC1C)", 0.879}, 
        {"Cc1c(cc(cc1))[N+](=O)[O-]", 0.800}, 
        {"CC1CC(CC(C1)CO)", 0.971}, 
        {"CCn1ccnc1", 1.000}, 
        {"N(CCCl)CC", 0.938}, 
        {"O([Si](C)C)[Si](c1ccccc1)", 1.000}, 
        {"CC(=O)C", 0.814}, 
        {"CCCl", 0.814}, 
        {"C(=O)CCCC1CCCCC1", 1.000}, 
        {"C(=O)CCCCCCCN", 1.000}, 
        {"C(=O)NO", 1.000}, 
        {"C(C)CC(C)CCc1ccccc1", 0.962}, 
        {"C(C)CNCC(=O)N", 1.000}, 
        {"C(Cc1cccc(O)c1)C", 0.957}, 
        {"C(CCc1ccccc1)N", 0.951}, 
        {"C(CO)NCCNC(CC)", 1.000}, 
        {"C(N)CNCc1ccccc1", 1.000}, 
        {"C=CCCC(O)CC", 1.000}, 
        {"C1=NCCN1CCO", 1.000}, 
        {"c1c[nH]cc1C", 1.000}, 
        {"c1ccc(cc1)C(C)C(=O)", 0.842}, 
        {"c1ccc(cc1)CNC(C)C", 1.000}, 
        {"c1ccc(OCCN(C)C)cc1", 1.000}, 
        {"c1cccs1", 1.000}, 
        {"c1ncc(C)n1", 0.971}, 
        {"c1nnc2ccccc12", 1.000}, 
        {"c1oncc1", 1.000}, 
        {"CC(=C)C(=O)OCC(O)C", 1.000}, 
        {"CC1=C(C)CCCC1(C)", 1.000}, 
        {"CC1C(O)CCC1CC", 1.000}, 
        {"CC1C=CCC1CCCCCCC", 1.000}, 
        {"CC1CCC(=O)C1CC", 1.000}, 
        {"n1nc(n[nH]1)c3ccccc3(c2ccc(cc2)C)", 1.000}, 
        {"Cc1ccc(OP(Oc2ccc(C)cc2))cc1", 1.000}, 
        {"Cc1occc1", 1.000}, 
        {"Cc3ccncc3", 1.000}, 
        {"CCC(=C(c1ccccc1))", 1.000}, 
        {"CCc1cc(cc(c1O)C)C(=O)O", 1.000}, 
        {"CCCC(C)C(=O)N", 1.000}, 
        {"CCCc1ccc(C)cc1", 0.960}, 
        {"CCCCS(=O)(=O)", 1.000}, 
        {"CCn2cncn2", 0.818}, 
        {"CCNc1ccc(c(c1)C(F)(F)F)", 1.000}, 
        {"Clc1ccc(Cc2ccccc2)cc1", 1.000}, 
        {"ClCCN(CCCl)", 1.000}, 
        {"CN(N=O)", 1.000}, 
        {"Cn1c(ccc1)C", 1.000}, 
        {"Cn1cccc2ccc(cc12)", 1.000}, 
        {"COc1ccccc1O", 0.935}, 
        {"N=Cc1ccccc1", 1.000}, 
        {"n1cnc2cnc(N)nc12", 1.000}, 
        {"Nc1ccccn1", 1.000}, 
        {"NCC(O)CO", 0.949}, 
        {"NCCc1ccc(O)cc1", 0.959}, 
        {"OC(=O)N", 0.850}, 
        {"Oc1ccccc1[N+](=O)[O-]", 1.000}, 
        {"Oc2cc(C)cc(C)c2", 1.000}, 
        {"OCC(F)(F)CCC", 1.000}, 
        {"O1C=CCc2ccccc12", 1.000}, 
        {"O1c2ccccc2(C=CC1)", 1.000}, 
        {"CCCCCCC(=O)C", 0.994}, 
        {"C1=CCCCC1(C)C", 0.986}, 
        {"CCCC(NC)C(=O)", 0.982}, 
        {"CCCCNCCN", 0.982}, 
        {"CN=C(N)", 0.955}, 
        {"CCCC(F)C", 0.977}, 
        {"c1ccc(CCN)cc1", 0.933}, 
        {"NCCNCCO", 0.955}, 
        {"c1ccc(CC(=O))cc1", 0.846}, 
        {"C1CCNCC1C", 0.880}, 
        {"c1cncnc1", 0.872}, 
        {"OCC(F)", 0.911}, 
        {"CCC(C)C(=O)", 0.868}, 
        {"c3ccc(cc3)C(F)(F)F", 0.857}, 
        {"CCCCc1ccccc1", 0.821}, 
        {"C(CCCO)CO", 0.828}, 
        {"CC(C)C(N)", 0.811}, 
        {"CCCN(CCCCC)", 0.820}        
    };
    
    private final static Object[][] SMARTS_NON_TOX = {
        {"C(=C)C#N", 1.000},
        {"C(=O)CCCCC(=O)", 1.000},
        {"C(C)OO", 1.000},
        {"Cc1cc(Cc2ccccc2)ccc1O", 1.000},
        {"C(CC)CC(C)(C)CC(C)(C)C", 1.000},
        {"C(O)C(C)(C)COC(=O)C(C)C", 1.000},
        {"C[N+](=O)[O-]", 1.000},
        {"C[N+](CCOCC)", 1.000},
        {"c1c(N)ccc2cc(cc(O)c12)S(=O)(=O)O", 1.000},
        {"O=S(=O)(c1ccccc1)c2ccccc2", 1.000},
        {"c1ccc(cc1[N+](=O)[O-])S(=O)(=O)", 1.000},
        {"O(c1ccccc1)CC2OC2", 1.000},
        {"n2c1ccccc1sc2SNC(C)C", 1.000},
        {"c1ccc(cc1)C(c2cccc(c2)C)C", 1.000},
        {"Cc1cccc(c1)S(=O)(=O)O", 1.000},
        {"CCC(=CCCC(C)(O)C)C", 1.000},
        {"CCC(C)(C)NC", 1.000},
        {"CCC(c1cc(c(O)cc1)C(C)(C)C)", 1.000},
        {"CCCCCC(=O)OCC(O)C", 1.000},
        {"CCCCCCCCCC[N+](C)(C)", 1.000},
        {"CCCCCCCCCCCCN(CCCN)", 1.000},
        {"CCCCCCCCCCCCOC(=O)C=C", 1.000},
        {"CCCCCCOC(=O)C(=C)C", 1.000},
        {"CCCCCP(=O)(O)", 1.000},
        {"CCCCN(C)C(=S)S", 1.000},
        {"CCCCOC=C", 1.000},
        {"CCCN(CC)CS", 1.000},
        {"CCCN(CCO)CCO", 1.000},
        {"CCOCCCCOCCOC", 1.000},
        {"CCOP(=S)OC", 1.000},
        {"CN(C)CCCNCCCN", 1.000},
        {"CN=N", 1.000},
        {"COC(=O)C=CC(=O)O", 1.000},
        {"COC(=O)CCCCCCCCCOC", 1.000},
        {"COCCOCCOC(=O)", 1.000},
        {"N(CCCC)S(=O)(=O)c1ccccc1", 1.000},
        {"N=C=O", 1.000},
        {"n1nc2ccccc2n1", 1.000},
        {"Nc1ccc(Nc2ccccc2)cc1", 1.000},
        {"Nc1nc(N)nc(n1)", 1.000},
        {"O=Cc1ccc(cc1)C(=O)", 1.000},
        {"Oc1ccc(cc1)S(=O)(=O)", 1.000},
        {"Oc1ccc2ccccc2c1N=N", 1.000},
        {"OCC#CCO", 1.000},
        {"CCCCCCOC(=O)C=C", 0.963},
        {"C(=O)OC(=O)", 0.929},
        {"Nc4cccc(c4)S(=O)(=O)O", 0.933},
        {"C(=O)OCCOC(=O)", 0.923},
        {"C(CC)COC(=O)C=C", 0.905},
        {"c1ccc(cc1)S(=O)(=O)O", 0.897},
        {"CC(C)(COCCC)COCCC", 0.889},
        {"CCCCCCCCCCCCCCN(C)C", 0.889},
        {"CNCCOC(=O)C(=C)", 0.889},
        {"COC(=O)C=C", 0.860},
        {"Nc1ccc(Cc2ccccc2)cc1", 0.875},
        {"CC(C)(CO)CO", 0.833},
        {"Cc1ccc(O)c(c1)C(C)(C)C", 0.833},
        {"c1cccc2ccccc12", 0.813},
        {"CC(=CCCC(=CC))C", 0.818},
        {"CCCOC=C", 1.000}
    };
     
    
    public SAReprotoxVermeer() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_REPROTOX_VERMEER, "Rules for reproductive toxicity classification (IRFMN/VERMEER)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_TOX.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Repro Tox alert no. " + (i+1));
            curSA.setDescription("Structural alert for reproductive toxicity defined by the SMARTS: " + SMARTS_TOX[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/reprovermeer/REPRO_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_REPRO_TOXIC, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS_TOX[i][1]);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTS_NON_TOX.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Repro NON Tox alert no. " + (i+1));
            curSA.setDescription("Structural alert for reproductive NON toxicity defined by the SMARTS: " + SMARTS_NON_TOX[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/reprovermeer/REPRO_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_REPRO_NONTOXIC, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS_NON_TOX[i][1]);
            Alerts.add(curSA);
            idx++;
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTS_TOX.length + SMARTS_NON_TOX.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (Object[] arr : SMARTS_TOX) {
                SA[idx] = SMARTSParser.parse((String)arr[0], DefaultChemObjectBuilder.getInstance());
                idx++;
            }
            for (Object[] arr : SMARTS_NON_TOX) {
                SA[idx] = SMARTSParser.parse((String)arr[0], DefaultChemObjectBuilder.getInstance());
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

            int nFragments = SMARTS_TOX.length + SMARTS_NON_TOX.length;
            
            for (int i=0; i<nFragments; i++) 
                if (Matches(SA[i])) 
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    
    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<SMARTS_TOX.length; i++) {
            String s = (String)SMARTS_TOX[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "REPRO_VER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

        for (int i=0; i<SMARTS_NON_TOX.length; i++) {
            String s = (String)SMARTS_NON_TOX[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "REPRO_VER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }      
}