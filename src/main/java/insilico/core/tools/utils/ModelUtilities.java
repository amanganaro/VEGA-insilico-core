package insilico.core.tools.utils;

import insilico.core.ad.item.ADIndex;
import insilico.core.ad.item.iADIndex;
import insilico.core.alert.Alert;
import insilico.core.constant.MessagesAD;
import insilico.core.model.InsilicoModelOutput;

import java.util.ArrayList;

/**
 * General utilities for model and training set handling purposes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ModelUtilities {

    /**
     * Builds the string for AD items warning, with all the AD items not
     * having optimal assessment. Each item is preceded by a newline char
     *
     * @param items list of AD items to be checked
     * @return warning string
     */
    public static String BuildADItemsWarningMsg(ArrayList<iADIndex> items) {
        String ADItemWarnings = "";
        for (iADIndex curAD : items) {
            if (curAD.GetAssessmentClass() != ADIndex.INDEX_HIGH) {
                ADItemWarnings += "\n- ";
                ADItemWarnings += curAD.GetAssessment();
            }
        }
        return ADItemWarnings;
    }

    public static void SetDefaultAssessment(InsilicoModelOutput Output, String Result) {
        SetDefaultAssessment(Output, Result, "");
    }


    public static void SetDefaultAssessment(InsilicoModelOutput Output, String Result, String Units) {

        if (!Units.isEmpty())
            Result += " " + Units;

        // Check if ADI is missing
        if (Output.getADI() == null) {

            Output.setAssessment(Result);
            Output.setAssessmentVerbose(String.format(MessagesAD.ASSESS_LONG_NA, Result));

        } else {

            // Builds AD item warnings string
            String ADItemWarnings =
                    ModelUtilities.BuildADItemsWarningMsg(Output.getADIndex());

            // Sets assessment message
            switch (Output.getADI().GetAssessmentClass()) {
                case ADIndex.INDEX_LOW:
                    Output.setAssessment(String.format(MessagesAD.ASSESS_SHORT_LOW, Result));
                    Output.setAssessmentVerbose(String.format(MessagesAD.ASSESS_LONG_LOW, Result, ADItemWarnings));
                    break;
                case ADIndex.INDEX_MEDIUM:
                    Output.setAssessment(String.format(MessagesAD.ASSESS_SHORT_MEDIUM, Result));
                    Output.setAssessmentVerbose(String.format(MessagesAD.ASSESS_LONG_MEDIUM, Result, ADItemWarnings));
                    break;
                case ADIndex.INDEX_HIGH:
                    Output.setAssessment(String.format(MessagesAD.ASSESS_SHORT_HIGH, Result));
                    Output.setAssessmentVerbose(String.format(MessagesAD.ASSESS_LONG_HIGH, Result));
                    if (!ADItemWarnings.isEmpty())
                        Output.setAssessmentVerbose(Output.getAssessmentVerbose() +
                                String.format(MessagesAD.ASSESS_LONG_ADD_ISSUES, ADItemWarnings));
                    break;
            }

        }

        // Override assessment if experimental value is available
        if (Output.HasExperimental()) {
            String Exp = Output.getExperimentalFormatted();
            if (!Units.isEmpty())
                Exp += " " + Units;
            Output.setAssessmentVerbose(String.format(MessagesAD.ASSESS_LONG_EXPERIMENTAL, Exp, Output.getAssessment()));
            Output.setAssessment(String.format(MessagesAD.ASSESS_SHORT_EXPERIMENTAL, Exp));
        }

    }



    /**
     * Builds the string with the list of given structural alerts
     *
     * @param items array of Alert
     * @return string with the names of the SA separed by ";"
     */
    public static String BuildSANameList(ArrayList<Alert> items) {
        String list = "";
        for (Alert curSA : items) {
            if (!list.equals(""))
                list += "; ";
            list += curSA.getName();
        }
        return list;
    }


}
