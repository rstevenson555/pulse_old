/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.object-refinery.com/jfreechart/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ----------------
 * MemoryUsage.java
 * ----------------
 * (C) Copyright 2002, 2003, by Object Refinery Limited and Contributors.
 *
 * Original Author:  Tony Bianchini;
 * Contributor(s):   David Gilbert;
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Sep-2002 : Version 1, based on code by Tony Bianchini (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 16-Oct-2002 : Removed redundant attributes (DG);
 * 18-Oct-2002 : Moved to com.jrefinery.chart.demo.premium package (DG);
 * 25-Apr-2003 : Updated for JFreeChart 0.9.8, and moved to com.jrefinery.chart.demo package (DG);
 *
 */
package com.bos.applets;

import com.bos.applets.arch.AppletMessageListener;
import com.bos.art.logParser.broadcast.beans.AccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.delegate.AccessRecordsDelegate;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jgroups.Message;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A demo application showing a dynamically updated chart that displays the current JVM memory usage.
 *
 * @author Bryce Alcock
 * @author Bob Stevenson
 * @author Will Webb
 */
public class AvgLoadTime extends JApplet implements AccessRecordsDelegate {
    private static final double MILLI_RESOLUTION = 1000.0;
    private static final int SIXTY_MINUTES = 60;
    private static final int THIRTY_SECONDS = 30 * 1000;
    private static final int VIEWABLE_MINUTES =  SIXTY_MINUTES;

    private JFreeChart chart = null;
    private JApplet applet = null;
    private int timeOffset = -60; // in minutes
    private long startTimeLong;
    private boolean isAutoRanging = false;
    private ChartPanel chartPanel;
    private DateAxis dateAxis = null;
    private NumberAxis requestVolumeRangeAxis;
    private NumberAxis timeRangeAxis;
    private javax.swing.JMenuItem expandGraphItem = new javax.swing.JMenuItem();
    private JPopupMenu popupMenu;
    /**
     * Time series for total memory used.
     */
    private TimeSeries requestVolumeServedSeries;
    private Map<Minute,Double> movingVolumeAverage;
    private Map<Minute,Double> movingAverage;

    /**
     * Time series for free memory.
     */
    private TimeSeries averageSeries;
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMddHHmm");
    private JPanel mainPanel = null;
    private ImageIcon downArrowIcon = null;
    private int precisionView = Calendar.MINUTE;
    private AvgLoadTime instance = null;
    private TimeSeriesCollection dataset = new TimeSeriesCollection();
    private Map<String,TimeSeries> machineMap = new HashMap<String,TimeSeries>();
    private int MAX_HISTORY_TO_KEEP = 1440; // 24 hours
    private ScrollableChartPanel scrollableChartPanel;
    private boolean realtime = true;
    private InstanceStatsGraph instanceStatsGraph;

    /**
     * this map is used to remove duplicate records
     * @param <K>
     * @param <V>
     */
    static private class LRUMap<K, V> extends LinkedHashMap<K, V>
    {
      private int maxCapacity;
      public LRUMap(int maxCapacity)
      {
            super(0, 0.75F, true);
            this.maxCapacity = maxCapacity;
      }
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
      {
            return size() >= this.maxCapacity;
      }
    }
    private LRUMap<MinuteMachineKey,Double> requestsServedMap = new LRUMap(10000);
    private LRUMap<MinuteMachineKey,Double> averageSeriesMap = new LRUMap(10000);
    private LRUMap<MinuteMachineKey,Double> ninetySeriesMap = new LRUMap(10000);

    private void initializeAxisRange() {

        DateTime beginDate = new DateTime(), endDate = new DateTime();
        beginDate = beginDate.minusMinutes(60);

        dateAxis.setRange(beginDate.withSecondOfMinute(0).toDate(), endDate.toDate());
    }

    class ScrollableChartPanel extends JPanel implements ChangeListener
    {
        private JSlider slider;
        private DateAxis axis;

         ScrollableChartPanel(DateAxis axis) {
            setLayout(new BorderLayout(0, 0));
            this.axis = axis;

            slider = new JSlider(-MAX_HISTORY_TO_KEEP, 0, 0);
            slider.setPaintLabels(true);
            slider.setMinorTickSpacing(30);
            slider.setMajorTickSpacing(60);
            slider.setPaintTicks(true);
            //slider.setSnapToTicks(true);
            slider.addChangeListener(this);
            Map<Integer,JLabel> sliderLabelMap = slider.createStandardLabels(60);
            Map<Integer,JLabel> newSliderLabelMap = new Hashtable();
            for(Map.Entry<Integer,JLabel> entry:sliderLabelMap.entrySet()) {
                JLabel cc = entry.getValue();
                Integer value = entry.getKey();

                int positiveValue = value.intValue() * -1;
                JLabel label = new JLabel(String.valueOf(positiveValue/60)+ "h");
                label.setForeground(Color.white);
                label.setFont(new Font("Verdana", Font.PLAIN, 8));
                label.setSize( label.getPreferredSize() );
                newSliderLabelMap.put(value,label);
            }
            slider.setLabelTable(new Hashtable(newSliderLabelMap));
            slider.setPaintLabels(true);
            slider.setBackground(Color.black);

            // add the slider to the south
            add(BorderLayout.SOUTH, slider);

         }

                    /**
         * Handles a state change event.
         *
         * @param event the event.
         */
        public void stateChanged(ChangeEvent event) {
            int value = slider.getValue();
            // value is in minutes

            DateTime begin = new DateTime(), end = new DateTime();
            if (value > 0) {
                //end = new DateTime().plusMinutes(60).plusMinutes((value ));
                //begin = end.minusMinutes(60);
            } else if ( value <0) {
                realtime = false;
                //System.out.println("value: " + (value * -1));
                end = end.minusMinutes( (value *-1));
                begin = end.minusMinutes( 60 );
            } else if ( value == 0) {
                realtime = true;
                begin = begin.minusMinutes(60);

            }
            // the range moves so this should not be fixed
            dateAxis.setRange(begin.withSecondOfMinute(0).toDate(), end.toDate());
        }
    }

    /**
     * Creates a new application.
     */
    public JPanel getMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        ChartFactory.setChartTheme(StandardChartTheme.createDarknessTheme());

        instance = this;

        // create two series that automatically discard data more than 30 seconds old...
        requestVolumeServedSeries = new TimeSeries("Total Pages");
        requestVolumeServedSeries.setMaximumItemAge(MAX_HISTORY_TO_KEEP);
        averageSeries = new TimeSeries("Average");
        averageSeries.setMaximumItemAge(MAX_HISTORY_TO_KEEP);

        movingVolumeAverage = new LRUMap(SIXTY_MINUTES);
        movingAverage = new LRUMap(SIXTY_MINUTES);

        TimeSeriesCollection volumeDataSet = new TimeSeriesCollection();

        volumeDataSet.addSeries(requestVolumeServedSeries);
        dataset.addSeries(averageSeries);

        dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        dateAxis.setLowerMargin(0.0);
        dateAxis.setUpperMargin(0.0);
        dateAxis.setTickLabelsVisible(true);
        dateAxis.setLabelPaint(Color.white);
        dateAxis.setTickLabelPaint(Color.white);
        dateAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));

        timeRangeAxis = new NumberAxis("Time (Sec)");
        timeRangeAxis.setLabelPaint(Color.white);
        timeRangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
        timeRangeAxis.setTickLabelPaint(Color.white);
        java.text.DecimalFormat format = new java.text.DecimalFormat("0.000");
        timeRangeAxis.setAutoRange(true);
        timeRangeAxis.setAutoTickUnitSelection(true);
        timeRangeAxis.setAutoRangeIncludesZero(true);
        timeRangeAxis.setNumberFormatOverride(format);
        timeRangeAxis.setLowerMargin(0.00);
        timeRangeAxis.setLowerMargin(0.20);
        timeRangeAxis.setUpperMargin(1.5);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setToolTipGenerator(
                new StandardXYToolTipGenerator(
                "{0}: ({1}, {2})",
                new SimpleDateFormat("HH:mm"),
                new java.text.DecimalFormat("#0.000")));

