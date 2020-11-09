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

import java.io.IOException;
import java.net.URL;

/**
 *
 * @author User
 */
public class SAMutagenCRS4_ORIGINAL extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    private AlertFileClassificationSMARTS SMARTSFileReader;
    
    
    public SAMutagenCRS4_ORIGINAL() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MUTAGEN_CRS4, "CRS4 rule set for mutagenicity");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        URL u = getClass().getResource("/insilico/core/alerts/builders/SA_Mutagen_CRS4.dat");
        
        try {
            
            SMARTSFileReader = new AlertFileClassificationSMARTS(u.openStream());

            for (int i=0; i<SMARTSFileReader.getSize(); i++) {
                Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
                curSA.setName("CRM" + (i+1));
                curSA.setDescription("CRS4 alert n. " + (i+1) + " for " 
                        + (SMARTSFileReader.getToxicity()[i] ? "Mutagenicity" : "NON-Mutagenicity") + ", defined by the SMARTS: " + SMARTSFileReader.getSMARTS()[i]);
                curSA.setImageURL("/insilico/core/alerts/png/mutagencrs4/CRS_" + (i+1) + ".png");

                // Toxicity of alerts
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_TOXIC, SMARTSFileReader.getToxicity()[i]);

                // Sets accuracy of each alert
                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, SMARTSFileReader.getAccuracy()[i]);

                // Sets Fisher test p-value of each alert
                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_FISHER, SMARTSFileReader.getFisherPValue()[i]);

                // Sets parents
                String Parents = "";
                for (Integer CurParent : SMARTSFileReader.getParentAlerts()[i])
                    Parents = AlertEncoding.MergeAlertIds(AlertEncoding.BuildAlertId(BlockIndex, CurParent), Parents);
                curSA.setParentAlerts(Parents);
                
                Alerts.add(curSA);
            }

        } catch (IOException e) {
            throw new InitFailureException("Error while trying to open resource file " + u.getFile());
        } catch (GenericFailureException e) {
            throw new InitFailureException(e.getMessage());
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new QueryAtomContainer[SMARTSFileReader.getSize()];
            
            for (int i=0; i<SMARTSFileReader.getSize(); i++) 
                SA[i] = SMARTSParser.parse(SMARTSFileReader.getSMARTS()[i], DefaultChemObjectBuilder.getInstance());

        } catch (Exception e) {
            throw new InitFailureException("Unable to initialize SMARTS");
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            for (int i=0; i<SA.length; i++) {
                if (Matcher.matches(SA[i]))
                    Res.add((Alert)Alerts.get(i).clone());
            }
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
                
        int idx = 1;
        for (String s : SMARTSFileReader.getSMARTS()) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "CRS_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }
    
}