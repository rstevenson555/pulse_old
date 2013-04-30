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
import com.bos.art.logParser.broadcast.beans.HistoryBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.delegate.AccessRecordsDelegate;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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

/**
 * A demo application showing a dynamically updated chart that displays the current JVM memory usage.
 *
 * @author Bryce Alcock
 * @author Bob Stevenson
 * @author Will Webb
 */
public class AvgLoadTime extends JApplet implements AccessRecordsDelegate {

    public class HotSpotChartPanel extends ChartPanel {

        private JButton historyButton = null;

        public HotSpotChartPanel(JFreeChart chart) {
            super(chart);

            historyButton = new JButton("History Mode");
            historyButton.setVisible(false);

            JPanel northPanel = new JPanel(new BorderLayout(5, 5));
            add(northPanel, BorderLayout.NORTH);
            northPanel.setOpaque(false);
            northPanel.add(historyButton, BorderLayout.WEST);
        }

        @Override
        public void paint(java.awt.Graphics g) {
            if (inHotSpotRange) {
                // now draw on top of the chart
                super.paint(g);

                historyButton.setVisible(true);
                // draw a snapshot of the chart on the bufferedimage
            } else {
                super.paint(g);
                historyButton.setVisible(false);
            }
        }

        class HistoryVisibleAction implements java.awt.event.ActionListener {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                Object object = e.getSource();
                if ("history_mode_on".equals(e.getActionCommand())) {
                }
            }

