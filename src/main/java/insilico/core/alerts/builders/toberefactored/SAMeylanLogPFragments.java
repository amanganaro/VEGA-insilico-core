package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;

public class SAMeylanLogPFragments extends AlertBlock implements iAlertBlock {

    private int nSK;
    private double[][] ConnMatrix;
    private boolean[] AtomAromatic;

    private int[] FragmentsCounter;
    private double GlobalCoefficient;

    private static final String[] FragmentNames = {
            "-CH3  [aliphatic carbon]            " , // 0
            "-CH2- [aliphatic carbon]            " , // 1
            "-CH< [aliphatic carbon]            " , // 2
            ">C< [aliphatic carbon -0 No H, not tert]  " , // 3
            "=CH2  [olefinic carbon]            " , // 4
            "'=CH- or =C<  [olefinc carbon]            " , // 5
            "#C    [acetylenic carbon]        " , // 6
            "-OH     [hydroxy, aliphatic attach]      " , // 7
            "-O-     [oxygen, aliphatic attach]      " , // 8
            "-NH2    [aliphatic attach]          " , // 9
            "-NH-    [aliphatic attach]          " , // 10
            "-N<     [aliphatic attach]        " , // 11
            "-CL     [chlorine, aliphatic attach]      " , // 12
            "-CL     [chlorine, olefinic attach]      " , // 13
            "-F      [fluorine, aliphatic attach]      " , // 14
            "-F      [fluorine, olefinic attach]      " , // 15
            "-Br     [bromine, aliphatic attach]      " , // 16
            "-Br     [bromine, olefinic attach]      " , // 17
            "-I      [iodine, aliphatic attach]      " , // 18
            "Aromatic carbon                " , // 19
            "Aromatic nitrogen                " , // 20
            "-CL     [chlorine, aromatic attach]      " , // 21
            "-Br     [bromine, aromatic attach]      " , // 22
            "-OH     [hydroxy, aromatic attach]      " , // 23
            "-N    [aliphatic N, one aromatic attach]    " , // 24
            "-O-   [oxygen, one aromatic attach]      " , // 25
            "-O-   [aliphatic O, two aromatic attach]    " , // 26
            "-CHO    [aldehyde, aliphatic attach]        " , // 27
            "-CHO    [aldehyde, aromatic attach]        " , // 28
            "-C(=O)- [carbonyl, aliphatic attach]        " , // 29
            "-C(=O)- [carbonyl, one aromatic attach]      " , // 30
            "-C#N    [cyano, aliphatic attach]        " , // 31
            "-C#N    [cyano, aromatic attach]        " , // 32
            "-NO2    [nitro, aliphatic attach]        " , // 33
            "-NO2    [nitro, aromatic attach]        " , // 34
            "-COOH   [acid, aliphatic attach]        " , // 35
            "-COOH   [acid, aromatic attach]        " , // 36
            "-N=O    [nitroso]            " , // 37
            "-S- [aliphatic sulfur,one aromatic attach]    " , // 38
            "Aromatic Sulfur              " , // 39
            "-C(=O)O  [ester, aliphatic attach]          " , // 40
            "-C(=O)O  [ester, aromatic attach]          " , // 41
            "-F      [fluorine, aromatic attach]      " , // 42
            "-C(=O)N  [aliphatic attach]            " , // 43
            "-C(=O)N  [aromatic attach]            " , // 44
            "-NC(=S)N-  [thiourea]              " , // 45
            "-SH     [aliphatic attach]        " , // 46
            "-SO2-OH [sulfonic], [coef*(1+0.3*(NUM-1))] " , // 47
            "-S-     [aliphatic attach]        " , // 48
            "S=P     [thio=phosphorus]          " , // 49
            "-O-P    [aliphatic attach]          " , // 50
            "-O-P    [aromatic attach]          " , // 51
            "-S-P [sulfur, phosphorus attach]          " , // 52
            "O=P                " , // 53
            "-N-P   [nitrogen, phosphorus attach]        " , // 54
            "-I      [aromatic attach]        " , // 55
            "-SO2-N   [aromatic attach]          " , // 56
            "-SO2-    [aromatic attach]          " , // 57
            "-NC(=O)N-    [urea]            " , // 58
            "-O-N  [oxygen, nitrogen attach]          " , // 59
            "Aromatic Oxygen              " , // 60
            "-OC(=O)N    [carbamate]            " , // 61
            "-SO2-    [sulfone, aliphatic attach]        " , // 62
            "-S(=O)-  [sulfoxide, aliphatic attach]       " , // 63
            "-N-   [aliphatic N, two aromatic attach]    " , // 64
            "Aromatic n=O  [nitrogen oxide]          " , // 65
            "-SS-    [disulfide]            " , // 66
            "Aromatic Nitrogen [5-member ring]          " , // 67
            "-S-   [aliphatic S, two aromatic attach]    " , // 68
            "-C(=O)-  [two aromatic attach, in ring]      " , // 69
            "-S-N  [sulfur, nitrogen attach]          " , // 70
            "-S-C(=O)-N-  [Thiocarbamate]              " , // 71
            "Olefinic Carbon  [two aromatic attach]       " , // 72
            "-S(=O)-   [sulfoxide, aromatic attach]       " , // 73
            "-N=C=S  [isothiocyanate, aliphatic attach]     " , // 74
            "-N=C=S  [isothiocyanate, aromatic attach]     " , // 75
            "-tert Carbon  [3 or more carbon attach]    " , // 76
            "-SH    [thiol, aromatic attach]        " , // 77
            "Ketone in a ring [olefin, aromatic attach]   " , // 78
            "-N=N-  [Azo]              " , // 79
            "-C(=S)N    [aromatic attach]          " , // 80
            "-OH      [alcohol, olefinic attach]      " , // 81
            "-SO2-N   [aliphatic attach]          " , // 82
            "-OH      [hydroxy, nitrogen attach]      " , // 83
            "-N=C     [aliphatic attach]        " , // 84
            "C#N-S    [cyano, sulfur attach]        " , // 85
            "C#N-N    [cyano, nitrogen attach]        " , // 86
            "C#N-C=N  [cyano, -C=N attach]          " , // 87
            "Carbonyl,  non-cylic, two aromatic attach     " , // 88
            "Aldehyde,  [-N-CHO; aromatic attach]          " , // 89
            "Aldehyde,  [-N-CHO; aliphatic attach]        " , // 90
            "-ONO2   [aliphatic attach]          " , // 91
            "-C(=O)-   [carbonyl, olefinic attach]        " , // 92
            "-SO2-O [sulfonate, aromatic attach]          " , // 93
            "SO2    [two aromatic attach]        " , // 94
            "-N-SO2-N-   [sulfamide]            " , // 95
            "-CO-CO    [aromatic attach]          " , // 96
            "-C(=S)N-   [aliphatic attach]          " , // 97
            "-S-C=   [S to aliphatic, double bonded C]  " , // 98
            "-Si-  [silicon, aliphat attach (not oxy)]     " , // 99
            "-Si-  [silicon, aromatic or oxygen attach]     " , // 100
            ">P-  [phosphine type]            " , // 101
            "-OH  [phosphorus attach]            " , // 102
            "-SO2-O [sulfonate, aliph att]          " , // 103
            ">N< [+5 valence; single bonds;no H attach]   " , // 104
            "Aromatic nitrogen  [fused ring location]     " , // 105
            "Aromatic nitrogen  [+5 valence type; no H]   " , // 106
            "Halogen {-CL,-Br,-F,-I} [Nitrogen attach]     " , // 107
            "-S-  [aliphatic sulfur, 2 nitrogen attach]     " , // 108
            "N#N  {alias: charged =N[+]=N[-] }        " , // 109
            "-Hg-  [mercury]              " , // 110
            "Formaldehyde experimental value - constant     " , // 111
            "#C  [acetylenic carbon-acetylenic attach]     " , // 112
            "-N(=O)=C [nitrone, aromatic attach]          " , // 113  - removed
            "-C#N=O   [cyanooxide, aromatic attach]       " , // 114
            "S=C=S  [carbon disulfide, experimental]       " , // 115
            "-C(=O)-S   [thioester, aliphatic attach]     " , // 116
            "-O-SO2-O-     [sulfate, linear]        " , // 117
            "-CO-CO    [aliphatic attach]          " , // 118
            "Aldehyde,  [-N-CHO; olefinic attach]          " , // 119
            "-C(=O)-SH  [aliphatic attach]            " , // 120
            "N=O  [nitroso; N+5 valence]          " , // 121
            "N-C(=S)-S  [cyclic]              " , // 122
            "-Se-   [aromatic attach]          " , // 123
            "-N(=O)- [N-oxide +4 type; no H attach]    " , // 124
            "-N(=O)=C [nitrone,aliphatic attach;linear]  " , // 125 - removed
            "Aromatic Selenium              " , // 126
            ">N< [+5 valence; single bonds; H attach]    " , // 127
            "Aromatic nitrogen  [+5 valence; H attach]     " , // 128
            "-C#N   [cyano attach]          " , // 129
            "-OC(=O)O-  [carbonate,aliphatic attach]       " , // 130
            "-OC(=O)O-  [carbonate,aromatic attachs]       " , // 131
            "O=C=O  [carbon dioxide, experimental]         " , // 132
            "Halogen [mono- or dioxy-type][one/struct]     " , // 133
            "-C(=O)-S   [thioester, olefinic attach]       " , // 134
            "-N<   [two or three olefinic attach]    " , // 135
            "-O-   [oxygen, two olefinic attach]      " , // 136
            "[Pb]  (Lead)              " , // 137
            "[As]  (Arsenic)              " , // 138
            "[Ge]  (Germanium)              " , // 139
            "Boron                " , // 140
            "-OH   { Metal or miscellaneous attach}    " , // 141
            "Tin [Sn]              " , // 142
            "Tin [Sn]  { oxygen attach }      " , // 143
            "Tin [Sn]  { oxygen and aromatic attach }  " , // 144
            "Tin [Sn]  { halogen or -OH attach }  " , // 145
            "Aluminum [Al]              " , // 146
            "Gold  [Au]=P  { Phosphorus attach }      " , // 147
            "Platinum [Pt] { halogen & nitrogen attach}    " , // 148
            "-O-P  [phosphine phosphorus attach]          " , // 149
            "-C(=O)-  [di-carbonyl attach]            " , // 150
            "SO2(-OH)-O  [sulfonic]              " , // 151
            "-SO2-  [additional sulfone,aliphat attach]   " , // 152
            "N=O  [nitroso; N+5 valence; single bonds]     " , // 153
            "Aromatic n=O [nitrogen oxide,nitrogen att]   " , // 154
            "Tin [Sn]  { divalent; carbon attach }    ", // 155
            "N-NO2", // 156
            "Tin [Sn]  { divalent; aromatic attach }" // 157
    };

