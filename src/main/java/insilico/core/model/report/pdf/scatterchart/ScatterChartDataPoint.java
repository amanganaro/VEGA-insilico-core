package insilico.core.model.report.pdf.scatterchart;

/**
 * Data Point class for Scatter Chart component
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ScatterChartDataPoint {

    private ScatterChartCoordinates coordinates;
    private ScatterChartCoordinates auxCoordinates;
    private String mark;
    

    public ScatterChartDataPoint() {
        coordinates = new ScatterChartCoordinates();
        auxCoordinates = new ScatterChartCoordinates();
        mark="";
    }

    public ScatterChartDataPoint(double x, double y) {
        this();
        coordinates.X = x; coordinates.Y = y;
    }

    public ScatterChartDataPoint(double x, double y, String PointMark) {
        this(x, y);
        mark=PointMark;
    }

    public ScatterChartDataPoint(double x, double y, double xAux, double yAux) {
        this(x, y);
        auxCoordinates.X = xAux; auxCoordinates.Y = yAux;
    }

    public ScatterChartDataPoint(double x, double y, String PointMark, double xAux, double yAux) {
        this(x, y, xAux, yAux);
        mark=PointMark;
    }


    public ScatterChartCoordinates GetCoordinates() {
        return coordinates;
    }

    public void SetCoordinates(ScatterChartCoordinates Coord) {
        coordinates = Coord;
    }

    public ScatterChartCoordinates GetAuxCoordinates() {
        return auxCoordinates;
    }

    public void SetAuxCoordinates(ScatterChartCoordinates Coord) {
        auxCoordinates = Coord;
    }

    public String GetMark() {
        return mark;
    }


    public void SetMark(String PointMark) {
        mark = PointMark;
    }

}
