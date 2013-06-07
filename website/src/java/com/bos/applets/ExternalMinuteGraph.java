/*
 * Created on Feb 24, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.bos.applets;

import com.bos.art.logParser.broadcast.beans.ErrorStatBean;
import com.bos.art.logParser.broadcast.beans.ExternalAccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.delegate.ErrorStatDelegate;
import com.bos.art.logParser.broadcast.beans.delegate.ExternalAccessRecordsDelegate;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jgroups.Message;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExternalMinuteGraph extends JPanel implements ExternalAccessRecordsDelegate,ErrorStatDelegate {
    private static final double MILLI_RESOLUTION = 1000.0;
    private static final int THIRTY_SECONDS = 30 * 1000;

    private ExternalMinuteGraph instance = null;
    private TimeSeriesCollection dataset[];
    private ArrayList<ExternalMinuteGraph.TimeSeriesCompareWrapper> orderedSeries[];
    private HashMap<String,TimeSeries> machineMap = new HashMap();
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMddHHmm"); 

    class TimeSeriesCompareWrapper implements Comparator {

        private String name;
        private TimeSeries series;

        TimeSeriesCompareWrapper() {
            name = null;
            series = null;
        }

        TimeSeriesCompareWrapper(String machine, TimeSeries series) {
            this.name = machine;
            this.series = series;
        }

        TimeSeries getTimeSeries() {
            return series;
        }

        String getMachineName() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 41 * hash + (this.series != null ? this.series.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            return ((ExternalMinuteGraph.TimeSeriesCompareWrapper) o).getMachineName().equals(getMachineName());
        }

        public int compare(Object o1, Object o2) {
            return ((ExternalMinuteGraph.TimeSeriesCompareWrapper) o1).getMachineName().compareTo(((ExternalMinuteGraph.TimeSeriesCompareWrapper) o2).getMachineName());
        }
    }

    public ExternalMinuteGraph(
            String plot_bgcolor,
            String chart_bgcolor,
            java.awt.Image image, ImageIcon expandImage, ImageIcon homeImage, int[] Classifications,String []classificationTitlesArg) {
        instance = this;
        classificationTitles = classificationTitlesArg;

        this.plot_bgcolor = plot_bgcolor;
        this.chart_bgcolor = chart_bgcolor;
        this.image = image;
        this.classifications = Classifications;
        pagesServed = new TimeSeries[this.classifications.length];
        pagesServedHashMap = new HashMap[this.classifications.length];
        averageSeries = new TimeSeries[this.classifications.length];
        charts = new JFreeChart[this.classifications.length];
        dateAxis = new DateAxis[this.classifications.length];
        volumeRangeAxis = new NumberAxis[this.classifications.length];
        startTimeLong = System.currentTimeMillis();
        chartPanel = new ChartPanel[this.classifications.length];        
        popupMenu = new JPopupMenu[this.classifications.length];
        expandGraphItem = new javax.swing.JMenuItem[this.classifications.length];
        mainPanel = new JPanel[this.classifications.length];
        dataset = new TimeSeriesCollection[this.classifications.length];
        orderedSeries = new ArrayList[this.classifications.length];
        init();

    }

    /**
     * Creates a new application.
     */
    public void init() {
        setLayout(new GridLayout(4, 1, 5, 5));
        setBackground(Color.decode("#8D8D8D"));
        boolean isPricing = false;

        for (int i = 0; i < classifications.length; ++i) {
            if (classifications[i] == 1 || classifications[i] == 2 || classifications[i] == 3) {

                isPricing = true;
                if (maxVolumePricing == null) {
                    maxVolumePricing = new TimeSeries("");
                    maxVolumePricing.setMaximumItemAge(60);                   
                }

                if (maxAveragePricing == null) {
                    maxAveragePricing = new TimeSeries("");
                    maxAveragePricing.setMaximumItemAge(60);
                }

            }

            pagesServed[i] = new TimeSeries("Total Requests");
            pagesServed[i].setMaximumItemAge(60);
            pagesServedHashMap[i] = new HashMap<Date,HashMap>();

            averageSeries[i] = new TimeSeries("Average");
            averageSeries[i].setMaximumItemAge(60);

            dataset[i] = new TimeSeriesCollection();
            TimeSeriesCollection volumeDataSet = new TimeSeriesCollection();
            orderedSeries[i] = new ArrayList<ExternalMinuteGraph.TimeSeriesCompareWrapper>();

            if (isPricing) {
                // volumeDataSet.addSeries(maxVolumePricing);
                volumeDataSet.addSeries(pagesServed[i]);
            } else {
                volumeDataSet.addSeries(pagesServed[i]);
            }
            dataset[i].addSeries(averageSeries[i]);
            // create two series that automatically discard data more than 30 seconds old...

            dateAxis[i] = new DateAxis();
            dateAxis[i].setDateFormatOverride(new SimpleDateFormat("HH:mm"));
            dateAxis[i].setLowerMargin(0.0);
            dateAxis[i].setUpperMargin(0.0);
            dateAxis[i].setTickLabelsVisible(true);
            dateAxis[i].setLabelPaint(Color.white);
    		dateAxis[i].setTickLabelPaint(Color.white);
    		dateAxis[i].setLabelFont(new Font("Arial", Font.BOLD, 12));

            NumberAxis timeRangeAxis = new NumberAxis("Time (Sec)");
            timeRangeAxis.setLabelPaint(Color.white);
            timeRangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
            timeRangeAxis.setTickLabelPaint(Color.white);
            java.text.DecimalFormat format = new java.text.DecimalFormat("0.000");

            timeRangeAxis.setAutoRange(true);
            timeRangeAxis.setAutoTickUnitSelection(true);
            timeRangeAxis.setAutoRangeIncludesZero(true);
            timeRangeAxis.setNumberFormatOverride(format);
            timeRangeAxis.setLowerMargin(0.00);
            timeRangeAxis.setUpperMargin(0.2);
            timeRangeAxis.setLabelPaint(Color.white);
            timeRangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
            timeRangeAxis.setTickLabelPaint(Color.white);

            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);

            renderer.setToolTipGenerator(
                    new StandardXYToolTipGenerator(
                    "{0}: ({1}, {2})",
                    new SimpleDateFormat("HH:mm"),
                    new java.text.DecimalFormat("#0.000")));

//            renderer.setSeriesPaint(0, Color.black);
            renderer.setSeriesPaint(0, new Color(0, 143, 255));

            if (isPricing) {
                Range range = new Range(0.0, 1.50);

                timeRangeAxis.setRange(range, true, true);
            }

            // set the first color in the series after average
            renderer.setSeriesPaint(1, Color.magenta);

            renderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int j = 1; j < 10; j++) {
                renderer.setSeriesStroke(j, new BasicStroke(3f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 0, new float[]{10.0f, 5.0f}, 0));
            }

            XYBarRenderer renderer2 = new XYBarRenderer(0.20);
            renderer2.setShadowVisible(false);
            renderer2.setDrawBarOutline(false);
            renderer2.setMargin(0);

            renderer2.setToolTipGenerator(
                    new StandardXYToolTipGenerator(
                    "{0}: ({1}, {2}/min)",
                    new SimpleDateFormat("HH:mm"),
                    new java.text.DecimalFormat("#000")));