            public void setImageIcon(ImageIcon image) {
                //this.icon = image;
                //historyButton = new JButton(image);
                //historyButton.setText("Show History");
                //historyButton.setVisible(false);
                historyButton.setIcon(image);

            }
        }
    }

    public class WrapperPanel extends JPanel {

        public WrapperPanel(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public void paintComponent(Graphics g) {
            if (historyNavShown == true && chartImageBitmap != null) {
                // now draw the chartpanel onto the bufferedimage
                //
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) chartImageBitmap.getGraphics();

                g.drawImage(chartImageBitmap, 0, 0, null);
            }
        }
    }

    public class TransparentPanel extends JPanel {

        public TransparentPanel() {
            super();
            //setDebugGraphicsOptions( DebugGraphics.FLASH_OPTION|DebugGraphics.LOG_OPTION);
        }

        public TransparentPanel(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }

        @Override
        public void paintComponent(Graphics g) {
            if (isOpaque() == true) {
                //g.setColor( new Color(125,125,125,60));
                //g.setColor( new Color(125,125,125,200));
                // Alpha of 0 is completly transparent, 255 is completely solid (opaque)
                //g.setColor( new Color(0,0,0,200));
                g.setColor(new Color(0, 0, 0, 60));
                //g.setColor( Color.black);
                ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .65f));
                g.fill3DRect(0, 0, (int) getBounds().getWidth(), (int) getBounds().getHeight(), true);
            }
            //
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .65f));
            super.paintComponent(g);
        }
    }
    private JFreeChart chart = null;
    private JApplet applet = null;
    private int timeOffset = -60; // in minutes
    private long startTimeLong;
    private boolean isAutoRanging = false;
    private AvgLoadTime.HotSpotChartPanel chartPanel;
    private DateAxis dateAxis = null;
    private javax.swing.JMenuItem expandGraphItem = new javax.swing.JMenuItem();
    private JPopupMenu popupMenu;
    /**
     * Time series for total memory used.
     */
    private TimeSeries pagesServed;
    /**
     * Time series for free memory.
     */
    private TimeSeries averageSeries;
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMddHHmm"); 

    private JPanel mainPanel = null;
    private ImageIcon downArrowIcon = null;
    BufferedImage chartImageBitmap = null;
    private int precisionView = Calendar.MINUTE;
    //private boolean drawBImg = false;
    private boolean inHotSpotRange = false;    
    private String lastActionMode = "realtime";
    private boolean historyNavShown = false;
    private AvgLoadTime.TransparentPanel historyPanel = null;
    private JButton backTime,forwardTime,realTime;
    private AvgLoadTime.WrapperPanel wrapperPanel = null;
    private AvgLoadTime instance = null;
    private TimeSeriesCollection dataset = new TimeSeriesCollection();
    private HashMap machineMap = new HashMap();

    private void buildHistoryPanel() {
        historyPanel = new AvgLoadTime.TransparentPanel(new BorderLayout(5, 5));
        AvgLoadTime.HistoryAction haction = new AvgLoadTime.HistoryAction();

        AvgLoadTime.TransparentPanel buttonPanel = new AvgLoadTime.TransparentPanel(new BorderLayout(5, 5));
        buttonPanel.setOpaque(false);
        historyPanel.add(buttonPanel, BorderLayout.NORTH);

        backTime = new JButton("< 1 hour");
        backTime.setBorderPainted(false);
        backTime.setFocusPainted(false);
        backTime.setBackground(Color.black);
        backTime.setForeground(Color.white);

        forwardTime = new JButton("> 1 hour");
        forwardTime.setBorderPainted(false);
        forwardTime.setFocusPainted(false);
        forwardTime.setBackground(Color.black);
        forwardTime.setForeground(Color.white);

        AvgLoadTime.TransparentPanel centerPanel = new AvgLoadTime.TransparentPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);

        AvgLoadTime.TransparentPanel nestedCenterPanel = new AvgLoadTime.TransparentPanel(new FlowLayout(FlowLayout.CENTER));
        nestedCenterPanel.setOpaque(false);

        JButton graphMode = new JButton("[Minute Mode]");
        graphMode.setBorderPainted(false);
        graphMode.setFocusPainted(false);
        graphMode.setBackground(Color.black);
        graphMode.setForeground(Color.white);
        graphMode.setActionCommand("minute_mode");
        graphMode.addActionListener(haction);
        graphMode.setVisible(false);

        nestedCenterPanel.add(graphMode);
        nestedCenterPanel.add(Box.createRigidArea(new Dimension(1, 30)));

        buttonPanel.add(nestedCenterPanel, BorderLayout.CENTER);

        AvgLoadTime.TransparentPanel westPanel = new AvgLoadTime.TransparentPanel(new FlowLayout());
        westPanel.setOpaque(false);
        buttonPanel.add(westPanel, BorderLayout.WEST);

        // add a little padding to the left of the button
        westPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        AvgLoadTime.TransparentPanel westButtonPanel = new AvgLoadTime.TransparentPanel(new GridLayout(2, 1));
        westButtonPanel.setOpaque(false);

        westButtonPanel.add(backTime);
        westButtonPanel.add(forwardTime);

        westPanel.add(westButtonPanel);

        Component spacer = Box.createRigidArea(new Dimension(30, 60));
        historyPanel.add(spacer, BorderLayout.CENTER);

        backTime.setActionCommand("back_time");
        backTime.addActionListener(haction);

        forwardTime.setActionCommand("forward_time");
        forwardTime.addActionListener(haction);

        realTime = new JButton("Realtime");
        realTime.setBackground(Color.black);
        realTime.setForeground(Color.white);
        realTime.setBorderPainted(false);
        realTime.setFocusPainted(false);

        realTime.setActionCommand("realtime");
        realTime.addActionListener(haction);

        AvgLoadTime.TransparentPanel eastPanel = new AvgLoadTime.TransparentPanel(new FlowLayout());
        eastPanel.setOpaque(false);

        buttonPanel.add(eastPanel, BorderLayout.EAST);
        eastPanel.add(realTime);
        eastPanel.add(Box.createRigidArea(new Dimension(10, 10)));
    }

    class HistoryAction implements java.awt.event.ActionListener {

        private DateTimeFormatter subtitleDateFormat = DateTimeFormat.forPattern("MM-dd-yyyy HH:mm:SS"); 

        public void actionPerformed(java.awt.event.ActionEvent e) {
            Object object = e.getSource();
            if ("back_time".equals(e.getActionCommand())) {
                forwardTime.setEnabled(false);
                backTime.setEnabled(false);
                realTime.setEnabled(false);

                timeOffset -= 60;

                TimeSeries series = null;
                if ((series = (TimeSeries) machineMap.get("90 %ile")) != null) {
                    series.clear();
                }
                averageSeries.clear();
                pagesServed.clear();

                updateDateRange();

                HistoryBean hbean = new HistoryBean();
                hbean.setChartName("AVG_CHART");
                Calendar begin = Calendar.getInstance();
                begin.add(Calendar.MINUTE, (int) timeOffset);
                hbean.setDate(begin.getTime());
                hbean.setDirection("F");
                hbean.setDataPoints(60);
                hbean.setDataPrecision("min");
                lastActionMode = "history";
                AppletMessageListener.getInstance().sendHistoryBean(hbean);
                
                chart.clearSubtitles();
                DateTime dt = new DateTime(begin.getTime());
                TextTitle title = new TextTitle(subtitleDateFormat.print(dt));
                title.setHorizontalAlignment(org.jfree.ui.HorizontalAlignment.CENTER);
                title.setVerticalAlignment(org.jfree.ui.VerticalAlignment.BOTTOM);
                chart.addSubtitle(title);

            } else if ("forward_time".equals(e.getActionCommand())) {
                forwardTime.setEnabled(false);
                backTime.setEnabled(false);
                realTime.setEnabled(false);
                timeOffset += 60;

                TimeSeries series = null;
                if ((series = (TimeSeries) machineMap.get("90 %ile")) != null) {
                    series.clear();
                }
                averageSeries.clear();
                pagesServed.clear();

                updateDateRange();

                HistoryBean hbean = new HistoryBean();
                hbean.setChartName("AVG_CHART");
                Calendar begin = Calendar.getInstance();
                begin.add(Calendar.MINUTE, (int) timeOffset);
                hbean.setDate(begin.getTime());
                hbean.setDirection("F");
                hbean.setDataPoints(60);
                hbean.setDataPrecision("min");
                lastActionMode = "history";
                AppletMessageListener.getInstance().sendHistoryBean(hbean);
                
                chart.clearSubtitles();
                DateTime dt = new DateTime(begin.getTime());
                TextTitle title = new TextTitle(subtitleDateFormat.print(dt));
                title.setHorizontalAlignment(org.jfree.ui.HorizontalAlignment.CENTER);
                title.setVerticalAlignment(org.jfree.ui.VerticalAlignment.BOTTOM);
                chart.addSubtitle(title);
                
            } else if ("minute_mode".equals(e.getActionCommand())) {
                ((JButton) object).setActionCommand("hour_mode");
                ((JButton) object).setText("[ Hour Mode ]");

            } else if ("hour_mode".equals(e.getActionCommand())) {
                ((JButton) object).setActionCommand("day_mode");
                ((JButton) object).setText("[ Day Mode  ]");

            } else if ("day_mode".equals(e.getActionCommand())) {
                ((JButton) object).setActionCommand("minute_mode");
                ((JButton) object).setText("[Minute Mode]");

            } else if ("realtime".equals(e.getActionCommand())) {
                TimeSeries series = null;
                if ((series = (TimeSeries) machineMap.get("90 %ile")) != null) {
                    series.clear();
                }
                averageSeries.clear();
                pagesServed.clear();

                hideHistoryPanel();
                
                chart.clearSubtitles();
                TextTitle title = new TextTitle("(Realtime)");
                title.setFont(new Font("Verdana", Font.BOLD, 14));
                title.setPaint(Color.white);
                title.setHorizontalAlignment(org.jfree.ui.HorizontalAlignment.CENTER);
                title.setVerticalAlignment(org.jfree.ui.VerticalAlignment.BOTTOM);                
                chart.addSubtitle(title);                

                HistoryBean hbean = new HistoryBean();
                hbean.setChartName("AVG_CHART");
                Calendar begin = Calendar.getInstance();
                begin.add(Calendar.MINUTE, -60);
                hbean.setDate(begin.getTime());
                hbean.setDirection("F");
                hbean.setDataPoints(60);
                hbean.setDataPrecision("min");
                lastActionMode = "realtime";
                AppletMessageListener.getInstance().sendHistoryBean(hbean);

            }
        }
    }

    /**
     * Creates a new application.
     */
    public JPanel getMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));

        chartImageBitmap = initGraphicsEnv().createCompatibleImage((int) 1, (int) 1);
        buildHistoryPanel();
        instance = this;

        // create two series that automatically discard data more than 30 seconds old...
        pagesServed = new TimeSeries("Total Pages");
        pagesServed.setMaximumItemCount(60);
        averageSeries = new TimeSeries("Average");
        averageSeries.setMaximumItemCount(60);

        TimeSeriesCollection volumeDataSet = new TimeSeriesCollection();

        volumeDataSet.addSeries(pagesServed);
        dataset.addSeries(averageSeries);

        dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        dateAxis.setLowerMargin(0.0);
        dateAxis.setUpperMargin(0.0);
        dateAxis.setTickLabelsVisible(true);
        dateAxis.setLabelPaint(Color.white);
		dateAxis.setTickLabelPaint(Color.white);
		dateAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));

        NumberAxis rangeAxis = new NumberAxis("Time (Sec)");
        rangeAxis.setLabelPaint(Color.white);
        rangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 12));
        rangeAxis.setTickLabelPaint(Color.white);
        java.text.DecimalFormat format = new java.text.DecimalFormat("0.000");
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoTickUnitSelection(true);
        rangeAxis.setAutoRangeIncludesZero(true);
        rangeAxis.setNumberFormatOverride(format);
        rangeAxis.setLowerMargin(0.00);
        rangeAxis.setLowerMargin(0.20);
        rangeAxis.setUpperMargin(0.5);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setToolTipGenerator(
                new StandardXYToolTipGenerator(
                "{0}: ({1}, {2})",
                new SimpleDateFormat("HH:mm"),
                new java.text.DecimalFormat("#0.000")));

