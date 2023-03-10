package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

/**
 *
 * @author User
 */
public class SADevtoxConcert extends AlertBlockFromSMARTS implements iAlertBlock {

    private Pattern[] SA;

    private final static String[] SMARTS_ACTIVE = {
            "CCCCCF",
            "CCCCC(C)COCCCCCCO",
            "CCCCCCOCc1ccccc1C",
            "CCCC(CC)c1ccc(O)cc1",
            "NC(CCC=O)C(O)=O",
            "CCn1cc(C(O)=O)c(=O)c2cc(F)c(cc12)N1CCNCC1",
            "CN1CCC(CC1)OC(=O)Cc1ccccc1",
            "OCCCC(=O)c1ccccc1",
            "CCN(C)C(=O)c1ccccc1",
            "CC(C)C=CCCCCC=O",
            "COCCc1ccc(O)cc1",
            "Fc1ccc(C=O)cc1",
            "Oc1ccc(C=Cc2ccccc2)cc1",
            "O=Cc1cccc(c1)S(=O)=O",
            "CCC(O)C(=O)OC",
            "CN(N=O)C(N)=O",
            "O=C1CCC(=O)N1",
            "CNC(=O)CS",
            "ClC(Cl)C=O",
            "CC(N)C1CCC(N)C(OC2C(N)CCCC2O)O1",
            "Clc1ccccc1Cc1ccccc1",
            "Cc1nccc(-c2ccccc2)c1C",
            "CCCCOCc1cccc(O)c1",
            "OCCNCc1cccc(Cl)c1",
            "Nc1nc(N)c2ccccc2n1",
            "Nc1ncnc2ncnc12",
            "CC1OCCC1(C)O",
            "CN(C)C(=O)C=C",
            "FC(F)CCl",
            "CCOC(=O)C1=C(C)NC(C)=C(C1c1ccccc1)C(=O)OC",
            "CC(=O)OC1CCC2C3CCC4=CC(=O)CCC4C3CCC12C",
            "CCC1C(=O)N(N(C1=O)c1ccccc1)c1ccccc1",
            "C(C(c1ccccc1)c1ccccc1)c1ccccc1",
            "Oc1ccc(CCNCCc2ccccc2)cc1",
            "OCCCCCCCC1CCCC1C=CCO",
            "O1c2ccccc2C=Nc2ccccc12",
            "CCCN1CCN(CC1)c1ccccc1",
            "NC(Cc1ccc(O)c(O)c1)C(O)=O",
            "Cc1ccc(C)c2ccccc12",
            "OC(=O)Cc1ccc(O)cc1",
            "NC(=O)OCCCOC(N)=O",
            "COc1cc(Cl)c(Cl)cc1Cl",
            "CCCNCCC(C)(C)CC",
            "CC(C)Oc1ccccc1Cl",
            "CCOP(O)(=S)OCC",
            "O=CCc1cccs1",
            "COP(=S)(OC)SC",
            "CCOC(=O)CC=O",
            "Nc1ccnnc1",
            "CCOS(C)(=O)=O",
            "c1cnncn1",
            "CCNC(S)=S",
            "C1CNC=NC1",
            "C#N",
            "CN(C)CCc1ccccc1",
            "CCCCCCN",
            "OCCCO",
            "CCOCCO",
            "CCCCCCCCC=O",
            "NCCc1cccc(O)c1",
            "Cc1ccc(N)cc1",
            "CCCCCCNCC",
            "CC(C)OCCO",
            "CP(O)=O",
            "CCCNc1ccccc1",
            "CCNP(=O)NCC",
            "CCCNC(=O)NCC",
            "CC1(CC=O)CCCCC1",
            "Nc1ccnc(N)n1",
            "CCCCNC(C)=O",
            "COCCCOC",
            "CN(C)CCNCCO",
            "CCCNC(C)CN",
            "NC(N)=S",
            "CCCN(CCC)CCO",
            "c1nc[nH]n1",
            "CCCC(C)C(O)=O",
            "CCCCCF",
            "CCCCC(C)COCCCCCCO",
            "CCCCCCOCc1ccccc1C",
            "CCCC(CC)c1ccc(O)cc1",
            "NC(CCC=O)C(O)=O",
            "CCn1cc(C(O)=O)c(=O)c2cc(F)c(cc12)N1CCNCC1",
            "CN1CCC(CC1)OC(=O)Cc1ccccc1",
            "OCCCC(=O)c1ccccc1",
            "CCN(C)C(=O)c1ccccc1",
            "CC(C)C=CCCCCC=O",
            "COCCc1ccc(O)cc1",
            "Fc1ccc(C=O)cc1",
            "Oc1ccc(C=Cc2ccccc2)cc1",
            "O=Cc1cccc(c1)S(=O)=O",
            "CCC(O)C(=O)OC",
            "CN(N=O)C(N)=O",
            "O=C1CCC(=O)N1",
            "CNC(=O)CS",
            "ClC(Cl)C=O",
            "CC(N)C1CCC(N)C(OC2C(N)CCCC2O)O1",
            "Clc1ccccc1Cc1ccccc1",
            "Cc1nccc(-c2ccccc2)c1C",
            "CCCCOCc1cccc(O)c1",
            "OCCNCc1cccc(Cl)c1",
            "Nc1nc(N)c2ccccc2n1",
            "Nc1ncnc2ncnc12",
            "CC1OCCC1(C)O",
            "CN(C)C(=O)C=C",
            "FC(F)CCl",
            "CCOC(=O)C1=C(C)NC(C)=C(C1c1ccccc1)C(=O)OC",
            "CC(=O)OC1CCC2C3CCC4=CC(=O)CCC4C3CCC12C",
            "CCC1C(=O)N(N(C1=O)c1ccccc1)c1ccccc1",
            "C(C(c1ccccc1)c1ccccc1)c1ccccc1",
            "Oc1ccc(CCNCCc2ccccc2)cc1",
            "OCCCCCCCC1CCCC1C=CCO",
            "O1c2ccccc2C=Nc2ccccc12",
            "CCCN1CCN(CC1)c1ccccc1",
            "NC(Cc1ccc(O)c(O)c1)C(O)=O",
            "Cc1ccc(C)c2ccccc12",
            "OC(=O)Cc1ccc(O)cc1",
            "NC(=O)OCCCOC(N)=O",
            "COc1cc(Cl)c(Cl)cc1Cl",
            "CCCNCCC(C)(C)CC",
            "CC(C)Oc1ccccc1Cl",
            "CCOP(O)(=S)OCC",
            "O=CCc1cccs1",
            "COP(=S)(OC)SC",
            "CCOC(=O)CC=O",
            "Nc1ccnnc1",
            "CCOS(C)(=O)=O",
            "c1cnncn1",
            "CCNC(S)=S",
            "C1CNC=NC1",
    };

