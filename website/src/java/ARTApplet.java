import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.Timer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.DefaultXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
public class ARTApplet extends JApplet {
    /** Time series for total memory used. */
    private TimeSeries total;
    /** Time series for free memory. */
    private TimeSeries free;
    public ARTApplet() {
        // create two series that automatically discard data more than 30 seconds old...
        this.total = new TimeSeries("Total", Millisecond.class);
        this.total.setHistoryCount(30000);
        this.free = new TimeSeries("Free", Millisecond.class);
        this.free.setHistoryCount(30000);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(total);
        dataset.addSeries(free);
        DateAxis domain = new DateAxis("Time");
        NumberAxis range = new NumberAxis("Memory");
        XYPlot xyplot = new XYPlot(dataset, domain, range, new DefaultXYItemRenderer());
        xyplot.setBackgroundPaint(Color.black);
        xyplot.getRenderer().setSeriesPaint(0, Color.red);
        xyplot.getRenderer().setSeriesPaint(1, Color.blue);
        domain.setAutoRange(true);
        domain.setLowerMargin(0.0);
        domain.setUpperMargin(0.0);
        domain.setTickLabelsVisible(true);
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        JFreeChart chart = new JFreeChart("Memory Usage", JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPopupMenu(null);
        getContentPane().add(chartPanel);
        new ARTApplet.DataGenerator().start();
    }
    /**
    * Adds an observation to the ’total memory’ time series.
    *
    * @param y the total memory used.
    */
    private void addTotalObservation(double y) {
        total.add(new Millisecond(), y);
    }
    /**
    * Adds an observation to the ’free memory’ time series.
    *
    * @param y the free memory.
    */
    private void addFreeObservation(double y) {
        free.add(new Millisecond(), y);
    }
    /**
    * The data generator.
    */
    class DataGenerator extends Timer implements ActionListener {
        /**
        * Constructor.
        */
        DataGenerator() {
            super(100, null);
            addActionListener(this);
        }
        /**
        * Adds a new free/total memory reading to the dataset.
        *
        * @param event the action event.
        */
        public void actionPerformed(ActionEvent event) {
            long f = Runtime.getRuntime().freeMemory();
            long t = Runtime.getRuntime().totalMemory();
            addTotalObservation(t);
            addFreeObservation(f);
        }
    }
}
