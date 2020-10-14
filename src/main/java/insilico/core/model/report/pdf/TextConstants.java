package insilico.core.model.report.pdf;

public class TextConstants {

    public static final String AD_SECTION_INTRO = "The applicability domain of predictions is assessed using an Applicability Domain Index (ADI) that has values from 0 (worst case) to 1 (best case). The ADI is calculated by grouping several other indices, each one taking into account a particular issue of the applicability domain. Most of the indices are based on the calculation of the most similar compounds found in the training and test set of the model, calculated by a similarity index that consider molecule's fingerprint and structural aspects (count of atoms, rings and relevant fragments).\n" +
            "\n" +
            "For each index, including the final ADI, three intervals for its values are defined, such that the first interval corresponds to a positive evaluation, the second one corresponds to a suspicious evaluation  and the last one corresponds to a negative evaluation.\n" +
            "\n" +
            "Following, all applicability domain components are reported along with their explanation and the intervals used.";

    public static final String AD_SIMILAR_MOLECULES_TITLE = "Similar molecules with known experimental value.";
    public static final String AD_SIMILAR_MOLECULES_INTRO = "This index takes into account how similar are the first two most similar compounds found. Values near 1 mean that the predicted compound is well represented in the dataset used to build the model, otherwise the prediction could be an extrapolation. Defined intervals are:";

    public static final String AD_ACCURACY_TITLE = "Accuracy (average error) of prediction for similar molecules.";
    public static final String AD_ACCURACY_INTRO = "This index takes into account the error in prediction for the two most similar compounds found. Values near 0 mean that the predicted compounds falls in an area of the model's space where the model gives reliable predictions, otherwise the greater is the value, the worse the model behaves. Defined intervals are:";

    public static final String AD_CONCORDANCE_TITLE = "Accuracy (average error) of prediction for similar molecules.";
    public static final String AD_CONCORDANCE_INTRO = "This index takes into account the error in prediction for the two most similar compounds found. Values near 0 mean that the predicted compounds falls in an area of the model's space where the model gives reliable predictions, otherwise the greater is the value, the worse the model behaves. Defined intervals are:";

    public static final String AD_MAXIMUM_ERROR_PREDICTION_TITLE = "Maximum error of prediction among similar molecules.";
    public static final String AD_MAXIMUM_ERROR_PREDICTION_INTRO = "This index takes into account the maximum error in prediction among the two most similar compounds. Values near 0 means that the predicted compounds falls in an area of the model's space where the model gives reliable predictions without any outlier value. Defined intervals are:";

    public static final String AD_ACF_TITLE = "Atom Centered Fragments similarity check.";
    public static final String AD_ACF_INTRO = "This index takes into account the presence of one or more fragments that aren't found in the training set, or that are rare fragments. First order atom centered fragments from all molecules in the training set are calculated, then compared with the first order atom centered fragments from the predicted compound; then the index is calculated as following: a first index RARE takes into account rare fragments (those who occur less than three times in the training set), having value of 1 if no such fragments are found, 0.85 if up to 2 fragments are found, 0.7 if more than 2 fragments are found; a second index NOTFOUND takes into account not found fragments, having value of 1 if no such fragments are found, 0.6 if a fragments is found, 0.4 if more than 1 fragment is found. Then, the final index is given as the product RARE * NOTFOUND. Defined intervals are:";

    public static final String AD_DESCRIPTORS_RANGE_CHECK_TITLE = "Model descriptors range check.";
    public static final String AD_DESCRIPTORS_RANGE_CHECK_INTRO = "This index checks if the descriptors calculated for the predicted compound are inside the range of descriptors of the training and test set. The index has value 1 if all descriptors are inside the range, 0 if at least one descriptor is out of the range. Defined intervals are:";

    public static final String AD_GLOBAL_INDEX_TITLE = "Global AD Index.";
    public static final String AD_GLOBAL_INDEX_INTRO = "The final global index takes into account all the previous indices, in order to give a general global assessment on the applicability domain for the predicted compound. Defined intervals are:";

    public static final String STATS_INTRO = "Following, statistics obtained applying the model to its original dataset:";
}
