package insilico.core.ad.reasoning;

import java.util.ArrayList;

public class UncertaintyClassBar {

    private String ClassName;
    private String ClassDescription;
    private String AxisName;
    private double XValue;
    private String XValueAsString;
    private double XValueInterval;
    private ArrayList<Double> Thresholds;
    private ArrayList<String> ThresholdsMarks;


    public UncertaintyClassBar() {
        ClassName = "";
        ClassDescription = "";
        Thresholds = new ArrayList<>();
        ThresholdsMarks = new ArrayList<>();
        XValue = 0;
        XValueAsString = "";
        XValueInterval = 0;
        AxisName = "predicted values";
    }

    /**
     * @return the ClassName
     */
    public String getClassName() {
        return ClassName;
    }

    /**
     * @param ClassName the ClassName to set
     */
    public void setClassName(String ClassName) {
        this.ClassName = ClassName;
    }

    /**
     * @return the ClassDescription
     */
    public String getClassDescription() {
        return ClassDescription;
    }

    /**
     * @param ClassDescription the ClassDescription to set
     */
    public void setClassDescription(String ClassDescription) {
        this.ClassDescription = ClassDescription;
    }

    /**
     * @return the AxisName
     */
    public String getAxisName() {
        return AxisName;
    }

    /**
     * @param AxisName the AxisName to set
     */
    public void setAxisName(String AxisName) {
        this.AxisName = AxisName;
    }

    /**
     * @return the XValue
     */
    public double getXValue() {
        return XValue;
    }

    /**
     * @param XValue the XValue to set
     */
    public void setXValue(double XValue) {
        this.XValue = XValue;
    }

    /**
     * @return the XValueAsString
     */
    public String getXValueAsString() {
        return XValueAsString;
    }

    /**
     * @param XValueAsString the XValueAsString to set
     */
    public void setXValueAsString(String XValueAsString) {
        this.XValueAsString = XValueAsString;
    }

    /**
     * @return the XValueInterval
     */
    public double getXValueInterval() {
        return XValueInterval;
    }

    /**
     * @param XValueInterval the XValueInterval to set
     */
    public void setXValueInterval(double XValueInterval) {
        this.XValueInterval = XValueInterval;
    }

    /**
     * @return the Thresholds
     */
    public ArrayList<Double> getThresholds() {
        return Thresholds;
    }

    /**
     * @param Thresholds the Thresholds to set
     */
    public void setThresholds(ArrayList<Double> Thresholds) {
        this.Thresholds = Thresholds;
    }

    /**
     * @return the ThresholdsMarks
     */
    public ArrayList<String> getThresholdsMarks() {
        return ThresholdsMarks;
    }

    /**
     * @param ThresholdsMarks the ThresholdsMarks to set
     */
    public void setThresholdsMarks(ArrayList<String> ThresholdsMarks) {
        this.ThresholdsMarks = ThresholdsMarks;
    }

}
