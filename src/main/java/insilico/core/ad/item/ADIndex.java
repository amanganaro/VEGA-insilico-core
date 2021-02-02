package insilico.core.ad.item;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import static insilico.core.constant.MessagesAD.*;

/**
 * Ancestor for AD index objects.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)

@NoArgsConstructor
public abstract class ADIndex implements iADIndex {

    private static final long serialVersionUID = 1L;

    public final static short INDEX_UNDEFINED = 0;
    public final static short INDEX_LOW = 1;
    public final static short INDEX_MEDIUM = 2;
    public final static short INDEX_HIGH = 3;

    protected String IndexName;
    protected String IndexNameLong;
    protected double IndexValue;
    protected short AssessmentClass;
    protected String Assessment;

    protected short DecimalDigits;


    public ADIndex(String name, String nameLong) {
        IndexName = name;
        IndexNameLong = nameLong;
        IndexValue = 0;
        AssessmentClass = INDEX_UNDEFINED;
        Assessment = "";
        DecimalDigits = 3;
    }


    public String GetIndexName() {
        return IndexName;
    }

    public String GetIndexNameLong() {
        return IndexNameLong;
    }

    public double GetIndexValue() {
        return IndexValue;
    }


    public String GetIndexValueFormatted() {

        DecimalFormatSymbols InternationalSymbols =
                new DecimalFormatSymbols();
        InternationalSymbols.setDecimalSeparator('.');
        String digits = "0.";
        for (int i=0; i<DecimalDigits; i++)
            digits += "#";
        DecimalFormat df = new DecimalFormat(digits, InternationalSymbols);

        return df.format(IndexValue);
    }


    public void SetIndexValue(double Value) {
        IndexValue = Value;
    }

    public String GetAssessment() {
        return Assessment;
    }


    public short GetAssessmentClass() {
        return AssessmentClass;
    }

    public String GetAssessmentClassAsString() {
        switch(AssessmentClass) {
            case INDEX_UNDEFINED:
                return "-";
            case INDEX_LOW:
                return AD_CLASS_LOW;
            case INDEX_MEDIUM:
                return AD_CLASS_MEDIUM;
            case INDEX_HIGH:
                return AD_CLASS_HIGH;
            default:
                return "-";
        }
    }

    protected abstract void SetAssessment();

}
