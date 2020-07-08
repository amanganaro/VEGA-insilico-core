package insilico.core.model.report.pdf.scatterchart;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

/**
 * ScatterChart Component
 * @author Alberto Manganaro
 */
public class ScatterChart {

    public boolean DrawMarks;
    public boolean DrawGrids;
    public boolean DrawAxisLegend;

    public Color BackColorGeneral;
    public Color BackColorChartArea;
    public Color GridColor;

    public ArrayList<ScatterChartDataSet> DataSeries;
    public ScatterChartAxis ChartAxis;

    private int clientWidth;
    private int clientHeight;
    private Rectangle ChartArea;

    private boolean DeduceClientSize;

    // Constructors
    public ScatterChart() {
        DataSeries = new ArrayList<ScatterChartDataSet>();
        ChartAxis = new ScatterChartAxis();

        DrawMarks = true;
        DrawGrids = true;
        DrawAxisLegend = true;
        BackColorGeneral = new Color(0xFF, 0xFF, 0xDD);
        BackColorChartArea = Color.WHITE;
        GridColor = new Color(0xEE, 0xEE, 0xEE);

        DeduceClientSize = true;
    }


    public ScatterChart(int GraphicsWidth, int GraphicsHeight) {
        this();

        clientWidth = GraphicsWidth;
        clientHeight = GraphicsHeight;
        DeduceClientSize = false;
    }

    /**
     * Scale a X point from data metric to final canvas metric
     *
     */
    private int ScaleXPoint(double x) {

        // scaling from data metrics to relative chart metrics [0,1]
        x = (x - ChartAxis.GetMinX()) / (ChartAxis.GetMaxX() - ChartAxis.GetMinX());

        // from chart metrics to canvas metrics with the right offset
        x = ChartArea.getX() + (x * ChartArea.getWidth());

        return (int)x;
    }


    /**
     * Scale a Y point from data metric to final canvas metric
     *
     */
    private int ScaleYPoint(double y) {

        // scaling from data metrics to relative chart metrics [0,1]
        y = (y - ChartAxis.GetMinY()) / (ChartAxis.GetMaxY() - ChartAxis.GetMinY());

        // from chart metrics to canvas metrics with the right offset
        y = ChartArea.getY() + ((1-y) * ChartArea.getHeight());

        return (int)y;
    }


    public void paintChart(Graphics g) {

        if (DataSeries.size() == 0)
            return;

        // canvas size, to be used also by other methods
        if (DeduceClientSize) {
            Rectangle d = g.getClipBounds();
            clientWidth = d.width;
            clientHeight = d.height;
        }

        // definition of chartarea
        ChartArea = new Rectangle(45, 30, clientWidth-45-30, clientHeight-45-30);

        // color of the background
        g.setColor(BackColorGeneral);
        g.fillRect(0, 0, clientWidth, clientHeight);
        g.setColor(BackColorChartArea);
        g.fillRect(ChartArea.x, ChartArea.y, ChartArea.width, ChartArea.height);

        // draws axis
        DrawAxis(g);

        // draws all data series
        for (ScatterChartDataSet ds : DataSeries) {

            // transforms dataset points to canvas points
            TransformDatasetPoints(ds);

            // draws chart points (and marks if needed)
            DrawDataSetPoints(ds, g);

            if (DrawMarks)
                DrawDataSetMarks(ds, g);
        }

    }


    public void AdaptAxisScaleToDataset() {
        double minX=0, minY=0, maxX=0, maxY=0;
        boolean first=true;

        if (DataSeries != null) {

            for (ScatterChartDataSet ds : DataSeries) {
                if (ds != null) {
                    if (first) {
                        minX = ds.MinX;
                        maxX = ds.MaxX;
                        minY = ds.MinY;
                        maxY = ds.MaxY;
                        first = false;
                    } else {
                        if (ds.MinX < minX)
                            minX = ds.MinX;
                        if (ds.MaxX > maxX)
                            maxX = ds.MaxX;
                        if (ds.MinY < minY)
                            minY = ds.MinY;
                        if (ds.MaxY > maxY)
                            maxY = ds.MaxY;
                    }
                }
            }

            double lagX = 0;
            double diam = maxX-minX;
            if (diam < 1)
                lagX = 0.1;
            else if (diam < 10)
                lagX = 0.5;
            else if (diam < 50)
                lagX = 1;
            else
                lagX = 5;

            double lagY = 0;
            diam = maxY-minY;
            if (diam < 1)
                lagY = 0.1;
            else if (diam < 10)
                lagY = 0.5;
            else if (diam < 50)
                lagY = 1;
            else
                lagY = 5;

            ChartAxis.SetRangeX(minX - lagX, maxX + lagX);
            ChartAxis.SetRangeY(minY - lagY, maxY + lagY);
        }
    }


