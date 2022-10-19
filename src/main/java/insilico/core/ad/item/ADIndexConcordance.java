package insilico.core.ad.item;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import insilico.core.constant.MessagesAD;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

public class ADIndexConcordance extends ADIndex {

    private static final long serialVersionUID = 1L;

    private double ThreshHigh;
    private double ThreshLow;

    private boolean QuantitativeMode;


    public ADIndexConcordance() {
        super(MessagesAD.CONCORDANCE_NAME, MessagesAD.CONCORDANCE_NAME_LONG);
        ThreshHigh = 0;
        ThreshLow = 0;
        QuantitativeMode = true;
    }


    public void SetThresholds(double High, double Low) {
        ThreshHigh = High;
        ThreshLow = Low;
        SetAssessment();
    }

    @Override
    protected void SetAssessment() {
        if (QuantitativeMode) {
            if (IndexValue <= ThreshLow) {
                Assessment = MessagesAD.CONCORDANCE_ASSESS_HIGH;
                AssessmentClass = INDEX_HIGH;
            } else if (IndexValue <= ThreshHigh) {
                Assessment = MessagesAD.CONCORDANCE_ASSESS_MEDIUM;
                AssessmentClass = INDEX_MEDIUM;
            } else {
                Assessment = MessagesAD.CONCORDANCE_ASSESS_LOW;
                AssessmentClass = INDEX_LOW;
            }
        } else {
            if (IndexValue >= ThreshHigh) {
                Assessment = MessagesAD.CONCORDANCE_ASSESS_HIGH;
                AssessmentClass = INDEX_HIGH;
            } else if (IndexValue >= ThreshLow) {
                Assessment = MessagesAD.CONCORDANCE_ASSESS_MEDIUM;
                AssessmentClass = INDEX_MEDIUM;
            } else {
                Assessment = MessagesAD.CONCORDANCE_ASSESS_LOW;
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