//        renderer.setSeriesPaint(0, Color.black);
        //renderer.setSeriesPaint(0, new Color(0, 143, 255));
        renderer.setSeriesPaint(0, Color.magenta);
        renderer.setSeriesStroke(0, new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        //renderer.setSeriesPaint(1, Color.magenta);
        for (int j = 1; j < 30; j++) {
            renderer.setSeriesStroke(j, new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 0, new float[]{10.0f, 5.0f}, 0));
        }

        requestVolumeRangeAxis = new NumberAxis("Page Volume");
        requestVolumeRangeAxis.setLabelPaint(Color.white);
        requestVolumeRangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
        requestVolumeRangeAxis.setTickLabelPaint(Color.white);
        java.text.DecimalFormat format2 = new java.text.DecimalFormat("#000");
        requestVolumeRangeAxis.setNumberFormatOverride(format2);
        requestVolumeRangeAxis.setUpperMargin(0.20);  // to leave room for price line

        XYBarRenderer renderer2 = new XYBarRenderer(0.20);
        renderer2.setShadowVisible(false);
        renderer2.setDrawBarOutline(false);
        renderer2.setMargin(0);

        renderer2.setToolTipGenerator(
                new StandardXYToolTipGenerator(
                "{0}: ({1}, {2}/min)",
                new SimpleDateFormat("HH:mm"),
                new java.text.DecimalFormat("#000")));

