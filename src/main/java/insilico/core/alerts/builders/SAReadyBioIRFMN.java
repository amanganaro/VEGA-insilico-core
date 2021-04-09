package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelector;
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
public class SAReadyBioIRFMN extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    
    private final static String[] SMARTSNonRB = {
        // nRB specifiche
        "c1ccc(c(c1)Cl)Cl", // 1" 
        "c1cc(cc(c1)CC)C", // 2" 
        "C(C(Cl))Cl", // 3" 
        "Nc1ccc(cc1Cl)", // 4" 
        "c1ccccc1C(=O)c2ccccc2", // 5" 
        "O=P(OC)(OC)O", // 6" 
        "O=CC1CC=CCC1C(=O)", // 7" 
        "Nc1ccc(c(N)c1)", // 8" 
        "CCCBr", // 9" 
        "Oc1ccc(c(c1)Cl)", // 10" 
        "F", // 11" 
        "c1ccc(cc1)C(c2ccccc2)C", // 12" 
        "P(=S)(OC)O", // 13" 
        "c1ccc(cc1)Nc2ccc(cc2)", // 14" 
        "c1ccc2c(c1)cccc2N", // 15" 
        "Cc1cccc2ccccc12", // 16" 
        "C(c1cccc(c1))N(C)C", // 17" 
        "CCCCCCCC(C)C", // 18" 
        "c1nc2ccccc2s1", // 19" 
        "[Sn]", // 20" 
        "C=N", // 21" 
        "c1cc(ccc1C(C))C(C)", // 22" 
        "CCCOc1ccccc1", // 23" 
        "O(C)CC(CC)CCCC", // 24" 
        "O=Cc1c(cccc1Cl)", // 25" 
        "c1ccc(c(c1)CC)C", // 26" 
        "Nc1ccc(c(c1)Cl)", // 27" 
        "CCCNCCOC", // 28" 
        "C=C(C)CC(C)(C)", // 29" 
        "c1ccc(cc1)Br", // 30" 
        "O=C(O)NC", // 31" 
        "CC(Cl)(Cl)Cl", // 32" 
        "C(=CCl)", // 33" 
        "SS", // 34" 
    };

    private final static String[] SMARTSNonRBUncertain = {
        // nRB bilanciate
        "C(c1ccccc1)c2ccccc2", // 1" 
        "Cc1ccccc1Cl", // 2" 
        "c1ccc2c(c1)cccc2", // 3" 
        "Br", // 4" 
        "Nc1cccc(c1)C", // 5" 
        "ON", // 6" 
        "c1ccc(c(c1)Cl)", // 7" 
        "c1ccc(cc1)S(=O)(=O)O", // 8" 
        "C(N)(C)CC(C)", // 9" 
        "N=N", // 10" "expert"
        "[R][Cl,F,Br,I]", // 11" "expert"
    };
    
    private final static String[] SMARTSRB = {
        // RB specifiche
        "O=C(OCCO)C", // 1" 
        "O=CCC(=O)", // 2" 
        "O=C(N(CCO))", // 3" 
        "O(CCCCCCCCC)CCC", // 4" 
        "C=C(C)CCC=C(C)C", // 5" 
        "CCC(OC)C", // 6" 
        "P(O)(O)OCCCCCCCCCCCC", // 7" 
        "O=C(OCc1ccccc1)", // 8" 
        "O=C(O)CCCCC(=O)", // 9" 
        "CCCCCCCCCCCCCCCCCCCCCC", // 10" 
    };
    
    private final static String[] SMARTSRBUncertain = {
        // RB bilanciate
        "OCCCCCCCCCCCCC", // 1" 
        "O=C(C)CC", // 2" 
        "C(OC)C=C", // 3" 
        "C(C)CCC=C(C)C", // 4" 
        "O=C(OC)CC", // 5" 
        "C(=O)OCCCC", // 6" 
        "CCOCCCC", // 7" 
        "OCCCCCCCC", // 8" 
        "CCCCCCCCCCCCC", // 9" 
        "CCC(=O)O", // 10" 
        "O=C(O)c1ccc(cc1)", // 11" 
        "OCCCC", // 12" 
        "O=C(N)C", // 13" 
        "O=CC", // 14" 
        "OCCO", // 15" 
        "NCCC", // 16" "unknown"
        "S(=O)", // 17" "unknown"
        "CCCCCCC", // 18" "unknown"
        "O(c1ccccc1)C", // 19" "unknown"
        "CCCC", // 20" "unknown"
        "c1(ccccc1)C(Cl)", // 21" "unknown"
        "[a][C;D2]=O", // 22" "expert"
        "C#N", // 23" "expert"        
    };
    
    private final static String[] DescriptionNonRB = {
        " (1,2-dichlorobenzene)", // 1
        " (1-ethyl-3-methylbenzene)", // 2
        " (1,2-dichloroethane)", // 3
        " (2-chloroaniline)", // 4
        " (diphenylmethanone)", // 5
        " (dimethoxyphosphinic acid)", // 6
        " (cyclohex-4-ene-1,2-dicarbaldehyde)", // 7
        " (benzene-1,3-diamine)", // 8
        " (1-bromopropane)", // 9
        " (3-chlorophenol)", // 10
        " (fluorine)", // 11
        " ((1-phenylethyl)benzene)", // 12
        " (methoxy(sulfanylidene)phosphinous acid)", // 13
        " (N-phenylaniline)", // 14
        " (naphthalen-1-amine)", // 15
        " (1-methylnaphthalene)", // 16
        " (benzyldimethylamine)", // 17
        " (2-methylnonane)", // 18
        " (1,3-benzothiazole)", // 19
        " (tin)", // 20
        " (methanimine)", // 21
        " (1,4-diethylbenzene)", // 22
        " (propoxybenzene)", // 23
        " (2-ethyl-1-methoxyhexane)", // 24
        " (2-chlorobenzaldehyde)", // 25
        " (1-ethyl-2-methylbenzene)", // 26
        " (3-chloroaniline)", // 27
        " ((2-methoxyethyl)(propyl)amine)", // 28
        " (2,4-dimethylpent-1-ene)", // 29
        " (bromobenzene)", // 30
        " (methylcarbamic acid)", // 31
        " (1,1,1-trichloroethane)", // 32
        " (chloroethene)", // 33
        " (dithioperoxol)", // 34
    };
    
    private final static String[] DescriptionNonRBUncertain = {
        " (benzylbenzene)", // 1
        " (1-chloro-2-methylbenzene)", // 2
        " (naphthalene)", // 3
        " (bromine)", // 4
        " (3-methylaniline)", // 5
        " (hydroxylamine)", // 6
        " (chlorobenzene)", // 7
        " (benzenesulfonic acid)", // 8
        " (pentan-2-amine)", // 9
        " (diazene)", // 10
        " (halogenated ring structure)", // 11
    };
    
    private final static String[] DescriptionRB = {
        " (2-hydroxyethyl acetate)", // 1
        " (propanedial)", // 2
        " (N-(2-hydroxyethyl)formamide)", // 3
        " (1-propoxynonane)", // 4
        " (2,6-dimethylhepta-1,5-diene)", // 5
        " (2-methoxybutane)", // 6
        " ((dodecyloxy)phosphonous acid)", // 7
        " (benzyl formate)", // 8
        " (6-oxohexanoic acid)", // 9
        " (docosane)", // 10
    };
    
    private final static String[] DescriptionRBUncertain = {
        " (tridecan-1-ol)", // 1
        " (butan-2-one)", // 2
        " (3-methoxyprop-1-ene)", // 3
        " (2-methylhept-2-ene)", // 4
        " (methyl propanoate)", // 5
        " (butyl formate)", // 6
        " (1-ethoxybutane)", // 7
        " (octan-1-ol)", // 8
        " (tridecane)", // 9
        " (propanoic acid)", // 10
        " (benzoic acid)", // 11
        " (butan-1-ol)", // 12
        " (acetamide)", // 13
        " (acetaldehyde)", // 14
        " (ethane-1,2-diol)", // 15
        " (propan-1-amine)", // 16
        " (sulfanone)", // 17
        " (heptanes)", // 18
        " (anisole)", // 19
        " (butane)", // 20
        " ((chloromethyl)benzene)", // 21
        " (carbonyl bound to aromatic structure)", // 22
        " (formonitrile)" // 23        
    };    
    
    
    
    public SAReadyBioIRFMN() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_READY_BIO_IRFMN, StringSelector.getString("sa_ready_bio_irfmn_initialization"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTSNonRB.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelector.getString("sa_ready_nio_irfmn_name_nonready"), i+1, DescriptionNonRB[i]));
            curSA.setDescription(String.format(StringSelector.getString("sa_ready_nio_irfmn_description_nonready"), SMARTSNonRB[i]));


            curSA.setImageURL("/insilico/core/alerts/png/readybio/READYBIO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_READY_BIO_NON_RB, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTSNonRBUncertain.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelector.getString("sa_ready_nio_irfmn_name_possiblenonready"), i+1, DescriptionNonRBUncertain[i]));
            curSA.setDescription(String.format(StringSelector.getString("sa_ready_nio_irfmn_description_possiblenonready"), SMARTSNonRBUncertain[i]));
            curSA.setImageURL("/insilico/core/alerts/png/readybio/READYBIO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_READY_BIO_NON_RB_POSSIBLE, true);
            Alerts.add(curSA);
            idx++;
        }
        
        for (int i=0; i<SMARTSRB.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelector.getString("sa_ready_nio_irfmn_name_ready"), i+1, SMARTSRB[i]));
            curSA.setDescription(String.format(StringSelector.getString("sa_ready_nio_irfmn_description_ready"), SMARTSRB[i]));
            curSA.setImageURL("/insilico/core/alerts/png/readybio/READYBIO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_READY_BIO_RB, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTSRBUncertain.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelector.getString("sa_ready_nio_irfmn_name_possible_ready"), i+1, SMARTSRBUncertain[i]));
            curSA.setDescription(String.format(StringSelector.getString("sa_ready_nio_irfmn_description_possible_ready"), SMARTSRBUncertain[i]));
            curSA.setImageURL("/insilico/core/alerts/png/readybio/READYBIO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_READY_BIO_RB_POSSIBLE, true);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            int nFragments = SMARTSNonRB.length + SMARTSNonRBUncertain.length + 
                    SMARTSRB.length + SMARTSRBUncertain.length;
            SA = new Pattern[nFragments];
            
            int idx = 0;
            for (String s : SMARTSNonRB) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : SMARTSNonRBUncertain) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : SMARTSRB) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            for (String s : SMARTSRBUncertain) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            
        } catch (Exception e) {
            throw new InitFailureException(StringSelector.getString("sa_exception_smarts_initialization"));
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            int nFragments = SMARTSNonRB.length + SMARTSNonRBUncertain.length + 
                    SMARTSRB.length + SMARTSRBUncertain.length;
            
            for (int i=0; i<nFragments; i++) 
                if (SA[i].matches(CurMol.GetStructure()))
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (InvalidMoleculeException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<SMARTSNonRB.length; i++) {
            String s = (String)SMARTSNonRB[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "READYBIO_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelector.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (int i=0; i<SMARTSNonRBUncertain.length; i++) {
            String s = (String)SMARTSNonRBUncertain[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "READYBIO_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelector.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (int i=0; i<SMARTSRB.length; i++) {
            String s = (String)SMARTSRB[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "READYBIO_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelector.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

        for (int i=0; i<SMARTSRBUncertain.length; i++) {
            String s = (String)SMARTSRBUncertain[i];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "READYBIO_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelector.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }

    }       
}