package insilico.core.descriptor.blocks.weights.other;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.blocks.weights.iWeight;
import insilico.core.localization.StringSelector;
import insilico.core.molecule.acf.GhoseCrippenACF;
import org.openscience.cdk.interfaces.IAtomContainer;

public class WeightsHydrophobicityGC implements iWeight {

    private static final String SYMBOL = "Hy";
    private static final String NAME = StringSelector.getString("descriptors_hydrophobicty_name");

    private final static double[] HY = {
            0, 	// 0  U-000
            -1.5603,	// 1  C-001
            -1.012,	// 2  C-002
            -0.6681,	// 3  C-003
            -0.3698,	// 4  C-004
            -1.788,	// 5  C-005
            -1.2486,	// 6  C-006
            -1.0305,	// 7  C-007
            -0.6805,	// 8  C-008
            -0.3858,	// 9  C-009
            0.7555,	// 10  C-010
            -0.2849,	// 11  C-011
            0.02,	// 12  C-012
            0.7894,	// 13  C-013
            1.6422,	// 14  C-014
            -0.7866,	// 15  C-015
            -0.3962,	// 16  C-016
            0.0383,	// 17  C-017
            -0.8051,	// 18  C-018
            -0.2129,	// 19  C-019
            0.2432,	// 20  C-020
            0.4697,	// 21  C-021
            0.2952,	// 22  C-022
            0,	// 23  C-023
            -0.3251,	// 24  C-024
            0.1492,	// 25  C-025
            0.1539,	// 26  C-026
            0.0005,	// 27  C-027
            0.2361,	// 28  C-028
            0.3514,	// 29  C-029
            0.1814,	// 30  C-030
            0.0901,	// 31  C-031
            0.5142,	// 32  C-032
            -0.3723,	// 33  C-033
            0.2813,	// 34  C-034
            0.1191,	// 35  C-035
            -0.132,	// 36  C-036
            -0.0244,	// 37  C-037
            -0.2405,	// 38  C-038
            -0.0909,	// 39  C-039
            -0.1002,	// 40  C-040
            0.4182,	// 41  C-041
            -0.2147,	// 42  C-042
            -0.0009,	// 43  C-043
            0.1388,	// 44  C-044
            0,	// 45  U-045
            0.7341,	// 46  H-046
            0.6301,	// 47  H-047
            0.518,	// 48  H-048
            -0.0371,	// 49  H-049
            -0.1036,	// 50  H-050
            0.5234,	// 51  H-051
            0.6666,	// 52  H-052
            0.5372,	// 53  H-053
            0.6338,	// 54  H-054
            0.362,	// 55  H-055
            -0.3567,	// 56  O-056
            -0.0127,	// 57  O-057
            -0.0233,	// 58  O-058
            -0.1541,	// 59  O-059
            0.0324,	// 60  O-060
            1.052,	// 61  O-061
            -0.7941,	// 62  O-062
            0.4165,	// 63  O-063
            0.6601,	// 64  Se-064
            0,	// 65  Se-065
            -0.5427,	// 66  N-066
            -0.3168,	// 67  N-067
            0.0132,	// 68  N-068
            -0.3883,	// 69  N-069
            -0.0389,	// 70  N-070
            0.1087,	// 71  N-071
            -0.5113,	// 72  N-072
            0.1259,	// 73  N-073
            0.1349,	// 74  N-074
            -0.1624,	// 75  N-075
            -2.0585,	// 76  N-076
            -1.915,	// 77  N-077
            0.4208,	// 78  N-078
            -1.4439,	// 79  N-079
            0,	// 80  U-080
            0.4797,	// 81  F-081
            0.2358,	// 82  F-082
            0.1029,	// 83  F-083
            0.3566,	// 84  F-084
            0.1988,	// 85  F-085
            0.7443,	// 86  Cl-086
            0.5337,	// 87  Cl-087
            0.2996,	// 88  Cl-088
            0.8155,	// 89  Cl-089
            0.4856,	// 90  Cl-090
            0.8888,	// 91  Br-091
            0.7452,	// 92  Br-092
            0.5034,	// 93  Br-093
            0.8995,	// 94  Br-094
            0.5946,	// 95  Br-095
            1.4201,	// 96  I-096
            1.1472,	// 97  I-097
            0,	// 98  I-098
            0.7293,	// 99  I-099
            0.7173,	// 100  I-100
            0,	// 101  F-101
            -2.6737,	// 102  Cl-102
            -2.4178,	// 103  Br-103
            -3.1121,	// 104  I-104
            0,	// 105  U-105
            0.6146,	// 106  S-106
            0.5906,	// 107  S-107
            0.8758,	// 108  S-108
            -0.4979,	// 109  S-109
            -0.3786,	// 110  S-110
            1.5188,	// 111  Si-111
            1.0255,	// 112  B-112
            0,	// 113  U-113
            0,	// 114  U-114
            0,	// 115  P-115
            -0.9359,	// 116  P-116
            -0.1726,	// 117  P-117
            -0.7966,	// 118  P-118
            0.6705,	// 119  P-119
            -0.4801,	// 120  P-120
    };

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }


    public double getWeightForFragmentId(int Id) {
        if ((Id<0) || (Id>HY.length-1))
            return Descriptor.MISSING_VALUE;
        return HY[Id];
    }

    public double[] getWeightsForFragmentId(int[] ACF) {
        double[] w = new double[ACF.length];
        for (int i=0; i<ACF.length; i++)
            w[i] = getWeightForFragmentId(ACF[i]);

        return w;
    }

    public double[] getWeights(IAtomContainer Mol, boolean HasExplicitHydrogen) {
        GhoseCrippenACF gc = new GhoseCrippenACF(Mol, HasExplicitHydrogen);
        int[] acf = gc.GetACF();
        return getWeightsForFragmentId(acf);
    }

}
