package insilico.core.model.report.pdf.scatterchart;

import java.awt.*;
import java.util.ArrayList;

/**
 * Dataset class for Scatter Chart component
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ScatterChartDataSet {

    public final static short DS_SCATTER = 1;
    public final static short DS_SCATTER_WITH_SPANNING = 2;
    public final static short DS_BAR = 3;

    public final static short SHAPE_CIRCLE_EMPTY = 1;
    public final static short SHAPE_CIRCLE_FILLED = 2;

    private ArrayList<ScatterChartDataPoint> points;

    private short DatasetType;
    private boolean HasAuxPoints;


    public Color PointColor;
    public Color PointColorFilling;
    public Color MarkColor;
    public int PointSize;
    public int MarkSize;
    public short PointShape;

    public double MinX;
    public double MinY;
    public double MaxX;
    public double MaxY;

    public ScatterChartDataSet(short dsType) {

        points = new ArrayList<>();
        MinX = 0; MinY = 0; MaxX = 0; MaxY = 0;

        DatasetType = dsType;
        switch (dsType) {
            case DS_BAR:
            case DS_SCATTER_WITH_SPANNING:
                HasAuxPoints = true; break;
            case DS_SCATTER: HasAuxPoints = false; break;
        }

        PointColor = Color.red;
        PointSize = 4;
        PointShape = SHAPE_CIRCLE_EMPTY;
        PointColorFilling = PointColor;
        MarkColor = Color.black;
        MarkSize = 9;
    }

    public short getDatasetType() {
        return DatasetType;
    }

    public boolean HasAuxiliaryPoints(){
        return HasAuxPoints;
    }

    public int Count() {
        return points.size();
    }


    public ScatterChartDataPoint GetPoint(int index){
        if ((index >= 0) && (index < points.size()))
            return points.get(index);
        else
            return null;
    }

    public void SetPoint(int index, ScatterChartDataPoint point){
        if ((index >= 0) && (index < points.size())){
            points.set(index, point);
        }
    }

    public void Add(ScatterChartDataPoint point){
        if (DatasetType == DS_BAR){
            ScatterChartCoordinates auxCoord = new ScatterChartCoordinates();
            auxCoord.X = point.GetCoordinates().X;
            auxCoord.Y = 0;
            if(point.GetCoordinates().Y < 0){
                point.SetAuxCoordinates(point.GetCoordinates());
                point.SetCoordinates(auxCoord);
            } else {
                point.SetAuxCoordinates(auxCoord);
            }
        }

        points.add(point);
        CheckDatasetBoundaries();
    }

    public void CheckDatasetBoundaries(){

        if(points.size() == 0){
            MinX = 0; MinY = 0; MaxX = 0; MaxY = 0;
            return;
        }

        MaxX = points.get(0).GetCoordinates().X;
        MaxY = points.get(0).GetCoordinates().Y;
        MinX = points.get(0).GetCoordinates().X;
        MinY = points.get(0).GetCoordinates().Y;
        
        for(ScatterChartDataPoint point : points){
            if (point.GetCoordinates().X > MaxX) MaxX = point.GetCoordinates().X;
            if (point.GetCoordinates().X < MinX) MinX = point.GetCoordinates().X;
            if (point.GetCoordinates().Y > MaxY) MaxY = point.GetCoordinates().Y;
            if (point.GetCoordinates().Y < MinY) MinY = point.GetCoordinates().Y;
            if (HasAuxPoints) {
                if (point.GetAuxCoordinates().X > MaxX) MaxX = point.GetAuxCoordinates().X;
                if (point.GetAuxCoordinates().X < MinX) MinX = point.GetAuxCoordinates().X;
                if (point.GetAuxCoordinates().Y > MaxY) MaxY = point.GetAuxCoordinates().Y;
                if (point.GetAuxCoordinates().Y < MinY) MinY = point.GetAuxCoordinates().Y;
            } 
        }

    }




}
