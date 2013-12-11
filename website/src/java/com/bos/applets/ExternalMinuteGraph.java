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
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.jgroups.Message;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * @author I0360D3
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExternalMinuteGraph extends JPanel implements ExternalAccessRecordsDelegate,ErrorStatDelegate {
    private static final double MILLI_RESOLUTION = 1000.0;
    private static final int SIXTY_MINUTES = 60;
    private static final int THIRTY_SECONDS = 30 * 1000;
    private static final int VIEWABLE_MINUTES =  SIXTY_MINUTES;

    private ExternalMinuteGraph instance = null;
    private TimeSeriesCollection dataset[];
    private ArrayList<ExternalMinuteGraph.TimeSeriesCompareWrapper> orderedSeries[];
    private Map<String,TimeSeries> machineMap = new HashMap<String,TimeSeries>();
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMddHHmm");
    private int MAX_HISTORY_TO_KEEP = 1440; // 24 hours
    private ScrollableChartPanel scrollableChartPanel[];
    private boolean realtime = true;
    private String plot_bgcolor;
    private String chart_bgcolor;
    private java.awt.Image image;
    private int[] classifications;
    private JFreeChart[] charts;
    private TextTitle averageTitle[] = null, requestTitle[] = null;
    private TimeSeries[] requestVolumeServedSeries;
    private Map<Minute,Double>[] movingVolumeAverage;
    private Map<Minute,Double>[] movingAverage;
    private Map<Date,HashMap>[] requestsServedMap;
    private TimeSeries[] averageSeries;
    private String[] classificationTitles;
    private long startTimeLong;
    private boolean isAutoRanging = false;
    private DateAxis[] dateAxis;
    private NumberAxis[] timeRangeAxis;
    private NumberAxis[] requestVolumeRangeAxis;
    private JPanel mainPanel[];
    private ChartPanel chartPanel[];
    private JPopupMenu popupMenu[];
    private JMenuItem expandGraphItem[];
    private JMenuItem expandGraphItem2[];

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

    private void adjustDateRange() {
        DateTime begin = new DateTime().plusMinutes(1);
        DateTime end = begin.minusMinutes(VIEWABLE_MINUTES);

        for(DateAxis da:dateAxis) {
            // the range moves so this should not be fixed
            da.setRange(end.withSecondOfMinute(0).toDate(), begin.withSecondOfMinute(0).toDate());
        }
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
                JLabel label = new JLabel(String.valueOf(positiveValue/60.0)+ "h");
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
            JSlider slider = (JSlider)event.getSource();
            // value is in minutes

            DateTime begin = new DateTime(), end = new DateTime();
            if (value > 0) {
                //end = new DateTime().plusMinutes(60).plusMinutes((value ));
                //begin = end.minusMinutes(60);
            } else if ( value <0) {
                realtime = false;
                //System.out.println("value: " + (value * -1));
                end = end.minusMinutes( (value *-1));
                begin = end.minusMinutes( VIEWABLE_MINUTES );
            } else if ( value == 0) {
                realtime = true;
                begin = begin.minusMinutes(VIEWABLE_MINUTES);

            }
            // the range moves so this should not be fixed
            //for(DateAxis da:dateAxis) {
                axis.setRange(begin.withSecondOfMinute(0).toDate(), end.toDate());
            //}
        }
    }

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
        requestVolumeServedSeries = new TimeSeries[this.classifications.length];
        requestsServedMap = new HashMap[this.classifications.length];
        movingVolumeAverage = new LRUMap[this.classifications.length];
        movingAverage = new LRUMap[this.classifications.length];
        averageSeries = new TimeSeries[this.classifications.length];
        charts = new JFreeChart[this.classifications.length];
        averageTitle = new TextTitle[this.classifications.length];
        requestTitle = new TextTitle[this.classifications.length];

        dateAxis = new DateAxis[this.classifications.length];
        timeRangeAxis = new NumberAxis[this.classifications.length];
        requestVolumeRangeAxis = new NumberAxis[this.classifications.length];
        startTimeLong = System.currentTimeMillis();
        chartPanel = new ChartPanel[this.classifications.length];
        popupMenu = new JPopupMenu[this.classifications.length];
        expandGraphItem = new javax.swing.JMenuItem[this.classifications.length];
        expandGraphItem2 = new javax.swing.JMenuItem[this.classifications.length];
        mainPanel = new JPanel[this.classifications.length];
        dataset = new TimeSeriesCollection[this.classifications.length];
        orderedSeries = new ArrayList[this.classifications.length];
        scrollableChartPanel = new ExternalMinuteGraph.ScrollableChartPanel[this.classifications.length];

        init();

    }

    /**
     * Creates a new application.
     */
    public void init() {
        setLayout(new GridLayout(4, 1, 5, 5));
        //setBackground(Color.decode("#8D8D8D"));
        setBackground(Color.black);
        boolean isPricing = false;
        ChartFactory.setChartTheme(StandardChartTheme.createDarknessTheme());

        for (int i = 0; i < classifications.length; ++i) {
            if (classifications[i] == 1 || classifications[i] == 2 || classifications[i] == 3) {

                isPricing = true;
//                if (maxVolumePricing == null) {
//                    maxVolumePricing = new TimeSeries("");
//                    maxVolumePricing.setMaximumItemAge(60);
//                }
//
//                if (maxAveragePricing == null) {
//                    maxAveragePricing = new TimeSeries("");
//                    maxAveragePricing.setMaximumItemAge(60);
//                }

            }

            requestVolumeServedSeries[i] = new TimeSeries("Total Requests");
            requestVolumeServedSeries[i].setMaximumItemAge(MAX_HISTORY_TO_KEEP);
            requestsServedMap[i] = new HashMap<Date,HashMap>();

            // sixty minutes
            movingVolumeAverage[i] = new LRUMap<Minute,Double>(SIXTY_MINUTES);
            movingAverage[i] = new LRUMap<Minute,Double>(SIXTY_MINUTES);

            averageSeries[i] = new TimeSeries("Average");
            averageSeries[i].setMaximumItemAge(MAX_HISTORY_TO_KEEP);

            dataset[i] = new TimeSeriesCollection();
            TimeSeriesCollection volumeDataSet = new TimeSeriesCollection();
            orderedSeries[i] = new ArrayList<ExternalMinuteGraph.TimeSeriesCompareWrapper>();

            if (isPricing) {
                // volumeDataSet.addSeries(maxVolumePricing);
                volumeDataSet.addSeries(requestVolumeServedSeries[i]);
            } else {
                volumeDataSet.addSeries(requestVolumeServedSeries[i]);
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
            //dateAxis[i].setAutoRange(true);

            timeRangeAxis[i] = new NumberAxis("Time (Sec)");
            timeRangeAxis[i].setLabelPaint(Color.white);
            timeRangeAxis[i].setLabelFont(new Font("Arial", Font.BOLD, 12));
            timeRangeAxis[i].setTickLabelPaint(Color.white);
            java.text.DecimalFormat format = new java.text.DecimalFormat("0.000");

            timeRangeAxis[i].setAutoRange(true);
            timeRangeAxis[i].setAutoTickUnitSelection(true);
            timeRangeAxis[i].setAutoRangeIncludesZero(true);
            timeRangeAxis[i].setNumberFormatOverride(format);
            timeRangeAxis[i].setLowerMargin(0.00);
            timeRangeAxis[i].setUpperMargin(0.2);
            timeRangeAxis[i].setLabelPaint(Color.white);
            timeRangeAxis[i].setLabelFont(new Font("Arial", Font.BOLD, 12));
            timeRangeAxis[i].setTickLabelPaint(Color.white);

            XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);

            lineRenderer.setToolTipGenerator(
                    new StandardXYToolTipGenerator(
                    "{0}: ({1}, {2})",
                    new SimpleDateFormat("HH:mm"),
                    new java.text.DecimalFormat("###.##")));


//            renderer.setSeriesPaint(0, Color.black);
            //renderer.setSeriesPaint(0, new Color(0, 143, 255));
            lineRenderer.setSeriesPaint(0, Color.magenta);
            lineRenderer.setSeriesStroke(0, new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (isPricing) {
                Range range = new Range(0.0, 1.50);

                timeRangeAxis[i].setRange(range, true, true);
            }

            // set the first color in the series after average
            //renderer.setSeriesPaint(1, Color.magenta);

            for (int j = 1; j < 30; j++) {
                lineRenderer.setSeriesStroke(j, new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 0, new float[]{10.0f, 5.0f}, 0));
            }

            XYBarRenderer barRenderer = new XYBarRenderer(0.20){
                @Override
                public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
                    super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass);    //To change body of overridden methods use File | Settings | File Templates.
                }
            };
            barRenderer.setShadowVisible(false);
            barRenderer.setDrawBarOutline(false);
            barRenderer.setMargin(0);

            barRenderer.setToolTipGenerator(
                    new StandardXYToolTipGenerator(
                    "{0}: ({1}, {2}/sec)",
                    new SimpleDateFormat("HH:mm"),
                    new java.text.DecimalFormat("#0.000")));

            barRenderer.setSeriesPaint(0, Color.decode("#979797"));
            barRenderer.setSeriesOutlinePaint(0, Color.black);
            //
            //
            requestVolumeRangeAxis[i] = new NumberAxis("Requests / Sec");
            requestVolumeRangeAxis[i].setLabelPaint(Color.white);
            requestVolumeRangeAxis[i].setLabelFont(new Font("Arial", Font.BOLD, 12));
            requestVolumeRangeAxis[i].setTickLabelPaint(Color.white);
            java.text.DecimalFormat requestVolumeNumberFormat = new java.text.DecimalFormat("###.##");

            requestVolumeRangeAxis[i].setNumberFormatOverride(requestVolumeNumberFormat);
            requestVolumeRangeAxis[i].setUpperMargin(0.2); // to leave room for price line
            if (isPricing) {
                Range range = new Range(0.0, MILLI_RESOLUTION);

                requestVolumeRangeAxis[i].setRange(range, true, true);
                requestVolumeRangeAxis[i].setAutoRange(true);
            }

            XYPlot xyplot = new XYPlot(volumeDataSet, dateAxis[i], requestVolumeRangeAxis[i], barRenderer);
            xyplot.setBackgroundPaint(Color.black);
            xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);

            xyplot.setRenderer(1, lineRenderer);
            xyplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
            xyplot.setRangeAxis(1, timeRangeAxis[i]);
            xyplot.setDataset(1, dataset[i]);
            xyplot.mapDatasetToRangeAxis(1, 1);

            xyplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            xyplot.setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);

            String title = classificationTitles[i];
            if (title == null)
                title = "No Title";

            //TextTitle chartTitle = new TextTitle(title);
            TextTitle chartTitle = new TextTitle(title,
                   new Font("Arial", Font.BOLD, 14), Color.white,
                   RectangleEdge.TOP, HorizontalAlignment.LEFT,
                   VerticalAlignment.CENTER, RectangleInsets.ZERO_INSETS);
            chartTitle.setPaint(Color.white);
            chartTitle.setFont(new Font("Verdana", Font.BOLD, 18));

            charts[i] = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, xyplot, false);
            charts[i].getPlot().setNoDataMessage("No data received yet...");
            charts[i].setTitle(chartTitle);
            charts[i].setBackgroundPaint(Color.black);

            Calendar begin = Calendar.getInstance();
            Date beginDate = begin.getTime();

            begin.add(Calendar.HOUR_OF_DAY, -1);
            Date endDate = begin.getTime();

            // the range moves so this should not be fixed
            dateAxis[i].setRange(endDate, beginDate);

            chartPanel[i] = new ChartPanel(charts[i]);

            LegendTitle legend = new LegendTitle(charts[i].getPlot());
            legend.setItemFont(new Font("Verdana", Font.BOLD, 6));

            charts[i].addLegend(legend);
            charts[i].getLegend().setFrame(new LineBorder());
            charts[i].getLegend().setBackgroundPaint(Color.decode("#CFCFCF"));
            charts[i].getLegend().setPosition(RectangleEdge.BOTTOM);

            popupMenu[i] = chartPanel[i].getPopupMenu();
            expandGraphItem[i] = new javax.swing.JMenuItem();
            expandGraphItem[i].setText("Maximize Graph");
            expandGraphItem[i].setActionCommand("ExpandGraph");

            popupMenu[i].add(expandGraphItem[i]);

            expandGraphItem2[i] = new javax.swing.JMenuItem();
            expandGraphItem2[i].setText("Maximize Graph(2)");
            expandGraphItem2[i].setActionCommand("ExpandGraph2");

            popupMenu[i].add(expandGraphItem2[i]);

            ExternalMinuteGraph.SymPopupMenu lSymPopupMenu = new ExternalMinuteGraph.SymPopupMenu();
            popupMenu[i].addPopupMenuListener(lSymPopupMenu);

            ExternalMinuteGraph.SymAction lSymAction = new ExternalMinuteGraph.SymAction();
            expandGraphItem[i].addActionListener(lSymAction);
            expandGraphItem2[i].addActionListener(lSymAction);

            mainPanel[i] = new JPanel();
            mainPanel[i].setLayout(new BorderLayout(0, 0));

            scrollableChartPanel[i] = new ExternalMinuteGraph.ScrollableChartPanel(dateAxis[i]);
            scrollableChartPanel[i].add(BorderLayout.CENTER, chartPanel[i]);
            mainPanel[i].add(BorderLayout.CENTER, scrollableChartPanel[i]);

            setSubTitleValues(0.0,0.0,i);

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
                    //dateAxis[i].setAutoRange(true);
                    //requestVolumeRangeAxis[i].setAutoRange(true);
                }
                isAutoRanging = true;
            }
        }
        // System.out.println("got external access bean");
        try {
            Date volumeServedDate = new DateTime(esb.getEventTime()).withSecondOfMinute(0).toDate();
            Minute minute = new Minute(volumeServedDate);

            int classify = esb.getClassification();

            for (int i = 0; i < classifications.length; ++i) {
                if (classifications[i] == classify) {

                    // ********************************************
                    // do volume served calcs
                    // ********************************************
                    Double minuteVolume = calcVolumePerMinute( esb, volumeServedDate, minute, i);
                    calcAndSetVolumeAxisRange(minute, i, minuteVolume);
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
                    //dateAxis[i].setAutoRange(true);
                    //requestVolumeRangeAxis[i].setAutoRange(true);
                }
                isAutoRanging = true;
            }
        }
        adjustDateRange();

        try {
            ExternalAccessRecordsMinuteBean armb = (ExternalAccessRecordsMinuteBean) obj;
            Date volumeServedDate = sdf.parseDateTime(armb.getTimeString()).toDate();
            Minute minute = new Minute(volumeServedDate);

            int classify = armb.getClassificationID();

            for (int i = 0; i < classifications.length; ++i) {
                if (classifications[i] == classify) {

                    Double runningAvg = calcAveragePerMinute(armb, minute, averageSeries[i]);
                    /** now do rolling 60 minute average for fixing scale **/
                    calcAndSetAverageAxisRange(minute, i, runningAvg);

                    // do 90 percent calcs
                    // ********************************************
                    String seriesKey = armb.getMachine() + armb.getInstance() + classifications[i];
                    TimeSeries i90perc = machineMap.get(seriesKey);

                    if (i90perc == null) {
                        String timeSeriesName = formatMachine(armb.getMachine(),armb.getInstance());
                        i90perc = new TimeSeries(timeSeriesName);
                        i90perc.setMaximumItemCount(MAX_HISTORY_TO_KEEP);

                        machineMap.put(seriesKey, i90perc);

                        String seriesComparison = armb.getMachine() + armb.getInstance();
                        orderedSeries[i].add(new ExternalMinuteGraph.TimeSeriesCompareWrapper(seriesComparison, i90perc));

                        try {
                            // first remove all the series that were added dynamically
                            // then re-add them in a sorted fashion
                            for(ExternalMinuteGraph.TimeSeriesCompareWrapper wrapper:orderedSeries[i]) {
                                dataset[i].removeSeries(wrapper.getTimeSeries());
                            }

                            Collections.sort(orderedSeries[i], new ExternalMinuteGraph.TimeSeriesCompareWrapper());

                            for(ExternalMinuteGraph.TimeSeriesCompareWrapper wrapper:orderedSeries[i]) {
                                dataset[i].addSeries(wrapper.getTimeSeries());
                            }

                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }

                    Double curr90pct = new Double(((double) armb.getI90Percentile()) / MILLI_RESOLUTION);
                    i90perc.removeAgedItems(false);
                    i90perc.addOrUpdate(minute, curr90pct);

                    // ********************************************

                    // do volume served calcs
                    // ********************************************
                    Double minuteVolume = calcVolumePerMinute(armb, volumeServedDate, minute, i);
                    calcAndSetVolumeAxisRange(minute, i, minuteVolume);

                    Double prevMinuteVolume = movingVolumeAverage[i].get(minute.previous());

                    if (prevMinuteVolume == null) {
                        prevMinuteVolume = 0.0;
                    }
                    //setSubTitleValues(runningAvg,prevMinuteVolume,charts[i]);
//                    averageTitle.setText(String.valueOf(runningAvg));
//                    requestTitle.setText(String.valueOf(prevMinuteVolume));

                    setTitles(runningAvg,prevMinuteVolume,i);
                }
            }

        } catch (IllegalArgumentException pe) {
            System.out.println("AvgLoadTime.process Error parsing data received " + pe);
        }
    }


    private void setSubTitleValues(Double runningAvg, Double runningVolume,int index) {
        List<Title> subTitles = new CopyOnWriteArrayList<Title>();

        DecimalFormat formatAvg = new DecimalFormat("#0.000");
        subTitles.add((averageTitle[index] = new TextTitle("Average " + formatAvg.format(runningAvg),
           new Font("Arial", Font.BOLD, 16), Color.white,
           RectangleEdge.TOP, HorizontalAlignment.CENTER,
           VerticalAlignment.CENTER, RectangleInsets.ZERO_INSETS)));

        DecimalFormat format2 = new DecimalFormat("###.##");

        subTitles.add((requestTitle[index] = new TextTitle("Requests " + format2.format(runningVolume) + " / sec",
            new Font("Arial", Font.BOLD, 16), Color.white,
            RectangleEdge.TOP, HorizontalAlignment.CENTER,
            VerticalAlignment.CENTER, RectangleInsets.ZERO_INSETS)));

        charts[index].setSubtitles(subTitles);
    }

    private void setTitles(Double runningAvg,Double runningVolume,int index) {
        DecimalFormat formatAvg = new DecimalFormat("#0.000");
        DecimalFormat format2 = new DecimalFormat("###.##");

        averageTitle[index].setText("Average " + formatAvg.format(runningAvg));
        requestTitle[index].setText("Requests " + format2.format(runningVolume) + " / sec");

    }

    private Double calcVolumePerMinute(ExternalAccessRecordsMinuteBean armb, Date volumeServedDate, Minute minute, int i) {
        HashMap<String,Double> currentMinHashMap = requestsServedMap[i].get(volumeServedDate);
        if (currentMinHashMap == null) {
            currentMinHashMap = new HashMap<String,Double>();
            requestsServedMap[i].put(volumeServedDate, currentMinHashMap);
        }
        Double minuteVolume = updateMinuteVolumeData(currentMinHashMap, armb);

        requestVolumeServedSeries[i].addOrUpdate(minute, minuteVolume);
        return minuteVolume;
    }

    private Double updateMinuteVolumeData(HashMap<String,Double> currentMinHashMap, ExternalAccessRecordsMinuteBean armb) {
        String machine = armb.getMachine();
        Double volume = (double) armb.getTotalUsers();
        Double currVolume = currentMinHashMap.get(machine);
        if ( currVolume == null) {
            currVolume = 0.0;
        }
        currentMinHashMap.put(machine, volume);
        return getVolumeForMinute(currentMinHashMap);
    }

    private Double calcAveragePerMinute(ExternalAccessRecordsMinuteBean armb, Minute minute, TimeSeries averageSeries) {
        // do average series calcs
        // ********************************************
        Double runningAvg = (Double) averageSeries.getValue(minute);

        if (runningAvg == null) {
            runningAvg = new Double(((double) armb.getAverageLoadTime()) / MILLI_RESOLUTION);
        }

        Double curravg = new Double(((double) armb.getAverageLoadTime()) / MILLI_RESOLUTION);
        runningAvg = new Double((runningAvg.doubleValue() + curravg.doubleValue()) / 2);

        averageSeries.addOrUpdate(minute, runningAvg);
        return runningAvg;
    }

    private Double calcVolumePerMinute(ErrorStatBean armb, Date volumeServedDate, Minute minute, int i) {
        HashMap<String,Double> currentMinHashMap = requestsServedMap[i].get(volumeServedDate);
        if (currentMinHashMap == null) {
            currentMinHashMap = new HashMap<String,Double>();
            requestsServedMap[i].put(volumeServedDate, currentMinHashMap);
        }
        Double minuteVolume = updateMinuteVolumeData(currentMinHashMap, armb);

        requestVolumeServedSeries[i].addOrUpdate(minute, minuteVolume);
        return minuteVolume;
    }

    private void calcAndSetAverageAxisRange(Minute minute, int i, Double runningAvg) {
        movingAverage[i].put(minute,runningAvg);
        Double avgMaxValue = 0.0;
        for(Map.Entry<Minute,Double> entry: movingAverage[i].entrySet()) {
            avgMaxValue = Math.max(avgMaxValue,entry.getValue());
        }
        avgMaxValue += (avgMaxValue * .85);
        timeRangeAxis[i].setRange(0.0,avgMaxValue);
        // ********************************************
    }

    private void calcAndSetVolumeAxisRange(Minute minute, int i, Double minuteVolume) {
        movingVolumeAverage[i].put(minute,minuteVolume);

        Double volumeMaxValue = 0.0;
        for(Map.Entry<Minute,Double> entry: movingVolumeAverage[i].entrySet()) {
            volumeMaxValue = Math.max(volumeMaxValue,entry.getValue());
        }
        // add 2% so the chart does not go all the way to the top
        volumeMaxValue += (volumeMaxValue * 0.2);
        requestVolumeRangeAxis[i].setRange(0.0,volumeMaxValue);
        // ********************************************
    }

    private String formatMachine(String machine,String instance) {
        int pos = machine.indexOf("-");
        String nMachine = "";
        //System.out.println("raw machine: " + machine);

        if (pos != -1) {
            pos = machine.indexOf("-", pos + 1);
            if (pos != -1) {
                nMachine = machine.substring(pos + 1) + "-90%";
            } else {
                nMachine = machine;
            }
        } else {
            //return machine
        }
        int dot = nMachine.indexOf(".");
        if (dot!=-1) {
            //pos = machine.indexOf("-", pos + 1);

            nMachine = nMachine.substring(0,dot );
            nMachine += "-" + instance;
            nMachine += "_90%";

        } else {
            //return machine;
        }
        return nMachine;
    }

    private Double updateMinuteVolumeData(HashMap<String,Double> currentMinHashMap, ErrorStatBean armb) {
        String machine = armb.getServer();
        Double volume = Double.parseDouble(armb.getValue());
        Double currVolume = currentMinHashMap.get(machine);
        if ( currVolume == null) {
            currVolume = 0.0;
        }

        currentMinHashMap.put(machine, volume );
        return getVolumeForMinute(currentMinHashMap);
    }

    private Double getVolumeForMinute(HashMap<String,Double> currentMinHashMap) {
        Double runningTotal = 0.0;
        for(Map.Entry<String,Double> e:currentMinHashMap.entrySet()) {
            Double val = e.getValue();
            if (val != null) {
                runningTotal += val;
            }
        }
        return runningTotal / 60.0;
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

                        GraphingFrame.getInstance().addGraphPanel(scrollableChartPanel[i], mainPanel[i]);
                        GraphingFrame.getInstance().show();

                    } else if (event.getActionCommand().equals("ExpandGraph2")) {

                        GraphingFrame2.getInstance().addGraphPanel(scrollableChartPanel[i], mainPanel[i]);
                        GraphingFrame2.getInstance().show();

                    } else if (event.getActionCommand().equals("RemoveGraph")) {
                        GraphingFrame.getInstance().returnPanel(mainPanel[i]);
                        GraphingFrame2.getInstance().returnPanel(mainPanel[i]);
                        mainPanel[i].updateUI();
                    }
                    break;
                }
                if (object == expandGraphItem2[i]) {
                    if (event.getActionCommand().equals("ExpandGraph")) {

                        GraphingFrame.getInstance().addGraphPanel(scrollableChartPanel[i], mainPanel[i]);
                        GraphingFrame.getInstance().show();

                    } else if (event.getActionCommand().equals("ExpandGraph2")) {

                        GraphingFrame2.getInstance().addGraphPanel(scrollableChartPanel[i], mainPanel[i]);
                        GraphingFrame2.getInstance().show();

                    } else if (event.getActionCommand().equals("RemoveGraph")) {
                        GraphingFrame.getInstance().returnPanel(mainPanel[i]);
                        GraphingFrame2.getInstance().returnPanel(mainPanel[i]);
                        mainPanel[i].updateUI();
                    }
                    break;
                }

            }
        }
    }

    void expand_actionPerformed(java.awt.event.ActionEvent event) {// to do: code goes here.
    }

}

