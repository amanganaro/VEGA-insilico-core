package insilico.core.ad.item;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import insilico.core.constant.MessagesAD;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

public class ADIndexMaxError extends ADIndex {

    private static final long serialVersionUID = 1L;

    private double ThreshHigh;
    private double ThreshLow;

    private boolean AllMissingValues;


    public ADIndexMaxError() {
        super(MessagesAD.MAXERR_NAME, MessagesAD.MAXERR_NAME_LONG);
        ThreshHigh = 0;
        ThreshLow = 0;
        AllMissingValues = false;
    }


    public void SetAllMissingValues() {
        AllMissingValues = true;
    }


    public void SetThresholds(double High, double Low) {
        ThreshHigh = High;
        ThreshLow = Low;
        SetAssessment();
    }

    @Override
    protected void SetAssessment() {
        if (AllMissingValues) {
            IndexValue = ThreshHigh * 2;
            Assessment = MessagesAD.MAXERR_ASSESS_LOW_FOR_MISVAL;
            AssessmentClass = INDEX_LOW;
        } else if (IndexValue >= ThreshHigh) {
            Assessment = MessagesAD.MAXERR_ASSESS_LOW;
            AssessmentClass = INDEX_LOW;
        } else if (IndexValue >= ThreshLow) {
            Assessment = MessagesAD.MAXERR_ASSESS_MEDIUM;
            AssessmentClass = INDEX_MEDIUM;
        } else {
            Assessment = MessagesAD.MAXERR_ASSESS_HIGH;
            AssessmentClass = INDEX_HIGH;
        }
    }
}