//        renderer2.setSeriesPaint(0, new Color(43, 217, 0));
        renderer2.setSeriesPaint(0, Color.decode("#979797"));
        renderer2.setSeriesOutlinePaint(0, Color.black);

        XYPlot xyplot = new XYPlot(volumeDataSet, dateAxis, requestVolumeRangeAxis, renderer2);
        String plot_bgcolor = "#000000";
        xyplot.setBackgroundPaint(Color.decode(plot_bgcolor));
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);

        xyplot.setRangeAxis(1, timeRangeAxis); // 1 is left axis,
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);
        xyplot.setDataset(1, dataset);
        xyplot.mapDatasetToRangeAxis(1, 1);
        xyplot.setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);

        xyplot.setRenderer(1, renderer);

        xyplot.setDatasetRenderingOrder(org.jfree.chart.plot.DatasetRenderingOrder.FORWARD);

        chart = new JFreeChart(
                "",
                JFreeChart.DEFAULT_TITLE_FONT,
                xyplot,
                false);

        TextTitle chartTitle = new TextTitle("Page Execution");
        chartTitle.setPaint(Color.white);
        chartTitle.setFont(new Font("Verdana", Font.BOLD, 20));
        chart.setTitle(chartTitle);

        chart.getPlot().setNoDataMessage("No data received yet...");

        try {
            String webCodeBase = getCodeBase().toString();

            MediaTracker mt = new MediaTracker(this);
            java.awt.Image image = getImage(new java.net.URL(getCodeBase() + getParameter("plot_image")));
            mt.addImage(image, 0);
            try {
                mt.waitForAll();
            } catch (InterruptedException ie) {
                ;
            }
            //chart.setBackgroundImage(image);
        } catch (java.net.MalformedURLException mfue) {
            System.err.println(mfue);
        }

        //chart.setBackgroundPaint(new Color(70, 70, 70));
        //mainPanel.setBackground(new Color(70, 70, 70));
        chart.setBackgroundPaint(Color.black);

        chartPanel = new ChartPanel(chart);
        chartPanel.setLayout(new BorderLayout(5, 5));
        this.chartPanel.setDomainZoomable(true);
        this.chartPanel.setRangeZoomable(true);

        popupMenu = chartPanel.getPopupMenu();
        expandGraphItem.setText("Maximize Graph");
        expandGraphItem.setActionCommand("ExpandGraph");

        popupMenu.add(expandGraphItem);

        AvgLoadTime.SymPopupMenu lSymPopupMenu = new AvgLoadTime.SymPopupMenu();
        popupMenu.addPopupMenuListener(lSymPopupMenu);

        AvgLoadTime.SymAction lSymAction = new AvgLoadTime.SymAction();
        expandGraphItem.addActionListener(lSymAction);

        scrollableChartPanel = new ScrollableChartPanel(dateAxis);
        instanceStatsGraph = new InstanceStatsGraph();

        //mainPanel.add(BorderLayout.CENTER, chartPanel);
        scrollableChartPanel.add(BorderLayout.CENTER, chartPanel);
        //mainPanel.setLayout(new BorderLayout(5,5));
        mainPanel.setLayout(new GridLayout(1,2));
        mainPanel.add(scrollableChartPanel);
        mainPanel.add(instanceStatsGraph);
        mainPanel.validate();

        LegendTitle legend = new LegendTitle(chart.getPlot());
        chart.addLegend(legend);
        chart.getLegend().setFrame(new LineBorder());
        chart.getLegend().setBackgroundPaint(Color.decode("#CFCFCF"));

        chart.getLegend().setPosition(RectangleEdge.LEFT);

        return mainPanel;
    }

    class SymPopupMenu implements PopupMenuListener {

        public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            Object object = event.getSource();
            if (object == popupMenu) {
                popupMenu_popupMenuWillBecomeVisible(event);
            }
        }

        public void popupMenuCanceled(PopupMenuEvent event) {
        }
    }

    private void popupMenu_popupMenuWillBecomeVisible(PopupMenuEvent event) {
        if (mainPanel.getComponentCount() == 0) {
            expandGraphItem.setText("Remove Graph");
            expandGraphItem.setActionCommand("RemoveGraph");
        } else {
            expandGraphItem.setText("Maximize Graph");
            expandGraphItem.setActionCommand("ExpandGraph");
        }

    }

    /**
     * get's called after we receive a beanbag of data ie from history
     *
     */
    public void didCompleteBagProcessing(Message msg) {
    }

    private void adjustDateRange() {
        //for(DateAxis da:dateAxis) {
            DateTime begin = new DateTime().plusMinutes(1);
            DateTime end = begin.minusMinutes(60);
            // the range moves so this should not be fixed
            dateAxis.setRange(end.withSecondOfMinute(0).toDate(), begin.withSecondOfMinute(0).toDate());
        //}
    }

    class MinuteMachineKey
    {
        String machine;
        String instance;
        Minute minute;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MinuteMachineKey that = (MinuteMachineKey) o;

            if (instance != null ? !instance.equals(that.instance) : that.instance != null) return false;
            if (machine != null ? !machine.equals(that.machine) : that.machine != null) return false;
            if (minute != null ? !minute.equals(that.minute) : that.minute != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = machine != null ? machine.hashCode() : 0;
            result = 31 * result + (instance != null ? instance.hashCode() : 0);
            result = 31 * result + (minute != null ? minute.hashCode() : 0);
            return result;
        }
    }


    /**
     * called by the receiver when objects are received we can then decide if we want to deal with this message or not
     *
     */
    public void didReceiveAccessRecordsBean(Message msg, TransferBean obj) {
        instanceStatsGraph.didReceiveAccessRecordsBean(msg,obj);

        if (!isAutoRanging) {
            if (System.currentTimeMillis() - 30 * 1000 > startTimeLong) {
                dateAxis.setAutoRange(true);
                isAutoRanging = true;
            }
        }
        if ( realtime) {
            adjustDateRange();
        }

        try {
            AccessRecordsMinuteBean armb = (AccessRecordsMinuteBean) obj;
            Date dpdate = sdf.parseDateTime(armb.getTimeString()).toDate();
            Minute minute = new Minute(dpdate);

            TimeSeries series = null;
            if ((series = (TimeSeries) machineMap.get("90 %ile")) == null) {
                series = new TimeSeries("90 %ile");
                series.setMaximumItemCount(MAX_HISTORY_TO_KEEP);
                machineMap.put("90 %ile", series);
                dataset.addSeries(series);
            }
            //series.addOrUpdate(minute, new Double(((double) armb.getI90Percentile()) / 1000.0));

            MinuteMachineKey minuteKey = new MinuteMachineKey();
            minuteKey.minute = minute;
            minuteKey.machine = armb.getMachine();
            minuteKey.instance = armb.getInstance();

            calc90PercentilePerMinute(armb, minute, series, minuteKey);

            Double runningAvg = calcAveragePerMinute(armb, minute, minuteKey);
            calcAndSetAverageAxisRange( minute, runningAvg);

            Double runningVolume = calcVolumePerMinute(armb, minute, minuteKey);
            calcAndSetVolumeAxisRange( minute, runningVolume);

        } catch (IllegalArgumentException pe) {
            System.out.println("AvgLoadTime.process Error parsing data received " + pe);
        }
    }

    private Double calcVolumePerMinute(AccessRecordsMinuteBean armb, Minute minute, MinuteMachineKey minuteKey) {
        Double total = updateMinuteVolumeData(armb, minute, minuteKey);
        requestVolumeServedSeries.addOrUpdate(minute, total);
        return total;
    }

    private Double calcAveragePerMinute(AccessRecordsMinuteBean armb, Minute minute, MinuteMachineKey minuteKey) {
        Double minuteAverage = updateMinuteAverageData(armb, minute, minuteKey);
        averageSeries.addOrUpdate(minute, minuteAverage);
        return minuteAverage;
    }

    private void calcAndSetVolumeAxisRange(Minute minute, Double minuteVolume) {
        movingVolumeAverage.put(minute,minuteVolume);

        Double volumeMaxValue = 0.0;
        for(Map.Entry<Minute,Double> entry: movingVolumeAverage.entrySet()) {
            volumeMaxValue = Math.max(volumeMaxValue,entry.getValue());
        }
        // add 2% so the chart does not go all the way to the top
        volumeMaxValue += (volumeMaxValue * 0.2);
        requestVolumeRangeAxis.setRange(0.0,volumeMaxValue);
        // ********************************************
    }

    private void calcAndSetAverageAxisRange(Minute minute, Double runningAvg) {
        movingAverage.put(minute,runningAvg);
        Double avgMaxValue = 0.0;
        for(Map.Entry<Minute,Double> entry: movingAverage.entrySet()) {
            avgMaxValue = Math.max(avgMaxValue,entry.getValue());
        }
        avgMaxValue += (avgMaxValue * .50);
        timeRangeAxis.setRange(0.0,avgMaxValue);
        // ********************************************
    }

    private Double calc90PercentilePerMinute(AccessRecordsMinuteBean armb, Minute minute, TimeSeries series, MinuteMachineKey minuteKey) {
        Double minute90Perc = updateMinute90PercentileData(armb, minute, minuteKey);
        series.addOrUpdate(minute, minute90Perc);
        return minute90Perc;
    }

    private Double updateMinuteVolumeData(AccessRecordsMinuteBean armb, Minute minute, MinuteMachineKey minuteKey) {
        Double num = (double)armb.getTotalUsers();
        requestsServedMap.put(minuteKey,num);
        // calc last 1 minutes
        Double total = getVolumeForMinute(minute);
        return total;
    }

    private Double updateMinuteAverageData(AccessRecordsMinuteBean armb, Minute minute, MinuteMachineKey minuteKey) {
        Double avg = (double) armb.getAverageLoadTime();
        averageSeriesMap.put(minuteKey,avg);
        Double minuteAverage = getAverageForMinute(minute);
        return minuteAverage;
    }

    private Double updateMinute90PercentileData(AccessRecordsMinuteBean armb, Minute minute, MinuteMachineKey minuteKey) {
        Double ninety = (double)armb.getI90Percentile();
        ninetySeriesMap.put(minuteKey,ninety);
        Double minute90Perc = get90PercentForMinute(minute);
        return minute90Perc;
    }

    private Double getAverageForMinute(Minute minute) {
        int count;// calc last 1 minutes
        Double avgTot = 0.0;
        count =1;
        for(Map.Entry<MinuteMachineKey,Double> val: averageSeriesMap.entrySet()) {
           if ( val.getKey().minute.equals(minute)) {
                avgTot += val.getValue();
                count++;
           }
        }
        avgTot /= count;
        Double minuteAverage = avgTot / 1000.0;
        return minuteAverage;
    }

    private Double getVolumeForMinute(Minute minute) {
        Double total = 0.0;
        for(Map.Entry<MinuteMachineKey,Double> val: requestsServedMap.entrySet()) {
           if ( val.getKey().minute.equals(minute)) {
            total += val.getValue();
           }
        }
        return total;
    }

    private Double get90PercentForMinute(Minute minute) {
        Double ninetyAvg = 0.0;
        int count =1;
        for(Map.Entry<MinuteMachineKey,Double> val: ninetySeriesMap.entrySet()) {
           if ( val.getKey().minute.equals(minute)) {
               //ninetyArray.add(val.getValue());
               ninetyAvg += val.getValue();
               count++;
           }
        }
        ninetyAvg /= count;
        Double minute90Perc = ninetyAvg / 1000.0;
        return minute90Perc;
    }

    class SymAction implements java.awt.event.ActionListener {

        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (event.getActionCommand().equals("ExpandGraph")) {

                GraphingFrame.getInstance().addGraphPanel(scrollableChartPanel, mainPanel);
                GraphingFrame.getInstance().show();

            } else if (event.getActionCommand().equals("RemoveGraph")) {
                GraphingFrame.getInstance().returnPanel(mainPanel);

                mainPanel.updateUI();
            }
        }
    }

    void expand_actionPerformed(java.awt.event.ActionEvent event) {
        // to do: code goes here.
    }

    public void AvgLoadTime() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    /**
     * Entry point for the sample application.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {

        JFrame frame = new JFrame("Memory Usage Demo");
        AvgLoadTime app = new AvgLoadTime();
        JPanel panel = app.getMainPanel();
        frame.getContentPane().setLayout(new BorderLayout(5,5));
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setBounds(200, 120, 600, 200);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }


    /*
     * (non-Javadoc) @see java.applet.Applet#init()
     */
    @Override
    public void init() {
        super.init();

        String webCodeBase = getCodeBase().toString();
        java.net.URL downArrowIconURL = null;
        try {
            downArrowIconURL = new java.net.URL(webCodeBase + "images/downarrow.gif");
            downArrowIcon = new ImageIcon(downArrowIconURL);
        } catch (Exception e) {
            System.err.println("Error loading image " + e);
        }

        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(BorderLayout.CENTER, getMainPanel());

        applet = this;

        initializeAxisRange();

        initGraphicsEnv();
    }

    /**
     * this applet is the main controller, so it starts the listener and stops it when exiting
     *
     */
    @Override
    public void start() {
        super.start();

        AppletMessageListener.getInstance().setAccessRecordsDelegate(this);
        startTimeLong = System.currentTimeMillis();
    }

    @Override
    public void stop() {
        AppletMessageListener.getInstance().stop();
        super.stop();
    }

    public GraphicsConfiguration initGraphicsEnv() {
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        return gc;
    }

}
