package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.fragmenter.FragmenterCRS4;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author User
 */
public class  SAMutagenCRS4 extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private InsilicoMolecule[] SA;
    private AlertFileClassificationSMARTS SMARTSFileReader;
    
    
    public SAMutagenCRS4() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MUTAGEN_CRS4, "CRS4 rule set for mutagenicity");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        URL u = getClass().getResource("/alerts_data/SA_Mutagen_CRS4.dat");
        
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
            throw new InitFailureException("Error while trying to open resource file " + u.getFile());
        } catch (GenericFailureException e) {
            throw new InitFailureException(e.getMessage());
        }
        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        SA = new InsilicoMolecule[SMARTSFileReader.getSize()];

        for (int i=0; i<SMARTSFileReader.getSize(); i++) {
            SA[i] = SmilesMolecule.Convert(SMARTSFileReader.getSMARTS()[i]);
            if (!SA[i].IsValid())
                throw new InitFailureException("Unable to init alert " + SMARTSFileReader.getSMARTS()[i]);
        }
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        
        AlertList Res = new AlertList();
        
        
        try {

            // Calculates molecule fragments
            List<IAtomContainer> Fragments_1 = FragmenterCRS4.getCCQfragments(CurMol.GetStructure());
            List<IAtomContainer> Fragments_2 = FragmenterCRS4.getRECAPfragments(CurMol.GetStructure());
            List<IAtomContainer> Fragments_3 = FragmenterCRS4.getROTATABLEfragments(CurMol.GetStructure());
            ArrayList<IAtomContainer> Fragments = new ArrayList<>();
            Fragments.addAll(Fragments_1);
            Fragments.addAll(Fragments_2);
            Fragments.addAll(Fragments_3);

            // Check against all rules
            UniversalIsomorphismTester tester = new UniversalIsomorphismTester();
            for (int i=0; i<SA.length; i++) 
                for (IAtomContainer curFrag : Fragments)
                    if (tester.isIsomorph(SA[i].GetStructure(),curFrag)) {
                        Res.add((Alert)Alerts.get(i).clone());
                        break;
                    }
            
            
        } catch (CDKException | CloneNotSupportedException e) {
            throw new GenericFailureException("Error while processing fragments: " + e.getMessage());
        } catch (InvalidMoleculeException ex) {
            throw new GenericFailureException("Invalid molecule, unable to process fragments");
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
//            Matcher = new CustomQueryMatcher(mol);
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
                List<IAtomContainer> matches = tester.getOverlaps(mol.GetStructure(), SA[i].GetStructure());
                int max = 0;
                for (IAtomContainer ac : matches)
                    if (ac.getAtomCount() > max) 
                        max = ac.getAtomCount();
                Res[i] = (double)max / (double)SA[i].GetStructure().getAtomCount();
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
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "CRS_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
            }
            idx++;
        }
    }
    
}