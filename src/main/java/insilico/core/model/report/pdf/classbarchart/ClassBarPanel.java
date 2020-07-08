package insilico.core.model.report.pdf.classbarchart;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ClassBarPanel extends JPanel {

    public ClassBarChart classBarChart;

    public ClassBarPanel() {
        classBarChart = null;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (classBarChart != null)
            classBarChart.paintChart(g);
    }

}
