package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

/**
 *
 * @author User
 */
public class SASkinConcert extends AlertBlockFromSMARTS implements iAlertBlock {

    private Pattern[] SA;

    private final static String[] SMARTS_ACTIVE = {
            "C1(C)CC=C(C)CC1",
            "Cc1ccc(cc1)C(C)C",
            "C(C)C1CCCCC1",
            "CCC=C(C)C",
            "C(=CCC)C",
            "C=CCCCCC",
            "c1ccc(cc1)C(C)C",
            "C(Cl)Cl",
            "C(OC)CCCCCC",
            "CC(CC)CCCC",
            "CCN(C)C",
            "O=CC=C",
            "CCCCCCC",
            "C(CBr)C",
            "CCl",
    };

    private final static String[] SMARTS_INACTIVE = {
            "O=C(N)",
            "OCCOCCC",
            "CCCCCCCCCCCCCCCCC",
            "O=Cc1ccccc1C",
            "Nc1ccccc1C",
            "c1ccc(cc1)N",
            "c1ncncn1",
            "OCCCOC",
            "O=C(O)C(C)C",
            "O=Cc1ccc(cc1)",
            "CC(=O)C",
            "OC(C)CO",
            "O=C(OCC(C))C",
            "O=C(O)CC",
            "O=S",
            "OCCOCCOCC",
            "Fc1ccc(cc1)",
            "P(O)O",
    };


    public SASkinConcert() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_SKIN_IRR_CONCERT, "Rules for Skin Irritation classification (CONCERT REACH)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;

        for (int i=0; i<SMARTS_ACTIVE.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Skin irritation ACTIVE alert no. " + (i+1));
            curSA.setDescription("Structural alert for Skin irritation ACTIVE defined by the SMARTS: " + SMARTS_ACTIVE[i]);
            curSA.setSMARTS(SMARTS_ACTIVE[i]);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_IRR, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTS_INACTIVE.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Skin irritation INACTIVE alert no. " + (i+1));
            curSA.setDescription("Structural alert for Skin irritation INACTIVE defined by the SMARTS: " + SMARTS_INACTIVE[i]);
            curSA.setSMARTS(SMARTS_INACTIVE[i]);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_SKIN_NON_IRR, true);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        int nFragments = SMARTS_ACTIVE.length + SMARTS_INACTIVE.length;
        SA = new Pattern[nFragments];

        int idx = 0;
        for (String s : SMARTS_ACTIVE) {
            SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
            idx++;
        }
        for (String s : SMARTS_INACTIVE) {
            SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
            idx++;
        }

    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            int nFragments = SMARTS_ACTIVE.length + SMARTS_INACTIVE.length;
            
            for (int i=0; i<nFragments; i++) 
                if (SA[i].matches(CurMol.GetStructure()))
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            return null;
        }
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
//        int idx = 1;
//
//        for (int i=0; i<SMARTS_SENS.length; i++) {
//            String s = (String)SMARTS_SENS[i][0];
//            try {
//                InsilicoMolecule mol = SmilesMolecule.Convert(s);
//                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "SKIN_VER_" + (idx) + ".png");
//            } catch (Exception e) {
//                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
//            }
//            idx++;
//        }
//
//        for (int i=0; i<SMARTS_NON_SENS.length; i++) {
//            String s = (String)SMARTS_NON_SENS[i][0];
//            try {
//                InsilicoMolecule mol = SmilesMolecule.Convert(s);
//                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "SKIN_VER_" + (idx) + ".png");
//            } catch (Exception e) {
//                System.out.println("errore in " + idx + " " + s + " - " + e.getMessage());
//            }
//            idx++;
//        }

    }    
}