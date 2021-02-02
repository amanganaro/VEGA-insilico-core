package insilico.core.constant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.Serializable;

/**
 * Static String repository for messages used in AD classes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

public class MessagesAD implements Serializable {

    private static final long serialVersionUID = 1L;


    public static String AD_CLASS_LOW =
            "LOW reliability";
    public static String AD_CLASS_MEDIUM =
            "MODERATE reliability";
    public static String AD_CLASS_HIGH =
            "GOOD reliability";

    public static String ASSESS_SHORT_LOW =
            "%s (low reliability)";
    public static String ASSESS_SHORT_MEDIUM =
            "%s (moderate reliability)";
    public static String ASSESS_SHORT_HIGH =
            "%s (good reliability)";
    public static String ASSESS_SHORT_EXPERIMENTAL =
            "%s (EXPERIMENTAL value)";

    public static String ASSESS_LONG_NA =
            "Prediction is %s, it is not possible to perform an assessment.";
    public static String ASSESS_LONG_LOW =
            "Prediction is %s, but the result may be not reliable. A check of " +
                    "the information given in the following section should be done, " +
                    "paying particular attention to the following issues: %s";
    public static String ASSESS_LONG_MEDIUM =
            "Prediction is %s, but the result shows some critical aspects, " +
                    "which require to be checked: %s";
    public static String ASSESS_LONG_HIGH =
            "Prediction is %s, the result appears reliable. Anyhow, you should " +
                    "check it through the evaluation of the information given in the "+
                    "following sections.";
    public static String ASSESS_LONG_EXPERIMENTAL =
            "Experimental value is %s. Model prediction is %s.";
    public static String ASSESS_LONG_ADD_ISSUES =
            " Anyway some issues could be not optimal: %s";

    public static String ADI_NAME =
            "AD index";
    public static String ADI_NAME_LONG =
            "Global AD Index";
    public static String ADI_ASSESS_HIGH =
            "the predicted compound is into the Applicability Domain of the model";
    public static String ADI_ASSESS_MEDIUM =
            "the predicted compound could be out of the Applicability Domain of the model";
    public static String ADI_ASSESS_LOW =
            "the predicted compound is outside the Applicability Domain of the model";

    public static String SIMILARITY_NAME =
            "Similarity index";
    public static String SIMILARITY_NAME_LONG =
            "Similar molecules with known experimental value";
    public static String SIMILARITY_ASSESS_HIGH =
            "strongly similar compounds with known experimental value in the training set have been found";
    public static String SIMILARITY_ASSESS_MEDIUM =
            "only moderately similar compounds with known experimental value in the training set have been found";
    public static String SIMILARITY_ASSESS_LOW =
            "no similar compounds with known experimental value in the training set have been found";

    public static String ACCURACY_NAME =
            "Accuracy index";
    public static String ACCURACY_NAME_LONG =
            "Accuracy of prediction for similar molecules";
    public static String ACCURACY_ASSESS_HIGH =
            "accuracy of prediction for similar molecules found in the training set is good";
    public static String ACCURACY_ASSESS_MEDIUM =
            "accuracy of prediction for similar molecules found in the training set is not optimal";
    public static String ACCURACY_ASSESS_LOW =
            "accuracy of prediction for similar molecules found in the training set is not adequate";
    public static String ACCURACY_ASSESS_LOW_FOR_MISVAL =
            "accuracy of prediction for similar molecules found in the training set is not adequate because all predictions are missing values";

    public static String CONCORDANCE_NAME =
            "Concordance index";
    public static String CONCORDANCE_NAME_LONG =
            "Concordance for similar molecules";
    public static String CONCORDANCE_ASSESS_HIGH =
            "similar molecules found in the training set have experimental values that agree with the predicted value";
    public static String CONCORDANCE_ASSESS_MEDIUM =
            "some similar molecules found in the training set have experimental values that disagree with the predicted value";
    public static String CONCORDANCE_ASSESS_LOW =
            "similar molecules found in the training set have experimental values that disagree with the predicted value";

    public static String MAXERR_NAME =
            "Max error index";
    public static String MAXERR_NAME_LONG =
            "Maximum error of prediction among similar molecules";
    public static String MAXERR_ASSESS_HIGH =
            "the maximum error in prediction of similar molecules found in the training set has a low value, considering the experimental variability";
    public static String MAXERR_ASSESS_MEDIUM =
            "the maximum error in prediction of similar molecules found in the training set has a moderate value, considering the experimental variability";
    public static String MAXERR_ASSESS_LOW =
            "the maximum error in prediction of similar molecules found in the training set has a high value, considering the experimental variability";
    public static String MAXERR_ASSESS_LOW_FOR_MISVAL =
            "the maximum error in prediction can not be evaluated because all predictions are missing values";

    public static String ACF_NAME =
            "ACF index";
    public static String ACF_NAME_LONG =
            "Atom Centered Fragments similarity check";
    public static String ACF_ASSESS_HIGH =
            "all atom centered fragment of the compound have been found in the compounds of the training set";
    public static String ACF_ASSESS_MEDIUM =
            "some atom centered fragments of the compound have not been found in the compounds of the training set or are rare fragments";
    public static String ACF_ASSESS_LOW =
            "a prominent number of atom centered fragments of the compound have not been found in the compounds of the training set or are rare fragments";

    public static String ACF_MESSAGE_RARE =
            "The fragment has less than 3 occurrences in the model's training set";
    public static String ACF_MESSAGE_MISSING =
            "The fragment has never been found in the model's training set";

    public static String RANGE_NAME =
            "Descriptors range check";
    public static String RANGE_NAME_LONG =
            "Model's descriptors range check";
    public static String RANGE_ASSESS_HIGH =
            "descriptors for this compound have values inside the descriptor range of the compounds of the training set";
    public static String RANGE_ASSESS_LOW =
            " descriptor(s) for this compound have values outside the descriptor range of the compounds of the training set.";

    public static String ALERT_PERSISTENCE_NAME =
            "Structural alerts concordance";
    public static String ALERT_PERSISTENCE_NAME_LONG =
            "Concordance of prediction with found structural alerts";
    public static String ALERT_PERSISTENCE_ASSESS_HIGH =
            "all found alerts are related to experimental values in agreement with the prediction";
    public static String ALERT_PERSISTENCE_ASSESS_MEDIUM =
            "no alerts that could confirm the prediction were found";
    public static String ALERT_PERSISTENCE_ASSESS_MEDIUM_NO_KNN =
            "KNN model has given no output and prediction has based only on found alerts";
    public static String ALERT_PERSISTENCE_ASSESS_LOW =
            "one or more found alerts are related to experimental values not in agreement with the prediction";
}
