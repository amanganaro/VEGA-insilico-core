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
public class SAMeylanLogPAdditionalFragments extends AlertBlockFromSMARTS implements iAlertBlock {

    private Pattern[] SA;
    private double GlobalCoefficient;

    private final static String[] SMARTSFragments = {
            "[*;!a]-N=C=S",  // 74
            "[a]-N=C=S",  // 75
            "N#C-S", //85
            "N#C-N", //86
            "N#C-C=N", //87
            "[a]C(=O)C=O", //96
            "*-SC=*", // 98
            "*-C#N=O", // 114
            "*-OS(=O)(=O)O-*", //117
            "C-C(=O)C(=O)", //118
            "[*;!a]C(=O)[SH]", // 120
            "N[C;R](=S)S", // 122
            "[OH]S(=O)(=O)O", // 151
            "[A]OO[A]", // not1 - correction for aliphatic -O-  (not in list)
            "[N+](=O)([O-])NCN[N+](=O)[O-]", // not2 - estimated correction (not in list)
            "CC(=O)C(C(=O)C)C(=O)C", // not3
            //"[O;!R]([Si])[Si]", // fix8 (linear aliph -O- when linked with 2 Si) - REMOVED (gives problems)
            //"[O;R]([Si])[Si]", // fix8bis (cyclic aliph -O- when linked with 2 Si) - REMOVED (gives problems)
    };


    private final static double[] SMARTSCoeff = {
            0.5236, // 74
            (1.3369 + 0.917), // 75
            0.354, // 85
            0.3731, // 86
            (0.0562 + 0.9218), // 87
            -0.1, // 96
            -0.1, // 98
            -0.35, // 114
            1.35, // 117
            (-1.33 + 1.5586 + 1), // 118
            -0.64, // 120
            0.7, // 122
            -2.5,  // 151
            -0.6 + (2 * 1.2566), // not1
            1.5, // not2
            3.45, // not3
            //+1.2566 + 0.0280, // fix 8
            //+1.2566 - 0.1348, // fix 8bis

    };



    public SAMeylanLogPAdditionalFragments() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_LOGP_MEYLAN_ADDITIONAL, "Meylan additional fragments for LoP calculation (Kowwin)");
    }


    @Override
    protected void BuildSAList() throws InitFailureException {

        for (int i=0; i<SMARTSFragments.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName("MEYA" + (i+1));
            curSA.setDescription("Meylan additional fragment for LogP calculation no. " + (i+1) + " defined by SMARTS: " +  SMARTSFragments[i]);
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

        try {

            for (int i=0; i<SA.length; i++) {
                if (SA[i].matches(CurMol.GetStructure())) {
                    Res.add((Alert)Alerts.get(i).clone());


                    int nMatches = SA[i].matchAll(CurMol.GetStructure()).countUnique();
                    if (i == 15)
                        GlobalCoefficient += SMARTSCoeff[i] * 1; // correction - not related to number of matches
                    else
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
