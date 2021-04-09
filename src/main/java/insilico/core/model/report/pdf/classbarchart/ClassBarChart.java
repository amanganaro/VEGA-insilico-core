package insilico.core.model.report.pdf.classbarchart;

import insilico.core.localization.StringSelector;
import org.xmlcml.euclid.Axis;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * ClassBarChart Component
 *
 * @author Alberto Manganaro
 */
public class ClassBarChart {

    public boolean drawMarks;

    public Color BackColorGeneral;
    public Color BackColorChartArea;
    public Color GridColor;
    public Color XTickColor;
    public Color XPointIntervalColor;
    public Color XPointIntervalColorGray;

    public ClassBarDataPoint XPoint;
    public ArrayList<ClassBarDataPoint> ThresholdPoints;
    public String AxisName;

    private int clientWidth;
    private int clientHeight;
    private Rectangle ChartArea;
    private int AxisLineY;

    private double MinXVal;
    private double MaxXVal;

    private boolean DeduceClientSize;

    // CONSTRUCTORS

    public ClassBarChart() {
        XPoint = null;
        ThresholdPoints = new ArrayList<ClassBarDataPoint>();

        drawMarks = true;
        BackColorGeneral = Color.WHITE;
        BackColorChartArea = Color.WHITE;
        GridColor = new Color(48,48,48);
        XPointIntervalColor = new Color(0.95f,0.95f,0.0f,0.6f); // green and transparent
        XPointIntervalColorGray = new Color(0.85f,0.85f,0.85f,0.6f); // light gray transparent
        XTickColor = Color.BLUE;
        AxisName = "X axis";

        DeduceClientSize = true;
    }


    public ClassBarChart(int GraphicsWidth, int GraphicsHeight) {
        
        this();
        clientWidth = GraphicsWidth;
        clientHeight = GraphicsHeight;
        DeduceClientSize = false;
    }


    public void paintChart(Graphics graphics) {
        
        if (XPoint == null)
            return;

        // canvas size, to be used also by other methods
        if (DeduceClientSize) {
            Rectangle rect = graphics.getClipBounds();
            clientWidth = rect.width;
            clientHeight = rect.height;
        }

        // definition of chartarea
        ChartArea = new Rectangle(15, 15, clientWidth-30, clientHeight-30);

        // color of the background
        graphics.setColor(BackColorGeneral);
        graphics.fillRect(0, 0, clientWidth, clientHeight);
        graphics.setColor(BackColorChartArea);
        graphics.fillRect(ChartArea.x, ChartArea.y, ChartArea.width, ChartArea.height);

        // Calculates min and max values
        MinXVal = XPoint.getCoordinates().X;
        MaxXVal = XPoint.getCoordinates().X;
        if (MinXVal > XPoint.getAuxCoordinates().X)
            MinXVal = XPoint.getAuxCoordinates().X;
        if (MaxXVal < XPoint.getAuxCoordinates().X)
            MaxXVal = XPoint.getAuxCoordinates().X;
        if (ThresholdPoints.size()>0)
            for (int i=0; i<ThresholdPoints.size(); i++) {
                if (MinXVal > ThresholdPoints.get(i).getCoordinates().X)
                    MinXVal = ThresholdPoints.get(i).getCoordinates().X;
                if (MaxXVal < ThresholdPoints.get(i).getCoordinates().X)
                    MaxXVal = ThresholdPoints.get(i).getCoordinates().X;
            }
        MinXVal -= (MaxXVal-MinXVal)/8;
        MaxXVal += (MaxXVal-MinXVal)/4;

        AxisLineY = ChartArea.y + ChartArea.height/2;

        // draws class bar
        DrawAxis(graphics);

        // draws ticks
        if (ThresholdPoints.size()>0)
            for (ClassBarDataPoint point : ThresholdPoints) {
                point.getCoordinates().canvasX = ScaleXPoint(point.getCoordinates().X);
                DrawTick(point, graphics);
            }

        // draws point
        XPoint.getCoordinates().canvasX = ScaleXPoint(XPoint.getCoordinates().X);
        XPoint.getAuxCoordinates().canvasX = ScaleXPoint(XPoint.getAuxCoordinates().X);
        DrawXPoint(XPoint, graphics);
    }

    // PRIVATE METHODS AND FUNCTIONS

    /**
     * Scale a x point to data metric to final canvas metric
     */
    private int ScaleXPoint(double x){

        x = (x - MinXVal) / (MaxXVal - MinXVal);

        x = ChartArea.x + (x * ChartArea.width);

        return (int) x;
    }