    private void TransformDatasetPoints(ScatterChartDataSet ds) {

        for (int i=0; i<ds.Count(); i++) {

            ScatterChartDataPoint p = ds.GetPoint(i);

            p.GetCoordinates().canvasX = ScaleXPoint(p.GetCoordinates().X);
            p.GetCoordinates().canvasY = ScaleYPoint(p.GetCoordinates().Y);
            if (ds.HasAuxiliaryPoints()) {
                p.GetAuxCoordinates().canvasX = ScaleXPoint(p.GetAuxCoordinates().X);
                p.GetAuxCoordinates().canvasY = ScaleYPoint(p.GetAuxCoordinates().Y);
            }

            ds.SetPoint(i, p);
        }
    }



    //// DRAWING of elements ///////////////////////////////////////////


    private void DrawDataSetPoints(ScatterChartDataSet ds, Graphics g) {

        if (ds == null) return;

        int size = ds.PointSize;
        if (((double)size % 2.0) != 0)
            size++;

        for (int i=0; i<ds.Count(); i++) {

            ScatterChartDataPoint p = ds.GetPoint(i);
            if (p == null) continue;

            if (ds.getDatasetType() == ScatterChartDataSet.DS_SCATTER_WITH_SPANNING) {
                g.setColor(ds.PointColor);
                g.drawLine(p.GetCoordinates().canvasX, p.GetCoordinates().canvasY,
                        p.GetAuxCoordinates().canvasX, p.GetAuxCoordinates().canvasY);
                g.setColor(Color.black);
                g.fillOval(p.GetAuxCoordinates().canvasX - 3,
                        p.GetAuxCoordinates().canvasY - 3, 6, 6);
            }

            if ((ds.getDatasetType() == ScatterChartDataSet.DS_SCATTER_WITH_SPANNING) ||
                    (ds.getDatasetType() == ScatterChartDataSet.DS_SCATTER)) {

                switch (ds.PointShape) {

                    case ScatterChartDataSet.SHAPE_CIRCLE_EMPTY:
                        g.setColor(ds.PointColor);
                        g.drawOval(p.GetCoordinates().canvasX - size/2,
                                p.GetCoordinates().canvasY - size/2, size, size);
                        break;

                    case ScatterChartDataSet.SHAPE_CIRCLE_FILLED:
                        g.setColor(ds.PointColorFilling);
                        g.fillOval(p.GetCoordinates().canvasX - size/2,
                                p.GetCoordinates().canvasY - size/2, size, size);
                        g.setColor(ds.PointColor);
                        g.drawOval(p.GetCoordinates().canvasX - size/2,
                                p.GetCoordinates().canvasY - size/2, size, size);
                        break;

                }

            }

            if (ds.getDatasetType() == ScatterChartDataSet.DS_BAR) {
                g.setColor(ds.PointColorFilling);
                g.fillRect(p.GetCoordinates().canvasX - size/2,
                        p.GetCoordinates().canvasY, size,
                        p.GetAuxCoordinates().canvasY - p.GetCoordinates().canvasY);
//                           (ChartArea.y + ChartArea.height) - p.GetCoordinates().canvasY);
                g.setColor(ds.PointColor);
                g.drawRect(p.GetCoordinates().canvasX - size/2,
                        p.GetCoordinates().canvasY, size,
//                           (ChartArea.y + ChartArea.height) - p.GetCoordinates().canvasY);
                        p.GetAuxCoordinates().canvasY - p.GetCoordinates().canvasY);
            }

        }
    }


