package insilico.core.constant;

import insilico.core.localization.StringSelectorCore;

/**
 * Static String repository for error messages used in model and molecule classes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MessagesError {

    public static String MODEL_INVALID_MOLECULE = StringSelectorCore.getString("msg_model_invalid_molecule");
    public static String MODEL_DESCRIPTORS_MISSING = StringSelectorCore.getString("msg_model_descriptors_missing");
    public static String MODEL_NOT_CALCULATED_ERROR = StringSelectorCore.getString("msg_model_not_calculated_error");
    public static String MODEL_AD_NOT_CALCULATED = StringSelectorCore.getString("msg_ad_not_calculated");
    public static String MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE = StringSelectorCore.getString("msg_model_assessment_for_not_calculated_molecule");

}
