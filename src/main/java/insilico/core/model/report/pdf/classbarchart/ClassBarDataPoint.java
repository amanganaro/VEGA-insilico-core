package insilico.core.model.report.pdf.classbarchart;

/**
 * Data point class for ClassBarChart component
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ClassBarDataPoint {
    private ClassBarChartCoordinates coordinates;
    private ClassBarChartCoordinates auxCoordinates;
    private String mark;

    // CONSTRUCTORS
    public ClassBarDataPoint() {
        coordinates = new ClassBarChartCoordinates();
        auxCoordinates = new ClassBarChartCoordinates();
        mark = "";
    }

    public ClassBarDataPoint(double x, double y){
        this();
        coordinates.X = x;
        coordinates.Y = y;
    }

    public ClassBarDataPoint(double x, double y, String pointMark){
        this(x,y);
        mark = pointMark;
    }

    public ClassBarDataPoint(double x, double y, double xAux, double yAux){
        this(x,y);
        auxCoordinates.X = xAux;
        auxCoordinates.Y = yAux;
    }

    public ClassBarDataPoint(double x, double y, String PointMark, double xAux, double yAux) {
        this(x, y, xAux, yAux);
        mark=PointMark;
    }

    // GETTERS AND SETTERS


    public ClassBarChartCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ClassBarChartCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public ClassBarChartCoordinates getAuxCoordinates() {
        return auxCoordinates;
    }

    public void setAuxCoordinates(ClassBarChartCoordinates auxCoordinates) {
        this.auxCoordinates = auxCoordinates;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
