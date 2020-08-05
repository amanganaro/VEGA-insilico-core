package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.CustomQueryMatcher;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 *
 * @author User
 */
public class SAMutagenIRFMN extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    private AlertFileClassificationSMARTS SMARTSFileReader;
    
    
    public SAMutagenIRFMN() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MUTAGEN_IRFMN, "IRFMN rule set for mutagenicity");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        URL u = getClass().getResource("/insilico/core/alerts/builders/SA_Mutagen_IRFMN.dat");
        
        try {
            
            SMARTSFileReader = new AlertFileClassificationSMARTS(u.openStream());

            for (int i=0; i<SMARTSFileReader.getSize(); i++) {
                Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
                curSA.setName("MNM" + (i+1));
                curSA.setDescription("IRFMN alert n. " + (i+1) + " for " 
                        + (SMARTSFileReader.getToxicity()[i] ? "Mutagenicity" : "NON-Mutagenicity") + ", defined by the SMARTS: " + SMARTSFileReader.getSMARTS()[i]);
                curSA.setImageURL("/insilico/core/alerts/png/mutagenirfmn/MN_" + (i+1) + ".png");

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
                
                // Sets value for biocide if available
                Double BiocideAcc = SACombaseMutagenicityAccuracy.FindAccuracy(SMARTSFileReader.getSMARTS()[i]);
                if (BiocideAcc != null)
                    curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, BiocideAcc);
                
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
                SA[i] = SMARTSParser.parse(SMARTSFileReader.getSMARTS()[i], SilentChemObjectBuilder.getInstance());

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


    @Override
    public double[] getOverlapsPerc(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException {

        if (!mol.IsValid())
            throw new InvalidMoleculeException("Given molecule is not marked as valid");
        CurMol = mol;
        
        // Init
        try {
            Matcher = new CustomQueryMatcher(mol);
            if (!IsInitialized) {
                InitSMARTS();
                IsInitialized = true;
            }
        } catch (Exception e) {
            throw new GenericFailureException("Unable to init matcher: " + e.getMessage());
        }
        
        // Calculate overlaps
        double[] Res = new double[SA.length];
        
        try {
            UniversalIsomorphismTester tester = new UniversalIsomorphismTester();
            for (int i=0; i<SA.length; i++) {
                List<IAtomContainer> matches = tester.getOverlaps(mol.GetStructure(), SA[i]);
                int max = 0;
                for (IAtomContainer ac : matches)
                    if (ac.getAtomCount() > max) 
                        max = ac.getAtomCount();
                Res[i] = (double)max / (double)SA[i].getAtomCount();
            }
        } catch (InvalidMoleculeException | CDKException e) {
            throw new GenericFailureException("Error during matching: " + e.getMessage());
        }        
        
        return Res;

    }
    
    
    public void SaveSmartsPNG() {
    
                
        int idx = 1;
        for (String s : SMARTSFileReader.getSMARTS()) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "MN_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }
    
}