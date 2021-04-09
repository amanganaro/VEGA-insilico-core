package insilico.core.descriptor.blocks.weights.other;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.blocks.weights.iWeight;
import insilico.core.localization.StringSelector;
import insilico.core.molecule.acf.GhoseCrippenACF;
import org.openscience.cdk.interfaces.IAtomContainer;

public class WeightsMolarRefractivityGC implements iWeight {

    private static final String SYMBOL = "MR";
    private static final String NAME = StringSelector.getString("descriptors_molarrefr_name");

    private final static double[] MR = {
            0, 	// 0  U-000
            2.968,	// 1  C-001
            2.9116,	// 2  C-002
            2.8028,	// 3  C-003
            2.6205,	// 4  C-004
            3.015,	// 5  C-005
            2.9244,	// 6  C-006
            2.6329,	// 7  C-007
            2.504,	// 8  C-008
            2.377,	// 9  C-009
            2.559,	// 10  C-010
            2.303,	// 11  C-011
            2.3006,	// 12  C-012
            2.9627,	// 13  C-013
            2.3038,	// 14  C-014
            3.2001,	// 15  C-015
            4.2654,	// 16  C-016
            3.9392,	// 17  C-017
            3.6005,	// 18  C-018
            4.487,	// 19  C-019
            3.2001,	// 20  C-020
            3.4825,	// 21  C-021
            4.2817,	// 22  C-022
            3.9556,	// 23  C-023
            3.4491,	// 24  C-024
            3.8821,	// 25  C-025
            3.7593,	// 26  C-026
            2.5009,	// 27  C-027
            2.5,	// 28  C-028
            3.0627,	// 29  C-029
            2.5009,	// 30  C-030
            0,	// 31  C-031
            2.6632,	// 32  C-032
            3.4671,	// 33  C-033
            3.6842,	// 34  C-034
            2.9372,	// 35  C-035
            4.019,	// 36  C-036
            4.777,	// 37  C-037
            3.9031,	// 38  C-038
            3.9964,	// 39  C-039
            3.4986,	// 40  C-040
            3.4997,	// 41  C-041
            2.7784,	// 42  C-042
            2.6267,	// 43  C-043
            2.5,	// 44  C-044
            0,	// 45  U-045
            0.8447,	// 46  H-046
            0.8939,	// 47  H-047
            0.8005,	// 48  H-048
            0.832,	// 49  H-049
            0.8,	// 50  H-050
            0.8188,	// 51  H-051
            0.9215,	// 52  H-052
            0.9769,	// 53  H-053
            0.7701,	// 54  H-054
            0,	// 55  H-055
            1.7646,	// 56  O-056
            1.4778,	// 57  O-057
            1.4429,	// 58  O-058
            1.6191,	// 59  O-059
            1.3502,	// 60  O-060
            1.945,	// 61  O-061
            0,	// 62  O-062
            0,	// 63  O-063
            0,	// 64  Se-064
            0,	// 65  Se-065
            2.6221,	// 66  N-066
            2.5,	// 67  N-067
            2.898,	// 68  N-068
            3.6841,	// 69  N-069
            4.2808,	// 70  N-070
            3.6189,	// 71  N-071
            2.5,	// 72  N-072
            2.7956,	// 73  N-073
            2.7,	// 74  N-074
            4.2063,	// 75  N-075
            4.0184,	// 76  N-076
            3.0009,	// 77  N-077
            4.7142,	// 78  N-078
            0,	// 79  N-079
            0,	// 80  U-080
            0.8725,	// 81  F-081
            1.1837,	// 82  F-082
            1.1573,	// 83  F-083
            0.8001,	// 84  F-084
            1.5013,	// 85  F-085
            5.6156,	// 86  Cl-086
            6.1022,	// 87  Cl-087
            5.9921,	// 88  Cl-088
            5.3885,	// 89  Cl-089
            6.1363,	// 90  Cl-090
            8.5991,	// 91  Br-091
            8.9188,	// 92  Br-092
            8.8006,	// 93  Br-093
            8.2065,	// 94  Br-094
            8.7352,	// 95  Br-095
            13.9462,	// 96  I-096
            14.0792,	// 97  I-097
            14.073,	// 98  I-098
            12.9918,	// 99  I-099
            13.3408,	// 100  I-100
            0,	// 101  F-101
            0,	// 102  Cl-102
            0,	// 103  Br-103
            0,	// 104  I-104
            0,	// 105  U-105
            7.8916,	// 106  S-106
            7.7935,	// 107  S-107
            9.4338,	// 108  S-108
            7.7223,	// 109  S-109
            5.7558,	// 110  S-110
            0,	// 111  Si-111
            0,	// 112  B-112
            0,	// 113  U-113
            0,	// 114  U-114
            0,	// 115  P-115
            5.5306,	// 116  P-116
            5.5152,	// 117  P-117
            6.836,	// 118  P-118
            10.0101,	// 119  P-119
            5.2806,	// 120  P-120
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
        if ((Id<0) || (Id>MR.length-1))
            return Descriptor.MISSING_VALUE;
        return MR[Id];
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
