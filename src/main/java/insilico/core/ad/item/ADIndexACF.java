package insilico.core.ad.item;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import insilico.core.constant.MessagesAD;
import insilico.core.localization.StringSelectorCore;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

public class ADIndexACF extends ADIndex {
    private static final long serialVersionUID = 1L;

    private double nACFRare;
    private double nACFMissing;
    private double ThreshHigh = 1.0;
    private double ThreshLow = 0.7;

    public ADIndexACF() {
        super(MessagesAD.ACF_NAME, MessagesAD.ACF_NAME_LONG);
        nACFRare = 0;
        nACFMissing = 0;
    }


    public void SetACF(double Rare, double Missing) {
        nACFRare = Rare;
        nACFMissing = Missing;
        SetAssessment();
    }


    @Override
    protected void SetAssessment() {

        String details = "";
        double IdxRare, IdxMissing;

        if (nACFMissing == 0)
            IdxMissing = 1;
        else {
            if (nACFMissing == 1)
                IdxMissing = 0.6;
            else
                IdxMissing = 0.4;
            details = ((int)nACFMissing) + " " + StringSelectorCore.getString("ad_acf_unknown_fragments");
        }

        if (nACFRare == 0)
            IdxRare = 1;
        else {
            if (nACFRare < 3)
                IdxRare = 0.85;
            else
                IdxRare = 0.7;
            if (!details.equalsIgnoreCase(""))
                details += " " + StringSelectorCore.getString("ad_acf_details_and") + " ";
            details += ((int)nACFRare) + " " + StringSelectorCore.getString("ad_acf_infrequent_fragments");
        }

        IndexValue = IdxRare * IdxMissing;

        if (IndexValue >= ThreshHigh) {
            Assessment = MessagesAD.ACF_ASSESS_HIGH;
            AssessmentClass = INDEX_HIGH;
        } else if (IndexValue >= ThreshLow) {
            Assessment = MessagesAD.ACF_ASSESS_MEDIUM;
            AssessmentClass = INDEX_MEDIUM;
        } else {
            Assessment = MessagesAD.ACF_ASSESS_LOW;
            AssessmentClass = INDEX_LOW;
        }

        if (!details.equals(""))
            Assessment += " (" + String.format(StringSelectorCore.getString("ad_acf_assessment"), details) + ")";
    }
}
