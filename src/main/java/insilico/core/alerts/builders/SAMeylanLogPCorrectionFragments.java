package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smarts.SmartsPattern;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class SAMeylanLogPCorrectionFragments extends AlertBlockFromSMARTS implements iAlertBlock {

    private Pattern[] SA;
    private double GlobalCoefficient;

    private final static String[] SMARTSFragments = {
            "*=C(C#N)C(=O)[!O]",  // 0
            "[OH]CC(=O)C[OH]",  // 1
            "[$([N;D1]),$([N;D2](-[*;D1])-*),$([N;D3](-[*;D1])(-[*;D1])-*)]CC(=O)[OH]",  // 2
            "[C;!R][$([C;D2]),$([C;D3](=N)(C)[C,c])]=NOC",  // 3
            "[OH]CC(=O)C[O;D2]",  // 4
            "n1oncc1",  // 5
            "[N;R1]=[C;R1]([R,R2])[R,R2]",  // 6
//            "[N;R]=[C;R]([R,R2])[R,R2]",  // 6 OLD CDK
//        "[$([N;D1]),$([N;D2]-*),$([N;D3](-*)-*)]=C([R])[R]",  // 6
            "CC(=O)NC(C(=O)[OH])C[S;D2]",  // 7
//        "[$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)]", // 8 wrong?
            "[$([N;D2](-*)-*)][$([N;D2](-*)-*)]", // 8
            "*=C(C#N)C(=O)[O;D2]", // 9
            "[OH][C;H1,H2]C([O;D2])[C;H1,H2][OH]", // 10
            "*[S;R](=O)[N;R]=[C;R][N;R]", // 11
//            "[*;R][C,c;R](=O)[O,o;R][*;R]", // 12 COMPLIANCE WITH OLDER CDK
            "[*;R1][C,c;R1](=O)[O,o;R1][*;R1]", // 12
            "*C(=O)NC(=O)NC(=O)*", // 13
            "*C(=O)C=CC(=O)[A]", // 14
            "C(-*)(-*)=NOC(=O)*", // 15
            "c1ncncn1", // 16
            "*-N[C;!R]([$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*);!R])=N-*", // 17
            "COC(C[OH])OC", // 18
            "[C;D2]C(=O)[C;D2][OH]", // 19
            "[N;D3](-*)(-*)C([S;D])C[O,N,$(C=O)]", // 20
//        "[*;!a]-N=NN-*", // 21 wrong
            "N=NN", // 21
//        "[N;D3](-*)(-*)[N;D3](-*)(-*)", // 22 wrong?
            "[N;D3](-*)(-*)[$([N;D2](-*)-*),$([N;D3](-*)(-*))]", // 22
            "*-NC(C[OH])C[OH]", // 23
            "*-[C;!R](=O)N[C;!R][C;!R](=O)N[C;!R]", // 24
            "*-C(=O)S[a]", // 25
            "[$(N(C)-*)]C(=O)N[C;D3](=O)", // 26
            "[OH][CH]C([OH])[CH][OH]", // 27
            "[O,S;D2]C(F)F", // 28
            "*-N[C;D2]O-*", // 29
            "[C,c]-O-C-O-[C,c]", // 30
            "[$([C;D2](C)-*)]-C(=O)-[C;D2]-O-*", // 31
            "*-C(=O)-O-C-C(=O)-N-*", // 32
            "C-C(=O)N-C-C(=O)[OH]", // 33
            "N-C(=O)-C-[$([N;D1]),$([N;D2]C),$([N;D3](C)C)]", // 34
            "*-C(=O)-[NH]-C-C(=O)-O-[a]", // 35
            "[a][$([C;D2]),$([C;D3]C),$([C;D4](C)C)]C(=O)[OH]", // 36
            "N=C(C#N)C#N", // 37
            "[OH]CC(=O)[OH]", // 38
            "*-[CH]=N-O-C(=O)-*", // 39
//            "[c;!$(c([*;R])([*;R])([*;R]))]1[c;!$(c([*;R])([*;R])([*;R]))][c;!$(c([*;R])([*;R])([*;R]))]n[c;!$(c([*;R])([*;R])([*;R]))][c;!$(c([*;R])([*;R])([*;R]))]1", // 40 OLD CDK
            "[c;!$(c([*;R1])([*;R1])([*;R1]))]1[c;!$(c([*;R1])([*;R1])([*;R1]))][c;!$(c([*;R1])([*;R1])([*;R1]))]n[c;!$(c([*;R1])([*;R1])([*;R1]))][c;!$(c([*;R1])([*;R1])([*;R1]))]1", // 40
//        "[!a](@[!a])(@[!a])@[!a](@[!a])@[!a]", // 41 wrong?
            "[$([C;!a](@[C;!a])(@[C;!a])@[C;!a])]@[$([C;!a](@[C;!a])(@[C;!a])@[C;!a])]", // 41
            "[C;!$(C(=O)[OH])][OH]", // 42
//        "[$([N;D1;!R]),$([N;D2!R](-*)-*),$([N;D3!R](-*)(-*)-*)][$([$(C);!$(C=O)]CC(=O)[OH]),$([$(C);!$(C=O)]CCC(=O)[OH]),$([$(C);!$(C=O)]CCCC(=O)[OH]),$([$(C);!$(C=O)]CCCCC(=O)[OH]),$([$(C);!$(C=O)]CCCCCC(=O)[OH]),$([$(C);!$(C=O)]CCCCCCC(=O)[OH]),$([$(C);!$(C=O)]CCCCCCCC(=O)[OH]),$([$(C);!$(C=O)]CCCCCCCCC(=O)[OH]),$([$(C);!$(C=O)]CCCCCCCCCC(=O)[OH])]", // 43 wrong?
            "[!$(NC=O);!$(NC=N);$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][$(CCC(=O)[OH]),$(CCCC(=O)[OH]),$(CCCCC(=O)[OH]),$(CCCCCC(=O)[OH]),$(CCCCCCC(=O)[OH]),$(CCCCCCCC(=O)[OH]),$(CCCCCCCCC(=O)[OH]),$(CCCCCCCCCC(=O)[OH])]", // 43
            "[$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][$(c1ncncn1),$(c1nnncc1),$(c1cnnnc1),$(c1ccnnn1),$(c1nncnc1),$(c1cnncn1),$(c1nccnn1),$(c1nccnc1),$(c1cnccn1),$(c1ncncc1),$(c1cncnc1),$(c1ccncn1)]", // 44
            "C(=O)([O;D2])N([A])[A]", // 45
            "c1cnn[nH]1", // 46
            "[C,c]C(=O)N(C(-*)(-*)-*)-N", // 47
            "*-NC(=S)NC(=O)-*", // 48
            "C=C(N-*)N-*", // 49
            "C(=O)NC(=[NH])C", // 50
            "NC(=N)NC(=O)N", // 51
            "[C;D2](C(=O)-*)C(=O)N", // 52
            "NC=N[$(c1ncsc1),$(c1cncs1),$(c1sccn1)]", // 53
            "[N;R][C;R]([N;R])=N[a]", // 54
            "C(-*)(-*)(c1ccccc1)c1ccccc1", // 55 NOT WORKING IN CDK OLD
//            "[$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][R1;a][$([R1;a]);!$([R1;a][OH])][$([R1;a][OH]),$([R1;a][R1;a][OH])]", // 56 OLD CDK
            "[$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][R;a][$([R;a]);!$([R;a][OH])][$([R;a][OH]),$([R;a][R;a][OH])]", // 56
            "[R;a](C(=O)[OH])[R;a](C(=O)[OH])", // 57
//            "[R;a]([N+](=O)[O-])[$([R;a][$([OH]),$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*),$(N=N)]),$([R;a][R;a][$([OH]),$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*),$(N=N)]),$([R;a][R;a][R;a][$([OH]),$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*),$(N=N)])]",  // 58 OLD CDK
            "[R1;a]([N+](=O)[O-])[$([R1;a][$([OH]),$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*),$(N=N)]),$([R1;a][R1;a][$([OH]),$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*),$(N=N)]),$([R1;a][R1;a][R1;a][$([OH]),$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*),$(N=N)])]",  // 58
            "[R;a]([OH])[R;a]C(=O)[O;D2]", // 59
            "*-N-C(=O)-C([Cl,Br,F,I])[Cl,Br,F,I]", // 60
//            "[$([C,c](=O)1[N,n][C,c](=O)[*;R][*;R]1);!$(C(=O)1NC(=O)C=C1)]", // 61 CDK OLD
            "[$([C,c](=O)1[N,n][C,c](=O)[*;R1][*;R1]1);!$(C(=O)1NC(=O)C=C1)]", // 61
            "C(=O)[C,$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][N;D3]([a])[a]", // 62
            "[n;R](=O)[$([n;R]),$([*;R][n;R]),$([*;R][*;R][n;R])]", // 63
            "[a]-N(C(=O)-*)C(=O)-*", // 64
            "[n;R][c;R](OC)[a;!n]", // 65
            "[n;R][c;R](OC)[n;R]", // 66
            "[N;D1]=C(-*)[a;R]", // 67
            "[O;D2]C([$(N-*)])C[OH]", // 68
            "CSC[$(S(=O)(C)-*)]", // 69
//        "[a;R](N(=O)-*)[a;R][NH]C(=O)C", // 70
            "SSSSSS", // 70
//            "[$(S(=O)(=O)(N)[a;R][a;R]S(=O)(=O)(N)),$(S(=O)(=O)(N)[a;R][a;R][a;R]S(=O)(=O)(N)),$(S(=O)(=O)(N)[a;R][a;R][a;R][a;R]S(=O)(=O)(N))]", // 71 OLD CDK
            "[$(S(=O)(=O)(N)[a;R1][a;R1]S(=O)(=O)(N)),$(S(=O)(=O)(N)[a;R1][a;R1][a;R1]S(=O)(=O)(N)),$(S(=O)(=O)(N)[a;R1][a;R1][a;R1][a;R1]S(=O)(=O)(N))]", // 71
//            "[$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][$([a;R][a;R][a;R]C(=O)[O;D2]),$([a;R][a;R][a;R][a;R]C(=O)[O;D2])]", // 72 OLD CDK
            "[$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][$([a;R1][a;R][a;R1]C(=O)[O;D2]),$([a;R1][a;R1][a;R1][a;R1]C(=O)[O;D2])]", // 72
            "S(=O)(=O)(N)C(C)N", // 73
            "[OH][a;R][a;R]C(=O)[OH]", // 74
            "c1ccccc1CCN", // 75
            "[$(C(=O)([OH])[A]C(=O)[OH]),$(C(=O)([OH])[A][A]C(=O)[OH]),$(C(=O)([OH])[A][A][A]C(=O)[OH]),$(C(=O)([OH])[A][A][A][A]C(=O)[OH]),$(C(=O)([OH])[A][A][A][A][A]C(=O)[OH]),$(C(=O)([OH])[A][A][A][A][A][A]C(=O)[OH]),$(C(=O)([OH])[A][A][A][A][A][A][A]C(=O)[OH]),$(C(=O)([OH])[A][A][A][A][A][A][A][A]C(=O)[OH])]", // 76
            "[$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*),O]C(=O)NN=O", // 77
            "S(=O)[$(C([Cl,Br,F,I])[Cl,Br,F,I])]", // 78
//            "[a;R](-*)[a;R]c(n)n", // 79 OLD CDK
            "[a;R1](-*)[a;R1]c(n)n", // 79
            "C(=O)N(O)C(=O)", // 80
            "C-S-C(=N-*)-[$(S(=O))]", // 81
            "[$(P=O)]-S-C-[$(S=O)]", // 82
            "[$(N(-*)-*)]-C-C(=O)-C-[$(N(-*)-*)]", // 83
            "[O;D1][N;D2]=[C;D3]", // 84
//        "[$([*;R]),$([*;R]=O)][$([*;R]),$([*;R]=O)][$([*;R]),$([*;R]=O)][C,c;R](=O)[O,o;R][*;R]", // 85 wrong
            "[$([C,c;R](=O)([O,o;R][*;R])[C,c;R](=O)),$([C,c;R](=O)([O,o;R][*;R])[*;R][C,c;R](=O)),$([C,c;R](=O)([O,o;R][*;R])[*;R][*;R][C,c;R](=O))]", // 85
            "[O;D2]C([OH])CN-*", // 86
            "C=C([N;D2])C(=O)-*", // 87
//        "[O;D2]CCOC[O;D2]", // 88
//        "CCOCCOC", // 89
            "OCC([OH])[C;R][O;R][C;R](=O)[C;R]", // 88
            "[!$(NC=O);!$(NC=N);$([N;D1]),$([N;D2](-*)-*),$([N;D3](-*)(-*)-*)][$(C~C~C(=O)[OH]),$(C~C~C~C(=O)[OH]),$(C~C~C~C~C(=O)[OH]),$(C~C~C~C~C~C(=O)[OH]),$(C~C~C~C~C~C~C(=O)[OH]),$(C~C~C~C~C~C~C~C(=O)[OH]),$(C~C~C~C~C~C~C~C~C(=O)[OH]),$(C~C~C~C~C~C~C~C~C~C(=O)[OH])]", // 89
            "[c;R]([$([C,Cl,Br,F,I]);!R])[c;R]([N;!R][C;D3]=O)[c;R]([$([C,Cl,Br,F,I]);!R])", // 90
            "[C;!R](=O)[C;!R]=N[O;D2]", // 91
            "[c;R]([OH])[c;R]([C;!R]=O)", // 92

    };


    private final static double[] SMARTSCoeff = {
            2.2489,
            2.0476,
            -2.0238,
            -1.9,
            1.7838,
            1.6,
            -1.5506,
            1.5505,
            1.133,
            1.1203,
            1.0649,
            -1.2901,
            -1.0577,
            1.0254,
            1.0235,
            -1,
            0.8856,
            -0.9698,
            0.95,
            0.9178,
            0.837,
            0.7931,
            0.7306,
            0.7037,
            0.7,
            0.7,
            0.6074,
            0.5944,
            0.55,
            0.5494,
            0.5036,
            0.5,
            0.5,
            0.4193,
            0.4,
            0.3895,
            -0.3662,
            0.3452,
            0.3114,
            -0.15,
            -0.1621,
            -0.3421,
            0.4064,
            -3.2 + (1.7),
            0.8566,
            0.1984,
            0.7525,
            -0.9,
            0.675,
            0.6,
            -1.5,
            0.8615,
            0.9755,
            1.8,
            0.5609,
            -0.5158,
            -0.351,
            -0.3425,
            0.5777,
            1.2556,
            0.6365,
            0.4812,
            -0.7203,
            1.25,
            -1.3338,
            0.4549,
            0.8955,
            -2.1,
            1.4209,
            1.42,
            -0.5634,
            1.0185,
            0.3953,
            -0.5241,
            1.193,
            -0.2226,
            -0.5865,
            1.0396,
            1.479,
            -0.9063,
            -0.7807,
            2.4,
            2.3,
            2.05,
            -1.05,
            (0.75 + 1.0577),
            1.4075,
            0.75,
//        1.4,
//        1.3548
            1.3,
            -1.7,
            -1.1239,
            2.1,
            0.777,

    };



    public SAMeylanLogPCorrectionFragments() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_LOGP_MEYLAN_CORRECTION, "Meylan correction fragments for LoP calculation (Kowwin)");
    }


    @Override
    protected void BuildSAList() throws InitFailureException {

        for (int i=0; i<SMARTSFragments.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName("MEYC" + (i+1));
            curSA.setDescription("Meylan correction fragment for LogP calculation no. " + (i+1) + " defined by SMARTS: " +  SMARTSFragments[i]);
            Alerts.add(curSA);
        }


    }


    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new Pattern[SMARTSFragments.length];

            for (int i=0; i<SMARTSFragments.length; i++)

                SA[i] = SmartsPattern.create(SMARTSFragments[i]).setPrepare(false);

        } catch (Exception e) {
            throw new InitFailureException("Unable to initialize SMARTS");
        }
    }


    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {

        AlertList Res = new AlertList();

        boolean alpha_amino_acids = false;

        try {

            for (int i=0; i<SA.length; i++) {
                if (SA[i].matches(CurMol.GetStructure())) {

                    int nMatches = SA[i].matchAll(CurMol.GetStructure()).countUnique();

//                    if(nMatches>0)
//                        System.out.println(i + "\t" + Alerts.get(i).getName() + "\t" +  SMARTSFragments[i] + "\t" + SMARTSCoeff[i]);

                    //// Special rules ////

                    if (i == 2) {
                        // alpha amino-acids
                        GlobalCoefficient += SMARTSCoeff[i] * 1; // not related to number of matches
                        Res.add((Alert)Alerts.get(i).clone());
                        alpha_amino_acids = true;
                        continue;
                    }

                    if (i == 42) {
                        // multi-alcool
                        if (nMatches > 1) {
                            GlobalCoefficient += SMARTSCoeff[i] * 1; // not related to number of matches
                            Res.add((Alert)Alerts.get(i).clone());
                        }
                        continue;
                    }

                    if (i == 43) {
                        // non-alpha amino-acids
                        // only if no alpha amino-acids have been found
                        if (alpha_amino_acids)
                            continue;
                        GlobalCoefficient += SMARTSCoeff[i] * 1; // not related to number of matches
                        Res.add((Alert)Alerts.get(i).clone());
                        continue;
                    }

                    if (i == 55) {
                        // not working in old CDK - skip to be compliant
                        continue;
                    }

                    if (i == 58) {
                        // not working in old CDK - skip to be compliant
                        continue;
                    }

                    if (i == 89) {
                        // non-alpha amino-acids - oleifinic
                        // only if no alpha amino-acids have been found
                        if (alpha_amino_acids)
                            continue;
                        GlobalCoefficient += SMARTSCoeff[i] * 1; // not related to number of matches
                        Res.add((Alert)Alerts.get(i).clone());
                        continue;
                    }

                    if (i == 78) {
                        // S(=O)C-polyhalo
                        if (nMatches > 1) {
                            GlobalCoefficient += SMARTSCoeff[i] * 1; // not related to number of matches
                            Res.add((Alert)Alerts.get(i).clone());
                        }
                        continue;
                    }

                    if (i == 76) {
                        // multi aliphatic alcool
                        if (nMatches > 1) {
                            GlobalCoefficient += SMARTSCoeff[i] * 1; // not related to number of matches
                            Res.add((Alert)Alerts.get(i).clone());
                        }
                        continue;
                    }

                    Res.add((Alert)Alerts.get(i).clone());
                    GlobalCoefficient += SMARTSCoeff[i] * nMatches;
                }
            }

        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            throw new GenericFailureException("Error while calculating SMARTS matching");
        }

        return Res;
    }


    public double GetCoefficient() {
        return GlobalCoefficient;
    }

}