    private final static double[] FragmentCoeff = {
            0.5473 , // 0
            0.4911 , // 1
            0.3614 , // 2
            0.9723 , // 3
            0.5184 , // 4
            0.3836 , // 5
            0.1334 , // 6
            -1.4086 , // 7
            -1.2566 , // 8
            -1.4148, // 9
            -1.4962 , // 10
            -1.8323 , // 11
            0.3102 , // 12
            0.4923 , // 13
            -0.0031 , // 14
            0.0545 , // 15
            0.3997 , // 16
            0.3933 , // 17
            0.8146 , // 18
            0.294 , // 19
            -0.7324 , // 20
            0.6445 , // 21
            0.89 , // 22
            -0.4802 , // 23
            -0.917 , // 24
            -0.4664 , // 25
            0.2923 , // 26
            -0.9422 , // 27
            -0.2828 , // 28
            -1.5586 , // 29
            -0.8667 , // 30
            -0.9218 , // 31
            -0.453 , // 32
            -0.8132 , // 33
            -0.1823 , // 34
            -0.6895 , // 35
            -0.1186 , // 36
            -0.1299 , // 37
            0.0535 , // 38
            0.4082 , // 39
            -0.9505 , // 40
            -0.7121 , // 41
            0.2004 , // 42
            -0.5236 , // 43
            0.1599 , // 44
            1.2905 , // 45
            -0.0001 , // 46
            -3.158 , // 47
            -0.4045 , // 48
            -0.6587 , // 49
            -0.0162 , // 50
            0.5345 , // 51
            0.627 , // 52
            -2.4239 , // 53
            -0.4367 , // 54
            1.1672 , // 55
            -0.2079 , // 56
            -1.9775 , // 57
            1.0453 , // 58
            0.2352 , // 59
            -0.0423 , // 60
            0.1283 , // 61
            -2.4292 , // 62
            -2.5459 , // 63
            -0.4657 , // 64
            -2.4729 , // 65
            (0.5497 / 2) , // 66
            -0.5262 , // 67
            0.5335 , // 68
            -0.2063 , // 69
            -0.0199 , // 70
            0.524 , // 71
            -0.4186 , // 72
            -2.1103 , // 73
            0.5236 , // 74
            1.3369 , // 75
            0.2676 , // 76
            0.6925 , // 77
            -0.5497 , // 78
            0.3541 , // 79
            1.096 , // 80
            -0.8855 , // 81
            -0.4351 , // 82
            -0.0427 , // 83
            -0.001 , // 84
            0.354 , // 85
            0.3731 , // 86
            0.0562 , // 87
            -0.6099 , // 88
            0.0563 , // 89
            -0.425 , // 90
            -0.02 , // 91
            -1.27 , // 92
            -0.365 , // 93
            -1.15 , // 94
            0.81 , // 95
            -0.1 , // 96
            -0.19 , // 97
            -0.1 , // 98
            0.3004 , // 99
            0.68 , // 100
            -0.5 , // 101
            0.475 , // 102
            -0.725 , // 103
            -6.6 , // 104
            -0.0001 , // 105
            -6.65 , // 106
            0.0001 , // 107
            1.2 , // 108
            1.28 , // 109
            -0.7 , // 110
            0.121 , // 111
            0.4 , // 112
            -2.2 , // 113
            -0.35 , // 114
            1.711 , // 115
            -1.1 , // 116
            1.35 , // 117
            -1.33 , // 118
            0.65 , // 119
            -0.64 , // 120
            -1 , // 121
            0.7 , // 122
            0.3 , // 123
            -2.34 , // 124
            -3.05 , // 125
            0.3 , // 126
            -4.6 , // 127
            -4.5 , // 128
            -0.0795 , // 129
            -1.09 , // 130
            -0.55 , // 131
            0.601 , // 132
            -3.45 , // 133
            -0.7 , // 134
            -1.05 , // 135
            -0.35 , // 136
            0.5 , // 137
            0.45 , // 138
            0.89 , // 139
            0 , // 140
            -0.15 , // 141
            1.06 , // 142
            -2.65 , // 143
            -4 , // 144
            -1.9 , // 145
            0.1 , // 146
            -3 , // 147
            1 , // 148
            -0.7 , // 149
            0.7 , // 150
            -2.5 , // 151
            -0.6 , // 152
            -2.6 , // 153
            -0.5 , // 154
            -3.7 , // 155
            -0.01, // 156
            -2.0 // 157
    };


