package insilico.core.constant;

/**
 * Static String repository for error messages used in model and molecule classes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MessagesError {

    public static String MODEL_INVALID_MOLECULE =
            "Unable to calculate model, input molecule is not valid";
    public static String MODEL_DESCRIPTORS_MISSING =
            "Unable to calculate some molecular descriptors, model can not be executed";
    public static String MODEL_NOT_CALCULATED_ERROR =
            "An error occurred during model execution";
    public static String MODEL_AD_NOT_CALCULATED =
            "Unable to perform Applicability Domain check";
    public static String MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE =
            "Model has not been calculated, an error has occurred";

}
