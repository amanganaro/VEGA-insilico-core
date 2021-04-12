package insilico.core.constant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import insilico.core.localization.StringSelectorCore;

import java.io.Serializable;

/**
 * Static String repository for messages used in AD classes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

public class MessagesAD implements Serializable {

    private static final long serialVersionUID = 1L;


    public static String AD_CLASS_LOW = StringSelectorCore.getString("msg_low_reliability");
    public static String AD_CLASS_MEDIUM = StringSelectorCore.getString("msg_moderate_reliability");
    public static String AD_CLASS_HIGH = StringSelectorCore.getString("msg_good_reliability");

    public static String ASSESS_SHORT_LOW = "%s (" + StringSelectorCore.getString("msg_low_reliability") + ")";
    public static String ASSESS_SHORT_MEDIUM = "%s (" + StringSelectorCore.getString("msg_moderate_reliability") + ")";
    public static String ASSESS_SHORT_HIGH = "%s (" + StringSelectorCore.getString("msg_good_reliability") + ")";
    public static String ASSESS_SHORT_EXPERIMENTAL = "%s (" + StringSelectorCore.getString("msg_experimental_value_label") + ")";

    public static String ASSESS_LONG_NA = StringSelectorCore.getString("msg_assess_long_na");
    public static String ASSESS_LONG_LOW = StringSelectorCore.getString("msg_assess_long_low");
    public static String ASSESS_LONG_MEDIUM = StringSelectorCore.getString("msg_assess_long_medium");
    public static String ASSESS_LONG_HIGH = StringSelectorCore.getString("msg_assess_long_high");
    public static String ASSESS_LONG_EXPERIMENTAL = StringSelectorCore.getString("msg_assess_long_experimental");
    public static String ASSESS_LONG_ADD_ISSUES = StringSelectorCore.getString("msg_assess_long_add_issues");

    public static String ADI_NAME = StringSelectorCore.getString("msg_adi_name");
    public static String ADI_NAME_LONG = StringSelectorCore.getString("msg_adi_name_long");
    public static String ADI_ASSESS_HIGH = StringSelectorCore.getString("msg_adi_assess_high");
    public static String ADI_ASSESS_MEDIUM = StringSelectorCore.getString("msg_adi_assess_medium");
    public static String ADI_ASSESS_LOW = StringSelectorCore.getString("msg_adi_assess_low");

    public static String SIMILARITY_NAME = StringSelectorCore.getString("msg_similarity_name");
    public static String SIMILARITY_NAME_LONG = StringSelectorCore.getString("msg_similarity_name_long");
    public static String SIMILARITY_ASSESS_HIGH = StringSelectorCore.getString("msg_similarity_assess_high");
    public static String SIMILARITY_ASSESS_MEDIUM = StringSelectorCore.getString("msg_similarity_assess_medium");
    public static String SIMILARITY_ASSESS_LOW = StringSelectorCore.getString("msg_similarity_assess_low");

    public static String ACCURACY_NAME = StringSelectorCore.getString("msg_accuracy_name");
    public static String ACCURACY_NAME_LONG = StringSelectorCore.getString("msg_accuracy_name_long");
    public static String ACCURACY_ASSESS_HIGH = StringSelectorCore.getString("msg_accuracy_assess_high");
    public static String ACCURACY_ASSESS_MEDIUM = StringSelectorCore.getString("msg_accuracy_assess_medium");
    public static String ACCURACY_ASSESS_LOW = StringSelectorCore.getString("msg_accuracy_assess_low");
    public static String ACCURACY_ASSESS_LOW_FOR_MISVAL = StringSelectorCore.getString("msg_accuracy_assess_low_for_misval");

    public static String CONCORDANCE_NAME = StringSelectorCore.getString("msg_concordance_name");
    public static String CONCORDANCE_NAME_LONG = StringSelectorCore.getString("msg_concordance_name_long");
    public static String CONCORDANCE_ASSESS_HIGH = StringSelectorCore.getString("msg_concordance_assess_high");
    public static String CONCORDANCE_ASSESS_MEDIUM = StringSelectorCore.getString("msg_concordance_assess_medium");
    public static String CONCORDANCE_ASSESS_LOW = StringSelectorCore.getString("msg_concordance_assess_low");


    public static String MAXERR_NAME = StringSelectorCore.getString("msg_maxerr_name");
    public static String MAXERR_NAME_LONG = StringSelectorCore.getString("msg_maxerr_name_long");
    public static String MAXERR_ASSESS_HIGH = StringSelectorCore.getString("msg_maxerr_assess_high");
    public static String MAXERR_ASSESS_MEDIUM = StringSelectorCore.getString("msg_maxerr_assess_medium");
    public static String MAXERR_ASSESS_LOW = StringSelectorCore.getString("msg_maxerr_assess_low");
    public static String MAXERR_ASSESS_LOW_FOR_MISVAL = StringSelectorCore.getString("msg_maxerr_assess_low_for_misval");

    public static String ACF_NAME = StringSelectorCore.getString("msg_acf_name");
    public static String ACF_NAME_LONG = StringSelectorCore.getString("msg_acf_name_long");

    public static String ACF_ASSESS_HIGH = StringSelectorCore.getString("msg_acf_assess_high");
    public static String ACF_ASSESS_MEDIUM = StringSelectorCore.getString("msg_acf_assess_medium");
    public static String ACF_ASSESS_LOW = StringSelectorCore.getString("msg_acf_assess_low");
    public static String ACF_MESSAGE_RARE = StringSelectorCore.getString("msg_acf_rare");
    public static String ACF_MESSAGE_MISSING = StringSelectorCore.getString("msg_acf_missing");

    public static String RANGE_NAME = StringSelectorCore.getString("msg_range_name");
    public static String RANGE_NAME_LONG = StringSelectorCore.getString("msg_range_long");
    public static String RANGE_ASSESS_HIGH = StringSelectorCore.getString("msg_assess_high");
    public static String RANGE_ASSESS_LOW = StringSelectorCore.getString("msg_assess_low");

    public static String ALERT_PERSISTENCE_NAME = StringSelectorCore.getString("msg_alert_persistence_name");
    public static String ALERT_PERSISTENCE_NAME_LONG = StringSelectorCore.getString("msg_alert_persistence_name_long");
    public static String ALERT_PERSISTENCE_ASSESS_HIGH = StringSelectorCore.getString("msg_alert_persistence_assess_high");
    public static String ALERT_PERSISTENCE_ASSESS_MEDIUM = StringSelectorCore.getString("msg_alert_persistence_assess_medium");
    public static String ALERT_PERSISTENCE_ASSESS_MEDIUM_NO_KNN = StringSelectorCore.getString("msg_alert_persistence_assess_medium_no_knn");
    public static String ALERT_PERSISTENCE_ASSESS_LOW = StringSelectorCore.getString("msg_alert_persistence_assess_low");
}
