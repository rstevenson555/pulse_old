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

import com.bcop.arch.logger.time.Classifications;
import com.bos.art.logParser.broadcast.beans.MemoryStatBean;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JApplet;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;

/**
 * A demo application showing a dynamically updated chart that displays the current JVM memory usage.
 *
 * @author Bryce Alcock
 * @author Bob Stevenson
 * @author Will Webb
 */
public class MemoryGraph extends JPanel {

    private int totalMemoryMegs;
    private ChartPanel chartPanel;
    private DateAxis dateAxis = null;
    private JFreeChart chart = null;
    private MemoryGraph instance = null;
    /**
     * Time series for total memory used.
     */
    private TimeSeries totalMemory;
    /**
     * Time series for free memory.
     */
    private TimeSeries usedMemory;
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    private TimeSeriesCollection dataset = new TimeSeriesCollection();
    private JMenuItem expandGraphItem = new JMenuItem();
    private JPopupMenu popupMenu;

    /**
     * Creates a new application.
     */
    public JPanel getMainPanel() {
        setLayout(new BorderLayout(0, 0));
        instance = this;

        totalMemory = new TimeSeries("Max-Heap", Second.class);
        totalMemory.setMaximumItemCount(60 * 60);


        usedMemory = new TimeSeries("Used-Heap", Second.class);
        usedMemory.setMaximumItemCount(60 * 60);

        dataset.addSeries(totalMemory);
        dataset.addSeries(usedMemory);

        dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        dateAxis.setLowerMargin(0.0);
        dateAxis.setUpperMargin(0.0);
        dateAxis.setTickLabelsVisible(true);
        dateAxis.setAutoRange(true);
        dateAxis.setAutoTickUnitSelection(true);

        NumberAxis rangeAxis = new NumberAxis("Memory (MB)");
        java.text.DecimalFormat format = new java.text.DecimalFormat("0MB");
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoTickUnitSelection(true);
        rangeAxis.setAutoRangeIncludesZero(true);
        rangeAxis.setNumberFormatOverride(format);
        rangeAxis.setLowerMargin(0.40);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setToolTipGenerator(
                new StandardXYToolTipGenerator(
                "{0}: ({1}, {2})",
                new SimpleDateFormat("HH:mm"),
                new java.text.DecimalFormat("###################0")));

        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        renderer.setSeriesPaint(1, Color.magenta);
        renderer.setSeriesStroke(1, new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{10.0f, 5.0f}, 0));


        XYPlot xyplot = new XYPlot(dataset, dateAxis, rangeAxis, renderer);
        dateAxis.setAutoRange(true);
        String plot_bgcolor = appletRef.getParameter("plot_bgcolor");
        if (plot_bgcolor != null && plot_bgcolor.length() > 0) {
            xyplot.setBackgroundPaint(Color.decode(plot_bgcolor));
        } else {
            //xyplot.setBackgroundPaint(Color.lightGray);
        }
        xyplot.setRangeAxisLocation(org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT);
        java.awt.Font font = new java.awt.Font("Serif", java.awt.Font.PLAIN, 13);
        chart = new JFreeChart(
                "Memory Usage",
                font,
                xyplot,
                false);

        chart.getPlot().setNoDataMessage("No data received yet...");

