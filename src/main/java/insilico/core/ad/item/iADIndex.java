package insilico.core.ad.item;

import java.io.Serializable;

public interface iADIndex extends Serializable {

    public String GetIndexName();
    public String GetIndexNameLong();

    public double GetIndexValue();
    public String GetIndexValueFormatted();
    public void SetIndexValue(double Value);

    public String GetAssessment();
    public short GetAssessmentClass();
    public String GetAssessmentClassAsString();
}
