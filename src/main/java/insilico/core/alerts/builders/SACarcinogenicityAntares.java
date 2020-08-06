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
public class SACarcinogenicityAntares extends AlertBlockFromSMARTS implements iAlertBlock {
    
    private QueryAtomContainer[] SA;
    
    private final static String[] CarcSMARTS = {
        "CN[N+]=O", // 1 - LR=5.53
        "NNC=O", // 2 - LR=3.83
        "CN(C=O)N=O", // 3 - LR=9.15
        "CCCCCCN(C)N=O", // 4 - LR=inf
        "CCCN(CCC)N=O", // 5 - LR=14.06
        "CNCCNN=O", // 6 - LR=inf
        "CNCCN(C)N=O", // 7 - LR=inf
        "CCNN=O", // 8 - LR=5.02
        "CCCCCN(C)N=O", // 9 - LR=inf
        "CCCCN(C)N=O", // 10 - LR=10.9
        "CC(O)CNN=O", // 11 - LR=inf
        "CN(N=O)C(=O)NCCO", // 12 - LR=inf
        "NC(=O)N(CCO)N=O", // 13 - LR=inf
        "CN(N=O)C(N)=O", // 14 - LR=8.17
        "CCCNN=O", // 15 - LR=4.77
        "O(c1ccccc1)c2ccccc2", // 16 - LR=1.11
        "COc1cccc(O)c1", // 17 - LR=1.27
        "CCc1ccc(OC)cc1O", // 18 - LR=inf
        "CCCNCNN=O", // 19 - LR=6.25
        "CCOC(=O)C(C)(C)O", // 20 - LR=inf
        "CC(C)(O)C(O)=O", // 21 - LR=2.19
        "Cc1ccccc1-c2ccc(N)cc2", // 22 - LR=7.91
        "Nc1ccc(cc1)-c2ccc(N)cc2", // 23 - LR=3.69
        "Nc1ccc(cc1)-c2ccccc2", // 24 - LR=3.61
        "Nc1ccc(C=C)cc1", // 25 - LR=4.3
        "Nc1cccc(c1)-c2ccccc2", // 26 - LR=1.58
        "Cc1ccc(N)c(C)c1", // 27 - LR=2.39
        "Cc1ccccc1N", // 28 - LR=1.24
        "Nc1ccc(Cc2ccc(N)cc2)cc1", // 29 - LR=4.35
        "CN(C)c1ccc(Cc2ccccc2)cc1", // 30 - LR=inf
        "Cc1ccc(N)cc1", // 31 - LR=1.86
        "Cc1ccc(NO)cc1", // 32 - LR=2.37
        "Cc1cccc(N)c1", // 33 - LR=1.56
        "CNc1ccc(C=C)cc1", // 34 - LR=inf
        "Nc1ccc2ccccc2c1", // 35 - LR=1.3
        "CNc1ccc(N)cc1", // 36 - LR=1.19
        "Nc1ccccc1O", // 37 - LR=1.24
        "COc1ccccc1N", // 38 - LR=1.38
        "Oc1cccc2ccccc12", // 39 - LR=4.04
        "Nc1ccc(O)c(N)c1", // 40 - LR=1.21
        "CC(C)C(C)(O)CO", // 41 - LR=10.94
        "Nc1cccc(c1)S(O)(=O)=O", // 42 - LR=3.91
        "Cc1cccc2ccccc12", // 43 - LR=15.62
        "NNCO", // 44 - LR=inf
        "CN(N)CO", // 45 - LR=inf
        "CC(O)CNN", // 46 - LR=10.28
        "NNCCO", // 47 - LR=4.53
        "NNc1ccccc1", // 48 - LR=1.67
        "NNCC=C", // 49 - LR=3.95
        "CCNN", // 50 - LR=4.28
        "CCCNN", // 51 - LR=4.02
        "CC(=O)NN", // 52 - LR=1.99
        "CCCN(N)CCC", // 53 - LR=8.7
        "CCCN(C)N", // 54 - LR=4.55
        "NN", // 55 - LR=3.12
        "ClCCCl", // 56 - LR=2.77
        "CCl", // 57 - LR=1.26
        "CCBr", // 58 - LR=3.56
        "CBr", // 59 - LR=8.08
        "c1ccc2cc3c(ccc4ccccc34)cc2c1", // 60 - LR=5.93
        "c1ccc-2c(c1)-c3cccc4cccc-2c34", // 61 - LR=inf
        "CN=O", // 62 - LR=1.58
        "N=O", // 63 - LR=2.34
        "NO", // 64 - LR=1.62
        "Oc1cccc(c1)-c2cccc(O)c2", // 65 - LR=inf
        "OS(O)(=O)=O", // 66 - LR=inf
        "COS(O)=O", // 67 - LR=inf
        "COS(=O)=O", // 68 - LR=7.27
        "ClC1CCCC(Cl)C1Cl", // 69 - LR=5.53
        "CC(Cl)CCCCCl", // 70 - LR=2.13
        "c1ccc(cc1)N=Nc2ccccc2", // 71 - LR=1.35
        "CCNCCCl", // 72 - LR=3.16
        "ClCCNCCCl", // 73 - LR=inf
        "Cc1ccncn1", // 74 - LR=2.77
        "c1cncnc1", // 75 - LR=1.33
        "CC(=C)C(O)=O", // 76 - LR=1.48
        "CC=C(C)CO", // 77 - LR=1.76
        "CC(C)=NO", // 78 - LR=inf
        "CC(C)=N", // 79 - LR=1.6
        "Cn1cncn1", // 80 - LR=inf
        "c1ncnn1", // 81 - LR=1.6
        "COc1ccc(CC=C)cc1", // 82 - LR=5.53
        "Nc1ncc2ncn(CCCCO)c2n1", // 83 - LR=inf
        "OCC1OC(CC1O)n2cnc3cncnc23", // 84 - LR=inf
        "CC1CCC=C(C)C1", // 85 - LR=6.25
        "C1C=CCC=C1", // 86 - LR=3.43
        "O=C(OCc1ccccc1)c2ccccc2", // 87 - LR=inf
        "CCOCc1ccccc1C", // 88 - LR=4.85
        "C(=Cc1ccccc1)c2ccccc2", // 89 - LR=1.58
        "[O-][N+](=O)c1ccco1", // 90 - LR=inf
        "CCNCC(C)=O", // 91 - LR=inf
        "N=[N+]", // 92 - LR=5.53
        "Cc1ccc(cc1)S(O)(=O)=O", // 93 - LR=3.95
        "O=C1c2ccccc2C(=O)c3ccccc13", // 94 - LR=3.16
        "Cc1cccnc1", // 95 - LR=2.37
        "CCCCC(O)CCCCC(O)CCC", // 96 - LR=2.77
        "Clc1cccc(Cl)c1Cl", // 97 - LR=2.37
        "COP=O", // 98 - LR=1.98
        "CC(CN)c1ccccc1", // 99 - LR=1.58
        "OCC#C", // 100 - LR=1.84
        "NNCc1ccccc1", // 101 - LR=2.37
        "C1CCc2ccccc2C1", // 102 - LR=2.06
        "c1ccsc1", // 103 - LR=1.19
        "Nc1ccccn1", // 104 - LR=1.28
        "C1CO1", // 105 - LR=1.05
        "CC(O)CCCC=O", // 106 - LR=1.76
        "C[S]=O", // 107 - LR=1.66
        "c1cscn1", // 108 - LR=2.53
        "CC1COCO1", // 109 - LR=inf
        "Nc1ccc([S]c2ccccc2)cc1", // 110 - LR=inf
        "Cc1ccc(cc1)C(N)=O", // 111 - LR=inf
        "CN(C)P(N(C)C)N(C)C", // 112 - LR=inf
        "[N+]c1cncn1", // 113 - LR=inf
        "O=C1CCO1", // 114 - LR=inf
        "OCCNCC=C", // 115 - LR=inf
        "CCNCCCC(C)C", // 116 - LR=7.03
        "c1ccoc1", // 117 - LR=3.24
        "CCOC(N)=O", // 118 - LR=2.19
        "C=CCCCCC=O", // 119 - LR=1.41
        "C1CN1", // 120 - LR=inf
        "c1cc2ccccc2s1", // 121 - LR=inf
        "Cc1ncc[nH]1", // 122 - LR=inf
        "[O-][N+](=O)c1ccc(o1)-c2cscn2", // 123 - LR=inf
        "C#C", // 124 - LR=2.39
        "CCF", // 125 - LR=1.46
        "CN=[N+]", // 126 - LR=inf
        "CCCN=CN" // 127 - LR=inf           
    };
    
    
    
    public SACarcinogenicityAntares() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_CARC_ANTARES, "Rules for carcinogenicity classification based on the ANTARES dataset (IRFMN)");
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