        try {
            String webCodeBase = appletRef.getCodeBase().toString();

            MediaTracker mt = new MediaTracker(this);
            java.awt.Image image = appletRef.getImage(new java.net.URL(appletRef.getCodeBase() + appletRef.getParameter("plot_image")));
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

        String chart_bgcolor = appletRef.getParameter("chart_bgcolor");
        if (chart_bgcolor != null && chart_bgcolor.length() > 0) {
            chart.setBackgroundPaint(Color.decode(chart_bgcolor));
            setBackground(Color.decode(chart_bgcolor));
        } else {
            //
        }

        chartPanel = new ChartPanel(chart);
        popupMenu = chartPanel.getPopupMenu();
        expandGraphItem.setText("Maximize Graph");
        expandGraphItem.setActionCommand("ExpandGraph");

        popupMenu.add(expandGraphItem);

        MemoryGraph.SymPopupMenu lSymPopupMenu = new MemoryGraph.SymPopupMenu();
        popupMenu.addPopupMenuListener(lSymPopupMenu);

        MemoryGraph.SymAction lSymAction = new MemoryGraph.SymAction();
        expandGraphItem.addActionListener(lSymAction);

        add(BorderLayout.CENTER, chartPanel);

        //StandardLegend legend = new StandardLegend();
        //legend.setAnchor(Legend.WEST);
        LegendTitle legend = new LegendTitle(chart.getPlot());
        //chart.setLegend(legend);
        chart.addLegend(legend);        
        chart.getLegend().setPosition(RectangleEdge.LEFT);
        chart.getLegend().setFrame(new LineBorder());
        chart.getLegend().setBackgroundPaint(Color.white);


        return this;
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
        if (getComponentCount() == 0) {
            expandGraphItem.setText("Remove Graph");
            expandGraphItem.setActionCommand("RemoveGraph");
        } else {
            expandGraphItem.setText("Maximize Graph");
            expandGraphItem.setActionCommand("ExpandGraph");
        }

    }

    /*
     * (non-Javadoc) @see java.applet.Applet#init()
     */
    public MemoryGraph(MemoryStatBean bean, JApplet a) {

        appletRef = a;

        //
        //
        getMainPanel();
        Calendar begin = Calendar.getInstance();
        Date beginDate = begin.getTime();
        begin.add(Calendar.HOUR_OF_DAY, -1);
        Date endDate = begin.getTime();

        dateAxis.setRange(endDate, beginDate);
        dateAxis.setAutoRange(true);

    }

    class SymAction implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            Object object = event.getSource();

            if (event.getActionCommand().equals("ExpandGraph")) {

                GraphingFrame.getInstance().addGraphPanel(chartPanel, instance);
                GraphingFrame.getInstance().show();

            } else if (event.getActionCommand().equals("RemoveGraph")) {
                GraphingFrame.getInstance().returnPanel(instance);
                updateUI();
            }

        }
    }

    /**
     * add graph to graphingFrame
     *
     */
    public void expandGraph() {
        GraphingFrame.getInstance().addGraphPanel(chartPanel, instance);
        GraphingFrame.getInstance().show();
    }

    public void update(MemoryStatBean bean) {
        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            long time = bean.getEventTime();
            Second second = new Second(new Date(time));
            if (bean.getClassification() == Classifications.FREE_MEMORY) {
                long mem = new Long(bean.getValue()).longValue();
                int megs = (int) ((long) mem / (long) 1000000);

                if (totalMemoryMegs == 0) {
                    usedMemory.addOrUpdate(second, new Integer(megs));
                } else {
                    usedMemory.addOrUpdate(second, new Integer(totalMemoryMegs - megs));
                }
                // see if there is a title set
                String title = null;
                if (chart.getTitle() != null) {
                    title = chart.getTitle().getText();
                }

                chart.setTitle(bean.getContext() + " on " + formatMachine(bean.getServer()) + " [" + bean.getBranchName() + "]");
            } else if (bean.getClassification() == Classifications.TOTAL_MEMORY) {
                long mem = new Long(bean.getValue()).longValue();
                int megs = (int) ((long) mem / (long) 1000000);
                totalMemoryMegs = megs;
                totalMemory.addOrUpdate(second, new Integer(megs));
            }

        } catch (Exception e) {
            System.out.println("Error updating MemoryStatBean...");
        }

    }

    private String formatMachine(String machine) {
        int pos = machine.indexOf("-");
        if (pos != -1) {
            pos = machine.indexOf("-", pos + 1);
            if (pos != -1) {
                return machine.substring(pos + 1);
            } else {
                return machine;
            }
        } else {
            return machine;
        }
    }
    private JApplet appletRef;
}