//        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesPaint(0, new Color(0, 143, 255));
        renderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        renderer.setSeriesPaint(1, Color.magenta);
        for (int j = 1; j < 10; j++) {
            renderer.setSeriesStroke(j, new BasicStroke(3f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 0, new float[]{10.0f, 5.0f}, 0));
        }

        NumberAxis rangeAxis2 = new NumberAxis("Page Volume");
        rangeAxis2.setLabelPaint(Color.white);
        rangeAxis2.setLabelFont(new Font("Arial", Font.BOLD, 12));
        rangeAxis2.setTickLabelPaint(Color.white);
        java.text.DecimalFormat format2 = new java.text.DecimalFormat("#000");
        rangeAxis2.setNumberFormatOverride(format2);
        rangeAxis2.setUpperMargin(0.20);  // to leave room for price line       

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

        XYPlot xyplot = new XYPlot(volumeDataSet, dateAxis, rangeAxis2, renderer2);
        String plot_bgcolor = "#000000";
        xyplot.setBackgroundPaint(Color.decode(plot_bgcolor));
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);

        xyplot.setRangeAxis(1, rangeAxis); // 1 is left axis, 
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
        
        TextTitle chartTitle = new TextTitle("App-Server Page Execution");
        chartTitle.setPaint(Color.white);
        chartTitle.setFont(new Font("Verdana", Font.BOLD, 20));
        chart.setTitle(chartTitle); 
                
        chart.clearSubtitles();
        TextTitle subtitle = new TextTitle("(Realtime)");
        subtitle.setFont(new Font("Verdana", Font.BOLD, 14));
        subtitle.setPaint(Color.white);
        subtitle.setHorizontalAlignment(org.jfree.ui.HorizontalAlignment.CENTER);
        subtitle.setVerticalAlignment(org.jfree.ui.VerticalAlignment.BOTTOM);
        chart.addSubtitle(subtitle);

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
            chart.setBackgroundImage(image);
        } catch (java.net.MalformedURLException mfue) {
            System.err.println(mfue);
        }

        chart.setBackgroundPaint(new Color(70, 70, 70));
        mainPanel.setBackground(new Color(70, 70, 70));
        historyPanel.setBackground(new Color(70, 70, 70));

        wrapperPanel = new AvgLoadTime.WrapperPanel(new BorderLayout(5, 5));
        chartPanel = new AvgLoadTime.HotSpotChartPanel(chart);
        chartPanel.setLayout(new BorderLayout(5, 5));

        //System.out.println("downArrowIcon: " + downArrowIcon );
        //chartPanel.setImageIcon( downArrowIcon );

        wrapperPanel.add(BorderLayout.CENTER, chartPanel);

        popupMenu = chartPanel.getPopupMenu();
        expandGraphItem.setText("Maximize Graph");
        expandGraphItem.setActionCommand("ExpandGraph");

        popupMenu.add(expandGraphItem);

        AvgLoadTime.SymPopupMenu lSymPopupMenu = new AvgLoadTime.SymPopupMenu();
        popupMenu.addPopupMenuListener(lSymPopupMenu);

        AvgLoadTime.SymAction lSymAction = new AvgLoadTime.SymAction();
        expandGraphItem.addActionListener(lSymAction);

        mainPanel.add(BorderLayout.CENTER, wrapperPanel);

        //StandardLegend legend = new StandardLegend();
        //legend.setAnchor(Legend.WEST);
        LegendTitle legend = new LegendTitle(chart.getPlot());
        chart.addLegend(legend);
        chart.getLegend().setFrame(new LineBorder());
        chart.getLegend().setBackgroundPaint(Color.decode("#CFCFCF"));
        
        
        chart.getLegend().setPosition(RectangleEdge.LEFT);

        chartPanel.addMouseMotionListener(new AvgLoadTime.MyMouseMotionListener());

        wrapperPanel.add(BorderLayout.SOUTH, historyPanel);
        historyPanel.setVisible(false);

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
        if (lastActionMode.equals("history")) {

            // hide the history nav panel
            historyPanel.setVisible(false);
            chartPanel.setVisible(true);
            // show the chart 
            // paint it to out bufferedImage
            chartPanel.paint(chartImageBitmap.getGraphics());
            // hide the chartPanel
            chartPanel.setVisible(false);

            // show the historyPanel
            historyPanel.setVisible(true);
            historyPanel.setPreferredSize(new Dimension(0, 50));
            
            forwardTime.setEnabled(true);
            backTime.setEnabled(true);
            realTime.setEnabled(true);

            mainPanel.repaint();
        }
    }

    /**
     * called by the receiver when objects are received we can then decide if we want to deal with this message or not
     *
     */
    public void didReceiveAccessRecordsBean(Message msg, TransferBean obj) {
        if (!isAutoRanging) {
            if (System.currentTimeMillis() - 30 * 1000 > startTimeLong) {
                dateAxis.setAutoRange(true);
                isAutoRanging = true;
            }
        }
        try {
            AccessRecordsMinuteBean armb = (AccessRecordsMinuteBean) obj;
            Date dpdate = sdf.parseDateTime(armb.getTimeString()).toDate();
            Minute minute = new Minute(dpdate);

            if (!inRange(dpdate)) {
                return;
            }

            TimeSeries series = null;
            if ((series = (TimeSeries) machineMap.get("90 %ile")) == null) {
                series = new TimeSeries("90 %ile");
                series.setMaximumItemCount(60);
                machineMap.put("90 %ile", series);
                dataset.addSeries(series);
            }
            series.addOrUpdate(minute, new Double(((double) armb.getI90Percentile()) / 1000.0));

            averageSeries.addOrUpdate(minute, new Double(((double) armb.getAverageLoadTime()) / 1000.0));
            pagesServed.addOrUpdate(minute, new Double(armb.getTotalUsers()));
        } catch (IllegalArgumentException pe) {
            System.out.println("AvgLoadTime.process Error parsing data received " + pe);
        }
    }

    /**
     * checks to see if the data-point is within our charts range
     *
     */
    public boolean inRange(Date dpdate) {
        Calendar beginCal = Calendar.getInstance();
        beginCal.set(Calendar.SECOND, 0);
        // go back in time, to the beginning time of the graph
        beginCal.add(Calendar.MINUTE, timeOffset + -1);
        Date beginDate = beginCal.getTime();

        // then add 60 minutes for an hour graph
        beginCal.add(Calendar.MINUTE, 60 + 1);
        Date endDate = beginCal.getTime();

        // some basic range checking
        // this range is NOT inclusive if the data-point is equal to the begindate or the enddate
        // so we've extended the ranges, by a minute each way
        if (!(dpdate.after(beginDate) && dpdate.before(endDate))) {
            // this date is out of range so throw it out.
            return false;
        }
        return true;
    }

    class SymAction implements java.awt.event.ActionListener {

        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (event.getActionCommand().equals("ExpandGraph")) {

                GraphingFrame.getInstance().addGraphPanel(wrapperPanel, mainPanel);
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

        Calendar cal = Calendar.getInstance();

        // go backwards
        cal.add(Calendar.MINUTE, -60);
        Date beginDate = cal.getTime();

        // go forward
        cal.add(Calendar.MINUTE, 60);
        Date endDate = cal.getTime();

        dateAxis.setRange(beginDate, endDate);

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

    class MyMouseMotionListener implements MouseMotionListener {

        public void mouseMoved(MouseEvent e) {
            saySomething("Mouse moved", e);
        }

        public void mouseDragged(MouseEvent e) {
            saySomething("Mouse dragged", e);
        }

        void saySomething(String eventDescription, MouseEvent e) {
            // Y is vertical
            // X is horizontal
            if (e.getY() < 40) {

                if (inHotSpotRange == false) {
                    inHotSpotRange = true;
                    chartPanel.update(chartPanel.getGraphics());
                }
            } else {
                if (inHotSpotRange == true) {
                    inHotSpotRange = false;
                    chartPanel.update(chartPanel.getGraphics());
                }
            }
            if (e.getX() < 15 && e.getY() < 15 && historyNavShown == false) {
                //System.out.println("panel width , height " + (int)chartPanel.getBounds().getWidth() + "," + (int)chartPanel.getBounds().getHeight());

                historyNavShown = true;
                // copy the chartPanel image into chartImageBitmap
                if (chartImageBitmap.getWidth() != wrapperPanel.getBounds().getWidth() || chartImageBitmap.getHeight() != wrapperPanel.getHeight()) {
                    chartImageBitmap.getGraphics().dispose();
                    chartImageBitmap = initGraphicsEnv().createCompatibleImage((int) wrapperPanel.getBounds().getWidth(), (int) wrapperPanel.getBounds().getHeight());
                }
                chartPanel.paint(chartImageBitmap.getGraphics());
                chartPanel.hide();

                showHistoryPanel();
            } else {
                // moved out
                //hoverCapture = null;
            }
        }
    }

    public GraphicsConfiguration initGraphicsEnv() {
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        return gc;
    }

    private void updateDateRange() {
        Calendar cal = Calendar.getInstance();

        // go backwards
        cal.add(Calendar.MINUTE, (int) timeOffset);
        Date beginDate = cal.getTime();

        // go forward
        int offset = 0;
        if (precisionView == Calendar.MINUTE) {
            offset = 60;
        }
        cal.add(Calendar.MINUTE, offset);
        Date endDate = cal.getTime();

        dateAxis.setRange(beginDate, endDate);
        dateAxis.setAutoRange(false);
    }

    public void showHistoryPanel() {
        historyNavShown = true;
        historyPanel.setVisible(true);
        historyPanel.setPreferredSize(new Dimension(0, 50));
        mainPanel.updateUI();
        isAutoRanging = true;
    }

    public void hideHistoryPanel() {
        historyNavShown = false;
        timeOffset = -60;
        historyPanel.setVisible(false);
        chartPanel.setVisible(true);
        mainPanel.updateUI();

        Calendar cal = Calendar.getInstance();

        // go backwards
        cal.add(Calendar.MINUTE, -60);
        Date beginDate = cal.getTime();

        // go forward
        cal.add(Calendar.MINUTE, 60);
        Date endDate = cal.getTime();

        dateAxis.setRange(beginDate, endDate);
        dateAxis.setAutoRange(true);

        wrapperPanel.repaint();

    }
}