//            renderer2.setSeriesPaint(0, new Color(43, 217, 0));
            renderer2.setSeriesPaint(0, Color.decode("#979797"));
            renderer2.setSeriesOutlinePaint(0, Color.black);
            //
            //
            volumeRangeAxis[i] = new NumberAxis("Requests / Minute");
            volumeRangeAxis[i].setLabelPaint(Color.white);
            volumeRangeAxis[i].setLabelFont(new Font("Arial", Font.BOLD, 12));
            volumeRangeAxis[i].setTickLabelPaint(Color.white);
            java.text.DecimalFormat format2 = new java.text.DecimalFormat("#000");

            volumeRangeAxis[i].setNumberFormatOverride(format2);
            volumeRangeAxis[i].setUpperMargin(0.2); // to leave room for price line
            if (isPricing) {
                Range range = new Range(0.0, MILLI_RESOLUTION);

                volumeRangeAxis[i].setRange(range, true, true);
                volumeRangeAxis[i].setAutoRange(true);
            }

            XYPlot xyplot = new XYPlot(volumeDataSet, dateAxis[i], volumeRangeAxis[i], renderer2);

            xyplot.setBackgroundPaint(Color.black);
            
            xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);

            xyplot.setRenderer(1, renderer);
            xyplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
            xyplot.setRangeAxis(1, timeRangeAxis);
            xyplot.setDataset(1, dataset[i]);
            xyplot.mapDatasetToRangeAxis(1, 1);

            xyplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            xyplot.setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);

            //String title = classificationTitles[classifications[i]];
            String title = classificationTitles[i];            
            if (title == null) 
                title = "No Title";            
            
            TextTitle chartTitle = new TextTitle(title);
            chartTitle.setPaint(Color.white);
            chartTitle.setFont(new Font("Verdana", Font.BOLD, 20));            
            
            charts[i] = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, xyplot, false);
            charts[i].getPlot().setNoDataMessage("No data received yet...");
            charts[i].setTitle(chartTitle);
            charts[i].setBackgroundImage(image);
            charts[i].setBackgroundPaint(new Color(70, 70, 70));

            Calendar begin = Calendar.getInstance();
            Date beginDate = begin.getTime();

            begin.add(Calendar.HOUR_OF_DAY, -1);
            Date endDate = begin.getTime();

            dateAxis[i].setRange(endDate, beginDate);

            chartPanel[i] = new ChartPanel(charts[i]);

            //System.out.println("downArrowIcon: " + downArrowIcon );
            //chartPanel.setImageIcon( downArrowIcon );


            //StandardLegend legend = new StandardLegend();
            //legend.setAnchor(Legend.WEST);
            LegendTitle legend = new LegendTitle(charts[i].getPlot());
            charts[i].addLegend(legend);
            charts[i].getLegend().setFrame(new LineBorder());
            charts[i].getLegend().setBackgroundPaint(Color.decode("#CFCFCF"));
            charts[i].getLegend().setPosition(RectangleEdge.LEFT);
            

            popupMenu[i] = chartPanel[i].getPopupMenu();
            expandGraphItem[i] = new javax.swing.JMenuItem();
            expandGraphItem[i].setText("Maximize Graph");
            expandGraphItem[i].setActionCommand("ExpandGraph");

            popupMenu[i].add(expandGraphItem[i]);

            ExternalMinuteGraph.SymPopupMenu lSymPopupMenu = new ExternalMinuteGraph.SymPopupMenu();
            popupMenu[i].addPopupMenuListener(lSymPopupMenu);

            ExternalMinuteGraph.SymAction lSymAction = new ExternalMinuteGraph.SymAction();
            expandGraphItem[i].addActionListener(lSymAction);

            mainPanel[i] = new JPanel();
            mainPanel[i].setLayout(new BorderLayout(0, 0));

            mainPanel[i].add(BorderLayout.CENTER, chartPanel[i]);

            add(mainPanel[i]);

        }

    }

    public void didCompleteBagProcessing(org.jgroups.Message msg) {
    }

    public void didReceiveErrorStatBean(Message msg, TransferBean bean) {
        ErrorStatBean esb = (ErrorStatBean)bean;
      
        if (!isAutoRanging) {
            if (System.currentTimeMillis() - THIRTY_SECONDS > startTimeLong) {
                for (int i = 0; i < classifications.length; ++i) {
                    dateAxis[i].setAutoRange(true);
                    volumeRangeAxis[i].setAutoRange(true);
                }
                isAutoRanging = true;
            }
        }
        // System.out.println("got external access bean");
        try {
            Date volumeServedDate = new Date(esb.getEventTime());
            Minute minute = new Minute(volumeServedDate);

            int classify = esb.getClassification();

            for (int i = 0; i < classifications.length; ++i) {
                if (classifications[i] == classify) {
                   
                    // ********************************************

                    // do volume served calcs 
                    // ********************************************
                    HashMap currentMinHashMap = pagesServedHashMap[i].get(volumeServedDate);
                    //long volumeSnapShotNow = System.currentTimeMillis();
                    if (currentMinHashMap == null) {
                        currentMinHashMap = new HashMap();
                        pagesServedHashMap[i].put(volumeServedDate, currentMinHashMap);
                    }
                    Double minuteVolume = updateMinuteVolumeData(currentMinHashMap, esb);

                    pagesServed[i].addOrUpdate(minute, minuteVolume);
                    // ********************************************
                }

            }
        } catch (IllegalArgumentException pe) {
            System.out.println("AvgLoadTime.process Error parsing data received " + pe);
        }
    }

        
    /**
     * called by the receiver when objects are received we can then decide if we want to deal with this message or not
     *
     */
    public void didReceiveExternalAccessRecordsBean(org.jgroups.Message msg, TransferBean obj) {

        if (!isAutoRanging) {
            if (System.currentTimeMillis() - THIRTY_SECONDS > startTimeLong) {
                for (int i = 0; i < classifications.length; ++i) {
                    dateAxis[i].setAutoRange(true);
                    volumeRangeAxis[i].setAutoRange(true);
                }
                isAutoRanging = true;
            }
        }
        // System.out.println("got external access bean");
        try {
            ExternalAccessRecordsMinuteBean armb = (ExternalAccessRecordsMinuteBean) obj;
            Date volumeServedDate = sdf.parseDateTime(armb.getTimeString()).toDate();
            Minute minute = new Minute(volumeServedDate);

            int classify = armb.getClassificationID();

            for (int i = 0; i < classifications.length; ++i) {
                if (classifications[i] == classify) {

                    // do average series calcs
                    // ********************************************
                    Double runningAvg = (Double) averageSeries[i].getValue(minute);

                    if (runningAvg == null) {
                        runningAvg = new Double(((double) armb.getAverageLoadTime()) / MILLI_RESOLUTION);
                    }

                    Double curravg = new Double(((double) armb.getAverageLoadTime()) / MILLI_RESOLUTION);
                    runningAvg = new Double((runningAvg.doubleValue() + curravg.doubleValue()) / 2);

                    averageSeries[i].addOrUpdate(minute, runningAvg);
                    // ********************************************

                    // do 90 percent calcs
                    // ********************************************
                    TimeSeries i90perc = machineMap.get(armb.getMachine() + classifications[i]);

                    if (i90perc == null) {
                        i90perc = new TimeSeries(formatMachine(armb.getMachine()));
                        i90perc.setMaximumItemAge(60);

                        machineMap.put(armb.getMachine() + classifications[i], i90perc);
                        orderedSeries[i].add(new ExternalMinuteGraph.TimeSeriesCompareWrapper(armb.getMachine(), i90perc));

                        //System.out.println("orderSeries size: " + orderedSeries[i].size());

                        try {
                            // first remove all the series that were added dynamically
                            // then re-add them in a sorted fashion
//                            for (int j = 0, tot = orderedSeries[i].size(); j < tot; j++) {
//                                dataset[i].removeSeries(((TimeSeriesCompareWrapper) orderedSeries[i].get(j)).getTimeSeries());
//                            }
                            for(ExternalMinuteGraph.TimeSeriesCompareWrapper wrapper:orderedSeries[i]) {
                                dataset[i].removeSeries(wrapper.getTimeSeries());
                            }

                            Collections.sort(orderedSeries[i], new ExternalMinuteGraph.TimeSeriesCompareWrapper());

//                            for (int j = 0, tot = orderedSeries[i].size(); j < tot; j++) {
//                                dataset[i].addSeries(((TimeSeriesCompareWrapper) orderedSeries[i].get(j)).getTimeSeries());
//                            }
                            for(ExternalMinuteGraph.TimeSeriesCompareWrapper wrapper:orderedSeries[i]) {
                                dataset[i].addSeries(wrapper.getTimeSeries());
                            }

                        } catch (Throwable t) {
                            t.printStackTrace();
                        }

                    }

                    //System.out.println("armb.90% "+(double)armb.getI90Percentile());
                    Double curr90pct = new Double(((double) armb.getI90Percentile()) / MILLI_RESOLUTION);
                    i90perc.removeAgedItems(false);
                    i90perc.addOrUpdate(minute, curr90pct);
                                        

                    // ********************************************

                    // do volume served calcs 
                    // ********************************************
                    HashMap<String,Double> currentMinHashMap = pagesServedHashMap[i].get(volumeServedDate);
                    //long volumeSnapShotNow = System.currentTimeMillis();
                    if (currentMinHashMap == null) {
                        currentMinHashMap = new HashMap<String,Double>();
                        pagesServedHashMap[i].put(volumeServedDate, currentMinHashMap);
                    }
                    Double minuteVolume = updateMinuteVolumeData(currentMinHashMap, armb);
                    
                    pagesServed[i].addOrUpdate(minute, minuteVolume);
                    // ********************************************
                }

            }
            for(TimeSeries series:pagesServed) {
                series.removeAgedItems(false);
            }
            for(TimeSeries series:averageSeries) {
                series.removeAgedItems(false);
            }
        } catch (IllegalArgumentException pe) {
            System.out.println("AvgLoadTime.process Error parsing data received " + pe);
        }
    }

    private String formatMachine(String machine) {
        int pos = machine.indexOf("-");
        if (pos != -1) {
            pos = machine.indexOf("-", pos + 1);
            if (pos != -1) {
                return machine.substring(pos + 1) + "-90%";
            } else {
                return machine;
            }
        } else {
            return machine;
        }
    }

    private Double updateMinuteVolumeData(HashMap<String,Double> currentMinHashMap, ErrorStatBean armb) {
        String machine = armb.getServer();
        Double volume = Double.parseDouble(armb.getValue());
        currentMinHashMap.put(machine, volume);
        return getVolumeForMinute(currentMinHashMap);
    }
    
    private Double updateMinuteVolumeData(HashMap<String,Double> currentMinHashMap, ExternalAccessRecordsMinuteBean armb) {
        String machine = armb.getMachine();
        Double volume = new Double((double) armb.getTotalUsers());
        currentMinHashMap.put(machine, volume);
        return getVolumeForMinute(currentMinHashMap);
    }

    private Double getVolumeForMinute(HashMap<String,Double> currentMinHashMap) {
        Double runningTotal = new Double(0.0);
        for(String machine:currentMinHashMap.keySet()) {
            Double val = (Double) currentMinHashMap.get(machine);
            if (val != null) {
                runningTotal = new Double(runningTotal.doubleValue() + val.doubleValue());
            }
        }
        return runningTotal;
    }

    class SymPopupMenu implements PopupMenuListener {

        public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            Object object = event.getSource();
            for (int i = 0; i < classifications.length; i++) {

                if (object == popupMenu[i]) {
                    popupMenu_popupMenuWillBecomeVisible(event);
                }
            }
        }

        public void popupMenuCanceled(PopupMenuEvent event) {
        }
    }

    private void popupMenu_popupMenuWillBecomeVisible(PopupMenuEvent event) {
        Object object = event.getSource();
        for (int i = 0; i < classifications.length; i++) {
            if (object == popupMenu[i]) {
                if (mainPanel[i].getComponentCount() == 0) {
                    expandGraphItem[i].setText("Remove Graph");
                    expandGraphItem[i].setActionCommand("RemoveGraph");
                } else {
                    expandGraphItem[i].setText("Maximize Graph");
                    expandGraphItem[i].setActionCommand("ExpandGraph");
                }
                break;
            }
        }
    }

    class SymAction implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            Object object = event.getSource();

            for (int i = 0; i < classifications.length; i++) {
                if (object == expandGraphItem[i]) {
                    if (event.getActionCommand().equals("ExpandGraph")) {

                        GraphingFrame.getInstance().addGraphPanel(chartPanel[i], mainPanel[i]);
                        GraphingFrame.getInstance().show();

                    } else if (event.getActionCommand().equals("RemoveGraph")) {
                        GraphingFrame.getInstance().returnPanel(mainPanel[i]);
                        mainPanel[i].updateUI();
                    }
                    break;
                }

            }
        }
    }

    void expand_actionPerformed(java.awt.event.ActionEvent event) {// to do: code goes here.
    }
    
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    private String plot_bgcolor;
    private String chart_bgcolor;
    private java.awt.Image image;
    private int[] classifications;
    private JFreeChart[] charts;
    private TimeSeries[] pagesServed;
    private HashMap<Date,HashMap>[] pagesServedHashMap;
    private TimeSeries[] averageSeries;
    private TimeSeries maxAveragePricing;
    private TimeSeries maxVolumePricing;
    private String[] classificationTitles;
    private long startTimeLong;
    private boolean isAutoRanging = false;
    private DateAxis[] dateAxis;
    private NumberAxis[] volumeRangeAxis;
    private JPanel mainPanel[];
    private ChartPanel chartPanel[];
    private JPopupMenu popupMenu[];
    private JMenuItem expandGraphItem[];
}

