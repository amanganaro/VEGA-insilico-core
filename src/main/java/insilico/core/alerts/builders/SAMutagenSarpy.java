package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelector;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.CustomQueryMatcher;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smarts.SmartsPattern;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 *
 * @author User
 */
public class SAMutagenSarpy extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    private AlertFileClassificationSMARTS SMARTSFileReader;
    
    
    public SAMutagenSarpy() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MUTAGEN_SARPY, StringSelector.getString("sa_mutagen_sarpy_initialization"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        URL u = getClass().getResource("/alerts_data/SA_Mutagen_Sarpy.dat");
        
        try {
            
            SMARTSFileReader = new AlertFileClassificationSMARTS(u.openStream());

            for (int i=0; i<SMARTSFileReader.getSize(); i++) {
                Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
                curSA.setName("SM" + (i+1));
                curSA.setDescription(String.format(StringSelector.getString("sa_mutagen_sarpy_description"),
                        i+1,
                        SMARTSFileReader.getToxicity()[i] ? StringSelector.getString("sa_mutagen_sarpy_mutagenicity") : StringSelector.getString("sa_mutagen_sarpy_non_mutagenicity"),
                        SMARTSFileReader.getSMARTS()[i]));
                curSA.setImageURL("/insilico/core/alerts/png/mutagensarpy/SRPY_" + (i+1) + ".png");

                // Toxicity of alerts
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_TOXIC, SMARTSFileReader.getToxicity()[i]);

                // Sets accuracy of each alert
                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, SMARTSFileReader.getAccuracy()[i]);

                // Sets Fisher test p-value of each alert
                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_FISHER, SMARTSFileReader.getFisherPValue()[i]);

                // Sets value for biocide if available
                Double BiocideAcc = SACombaseMutagenicityAccuracy.FindAccuracy(SMARTSFileReader.getSMARTS()[i]);
                if (BiocideAcc != null)
                    curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY_BIOCIDES, BiocideAcc);
                
                // Sets parents
                String Parents = "";
                for (Integer CurParent : SMARTSFileReader.getParentAlerts()[i])
                    Parents = AlertEncoding.MergeAlertIds(AlertEncoding.BuildAlertId(BlockIndex, CurParent), Parents);
                curSA.setParentAlerts(Parents);
                
                Alerts.add(curSA);
            }

        } catch (IOException e) {
            throw new InitFailureException(String.format(StringSelector.getString("sa_open_file_error"), u.getFile()));
        } catch (GenericFailureException e) {
            throw new InitFailureException(e.getMessage());
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new Pattern[SMARTSFileReader.getSize()];
            
            for (int i=0; i<SMARTSFileReader.getSize(); i++) 
                SA[i] = SmartsPattern.create(SMARTSFileReader.getSMARTS()[i], DefaultChemObjectBuilder.getInstance()).setPrepare(false);

        } catch (Exception e) {
            throw new InitFailureException(StringSelector.getString("sa_exception_smarts_initialization"));
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            for (int i=0; i<SA.length; i++) {
                if ((SA[i].matches(CurMol.GetStructure())))
                    Res.add((Alert)Alerts.get(i).clone());
            }
            
        } catch (InvalidMoleculeException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    
    @Override
    public double[] getOverlapsPerc(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException {

        if (!mol.IsValid())
            throw new GenericFailureException(String.format(StringSelector.getString("sa_invalid_molecule_err"), "Invalid Molecule"));
        CurMol = mol;
        
        // Init
        try {
//            Matcher = new CustomQueryMatcher(mol);
            if (!IsInitialized) {
                InitSMARTS();
                IsInitialized = true;
            }
        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelector.getString("sa_invalid_molecule_err"), e.getMessage()));
        }
        
        // Calculate overlaps
        double[] Res = new double[SA.length];
        
        try {
//            UniversalIsomorphismTester tester = new UniversalIsomorphismTester();
            for (int i=0; i<SA.length; i++) {
                Iterable<IAtomContainer> matches = SA[i].matchAll(mol.GetStructure()).toSubstructures();
//                List<IAtomContainer> matches = tester.getOverlaps(mol.GetStructure(), SA[i]);
                int max = 0;
                for (IAtomContainer ac : matches) {
                    if (ac.getAtomCount() > max)
                        max = ac.getAtomCount();
                }
                Res[i] = (double)max / mol.GetStructure().getAtomCount();
            }
        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelector.getString("sa_matching_error_overlaps"),e.getMessage()));
        }        
        
        return Res;

    }
    

    public void SaveSmartsPNG() {
    
                
        int idx = 1;
        for (String s : SMARTSFileReader.getSMARTS()) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "SRPY_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelector.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }
    }
    
}