    public SAMeylanLogPFragments() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_LOGP_MEYLAN, "Meylan fragments for LoP calculation (Kowwin)");
    }


    @Override
    protected void BuildSAList() throws InitFailureException {
        for (int i=0; i<FragmentNames.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName("MEY" + (i+1));
            curSA.setDescription("Meylan fragment for LogP calculation no. " + (i+1) + ": " + FragmentNames[i]);
            Alerts.add(curSA);
        }
    }


    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {

        try {
            // Inits basic data
            nSK = CurMol.GetStructure().getAtomCount();
            AtomAromatic = new boolean[nSK];
            for (int i=0; i<nSK; i++)
                AtomAromatic[i] = CurMol.GetStructure().getAtom(i).getFlag(CDKConstants.ISAROMATIC);
            try {
                ConnMatrix = CurMol.GetMatrixConnectionAugmented();
            } catch (GenericFailureException ex) {
                throw new GenericFailureException("Unable to calculate Connection matrix");
            }

            FragmentsCounter = new int[FragmentNames.length];
            for (int i=0; i<FragmentNames.length; i++) {
                FragmentsCounter[i] = 0;
            }

            // Searches fragments for each atom
            for (int i=0; i<nSK; i++) {
                int FragIdx = CheckAtomFragments(i);
                if (FragIdx > -1)
                    FragmentsCounter[FragIdx]++;
            }

            // Calculates coefficient and SAs
            GlobalCoefficient = 0;
            AlertList Results = new AlertList();
            for (int i=0; i<FragmentsCounter.length; i++) {
                if (FragmentsCounter[i] > 0)
                    try {
                        Results.add((Alert)Alerts.get(i).clone());
                    } catch (CloneNotSupportedException e) {
                        throw new GenericFailureException("Unable to clone alert");
                    }
                GlobalCoefficient += FragmentsCounter[i] * FragmentCoeff[i];
            }

            return Results;

        } catch (InvalidMoleculeException e) {
            throw new GenericFailureException("Invalid molecule, unable to process fragments");
        }
    }


    private int CheckAtomFragments(int At) throws InvalidMoleculeException {

        boolean isAromatic, isInRing;
        int Charge;
        int nH;
        int VD = 0;
        int nSingle = 0, nDouble = 0, nTriple = 0, nArom = 0;
        int nSngArom = 0, nSngAliph = 0, nSngOlefinicCarbon = 0;
        int nSngOminus = 0, nSngOH = 0, nSngOvd2 = 0, nSngNitrogen = 0;
        int nSngPhosphor = 0, nSngPhosphine = 0, nSngSulfur = 0, nSngAldehyde = 0;
        int nSngHalo = 0, nSngAnyCarbon = 0;
        int nDblArom = 0, nDblAliph = 0, nDblOxygen = 0, nDblNitrogen = 0;
        int nDblCarbonAliph = 0, nDblSulfur = 0, nDblPhosphorus = 0;
        int nDblAnyCarbon = 0;
        int nTriArom = 0, nTriAliph = 0, nTriNitrogen = 0, nTriCarbon = 0;
        int nAromArom = 0, nAromAliph = 0;


        // Calculates useful info on current atom

        if (AtomAromatic[At])
            isAromatic = true;
        else
            isAromatic = false;

        if (CurMol.GetSSSR().contains(CurMol.GetStructure().getAtom(At)))
            isInRing = true;
        else
            isInRing = false;

        try {
            Charge = CurMol.GetStructure().getAtom(At).getFormalCharge();
        } catch (Exception e) {
            Charge = 0;
        }

        for (int j=0; j<nSK; j++) {
            if (j==At)
                continue;
            if (ConnMatrix[At][j]>0) {
                VD++;
                int Z = (int)ConnMatrix[j][j];
                double b = ConnMatrix[At][j];

                if (b==1) {
                    nSingle++;
                    if (Z == 6)
                        nSngAnyCarbon++;
                    if (AtomAromatic[j])
                        nSngArom++;
                    else {
                        if (Z == 6)
                            nSngAliph++;

                        // Check for -N
                        if (Z == 7)
                            nSngNitrogen++;

                        // Check for -S
                        if (Z == 16)
                            nSngSulfur++;

                        // Check for -P
                        if (Z == 15) {
                            nSngPhosphor++;
                            int PVD = 0;
                            for (int k=0; k<nSK; k++) {
                                if (k==j)
                                    continue;
                                if (ConnMatrix[j][k]>0)
                                    PVD++;
                            }
                            if (PVD == 3)
                                nSngPhosphine++;

                        }

                        // Check for -P
                        if ((Z == 9) || (Z == 17) || (Z == 35) || (Z == 53))
                            nSngHalo++;

                        // Check for oleifinic carbon binding: [At]-C=C
                        // Check for attached aldehyde -[CH]=O
                        if (Z == 6) {
                            boolean isOleifinicCarbon=false;
                            for (int k=0; k<nSK; k++) {
                                if ((k==At) || (k==j))
                                    continue;
                                if (ConnMatrix[j][k]>0) {
                                    if ((ConnMatrix[j][k]==2) && (ConnMatrix[k][k]==6))
                                        isOleifinicCarbon = true;
                                }
                            }
                            if (isOleifinicCarbon)
                                nSngOlefinicCarbon++;

                            boolean isAldehyde = false;
                            for (int k=0; k<nSK; k++) {
                                if ((k==At) || (k==j))
                                    continue;
                                if (ConnMatrix[j][k]>0) {
                                    if ((ConnMatrix[j][k]==2) && (ConnMatrix[k][k]==8))
                                        isAldehyde = true;
                                    else {
                                        isAldehyde = false;
                                        break;
                                    }
                                }
                            }
                            if (isAldehyde)
                                nSngAldehyde++;

                        }

                        // Check for OH and [O-] and -O-
                        if (Z == 8) {
                            if (CurMol.GetStructure().getAtom(j).getFormalCharge() == -1)
                                nSngOminus++;
                            else {
                                int OVD = 0;
                                for (int k=0; k<nSK; k++) {
                                    if (k==j)
                                        continue;
                                    if (ConnMatrix[j][k]>0)
                                        OVD++;
                                }
                                if (OVD == 1)
                                    nSngOH++;
                                if (OVD == 2)
                                    nSngOvd2++;
                            }
                        }

                    }
                }

                if (b==2) {
                    nDouble++;
                    if (Z == 6)
                        nDblAnyCarbon++;
                    if (AtomAromatic[j])
                        nDblArom++;
                    else {
                        if (Z == 6)
                            nDblAliph++;

                        // Check for =C
                        if (Z == 6)
                            nDblCarbonAliph++;

                        // Check for =N
                        if (Z == 7)
                            nDblNitrogen++;

                        // Check for =O
                        if (Z == 8)
                            nDblOxygen++;

                        // Check for =S
                        if (Z == 16)
                            nDblSulfur++;

                        // Check for =P
                        if (Z == 15)
                            nDblPhosphorus++;
                    }
                }

                if (b==3) {
                    nTriple++;
                    if (AtomAromatic[j])
                        nTriArom++;
                    else {
                        if (Z == 6)
                            nTriAliph++;

                        // Check for #N (cyano)
                        if (Z == 7)
                            nTriNitrogen++;
                    }
                }

                if (b == 1.5) {
                    nArom++;
                    if (AtomAromatic[j])
                        nAromArom++;
                    else
                        nAromAliph++;
                }
            }
        }

        // counts H
        try {
            nH = CurMol.GetStructure().getAtom(At).getImplicitHydrogenCount();
        } catch (Exception e) {
            nH = 0;
        }


        //// CARBON ////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 6) {

            if (isAromatic) {
                return 19;
            } else {

                if ((nH == 3) && (VD == 1) && (nSingle == 1)) {
                    return 0;
                }
                if ((nH == 2) && (VD == 2) && (nSingle == 2)) {
                    return 1;
                }
                if ((nH == 1) && (VD == 3) && (nSingle == 3)) {
                    return 2;
                }
                if ((nH == 0) && (VD == 4)) {
                    // >C<
                    if (nSngAnyCarbon < 3)
                        return 3;
                    else
                        return 76;
                }
                if ((nH == 2) && (VD == 1) && (nDblCarbonAliph == 1)) {
                    return 4;
                }
                if ((nH == 1) && (VD == 2) && (nDblCarbonAliph == 1) && (nSingle == 1) ||
                        (nH == 0) && (VD == 3) && (nDblCarbonAliph == 1) && (nSingle == 2)) {
                    return 5;
                }
                if ((nH == 1) && (VD == 1) && (nTriple == 1)) {
                    return 6;
                }
                if ((nH == 1) && (VD == 2) && (nSngAliph == 1) && (nDblOxygen == 1)) {
                    return 27;
                }
                if ((nH == 1) && (VD == 2) && (nSngArom == 1) && (nDblOxygen == 1)) {
                    return 28;
                }
                if ((nH == 0) && (VD == 3) && (nDblOxygen == 1) && (nSngNitrogen == 2)) {
                    return 58;
                }
                if ((nH == 0) && (VD == 3) && (nSngAliph == 2) && (nDblOxygen == 1)) {
                    return 29;
                }
                if ((nH == 0) && (VD == 3) && (nSngOlefinicCarbon == 1) && (nSngArom == 1) && (nDblOxygen == 1)) {
                    return 78;
                }
                if ((nH == 0) && (VD == 3) && (nSngAliph == 1) && (nSngArom == 1) && (nDblOxygen == 1)) {
                    return 30;
                }
                if ((nH == 0) && (VD == 2) && (nSngAliph == 1) && (nTriNitrogen == 1)) {
                    return 31;
                }
                if ((nH == 0) && (VD == 2) && (nSngArom == 1) && (nTriNitrogen == 1)) {
                    return 32;
                }
                if ((nH == 0) && (VD == 3) && (nSngOH == 1) && (nDblOxygen == 1)) {
                    // Carboxyl
                    if (nSngArom == 1) {
                        int idxOH = GetIdx_sOH(At);
                        if (idxOH > -1) {
                            int bufFrag = CheckAtomFragments(idxOH);
                            if (bufFrag > -1) FragmentsCounter[bufFrag]--;
                        }
                        return 36;
                    } else {
                        int idxOH = GetIdx_sOH(At);
                        if (idxOH > -1) {
                            int bufFrag = CheckAtomFragments(idxOH);
                            if (bufFrag > -1) FragmentsCounter[bufFrag]--;
                        }
                        return 35;
                    }
                }
                if ((nH == 0) && (VD == 3) && (nDblOxygen == 1) && (nSngNitrogen == 1) && (nSngOvd2 == 1)) {
                    int idxO = GetIdx_sOs(At);
                    if (idxO > -1) {
                        int bufFrag = CheckAtomFragments(idxO);
                        if (bufFrag > -1) FragmentsCounter[bufFrag]--;
                    }
                    return 61;
                }
                if ((nH == 0) && (VD == 3) && (nSngOvd2 == 1) && (nDblOxygen == 1)) {
                    // Ester
                    if (nSngArom == 1) {
                        int idxO = GetIdx_sOs(At);
                        if (idxO > -1) {
                            int bufFrag = CheckAtomFragments(idxO);
                            if (bufFrag > -1) FragmentsCounter[bufFrag]--;
                        }
                        return 41;
                    } else {
                        int idxO = GetIdx_sOs(At);
                        if (idxO > -1) {
                            int bufFrag = CheckAtomFragments(idxO);
                            if (bufFrag > -1) FragmentsCounter[bufFrag]--;
                        }
                        return 40;
                    }
                }
                if ((nH == 0) && (VD == 3) && (nSngNitrogen == 1) && (nDblOxygen == 1)) {
                    if (nSngSulfur == 1) {
                        return 71;
                    }
                    if (nSngArom == 1) {
                        return 44;
                    } else {
                        return 43;
                    }
                }
                if ((nH == 0) && (VD == 3) && (nDblSulfur == 1) && (nSngNitrogen == 2)) {
                    // thiourea
                    return 45;
                }
                if ((nH == 0) && (VD == 3) && (nDblOxygen == 1) && (nSngArom == 2)) {
                    return 69;
                }
                if ((nH == 0) && (VD == 3) && (nDblCarbonAliph == 1) && (nSngArom == 2)) {
                    return 72;
                }

                // TODO 76

                if ((nH == 0) && (VD == 3) && (nDblSulfur == 1) && (nSngArom == 1) && (nSngNitrogen == 1)) {
                    return 80;
                }
                if ((nH == 0) && (VD == 3) && (nDblSulfur == 1) && (nSngAliph == 1) && (nSngNitrogen == 1)) {
                    return 97;
                }

                // TODO 85 - 86 - 87

                if ((nH == 0) && (VD == 3) && (nSngArom == 2) && (nDblOxygen == 1)) {
                    return 88;
                }
                if ((nH == 0) && (VD == 3) && (nSngOlefinicCarbon == 1) && (nDblOxygen == 1)) {
                    return 92;
                }
                if (nTriAliph == 1) {
                    return 112;
                }

                if ((nDblOxygen == 1) && (nSngOvd2 == 2)) {
                    return 131;   // TO BE FIXED
                }

                if ((VD == 3) && (nSngAliph == 1) && (nDblOxygen == 1) && (nSngSulfur == 1)) {
                    return 116;
                }
                if ((VD == 3) && (nSngOlefinicCarbon == 1) && (nDblOxygen == 1) && (nSngSulfur == 1)) {
                    return 134;
                }
                if ((nH == 0) && (VD == 3) && ((nSngAnyCarbon+nDblAnyCarbon) < 3)) {
                    return 3;
                }


            }
        }


        //// OXYGEN ////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 8) {

            if (isAromatic) {
                return 60;
            } else {
                if ((nH == 1) && (VD == 1)) {
                    // -OH
                    if (nSngOlefinicCarbon == 1) {
                        return 81;
                    }
                    if (nSngAliph == 1) {
                        return 7;
                    }
                    if (nSngArom == 1) {
                        return 23;
                    }
                    if (nSngNitrogen == 1) {
                        return 83;
                    }
                    if (nSngPhosphor == 1) {
                        return 102;
                    }
                }

                if ((nH == 0) && (VD == 2)) {
                    // -O-

                    if (nSngOlefinicCarbon == 2) {
                        return 136;
                    }
                    if (nSngPhosphor == 1) {
                        if (nSngPhosphine == 1) {
                            return 149;
                        }
                        if (nSngArom == 1) {
                            return 51;
                        } else {
                            return 50;
                        }
                    }
                    if (nSngNitrogen == 1) {
                        return 59;
                    }
                    if (nSngArom == 1) {
                        return 25;
                    }
                    if (nSngArom == 2) {
                        return 26;
                    }
                    if (nSngArom == 0) {
                        return 8;
                    }
                }

                if ((nH == 0) && (VD == 1) && (nDblPhosphorus == 1)) {
                    return 53;
                }

            }
        }


        //// NITROGEN //////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 7) {

            if (isAromatic) {
                if ((nDblOxygen == 1) ||
                        ( (Charge == +1) && (nSngOminus == 1)) ){
                    return 65;
                }

                IRingSet curRings = CurMol.GetSSSR().getRings(CurMol.GetStructure().getAtom(At));
                boolean isInFiveMemberedRing = false;
                for (int i=0; i< curRings.getAtomContainerCount(); i++) {
                    IRing curRing = (IRing) curRings.getAtomContainer(i);
                    if (curRing.getAtomCount() == 5) {
                        isInFiveMemberedRing = true; break;
                    }
                }
                if (isInFiveMemberedRing) {
                    return 67;
                }
//                if (singleRings.getAtomContainerCount() > 1) {
                if (nArom > 2) {
                    return 105;
                }

                if ((VD == 3) && (nH == 0)) {
                    return 106;
                }
                if ((VD == 3) && (nH == 1)) {
                    return 128;
                }

                return 20;
            } else {
                if ((nDouble == 0) && (nSngPhosphor == 1)) {
                    return 54;
                }
                if ((nH == 2) && (VD == 1) && (nSingle == 1) && (nSngArom == 0)) {
                    return 9;
                }
                if ((nH == 1) && (VD == 2) && (nSngAldehyde == 1) && (nSngArom == 1)) {
                    return 89;
                }
                if ((nH == 1) && (VD == 2) && (nSngAldehyde == 1) && (nSngOlefinicCarbon == 1)) {
                    return 119;
                }
                if ((nH == 1) && (VD == 2) && (nSngAldehyde == 1) && (nSngAliph == 2)) {
                    return 90;
                }
                if ((nH == 1) && (VD == 2) && (nSingle == 2) && (nSngArom == 0)) {
                    return 10;
                }
                if ((nH == 0) && (VD == 3) && (nSngOlefinicCarbon >= 2)) {
                    return 135;
                }
                if ((nH == 0) && (VD == 3) && (nSingle == 3) && (nSngArom == 0)) {
                    return 11;
                }
                if ((VD == 3) && (Charge == +1) && (nDblOxygen == 1) && (nSngOminus == 1)) {
                    // Nitro group
                    if (nSngOvd2 == 1) {
                        return 91;
                    }
                    if (nSngNitrogen == 1) {
                        return 156;
                    }
                    if (nSngArom == 1) {
                        return 34;
                    } else {
                        return 33;
                    }
                }
                if ((nH == 0) && (VD == 2) && (nSingle == 1) && (nDblOxygen == 1)) {
                    return 37;
                }
                if ((VD == 2) && (nSngArom == 2)) {
                    return 64;
                }

                // TODO 74 - 75

                if ((VD == 2) && (nDblNitrogen == 1)) {
                    return 79;
                }
                if ((VD == 2) && (nDblCarbonAliph == 1) && (nSngAliph == 1)) {
                    return 84;
                }
                if ((VD == 4) && (nSingle == 4) && (nH == 0)) {
                    return 104;
                }
                if ((VD == 4) && (nSingle == 4) && (nH == 1)) {
                    return 127;
                }
                if ((VD == 2) && (nTriNitrogen == 1)) {
                    return 109;
                }
                if ((Charge == +1) && (VD == 3) && (nSngArom == 1) && (nSngOminus == 1) && (nDblCarbonAliph == 1)) {
                    return 113;
                }
                if ((VD == 3) && (nSingle == 2) && (nDblOxygen == 1)) {
                    return 124;
                }
                if ((nDblOxygen == 1) && ( ((VD == 2) && (nH > 0)) || (VD > 2) ) ){
                    return 121;
                }
                if ((Charge == +1) && (VD == 3) && (nDblCarbonAliph == 1) && (nSngOminus == 1) && (nSngAliph == 1)) {
                    return 125;
                }
                if ((nDblOxygen == 1) && ((nSingle + nH) == 3)) {
                    return 153;
                }
                if (nSngArom == 1) {
                    return 24;
                }


            }
        }


        //// PHOSPHOR ////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 15) {

            if (isAromatic) {

            } else {
                if ((VD == 3) && (nSingle == 3)) {
                    return 101;
                }
            }

        }


        //// SULFUR ////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 16) {

            if (isAromatic) {
                return 39;
            } else {
                if ((VD == 2) && (nSngArom == 2)) {
                    return 38;
                }
                if ((VD == 1) && (nSngAliph == 1)) {
                    return 46;
                }
                if ((VD == 4) && (nDblOxygen == 2) && (nSngOH == 1) && (nSngOvd2==0)) {
                    // added check to avoid match on SO4 [S(=O)(=O)(OH)O-*]
                    return 47;
                }
                if ((VD == 2) && (nSngAliph == 2)) {
                    return 48;
                }
                if ((VD > 1) && (nDblPhosphorus == 1)) {
                    return 49;
                }
                if ((VD == 2) && (nSngPhosphor == 1)) {
                    return 52;
                }
                if ((VD == 4) && (nDblOxygen == 2)) {
                    // SO2
                    if ((nSngArom == 1) && (nSngNitrogen == 1)) {
                        return 56;
                    }
                    if ((nSngArom == 1) && (nSngAliph == 1)) {
                        return 57;
                    }
                    if ((nSngAliph == 2)) {
                        return 62;
                    }
                    if ((nSngAliph == 1) && (nSngNitrogen == 1)) {
                        return 82;
                    }
                    if ((nSngAliph == 1) && (nSngOminus == 1)) {
                        return 93;
                    }
                    if ((nSngArom == 2)) {
                        return 94;
                    }
                    if ((nSngNitrogen == 2)) {
                        return 95;
                    }
                    if ((nSngAliph == 1) && ((nSngOH == 1) || (nSngOvd2 == 1))) {
                        return 103;
                    }
                }
                if ((VD == 3) && (nSngAliph == 2) && (nDblOxygen == 1)) {
                    return 63;
                }
                if ((VD == 2) && (nSngSulfur == 1)) {
                    return 66;
                }

                // TODO: 25/06/2020 ? Always false
                if ((VD == 2) && (nSngArom == 2)) {
                    return 68;
                }
                if ((VD == 2) && (nSngNitrogen == 1)) {
                    return 70;
                }
                if ((VD == 3) && (nDblOxygen == 1) && (nSngArom == 1)) {
                    return 73;
                }
                if ((VD == 1) && (nH == 1) && (nSngArom == 1)) {
                    return 77;
                }
                if ((VD == 2) && (nSngNitrogen == 2)) {
                    return 108;
                }
            }
        }


        //// Cl ////////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 17) {

            if (VD==1) {

                if (nSngOlefinicCarbon == 1) {
                    return 13;
                }
                if (nSngNitrogen == 1) {
                    return 107;
                }
                if ( (nSngAliph == 1) || (nSngOvd2 == 1) || (nSngOH == 1) ) {
                    // modified to match also [Cl]-O- and [Cl]-OH
                    return 12;
                }
                if (nSngArom == 1) {
                    return 21;
                }
            }
        }


        //// F /////////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 9) {

            if (VD==1) {

                if (nSngOlefinicCarbon == 1) {
                    return 15;
                }
                if (nSngNitrogen == 1) {
                    return 107;
                }
                if (nSngAliph == 1) {
                    return 14;
                }
                if (nSngArom == 1) {
                    return 42;
                }

            }
        }


        //// Br ////////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 35) {

            if (VD==1) {

                if (nSngOlefinicCarbon == 1) {
                    return 17;
                }
                if (nSngNitrogen == 1) {
                    return 107;
                }
                if (nSngAliph == 1) {
                    return 16;
                }
                if (nSngArom == 1) {
                    return 22;
                }
            }
        }


        //// I /////////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 53) {

            if (VD==1) {

                if (nSngAliph == 1) {
                    return 18;
                }
                if (nSngArom == 1) {
                    return 55;
                }
            }
        }


        //// Tin ///////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 50) {

            if ((nSngOvd2 == 1) && (nSngArom == 1)) {
                return 144;
            }
            if (nSngOvd2 > 0) {
                return 143;
            }
            if ((nSngOH > 0) || (nSngHalo>0)){
                return 145;
            }
            if ((VD == 2) && (nSngAliph == 2)) {
                return 155;
            }
            if ((VD == 2) && (nSngArom == 2)) {
                return 157;
            }
            return 142;

        }


        //// Silicon ///////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 14) {

            if ((nSngArom == 0) && (nSngOvd2 == 0) && (nSngOH == 0)) {
                return 99;
            }
            if ((nSngArom > 0) || (nSngOvd2 > 0) || (nSngOH > 0)) {
                return 100;
            }

        }


        //// Mercury ///////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 80) {

            if (VD == 2) {
                return 110;
            }

        }


        //// Selenium //////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 34) {

            if (isAromatic) {
                return 126;
            } else {
                if ((VD == 2) && (nSngArom == 2)) {
                    return 123;
                }
            }

        }


        //// Lead //////////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 82) {
            return 137;
        }


        //// Arsenic ///////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 33) {
            return 138;
        }


        //// Germanium /////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 32) {
            return 139;
        }


        //// Aluminium /////////////////////////////////////////////////////////

        if (ConnMatrix[At][At] == 13) {
            return 146;
        }


        return -1;
    }


    // Utilities to search groups to be skipped

    private int GetIdx_sOH(int At) {

        for (int i=0; i<nSK; i++) {
            if (i==At) continue;
            if ((ConnMatrix[At][i] == 1) && (ConnMatrix[i][i] == 8)) {
                int VD = 0;
                for (int j=0; j<nSK; j++) {
                    if (j==i) continue;
                    if (ConnMatrix[i][j] > 0)
                        VD++;
                }
                if (VD == 1) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int GetIdx_sOs(int At) {

        for (int i=0; i<nSK; i++) {
            if (i==At) continue;
            if ((ConnMatrix[At][i] == 1) && (ConnMatrix[i][i] == 8)) {
                int VD = 0;
                for (int j=0; j<nSK; j++) {
                    if (j==i) continue;
                    if (ConnMatrix[i][j] > 0)
                        VD++;
                }
                if (VD == 2) {
                    return i;
                }
            }
        }

        return -1;
    }


    public double GetCoefficient() {
        return GlobalCoefficient;
    }


}