    private final static String[] SMARTS_INACTIVE = {
            "CCCC(=O)NCCc1ccccc1",
            "CC(C)(C)NCCc1ccccc1",
            "CCCCOc1ccc(C)cc1",
            "CCCCOCC(C)OC(C)=O",
            "Cc1ccccc1OCCN",
            "NCC1OC(CCC1O)OC1C(N)CC(N)C(O)C1O",
            "CCCCCCCCN(C)CCO",
            "Cc1cc(N)ccc1O",
            "CCCOP(O)(O)=O",
            "Clc1cc2oc3cc(Cl)c(Cl)cc3c2cc1Cl",
            "CN(C)CC(c1ccccc1)c1ccccc1",
            "C(c1ccccc1)n1cnc2ccccc12",
            "CCN(CC)CCNc1ccccc1C",
            "CNCC(C)NC(=O)c1ccccc1",
            "CCC(C)NC(C)Cc1ccccc1",
            "O=Cc1cnc2ccccc12",
            "c1ccc2nccnc2c1",
            "OCNc1ccccc1",
            "Sc1ccc(C=O)cc1",
            "N#Cc1ccccc1",
            "CN(C)C(N)=S",
            "BrCBr",
            "CCCCCCCCCCCCCCC(=O)OCCC",
            "CCOC(=O)C(CCc1ccccc1)NC(C)C=O",
            "OC1CCOC(OC2COC(O)C(O)C2O)C1O",
            "CCCN1c2ccccc2Sc2ccc(S)cc12",
            "CCN(CC)CCNCc1ccc(Cl)cc1",
            "Cc1ccccc1N=Nc1ccccc1",
            "CCN(CC)CCOCc1ccccc1",
            "C(Oc1ccccc1)c1ccccc1",
            "Clc1ccc(Cc2ccccn2)cc1",
            "Nc1ccc(cc1)S(=O)(=O)NC=O",
            "COC(=O)C1C(C=C(C)C)C1(C)C",
            "CCNc1ccc(cc1)S(N)(=O)=O",
            "CNCCC(O)C(O)C(O)CO",
            "OCCCc1ccc(Cl)cc1",
            "CC(C=O)=Cc1ccccc1",
            "Cc1ccc2ccccc2n1",
            "COC(=CC=O)C(=O)C(C)=C",
            "Brc1cc(Br)c(Br)c(Br)c1Br",
            "c1nncn1-c1ccccc1",
            "CCc1cccc(CN)c1",
            "CC1CCC(CN)CC1",
            "CCc1cccc(S)c1",
            "CCCCCSCCN",
            "Nc1cnccn1",
            "CN(C)NC=O",
            "CNC(=N)NC",
            "CCOP(C)=O",
            "CCNNCC",
            "c1cn[nH]c1",
            "OCC=O",
            "c1ccc2ccccc2c1",
            "CC=CCN",
            "CCCCCCCCCCC(C)CC(C)CC",
            "CCCCCCOC",
            "OCc1ccccc1",
            "CCS",
            "COCC=O",
            "NCc1ccccc1",
            "CC=CC(O)=O",
            "c1nc2ccccc2[nH]1",
            "FCF",
            "c1ccc(cc1)-c1ccccc1",
            "CCNCCC(C)CO",
            "c1ccocc1",
            "CCC(C)C(C)N",
            "O=CCC=O",
            "Oc1ccc(C=O)cc1",
            "NS(=O)(=O)c1ccccc1",
            "Cc1ccccc1Cc1ccccc1",
            "Clc1ccc(Cl)c(Cl)c1",
            "Nc1ccc(N)cc1",
            "Cc1ccsc1",
            "CC(C)COC(C)=O",
            "CC[N+](C)(C)C",
            "COC(c1ccccc1)c1ccccc1",
            "CCCC(=O)NCCc1ccccc1",
            "CC(C)(C)NCCc1ccccc1",
            "CCCCOc1ccc(C)cc1",
            "CCCCOCC(C)OC(C)=O",
            "Cc1ccccc1OCCN",
            "NCC1OC(CCC1O)OC1C(N)CC(N)C(O)C1O",
            "CCCCCCCCN(C)CCO",
            "Cc1cc(N)ccc1O",
            "CCCOP(O)(O)=O",
            "Clc1cc2oc3cc(Cl)c(Cl)cc3c2cc1Cl",
            "CN(C)CC(c1ccccc1)c1ccccc1",
            "C(c1ccccc1)n1cnc2ccccc12",
            "CCN(CC)CCNc1ccccc1C",
            "CNCC(C)NC(=O)c1ccccc1",
            "CCC(C)NC(C)Cc1ccccc1",
            "O=Cc1cnc2ccccc12",
            "c1ccc2nccnc2c1",
            "OCNc1ccccc1",
            "Sc1ccc(C=O)cc1",
            "N#Cc1ccccc1",
            "CN(C)C(N)=S",
            "BrCBr",
            "CCCCCCCCCCCCCCC(=O)OCCC",
            "CCOC(=O)C(CCc1ccccc1)NC(C)C=O",
            "OC1CCOC(OC2COC(O)C(O)C2O)C1O",
            "CCCN1c2ccccc2Sc2ccc(S)cc12",
            "CCN(CC)CCNCc1ccc(Cl)cc1",
            "Cc1ccccc1N=Nc1ccccc1",
            "CCN(CC)CCOCc1ccccc1",
            "C(Oc1ccccc1)c1ccccc1",
            "Clc1ccc(Cc2ccccn2)cc1",
            "Nc1ccc(cc1)S(=O)(=O)NC=O",
            "COC(=O)C1C(C=C(C)C)C1(C)C",
            "CCNc1ccc(cc1)S(N)(=O)=O",
            "CNCCC(O)C(O)C(O)CO",
            "OCCCc1ccc(Cl)cc1",
            "CC(C=O)=Cc1ccccc1",
            "Cc1ccc2ccccc2n1",
            "COC(=CC=O)C(=O)C(C)=C",
            "Brc1cc(Br)c(Br)c(Br)c1Br",
            "c1nncn1-c1ccccc1",
            "CCc1cccc(CN)c1",
            "CC1CCC(CN)CC1",
            "CCc1cccc(S)c1",
            "CCCCCSCCN",
            "Nc1cnccn1",
            "CN(C)NC=O",
            "CNC(=N)NC",
            "CCOP(C)=O",
            "CCNNCC",
            "c1cn[nH]c1",
    };


    public SADevtoxConcert() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_DEVTOX_CONCERT, "Rules for Developmental Toxicity classification (CONCERT REACH)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;

        for (int i=0; i<SMARTS_ACTIVE.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Devtox ACTIVE alert no. " + (i+1));
            curSA.setDescription("Structural alert for Developmental Toxicity ACTIVE defined by the SMARTS: " + SMARTS_ACTIVE[i]);
            curSA.setSMARTS(SMARTS_ACTIVE[i]);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_REPRO_TOXIC, true);
            Alerts.add(curSA);
            idx++;
        }

        for (int i=0; i<SMARTS_INACTIVE.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName("Devtox INACTIVE alert no. " + (i+1));
            curSA.setDescription("Structural alert for Developmental Toxicity INACTIVE defined by the SMARTS: " + SMARTS_INACTIVE[i]);
            curSA.setSMARTS(SMARTS_INACTIVE[i]);
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_REPRO_NONTOXIC, true);
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