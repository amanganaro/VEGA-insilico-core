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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author User
 */
public class SAMutagenSarpy18K extends AlertBlockFromSMARTS implements iAlertBlock {
    
    public static final String KEY_ALERT_MUTA_SARPY18K_CLASS = "ra_acc";

    private QueryAtomContainer[] SA;
    private AlertFileClassificationSMARTS SMARTSFileReader;
    
    
    public SAMutagenSarpy18K() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MUTAGEN_SARPY_18K, "Rules for mutagenicity classification (IRFMN/Sarpy 18K)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        URL u = getClass().getResource("/insilico/core/alerts/builders/SA_Mutagen_Sarpy_18k.dat");
        
        try {
            
            SMARTSFileReader = new AlertFileClassificationSMARTS(u.openStream());

            for (int i=0; i<SMARTSFileReader.getSize(); i++) {
                Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
                curSA.setName("SM" + (i+1));
                curSA.setDescription("Sarpy alert n. " + (i+1) + " for " 
                        + (SMARTSFileReader.getToxicity()[i] ? "Mutagenicity" : "NON-Mutagenicity") + ", defined by the SMARTS: " + SMARTSFileReader.getSMARTS()[i]);
                //curSA.setImageURL("/insilico/core/alerts/png/mutagensarpy/SRPY_" + (i+1) + ".png");

                // Toxicity of alerts
                curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_TOXIC, SMARTSFileReader.getToxicity()[i]);

                // Sets accuracy of each alert
                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, SMARTSFileReader.getAccuracy()[i]);

                // Sets class of the alert - for now it is stored in the p-value field of the reades
                curSA.setNumericProperty(KEY_ALERT_MUTA_SARPY18K_CLASS, (int)SMARTSFileReader.getFisherPValue()[i]);
                
                // Sets Fisher test p-value of each alert
//                curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_FISHER, SMARTSFileReader.getFisherPValue()[i]);

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
            
            for (int i=0; i<SMARTSFileReader.getSize(); i++) {
                SA[i] = SMARTSParser.parse(SMARTSFileReader.getSMARTS()[i], SilentChemObjectBuilder.getInstance());
            }
            
        } catch (Exception e) {
            throw new InitFailureException("Unable to initialize SMARTS");
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            for (int i=0; i<SA.length; i++) {
                
                try {
                if (!Alerts.get(i).getBoolProperty(InsilicoConstants.KEY_ALERT_IS_TOXIC))
                    if (Alerts.get(i).getNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY) < 0.8)
                        continue;
                } catch (Exception e) {}
                
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "SRPY_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }
    
    
    public void CalcStats(ArrayList<String> Mols, ArrayList<Integer> Exp) throws Exception {
        
        System.out.println("Processing mols: " + Mols.size());
        
        if (!IsInitialized) {
            InitSMARTS();
            IsInitialized = true;
        }

        int[] matches = new int[SA.length];
        int[] correct = new int[SA.length];
        for (int i=0; i<SA.length; i++) {
            matches[i] = 0;
            correct[i] = 0;
        }
        
        for (int m=0; m< Mols.size(); m++) {
            
            if (m%250 == 0) System.out.println(m);
            
            InsilicoMolecule mol = SmilesMolecule.Convert(Mols.get(m));
            Matcher = new CustomQueryMatcher(mol);
            for (int i=0; i<SA.length; i++) {
                if (Matcher.matches(SA[i])) {
                    matches[i]++;
                    if (Exp.get(m) == 0) {
                        if (!Alerts.get(i).getBoolProperty(InsilicoConstants.KEY_ALERT_IS_TOXIC))
                            correct[i]++;
                    }
                    if (Exp.get(m) == 1) {
                        if (Alerts.get(i).getBoolProperty(InsilicoConstants.KEY_ALERT_IS_TOXIC))
                            correct[i]++;
                    }
                }                
            }
        }        
        
        System.out.println();
        System.out.println();
        System.out.println("no.\tmatch\tcorrect");
        for (int i=0; i<SA.length; i++) {
            System.out.print(i+1 + "\t");
            System.out.println(matches[i] + "\t" + correct[i]);
        }
        
    }
    
}