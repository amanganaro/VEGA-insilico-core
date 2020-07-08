package insilico.core.model.report.pdf.scatterchart;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ScatterChartPanel extends JPanel {

    public ScatterChart chart;

    public ScatterChartPanel() {
        chart = null;
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (chart != null)
            chart.paintChart(g);
    }
}