    private void DrawDataSetMarks(ScatterChartDataSet ds, Graphics g) {

        if (ds == null) return;

        int offsetX = ds.PointSize/2 + 3;
        int offsetY = ds.PointSize/2 + 3;

        Font CurFont = new Font("serif", Font.PLAIN, ds.MarkSize);
        g.setFont(CurFont);
        g.setColor(ds.MarkColor);
        FontMetrics fm = g.getFontMetrics(CurFont);

        for (int i=0; i<ds.Count(); i++) {
            ScatterChartDataPoint p = ds.GetPoint(i);
            if (p == null) continue;
            if (ds.getDatasetType() == ScatterChartDataSet.DS_BAR) {
                offsetX = (-1) * (fm.stringWidth(p.GetMark()) / 2);
                offsetY = fm.getHeight();
            }
            g.drawString(p.GetMark(), p.GetCoordinates().canvasX + offsetX,
                    p.GetCoordinates().canvasY - offsetY);
        }

    }


    private void DrawAxis(Graphics g) {

        DrawGridMarksAndTicks(g);
        DrawXAxis(g);
        DrawYAxis(g);

    }


    private void DrawXAxis(Graphics g) {

        g.setColor(Color.BLACK);
        int x0 = ChartArea.x;
        int x1 = x0 + ChartArea.width;
        int y = ChartArea.y + ChartArea.height;
        g.drawLine(x0,y,x1,y);

        if (DrawAxisLegend) {
            Font CurFont = new Font("serif", Font.PLAIN, 11);
            g.setFont(CurFont);
            g.setColor(Color.BLUE);
            FontMetrics fm = g.getFontMetrics(CurFont);
            g.drawString(ChartAxis.GetNameX(), x1-fm.stringWidth(ChartAxis.GetNameX()),
                    y+6+2*fm.getHeight());
        }

    }


    private void DrawYAxis(Graphics g) {

        g.setColor(Color.BLACK);
        int y0 = ChartArea.y + ChartArea.height;
        int y1 = ChartArea.y;
        int x = ChartArea.x;
        g.drawLine(x,y0,x,y1);

        if (DrawAxisLegend) {
            Font CurFont = new Font("serif", Font.PLAIN, 11);
            g.setFont(CurFont);
            g.setColor(Color.BLUE);
            FontMetrics fm = g.getFontMetrics(CurFont);
            g.drawString(ChartAxis.GetNameY(), 10, fm.getHeight());
        }

    }


    private void DrawGridMarksAndTicks(Graphics g) {

        double CurTick;
        String CurTickStr;
        int w, h;

        DecimalFormatSymbols InternationalSymbols = new DecimalFormatSymbols();
        InternationalSymbols.setDecimalSeparator('.');
        DecimalFormat df_float = new DecimalFormat("0.#", InternationalSymbols);


        Font CurFont = new Font("serif", Font.PLAIN, 10);
        g.setFont(CurFont);
        FontMetrics fm = g.getFontMetrics(CurFont);
        h = fm.getHeight();

        for (int i=0; i<ChartAxis.GetTicksX().size(); i++) {
            CurTick = ChartAxis.GetTicksX().get(i);
            if (CurTick == (int)CurTick)
                CurTickStr = Integer.valueOf((int)CurTick).toString();
            else
                CurTickStr = df_float.format(CurTick);
            w = fm.stringWidth(CurTickStr);

            int y0 = ChartArea.y + ChartArea.height;
            int y1 = ChartArea.y;
            int x = ScaleXPoint(CurTick);

            if (DrawGrids) {
                if (CurTick == 0)
                    g.setColor(new Color(0x05, 0x05, 0x05));
                else
                    g.setColor(GridColor);
                g.drawLine(x,y0,x,y1);
            }

            g.setColor(Color.BLACK);
            g.drawLine(x,y0,x,y0+2);
            g.drawString(CurTickStr, x-(int)(w/2), y0+6+h);
        }

        for (int i=0; i<ChartAxis.GetTicksY().size(); i++) {
            CurTick = ChartAxis.GetTicksY().get(i);
            if (CurTick == (int)CurTick)
                CurTickStr = Integer.valueOf((int)CurTick).toString();
            else
                CurTickStr = df_float.format(CurTick);
            w = fm.stringWidth(CurTickStr);

            int x0 = ChartArea.x;
            int x1 = x0 + ChartArea.width;
            int y = ScaleYPoint(CurTick);

            if (DrawGrids) {
                if (CurTick == 0)
                    g.setColor(new Color(0xBA, 0xBA, 0xBA));
                else
                    g.setColor(GridColor);
                g.drawLine(x0,y,x1,y);
            }
            g.setColor(Color.BLACK);
            g.drawLine(x0-2,y,x0,y);
            g.drawString(CurTickStr, x0-6-w, y+(int)(h/2));

        }

    }


}
