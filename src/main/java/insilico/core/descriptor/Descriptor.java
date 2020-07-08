package insilico.core.descriptor;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Molecular descriptor object. Stores information about the name, the precision
 * and the value of a descriptor.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class Descriptor implements Serializable, Cloneable {

    public final static int MISSING_VALUE = -999;
    public final static String MISSING_VALUE_STR = "-999";
    public final static int DEFAULT_PRECISION = 3;

    private static final long serialVersionUID = 1L;

    private String Name;
    private String Description;
    private double Value;
    private int Precision;


    /**
     * Constructor.
     *
     * @param Name the name of the descriptor
     * @param Description the description of the descriptor
     * @param Precision the precision (number of decimal digits, 0 for integer descriptors)
     * @param Value the calculated value of the descriptor
     */
    public Descriptor(String Name, String Description, int Precision, double Value) {
        this.Name = Name;
        this.Description = Description;
        this.Precision = Precision;
        this.Value = Value;
    }


    public Object Clone() throws CloneNotSupportedException {
        return super.clone();
    }


    /**
     * Overload of the constructor. Value is set to {@link #MISSING_VALUE}.
     *
     * @param Name the name of the descriptor
     * @param Description the description of the descriptor
     * @param Precision the precision (number of decimal digits, 0 for integer descriptors)
     */
    public Descriptor(String Name, String Description, int Precision) {
        this(Name, Description, Precision, MISSING_VALUE);
    }


    /**
     * Overload of the constructor. Value is set to {@link #MISSING_VALUE}.
     * Precision is set to {@link #DEFAULT_PRECISION}
     *
     * @param Name the name of the descriptor
     * @param Description the description of the descriptor
     */
    public Descriptor(String Name, String Description) {
        this(Name, Description, DEFAULT_PRECISION, MISSING_VALUE);
    }


    /**
     * Overload of the constructor. Value is set to {@link #MISSING_VALUE}.
     * Precision is set to {@link #DEFAULT_PRECISION}. Description is set
     * to an empty string.
     *
     * @param Name the name of the descriptor
     */
    public Descriptor(String Name) {
        this(Name, "", DEFAULT_PRECISION, MISSING_VALUE);
    }


    /**
     * Overload of the constructor. Value is set to {@link #MISSING_VALUE}.
     * Precision is set to {@link #DEFAULT_PRECISION}. Name and Description
     * are set to an empty string.
     */
    public Descriptor() {
        this("", "", DEFAULT_PRECISION, MISSING_VALUE);
    }


    /**
     * Returns the actual value of the descriptor as a string, formatted
     * with the number of decimal digits specified in the Precision.
     *
     * @return the formatted descriptor value as string
     */
    public String getFormattedValue() {
        String str = "";
        StringBuilder format = new StringBuilder("0");
        if (getPrecision()>0) {
            format.append(".");
            format.append("#".repeat(Math.max(0, getPrecision())));
        }
        DecimalFormatSymbols InternationalSymbols =
                new DecimalFormatSymbols();
        InternationalSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat(format.toString(), InternationalSymbols);
        if (getValue() != MISSING_VALUE)
            str = df.format(getValue());
        else
            str = MISSING_VALUE_STR;
        return str;
    }


    /**
     * @return the Name
     */
    public String getName() {
        return Name;
    }

    /**
     * @param Name the Name to set
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     * @return the Description
     */
    public String getDescription() {
        return Description;
    }

    /**
     * @param Description the Description to set
     */
    public void setDescription(String Description) {
        this.Description = Description;
    }

    /**
     * @return the Value
     */
    public double getValue() {
        return Value;
    }

    /**
     * @param Value the Value to set
     */
    public void setValue(double Value) {
        this.Value = Value;
    }

    /**
     * @return the Precision
     */
    public int getPrecision() {
        return Precision;
    }

    /**
     * @param Precision the Precision to set
     */
    public void setPrecision(int Precision) {
        this.Precision = Precision;
    }

}

