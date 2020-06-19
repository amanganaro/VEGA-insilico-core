package insilico.core.model.report.pdf.scatterchart;

import java.util.ArrayList;

/**
 * ScatterChartAxis for Scatter Chart Component
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ScatterChartAxis {

    private String nameX;
    private String nameY;

    private double minX;
    private double maxX;
    private double maxY;
    private double minY;

    private ArrayList<Double> ticksX;
    private ArrayList<Double> ticksY;

    public ScatterChartAxis(){
        nameX = ""; nameY = "";

        SetRangeX(0,100);
        SetRangeY(0,100);
    }

    // GETTERS AND SETTERS
    public double GetMinX() {
        return minX;
    }


    public double GetMinY() {
        return minY;
    }


    public double GetMaxX() {
        return maxX;
    }


    public double GetMaxY() {
        return maxY;
    }


    public String GetNameX() {
        return nameX;
    }


    public String GetNameY() {
        return nameY;
    }


    public void SetNameX(String Name) {
        nameX = Name;
    }


    public void SetNameY(String Name) {
        nameY = Name;
    }


    public ArrayList<Double> GetTicksX() {
        return ticksX;
    }


    public ArrayList<Double> GetTicksY() {
        return ticksY;
    }


    // METHODS

    public void SetRangeX(double minValue, double maxValue){
        minX = minValue;
        maxX = maxValue;
        CalculateTicksX();
    }

    public void SetRangeY(double minValue, double maxValue){
        minY = minValue;
        maxY = maxValue;
        CalculateTicksY();
    }

    private void CalculateTicksX() {

        ticksX = new ArrayList<>();

        // calculates ticks for X axis
        CalculateTicks(maxX, minX, ticksX);
    }

    private void CalculateTicksY() {

        ticksY = new ArrayList<Double>();

        // calculates ticks for Y axis
        CalculateTicks(maxY, minY, ticksY);

    }

    // Calculate Ticks for an axis
    private void CalculateTicks(double max, double min, ArrayList<Double> axisTicks) {
        int t = (int) (max - min);
        double step=0;
        if (t<1)
            step = 0.1;
        else if (t<5)
            step = 0.5;
        else if (t<20)
            step = 1;
        else
            step = 5;

        boolean cond=true;

        int bufTick = (int)Math.floor(min) * 10;
        while (cond) {
            if ((bufTick>=(min *10)) && (bufTick%(step*10)==0))
                cond = false;
            else
                bufTick += 1;
        }
        double curTick = bufTick/10.0;

        cond = true;

        while (cond) {
            axisTicks.add(curTick);
            curTick += step;
            if (curTick> max)
                cond = false;
        }
    }

}
