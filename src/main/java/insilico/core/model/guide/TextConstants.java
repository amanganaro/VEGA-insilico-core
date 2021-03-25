package insilico.core.model.guide;

public class TextConstants {

    public static final String AD_SECTION_INTRO = "The applicability domain of predictions is assessed using an Applicability Domain Index (ADI) that has values from 0 (worst case) to 1 (best case). The ADI is calculated by grouping several other indices, each one taking into account a particular issue of the applicability domain. Most of the indices are based on the calculation of the most similar compounds found in the training and test set of the model, calculated by a similarity index that consider molecule's fingerprint and structural aspects (count of atoms, rings and relevant fragments).\n" +
            "\n" +
            "For each index, including the final ADI, three intervals for its values are defined, such that the first interval corresponds to a positive evaluation, the second one corresponds to a suspicious evaluation  and the last one corresponds to a negative evaluation.\n" +
            "\n" +
            "Following, all applicability domain components are reported along with their explanation and the intervals used.";

    public static final String AD_SIMILAR_MOLECULES_TITLE = "Similar molecules with known experimental value.";
    public static final String AD_SIMILAR_MOLECULES_INTRO = "This index takes into account how similar are the first two most similar compounds found. Values near 1 mean that the predicted compound is well represented in the dataset used to build the model, otherwise the prediction could be an extrapolation. Defined intervals are:";

    public static final String AD_ACCURACY_DM_TITLE = "Accuracy (average error) of prediction for similar molecules.";
    public static final String AD_ACCURACY_DM_INTRO = "This index takes into account the error in prediction for the two most similar compounds found. Values near 0 mean that the predicted compounds falls in an area of the model's space where the model gives reliable predictions, otherwise the greater is the value, the worse the model behaves. Defined intervals are:";

    public static final String AD_ACCURACY_TITLE = "Accuracy of prediction for similar molecules.";
    public static final String AD_ACCURACY_INTRO = "This index takes into account the classification\n" +
            "accuracy in prediction for the two most similar compounds found. Values near 1 mean that the\n" +
            "predicted compounds falls in an area of the model's space where the model gives reliable predictions\n" +
            "(no misclassifications), otherwise the lower is the value, the worse the model behaves. Defined\n" +
            "intervals are:";

    public static final String AD_RELIABILITY_TITLE = "Model assignment reliability.";
    public static final String AD_RELIABILITY_INTRO = "\"This index checks if the two neural network output values (positive \n" +
            "and non-positive) lead to an unreliable prediction; when the difference between these two values is \n" +
            "lower than 0.1, the neuron where the predicted compound falls can not provide a good classification, \n" +
            "thus the index is set to 0. Otherwise the index is set to 1.\"";

    public static final String AD_NNM_CONCORDANCE_TITLE = "Neural map neurons concordance.";
    public static final String AD_NNM_CONCORDANCE_INTRO = "This index checks the concordance of the predicted compound with the experimental values of the other compounds that falls int the same neuron. The index is built considering two sub-indices: Population (the number of compounds found in the neuron) and Concordance (the number of compounds in the neuron that have experimental value matching with current prediction divided by the number of compounds in the neuron). Low values mean that the predicted compound falls in a zone of the neural network that has no experimental compounds, or that has experimental compounds with eterogeneous experimental values, thus leading to a low reliability of the prediction. ";

    public static final String AD_CONCORDANCE_TITLE = "Concordance with similar molecules (average difference between target compound prediction and experimental values of similar molecules)";
    public static final String AD_CONCORDANCE_INTRO = "This index takes into account the difference between the predicted value and the experimental values of the two most similar compounds. Values near 0 mean that the prediction made agrees with the experimental values found in the model's space, thus the prediction is reliable. Defined intervals are: ";

    public static final String AD_MAXIMUM_ERROR_PREDICTION_TITLE = "Maximum error of prediction among similar molecules.";
    public static final String AD_MAXIMUM_ERROR_PREDICTION_INTRO = "This index takes into account the maximum error in prediction among the two most similar compounds. Values near 0 means that the predicted compounds falls in an area of the model's space where the model gives reliable predictions without any outlier value. Defined intervals are:";

    public static final String AD_ACF_TITLE = "Atom Centered Fragments similarity check.";
    public static final String AD_ACF_INTRO = "This index takes into account the presence of one or more fragments that aren't found in the training set, or that are rare fragments. First order atom centered fragments from all molecules in the training set are calculated, then compared with the first order atom centered fragments from the predicted compound; then the index is calculated as following: a first index RARE takes into account rare fragments (those who occur less than three times in the training set), having value of 1 if no such fragments are found, 0.85 if up to 2 fragments are found, 0.7 if more than 2 fragments are found; a second index NOTFOUND takes into account not found fragments, having value of 1 if no such fragments are found, 0.6 if a fragments is found, 0.4 if more than 1 fragment is found. Then, the final index is given as the product RARE * NOTFOUND. Defined intervals are:";

    public static final String AD_DESCRIPTORS_RANGE_CHECK_TITLE = "Model descriptors range check.";
    public static final String AD_DESCRIPTORS_RANGE_CHECK_INTRO = "This index checks if the descriptors calculated for the predicted compound are inside the range of descriptors of the training and test set. The index has value 1 if all descriptors are inside the range, 0 if at least one descriptor is out of the range. Defined intervals are:";

    public static final String AD_GLOBAL_INDEX_TITLE = "Global AD Index.";
    public static final String AD_GLOBAL_INDEX_INTRO = "The final global index takes into account all the previous indices, in order to give a general global assessment on the applicability domain for the predicted compound. Defined intervals are:";

    public static final String AD_STRUCTURAL_ALERT_CONCORDANCE_TITLE = "Structural alert concordance";
    public static final String AD_STRUCTURAL_ALERT_CONCORDANCE_INTRO = "This index takes into account the concordance between the prediction provided by the k-NN model and the alerts found. Defined values are: ";

    public static final String AD_LOGP_RELIABILITY_TITLE = "LogP reliability ";
    public static final String AD_LOGP_RELIABILITY_INTRO = "This index takes into account the reliability of the logP value used in the model. Note that the Meylan BCF model is strongly based on the logP prediction of the compound, thus this index is highly relevant for the assessment of the final prediction. The reliability of the logP value comes from the assessment of the VEGA LogP model (that provides the used logP value), which is also provided in the 'Prediction summary' section of the report. Defined intervals are:";

    public static final String STATS_INTRO = "Following, statistics obtained applying the model to its original dataset:";

}