    // DRAWING METHODS
    private void DrawAxis(Graphics graphics){

        graphics.setColor(GridColor);

        int x0 = ChartArea.x;
        int x1 = x0 + ChartArea.width;
        AxisLineY = ChartArea.y + ChartArea.height/2;
        graphics.drawLine(x0, AxisLineY, x1, AxisLineY);

        // Axis name
        Font currentFont = new Font("serif", Font.PLAIN, 12);
        graphics.setFont(currentFont);
        graphics.setColor(Color.GRAY);
        FontMetrics fontMetrics = graphics.getFontMetrics(currentFont);
        int strX = x1 - fontMetrics.stringWidth(AxisName) -10;
        int strY = AxisLineY + fontMetrics.getHeight() +25;
        graphics.drawString(AxisName, strX, strY);

        double TickInterval = 0.0;
        if ((MaxXVal - MinXVal) < 2)
            TickInterval = 0.1;
        else if ((MaxXVal - MinXVal) < 5)
            TickInterval = 0.5;
        else
            TickInterval = 1.0;

        // Draw minor ticks
        int bufVal = (int) Math.ceil(MinXVal * 10.0);
        int bufInt = (int) Math.floor(TickInterval * 10.0);
        while((bufVal % bufInt) != 0)
            bufVal += 1;
        double curVal = bufInt / 10.0;
        double maxVal = (Math.floor(MaxXVal * 10.0) / 10.0);

        while (curVal <= maxVal){
            boolean WriteTickMark = true;
            for (ClassBarDataPoint thresholdPoint : ThresholdPoints) {
                double diff = Math.abs(curVal - thresholdPoint.getCoordinates().X);
                if (diff < 0.09) {
                    WriteTickMark = false;
                    break;
                }
            }

            DecimalFormat df = new DecimalFormat("#.#");
            currentFont = new Font("serif", Font.PLAIN, 10);
            graphics.setFont(currentFont);
            fontMetrics = graphics.getFontMetrics(currentFont);

            graphics.setColor(GridColor);
            ClassBarDataPoint p = new ClassBarDataPoint(curVal, 0);
            p.getCoordinates().canvasX = ScaleXPoint(p.getCoordinates().X);
            int x = p.getCoordinates().canvasX;
            if (x>20) {
                int y0 = AxisLineY - 4;
                int y1 = AxisLineY + 4;
                graphics.drawLine(x,y0,x,y1);

                if (WriteTickMark) {
                    String strVal = df.format(curVal);
                    strX = x - fontMetrics.stringWidth(strVal)/2;
                    strY = AxisLineY - fontMetrics.getHeight() - 5;
                    graphics.drawString(strVal, strX, strY);
                }
            }
            curVal += TickInterval;

        }
    }

    private void DrawTick(ClassBarDataPoint point, Graphics graphics){

        graphics.setColor(GridColor);
        int x = point.getCoordinates().canvasX;
        int y0 = AxisLineY -15;
        int y1 = AxisLineY -15;
        graphics.drawLine(x, y0, x, y1);

        // Tick name
        Font CurFont = new Font("serif", Font.BOLD, 12);
        graphics.setFont(CurFont);
        graphics.setColor(GridColor);
        FontMetrics fontMetrics = graphics.getFontMetrics(CurFont);
        int strX = x - fontMetrics.stringWidth(point.getMark())/2;
        int strY = AxisLineY - fontMetrics.getHeight() - 15;
        graphics.drawString(point.getMark(), strX, strY);
        
    }

    private void DrawXPoint(ClassBarDataPoint point, Graphics graphics) {

        // checks if a proper confidence interval should be drawn
        boolean HasInterval = true;
        if (point.getCoordinates().X == point.getAuxCoordinates().X)
            HasInterval = false;

        // Draw confidence interval
        int cx0 = point.getCoordinates().canvasX;
        int cx1 = point.getAuxCoordinates().canvasX;
        int cy0 = AxisLineY - 10;
        int cy1 = AxisLineY + 10;
        if (HasInterval)
            graphics.setColor(XPointIntervalColor);
        else {
            graphics.setColor(XPointIntervalColorGray);
            cx1 = ChartArea.x + ChartArea.width;
        }
        graphics.fillRect(cx0, cy0, cx1-cx0, cy1-cy0);

        // Draw confidence interval tick
        if (HasInterval) {
            graphics.setColor(XTickColor);
            graphics.drawLine(cx1,cy0,cx1,cy1);
        }

        // Draw point tick
        graphics.setColor(XTickColor);
        int y0 = AxisLineY - 10;
        int y1 = AxisLineY + 10;
        int x = point.getCoordinates().canvasX;
        graphics.drawLine(x-1,y0,x-1,y1);
        graphics.drawLine(x,y0,x,y1);
        graphics.drawLine(x+1,y0,x+1,y1);

        // Draw point mark
        DecimalFormat df = new DecimalFormat("#.##");
        Font CurFont = new Font("serif", Font.BOLD, 14);
        graphics.setFont(CurFont);
        graphics.setColor(GridColor);
        FontMetrics fontMetrics = graphics.getFontMetrics(CurFont);

        String strVal = StringSelector.getString("chart_prediction");
        int strX = x - fontMetrics.stringWidth(strVal)/2;
        int strY = AxisLineY + fontMetrics.getHeight() + 15;
        graphics.drawString(strVal, strX, strY);

        strVal = df.format(point.getCoordinates().X);
        strX = x - fontMetrics.stringWidth(strVal)/2;
        strY = AxisLineY + 2 * fontMetrics.getHeight() + 15;
        graphics.drawString(strVal, strX, strY);

        if (HasInterval) {
            strVal = StringSelector.getString("chart_safe_prediction");
            strX = cx1 - fontMetrics.stringWidth(strVal)/2;
            strY = AxisLineY + fontMetrics.getHeight() + 15;
            graphics.drawString(strVal, strX, strY);

            strVal = df.format(point.getAuxCoordinates().X);
            strX = cx1 - fontMetrics.stringWidth(strVal)/2;
            strY = AxisLineY + 2 * fontMetrics.getHeight() + 15;
            graphics.drawString(strVal, strX, strY);
        }

    }





}
