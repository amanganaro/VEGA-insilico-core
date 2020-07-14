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
public class SABCFIRFMN extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    private AlertFileQuantitativeSMARTS SMARTSFileReader;
    
    
    public SABCFIRFMN() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_BCF_IRFMN, "IRFMN rule set for BCF");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        URL u = getClass().getResource("/insilico/core/alerts/builders/SA_BCF_IRFMN.dat");
        
        try {
            
            SMARTSFileReader = new AlertFileQuantitativeSMARTS(u.openStream());

            for (int i=0; i<SMARTSFileReader.getSize(); i++) {
                Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
                curSA.setName("MNB" + (i+1));
                String RuleDesc = SMARTSFileReader.getDescription()[i].isEmpty()?"":(", " + SMARTSFileReader.getDescription()[i]);
                curSA.setDescription("IRFMN alert n. " + (i+1) + " for BCF" + 
                        RuleDesc + ", with mean value = " +
                        SMARTSFileReader.getMean()[i] + " and standard deviation = " +
                        SMARTSFileReader.getStdDev()[i] + " (based on " + SMARTSFileReader.getHits()[i] + " compounds)" +
                        ", defined by the SMARTS: " + SMARTSFileReader.getSMARTS()[i]);
                curSA.setImageURL("/insilico/core/alerts/png/bcfirfmn/MNB_" + (i+1) + ".png");

                // Mean value of alerts
                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, SMARTSFileReader.getMean()[i]);

                // Standard deviation of alerts
                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_STDEV, SMARTSFileReader.getStdDev()[i]);

                // Hits of alerts
                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_HITS, SMARTSFileReader.getHits()[i]);

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

        } catch (Exception ex) {
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