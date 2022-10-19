package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 *
 * @author User
 */
public class SACarcinogenicityIRFMN extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static String[] CarcSMARTS = {
        "CCCCCCN(C)N=O", // 1
        "CNCCNN=O", // 2
        "CNCCN(C)N=O", // 3
        "CCCCCN(C)N=O", // 4
        "CC(O)CNN=O", // 5
        "CN(N=O)C(=O)NCCO", // 6
        "NC(=O)N(CCO)N=O", // 7
        "CCc1ccc(OC)cc1O", // 8
        "CCOC(=O)C(C)(C)O", // 9
        "CN(C)c1ccc(Cc2ccccc2)cc1", // 10
        "CNc1ccc(C=C)cc1", // 11
        "NNCO", // 12
        "CN(N)CO", // 13
        "c1ccc2c(c1)c4cccc3cccc2c43", // 14
        "Oc1cccc(c1)-c2cccc(O)c2", // 15
        "OS(O)(=O)=O", // 16
        "COS(O)=O", // 17
        "ClCCNCCCl", // 18
        "CC(C)=NO", // 19
        "Cn1cncn1", // 20
        "Nc1ncc2ncn(CCCCO)c2n1", // 21
        "OCC1OC(CC1O)n3cnc2cncnc32", // 22
        "O=C(OCc1ccccc1)c2ccccc2", // 23
        "[O-][N+](=O)c1ccco1", // 24
        "CCNCC(C)=O", // 25
        "CC1COCO1", // 26
        "Nc1ccc([S]c2ccccc2)cc1", // 27
        "Cc1ccc(cc1)C(N)=O", // 28
        "CN(C)P(N(C)C)N(C)C", // 29
        "Nc1ccc([S]c2ccccc2)cc1", // 30
        "O=C1CCO1", // 31
        "OCCNCC=C", // 32
        "C1CN1", // 33
        "c1cc2ccccc2s1", // 34
        "Cc1nccn1", // 35
        "[O-][N+](=O)c1ccc(o1)-c2cscn2", // 36
        "CN=[N+]", // 37
        "CCCN=CN", // 38
        "Cc1cccc2ccccc12", // 39
        "CCCN(CCC)N=O", // 40
        "CC(C)C(C)(O)CO", // 41
        "CCCCN(C)N=O", // 42
        "CC(O)CNN", // 43
        "CN(C=O)N=O", // 44
        "CCCN(N)CCC", // 45
        "CN(N=O)C(N)=O", // 46
        "CBr", // 47
        "Cc1ccccc1-c2ccc(N)cc2", // 48
        "COS(=O)=O", // 49
        "CCNCCCC(C)C", // 50
        "CCCNCNN=O", // 51
        "CC1CCC=C(C)C1", // 52
        "c1ccc2cc3c(ccc4ccccc34)cc2c1", // 53
        "CN[N+]=O", // 54
        "ClC1CCCC(Cl)C1Cl", // 55
        "COc1ccc(CC=C)cc1", // 56
        "N=[N+]", // 57
        "CCNN=O", // 58
        "CCOCc1ccccc1C", // 59
        "CCCNN=O", // 60
        "CCCN(C)N", // 61
        "NNCCO", // 62
        "Nc1ccc(Cc2ccc(N)cc2)cc1", // 63
        "Nc1ccc(C=C)cc1", // 64
        "CCNN", // 65
        "Oc1cccc2ccccc12", // 66
        "CCCNN", // 67
        "NNCC=C", // 68
        "Cc1ccc(cc1)S(O)(=O)=O", // 69
        "Nc1cccc(c1)S(O)(=O)=O", // 70
        "NNC=O", // 71
        "Nc1ccc(cc1)-c2ccc(N)cc2", // 72
        "Nc1ccc(cc1)-c2ccccc2", // 73
        "CCBr", // 74
        "C1C=CCC=C1", // 75
        "c1ccoc1", // 76
        "CCNCCCl", // 77
        "O=C1c2ccccc2C(=O)c3ccccc13", // 78
        "NN", // 79
        "ClCCCl", // 80
        "Cc1ccncn1", // 81
        "CCCCC(O)CCCCC(O)CCC", // 82
        "c1cscn1", // 83
        "Cc1ccc(N)c(C)c1", // 84
        "C#C", // 85
        "Cc1ccc(NO)cc1", // 86
        "Cc1ccc(NO)cc1", // 87
        "Cc1cccnc1", // 88
        "Clc1cccc(Cl)c1Cl", // 89
        "NNCc1ccccc1", // 90
        "N=O", // 91
        "CC(C)(O)C(O)=O", // 92
        "CCOC(N)=O", // 93
        "CC(Cl)CCCCCl", // 94
        "C1CCc2ccccc2C1", // 95
        "CC(=O)NN", // 96
        "COP=O", // 97
        "Cc1ccc(N)cc1", // 98
        "Cc1ccc(N)cc1", // 99
        "OCC#C", // 100
        "CC=C(C)CO", // 101
        "CC(O)CCCC=O", // 102
        "NNc1ccccc1", // 103
        "C[S]=O", // 104
        "NO", // 105
        "CC(C)=N", // 106
        "c1ncnn1", // 107
        "Nc1cccc(c1)-c2ccccc2", // 108
        "CN=O", // 109
        "C(=Cc1ccccc1)c2ccccc2", // 110
        "CC(CN)c1ccccc1", // 111
        "Cc1cccc(N)c1", // 112
        "CC(=C)C(O)=O", // 113
        "CCF", // 114
        "C=CCCCCC=O", // 115
        "COc1ccccc1N", // 116
        "c1ccc(cc1)N=Nc2ccccc2", // 117
        "c1cncnc1", // 118
        "Nc1ccc2ccccc2c1", // 119
        "Nc1ccccn1", // 120
        "COc1cccc(O)c1", // 121
        "CCl", // 122
        "Cc1ccccc1N", // 123
        "Nc1ccccc1O", // 124
        "Nc1ccc(O)c(N)c1", // 125
        "CNc1ccc(N)cc1", // 126
        "c1ccsc1", // 127
        "Cc1ccccc1[N+]", // 128
        "O(c1ccccc1)c2ccccc2", // 129
        "C1CO1" // 130    
    };
    
    
    
    public SACarcinogenicityIRFMN() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_CARC_IRFMN, "Rules for carcinogenicity classification (IRFMN)");
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        for (int i=0; i<CarcSMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName("Carcinogenity alert no. " + (i+1));
            curSA.setDescription("Structural alert for carcinogenity defined by the SMARTS: " + CarcSMARTS[i]);
            Alerts.add(curSA);
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new QueryAtomContainer[CarcSMARTS.length];
            
            int idx = 0;
            for (String s : CarcSMARTS) {
                SA[idx] = SMARTSParser.parse(s, DefaultChemObjectBuilder.getInstance());
                idx++;
            }
            
        } catch (Exception e) {
            throw new InitFailureException("Unable to initialize SMARTS");
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            for (int i=0; i<CarcSMARTS.length; i++) 
                if (Matches(SA[i])) 
                    Res.add((Alert)Alerts.get(i).clone());
            
        } catch (CDKException | CloneNotSupportedException e) {
            return null;
        }
        
        return Res; 
    }

    
}