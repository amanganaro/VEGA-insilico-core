package insilico.core.ad.item;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

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
