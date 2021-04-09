package insilico.core.ad.item;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import insilico.core.constant.MessagesAD;
import insilico.core.localization.StringSelector;

/**
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

public class ADIndexRange extends ADIndex {

    private static final long serialVersionUID = 1L;

    private int OutDescriptors;

    public ADIndexRange() {
        super(MessagesAD.RANGE_NAME, MessagesAD.RANGE_NAME_LONG);
        OutDescriptors = 0;
    }


    @Override
    protected void SetAssessment() {
        if (OutDescriptors == 0) {
            Assessment = MessagesAD.RANGE_ASSESS_HIGH;
            AssessmentClass = INDEX_HIGH;
            IndexValue = 1;
        } else {
            Assessment = OutDescriptors + MessagesAD.RANGE_ASSESS_LOW;
            AssessmentClass = INDEX_LOW;
            IndexValue = 0.0;
        }
    }


    @Override
    public String GetIndexValueFormatted() {
        return IndexValue==1? StringSelector.getString("bool_formatted_true") : StringSelector.getString("bool_formatted_false");
    }

    /**
     * @param OutDescriptors the OutDescriptors to set
     */
    public void setOutDescriptors(int OutDescriptors) {
        this.OutDescriptors = OutDescriptors;
        SetAssessment();
    }
}
