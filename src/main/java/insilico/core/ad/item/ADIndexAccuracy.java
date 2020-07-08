package insilico.core.ad.item;

import insilico.core.constant.MessagesAD;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ADIndexAccuracy extends ADIndex {

    private static final long serialVersionUID = 1L;

    private double ThreshHigh;
    private double ThreshLow;

    private boolean QuantitativeMode;
    private boolean AllMissingValues;

    public ADIndexAccuracy() {
        super(MessagesAD.ACCURACY_NAME, MessagesAD.ACCURACY_NAME_LONG);
        ThreshHigh = 0;
        ThreshLow = 0;
        QuantitativeMode = true;
        AllMissingValues = false;
    }


    public void SetAllMissingValues() {
        // Needed only for quantitative models
        AllMissingValues = true;
    }


    public void SetThresholds(double High, double Low) {
        ThreshHigh = High;
        ThreshLow = Low;
        SetAssessment();
    }

    @Override
    protected void SetAssessment() {

        if (QuantitativeMode) {
            if (AllMissingValues) {
                IndexValue = ThreshHigh * 2;
                Assessment = MessagesAD.ACCURACY_ASSESS_LOW_FOR_MISVAL;
                AssessmentClass = INDEX_LOW;
            } else if (IndexValue <= ThreshLow) {
                Assessment = MessagesAD.ACCURACY_ASSESS_HIGH;
                AssessmentClass = INDEX_HIGH;
            } else if (IndexValue <= ThreshHigh) {
                Assessment = MessagesAD.ACCURACY_ASSESS_MEDIUM;
                AssessmentClass = INDEX_MEDIUM;
            } else {
                Assessment = MessagesAD.ACCURACY_ASSESS_LOW;
                AssessmentClass = INDEX_LOW;
            }
        } else {
            if (IndexValue >= ThreshHigh) {
                Assessment = MessagesAD.ACCURACY_ASSESS_HIGH;
                AssessmentClass = INDEX_HIGH;
            } else if (IndexValue >= ThreshLow) {
                Assessment = MessagesAD.ACCURACY_ASSESS_MEDIUM;
                AssessmentClass = INDEX_MEDIUM;
            } else {
                Assessment = MessagesAD.ACCURACY_ASSESS_LOW;
                AssessmentClass = INDEX_LOW;
            }
        }
    }

    /**
     * @return the QuantitativeMode
     */
    public boolean isQuantitativeMode() {
        return QuantitativeMode;
    }

    /**
     * @param QuantitativeMode the QuantitativeMode to set
     */
    public void setQuantitativeMode(boolean QuantitativeMode) {
        this.QuantitativeMode = QuantitativeMode;
    }
}
