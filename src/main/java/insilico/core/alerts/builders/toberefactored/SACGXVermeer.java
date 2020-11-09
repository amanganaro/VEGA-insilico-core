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
public class SACGXVermeer extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static Object[][] SMARTS_TOX = {
        {"[N+](=NC)", 1.000},
        {"C(CSC)Cl", 1.000},
        {"C(O)C(Cl)Cl", 1.000},
        {"C=Cc3oc(cc3)[N+](=O)[O-]", 1.000},
        {"[N+]c1ccccc1C", 1.000},
        {"C(Br)Br", 1.000},
        {"C(C#C)", 1.000},
        {"C(C(Cl)(Cl))(Cl)Cl", 1.000},
        {"O=C(c1ccccc1)c2ccccc2", 1.000},
        {"c1cc(c(cc1C)C)C", 1.000},
        {"c1cc(ccc1Cc2ccc(cc2)Cl)Cl", 1.000},
        {"c1ccc(cc1)C(=CCC)C", 1.000},
        {"c1ccc(cc1)c2ccc(cc2)Cl", 1.000},
        {"c1ccc2cc3ccccc3cc2c1", 1.000},
        {"c1cnc2ccccn12", 1.000},
        {"c1nc(cc(n1)Cl)", 1.000},
        {"c1nncs1", 1.000},
        {"CC(CCC(C(CCCl))Cl)Cl", 1.000},
        {"Cc1ccccc1C(=O)OCCCC", 1.000},
        {"N(=Nc1ccc(cc1)NC)c2ccccc2", 1.000},
        {"N(=Nc1ccccc1C)c2ccccc2", 1.000},
        {"N(CCCl)CCCl", 1.000},
        {"n1c(N)scc1c2ccc(N)cc2", 1.000},
        {"n1c(nc(nc1NC))NC", 1.000},
        {"Nc1nc2ccccc2n1C", 1.000},
        {"NCc1ccc(O)cc1", 1.000},
        {"NN([O-])", 1.000},
        {"O(c1ccccc1)c2ccc(N)cc2", 1.000},
        {"O=[N+]([O-])c1cccs1", 1.000},
        {"O=[N+]([O-])c1cnc(n1)", 1.000},
        {"O=[N+]([O-])c1oc(cc1)c2ncsc2", 1.000},
        {"O=[N+]([O-])c1oc(cc1)c2nnco2", 1.000},
        {"O=C(Nc1ccc(O)cc1)C", 1.000},
        {"O=C(NN)c1ccncc1", 1.000},
        {"O=C(O)C(=CC)C", 1.000},
        {"O=C(O)C(Oc1ccc(cc1))(C)C", 1.000},
        {"O=C1NCNC(=O)C1CC", 1.000},
        {"O=C1OCC1", 1.000},
        {"O=NN(C(=O)NCC)CC", 1.000},
        {"O=NN(C(=O)NCCCC)C", 1.000},
        {"O=NN(C)CC(=O)C", 1.000},
        {"O=NN(C)CCCCC", 1.000},
        {"O=NN(CC(O)C)CCC", 1.000},
        {"O=NN(CO)C", 1.000},
        {"O=NNCCCCc1cnccc1", 1.000},
        {"O=P(O)(OC)OC", 1.000},
        {"O=S(=O)(OC)", 1.000},
        {"Oc1c(c(c(c(c1Cl))Cl))Cl", 1.000},
        {"OC1CC(=CC)CCC1", 1.000},
        {"OCC(CBr)", 1.000},
        {"P(N(C)C)", 1.000},
        {"SN(C)C", 1.000},
        {"O=CN(N)C", 0.943},
        {"N(CCCC)CCCC", 0.941},
        {"O=[N+]([O-])c1oc(cc1)", 0.938},
        {"NNCCN(C)", 0.933},
        {"O=NNCC", 0.929},
        {"C(C=C)c1ccc(OC)cc1", 0.929},
        {"O=P(OC)(OC)", 0.923},
        {"O=NN(CCC)", 0.917},
        {"O=CN(N=C)C", 0.909},
        {"Nc1ccc(cc1)c2ccccc2", 0.906},
        {"NNCC", 0.891},
        {"c1c(cc(cc1Cl)Cl)Cl", 0.889},
        {"n1c(nc(nc1N))N", 0.889},
        {"c1occc1", 0.886},
        {"c1ccc(cc1)c2ccccc2", 0.883},
        {"O=P(OC)", 0.882},
        {"C=CCN(N)", 0.882},
        {"N1CCOCC1", 0.875},
        {"Nc2ncccc2", 0.875},
        {"NNCCO", 0.875},
        {"N(N)C", 0.859},
        {"c1nc2ccccc2n1", 0.857},
        {"CC(O)(C(OC))C(C)C", 0.846},
        {"CC(C)Cl", 0.840},
        {"O=CNN", 0.839},
        {"C(CCl)Cl", 0.839},
        {"Nc1ccc(cc1)Cc2ccc(N)cc2", 0.833},
        {"O1c2ccc(cc2OC1)", 0.833},
        {"n1c(nc(c(c1)))N", 0.833},
        {"C(NN)c1ccccc1", 0.833},
        {"C(CBr)", 0.833},
        {"c1ccc2OCOc2c1", 0.833},
        {"Nc1nc(cc(n1))", 0.833},
        {"CCCCC=CC(C)C", 0.824},
        {"NCC=C", 0.813},
        {"Nc1cc(c(cc1C)C)", 0.800},
        {"NCCCN", 0.800},
        {"CNc1cccc(c1)Cl", 0.800}
    };
    
    private final static Object[][] SMARTS_NON_TOX = {
        {"C(N)NS(=O)(=O)c1ccc(cc1)", 1.000},
        {"C(=S)(N(C)C)SS", 1.000},
        {"c1cc(c(O)c(c1)C(C)CC)", 1.000},
        {"c1ccc(cc1)C(=O)CCl", 1.000},
        {"c1ccc(OCCCNC)cc1", 1.000},
        {"c1ccc(OP(O)O)cc1", 1.000},
        {"C1NCNCN1", 1.000},
        {"CC(=NNc1ccccc1)", 1.000},
        {"CC(OC(=O)C(c1ccc(cc1))C)", 1.000},
        {"CCCC(NC(=O)c1ccc(cc1))", 1.000},
        {"CCSP(OC)OC", 1.000},
        {"FC(F)(Cl)", 1.000},
        {"NC(=S)NN", 1.000},
        {"O(CCCC)CCl", 1.000},
        {"O=C(O)C=C(C=CC)", 1.000},
        {"O=C(O)CCCCCCCCCCCCCC", 1.000},
        {"O=C(O)COc1ccc(cc1Cl)", 1.000},
        {"O=C1C(=CCC(CC)C1)C", 1.000},
        {"O=S(=O)c1ccccc1C=C", 1.000},
        {"OCC(O)CC(O)C(O)", 1.000},
        {"P(=S)(OC)OC", 0.909},
        {"C(N)CCCCC(=O)", 0.857},
        {"NC(CNc2ccccc2)", 0.800},
        {"c1ccc(cc1)NCCN(C)C", 0.800}
    };
     
    
    public SACGXVermeer() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_CGX_VERMEER, "Rules for CGX carcinogenicity classification (IRFMN/VERMEER)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_TOX.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("CGX Carcinogenicity alert no. " + (i+1));
            curSA.setDescription("Structural alert for carcinogenicity (CGX) defined by the SMARTS: " + SMARTS_TOX[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/cgxvermeer/CGX_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_CARC_TOXIC, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS_TOX[i][1]);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTS_NON_TOX.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("CGX NON-Carcinogenicity alert no. " + (i+1));
            curSA.setDescription("Structural alert for NON-carcinogenicity (CGX) defined by the SMARTS: " + SMARTS_NON_TOX[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/cgxvermeer/CGX_VER_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_CARC_NONTOXIC, true);
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "CGX_VER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

        for (int i=0; i<SMARTS_NON_TOX.length; i++) {
            String s = (String)SMARTS_NON_TOX[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "CGX_VER_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }      
}