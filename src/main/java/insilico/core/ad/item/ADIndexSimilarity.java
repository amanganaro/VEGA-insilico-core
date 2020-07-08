package insilico.core.ad.item;

import insilico.core.constant.MessagesAD;

public class ADIndexSimilarity extends ADIndex{

    private static final long serialVersionUID = 1L;

    private double ThreshHigh;
    private double ThreshLow;


    public ADIndexSimilarity() {
        super(MessagesAD.SIMILARITY_NAME, MessagesAD.SIMILARITY_NAME_LONG);
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
            Assessment = MessagesAD.SIMILARITY_ASSESS_HIGH;
            AssessmentClass = INDEX_HIGH;
        } else if (IndexValue >= ThreshLow) {
            Assessment = MessagesAD.SIMILARITY_ASSESS_MEDIUM;
            AssessmentClass = INDEX_MEDIUM;
        } else {
            Assessment = MessagesAD.SIMILARITY_ASSESS_LOW;
            AssessmentClass = INDEX_LOW;
        }
    }

}
