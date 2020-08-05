package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 *
 * @author User
 */
public class SANephroVermeer extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static Object[][] SMARTS_TOX = {
        {"c1ccc(cc1)S(=O)(=O)O", 1.0},
        {"NS(=O)(=O)c1ccc(cc1)N", 1.0},
        {"c1cc(cc(c1)C#N)", 1.0},
        {"CC(=CCCC(=C))", 1.0},
        {"C(Cl)(Cl)Cl", 1.0},
        {"c1ccc(cc1)c2ccccc2", 0.744}
    };
     
    
    public SANephroVermeer() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_REPROTOX_VERMEER, "Rules for nephrotoxicity classification (IRFMN/VERMEER)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS_TOX.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Nephro Tox alert no. " + (i+1));
            curSA.setDescription("Structural alert for reproductive toxicity defined by the SMARTS: " + SMARTS_TOX[i][0]);
            curSA.setImageURL("/insilico/core/alerts/png/nephrotoxicity/NEPHRO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_NEPHRO_TOXIC, true);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS_TOX[i][1]);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTS_TOX.length;
            SA = new QueryAtomContainer[nFragments];
            
            int idx = 0;
            for (Object[] arr : SMARTS_TOX) {
                SA[idx] = SMARTSParser.parse((String)arr[0], SilentChemObjectBuilder.getInstance());
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

            int nFragments = SMARTS_TOX.length; 
            
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "NEPHRO_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }

    }        
    
}