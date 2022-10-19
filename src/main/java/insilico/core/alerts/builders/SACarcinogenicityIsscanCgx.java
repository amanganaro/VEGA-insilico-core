package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

/**
 *
 * @author User
 */
public class SACarcinogenicityIsscanCgx extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private Pattern[] SA;
    
    private final static String[] CarcSMARTS = {
        "O=NNCC", // 1 - LR=inf
        "c1occc1", // 2 - LR=inf
        "O=CN(N)C", // 3 - LR=inf
        "CCCN(CC)CC", // 4 - LR=inf
        "C1CC(=CC)CCC1", // 5 - LR=inf
        "Nc1ccc(cc1C)C", // 6 - LR=inf
        "NCCCN", // 7 - LR=inf
        "O=S(=O)(OC)", // 8 - LR=inf
        "c1ccc2OCOc2c1", // 9 - LR=inf
        "Nc1ncccc1", // 10 - LR=inf
        "N(CCCl)CCCl", // 11 - LR=inf
        "c1cn(cnc1)", // 12 - LR=inf
        "C=C(C=C)C", // 13 - LR=inf
        "O=NNC", // 14 - LR=37.42
        "O=P(OC)", // 15 - LR=3.78
        "O(c1ccc(cc1)CC=C)", // 16 - LR=4.12
        "c1ncn(c1)C", // 17 - LR=3.78
        "C(CCCC(CC)Cl)Cl", // 18 - LR=2.75
        "c1ncsc1", // 19 - LR=5.84
        "C=CCN", // 20 - LR=4.81
        "O=Cc1ccccc1O", // 21 - LR=2.06
        "O(c1ccc(cc1N))C", // 22 - LR=1.37
        "O1CC1C", // 23 - LR=1.26
        "SN(C)C", // 24 - LR=inf
        "C(CCl)Cl", // 25 - LR=1.72
        "c1c(cc(cc1Cl)Cl)Cl", // 26 - LR=2.06
        "NNCC", // 27 - LR=13.62
        "O=CN(N)", // 28 - LR=7.32
        "C(OC)C(C)C", // 29 - LR=1.37
        "c1ccc2cc(ccc2c1)", // 30 - LR=1.58
        "Nc1cccc(c1C)C", // 31 - LR=1.37
        "NNc1ccccc1", // 32 - LR=1.37
        "c1cc(ccc1C)Cl", // 33 - LR=1.89
        "N(CCO)CCO", // 34 - LR=2.06
        "Nc1ccc(cc1N)", // 35 - LR=1.03
        "c1ccc(cc1N)C", // 36 - LR=1.42
        "O(c1ccc(cc1)C)C", // 37 - LR=1.85
        "C(c1ccccc1)CO", // 38 - LR=1.03
        "C(=CCC)CC", // 39 - LR=1.81
        "N(Cc1ccccc1)C", // 40 - LR=1.37
        "Nc1ccc(cc1)C", // 41 - LR=1.51
        "Nc1ccccc1", // 42 - LR=1.01
        "n1cccc(c1)" // 43 - LR=1.17    
    };
    
    
    
    public SACarcinogenicityIsscanCgx() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_CARC_ISSCANCGX, StringSelectorCore.getString("sa_carc_isscancgx_initialization"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        for (int i=0; i<CarcSMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName(StringSelectorCore.getString("sa_carc_isscancgx_name") + (i+1));
            curSA.setDescription(StringSelectorCore.getString("sa_carc_isscancgx_description") + CarcSMARTS[i]);
            Alerts.add(curSA);
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new Pattern[CarcSMARTS.length];
            
            int idx = 0;
            for (String s : CarcSMARTS) {
                SA[idx] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
                idx++;
            }
            
        } catch (Exception e) {
            throw new InitFailureException(StringSelectorCore.getString("sa_exception_smarts_initialization"));
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            for (int i=0; i<CarcSMARTS.length; i++) 
                if ((SA[i]).matches(CurMol.GetStructure()))
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            return null;
        }
        
        return Res; 
    }

    
}