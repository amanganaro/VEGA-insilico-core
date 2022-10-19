package insilico.core.ad.item;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import insilico.core.constant.MessagesAD;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

public class ADIndexADI extends ADIndex {

    private static final long serialVersionUID = 1L;

    protected double ThreshHigh;
    protected double ThreshLow;


    public ADIndexADI() {
        super(MessagesAD.ADI_NAME, MessagesAD.ADI_NAME_LONG);
        ThreshHigh = 0;
        ThreshLow = 0;
    }


    public void SetThresholds(double High, double Low) {
        ThreshHigh = High;
        ThreshLow = Low;
        SetAssessment();
    }

    @Override
    protected void SetAssessment() {
        if (IndexValue >= ThreshHigh) {
            Assessment = MessagesAD.ADI_ASSESS_HIGH;
            AssessmentClass = INDEX_HIGH;
        } else if (IndexValue >= ThreshLow) {
            Assessment = MessagesAD.ADI_ASSESS_MEDIUM;
            AssessmentClass = INDEX_MEDIUM;
        } else {
            Assessment = MessagesAD.ADI_ASSESS_LOW;
            AssessmentClass = INDEX_LOW;
        }
    }
}
