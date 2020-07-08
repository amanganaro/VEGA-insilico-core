package insilico.core.ad.item;

import insilico.core.constant.MessagesAD;

/**
 *
 * @author Alberto Manganaro <a.manganaro@kode-solutions.net>
 */
public class ADIndexADIAggregate extends ADIndexADI {

    private final double ADIValueHigh;
    private final double ADIValueMedium;
    private final double ADIValueLow;


    public ADIndexADIAggregate(double ADThresholdHigh, double ADThresholdLow,
                               double ADValueHigh, double ADValueMedium, double ADValueLow) {
        super();

        this.ThreshHigh = ADThresholdHigh;
        this.ThreshLow = ADThresholdLow;
        this.ADIValueHigh = ADValueHigh;
        this.ADIValueMedium = ADValueMedium;
        this.ADIValueLow = ADValueLow;
    }


    public void SetValue(double ADIValue, iADIndex AccIndex,
                         iADIndex ConcIndex, iADIndex MaxErrIndex) {

        if (ADIValue <= ThreshLow) {

            this.SetIndexValue(ADIValue);
            Assessment = MessagesAD.ADI_ASSESS_LOW;
            AssessmentClass = INDEX_LOW;

        } else {

            if ( (AccIndex.GetAssessmentClass() == INDEX_HIGH) &&
                    (ConcIndex.GetAssessmentClass() == INDEX_HIGH) &&
                    (MaxErrIndex.GetAssessmentClass() == INDEX_HIGH) &&
                    (ADIValue >= ThreshHigh) ) {

                Assessment = MessagesAD.ADI_ASSESS_HIGH;
                AssessmentClass = INDEX_HIGH;
                this.SetIndexValue(ADIValueHigh);

            } else if ( (AccIndex.GetAssessmentClass() >= INDEX_MEDIUM) &&
                    (ConcIndex.GetAssessmentClass() >= INDEX_MEDIUM) &&
                    (MaxErrIndex.GetAssessmentClass() >= INDEX_MEDIUM)) {

                Assessment = MessagesAD.ADI_ASSESS_MEDIUM;
                AssessmentClass = INDEX_MEDIUM;
                this.SetIndexValue(ADIValueMedium);

            } else {

                Assessment = MessagesAD.ADI_ASSESS_LOW;
                AssessmentClass = INDEX_LOW;
                this.SetIndexValue(ADIValueLow);

            }
        }
    }

}