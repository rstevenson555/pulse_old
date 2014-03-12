package com.bos.applets;

import com.bos.applets.jfree.SortedColumnCategoryDataset;
import com.bos.art.logParser.broadcast.beans.AccessRecordsMinuteBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.delegate.AccessRecordsDelegate;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Series;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jgroups.Message;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: i0360b6
 * Date: Sep 16, 2013
 * Time: 8:24:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class InstanceStatsGraph extends JPanel implements AccessRecordsDelegate {
    private SortedColumnCategoryDataset dataset;
    private SortedColumnCategoryDataset dataset2;
    private Series requestVolumeSeries;
    private Series responseTimeSeries;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private JMenuItem expandGraphItem = new JMenuItem();
    private JPopupMenu popupMenu;
    private InstanceStatsGraph instance;

    public InstanceStatsGraph() {
        init();
        instance = this;
    }

    public void init() {
        //setLayout(new GridLayout(4, 1, 5, 5));
        setBackground(Color.black);
        setLayout(new BorderLayout(5, 5));

        ChartFactory.setChartTheme(StandardChartTheme.createDarknessTheme());
        dataset = new SortedColumnCategoryDataset();
        dataset2 = new SortedColumnCategoryDataset();

        requestVolumeSeries = new TimeSeries("requestvolume");
        responseTimeSeries = new TimeSeries("responsetime");

        //dataset = createDataset();
        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setLayout(new BorderLayout(5,5));

        this.chartPanel.setDomainZoomable(true);
        this.chartPanel.setRangeZoomable(true);

        popupMenu = chartPanel.getPopupMenu();
        expandGraphItem.setText("Maximize Graph");
        expandGraphItem.setActionCommand("ExpandGraph");

        popupMenu.add(expandGraphItem);

        SymPopupMenu lSymPopupMenu = new SymPopupMenu();
        popupMenu.addPopupMenuListener(lSymPopupMenu);

        SymAction lSymAction = new SymAction();
        expandGraphItem.addActionListener(lSymAction);

        add(BorderLayout.CENTER,chartPanel);

    }

    private JFreeChart createChart(final CategoryDataset dataset) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                "Instance Stats",         // chart title
                "",               // domain axis label
                "Request Volume",                  // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
                //CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 90.0)
                CategoryLabelPositions.UP_90
        );

        plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);

        final NumberAxis axis2 = new NumberAxis("Secondary");
        axis2.setLabel("secondary");
        axis2.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        axis2.setAutoRange(true);

        plot.setRangeAxis(1, axis2);
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setDataset(1, dataset2);
        plot.mapDatasetToRangeAxis(1, 1);

        System.out.println("range axis count: " + plot.getRangeAxisCount());
        //plot.setRangeAxisLocation(1,AxisLocation.TOP_OR_RIGHT);

        final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
        renderer2.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        plot.setRenderer(1, renderer2);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        // disable bar outlines...
        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        return chart;
    }

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
    private LRUMap<Minute,Double> requestVolumeSeriesMap = new LRUMap(1);

    private String formatInstanceName(AccessRecordsMinuteBean armb) {
        String machine = armb.getMachine();
        // b2b format is: prod-b2b-app3
        // b2c format is prod-ec-app3-officemax-omx1

        //System.out.println("machine1: "+machine);

        int pos = machine.indexOf("-");
        //pos = machine.indexOf("-",pos);
        pos = machine.indexOf("-",pos+1);
        pos++;
        //int end = machine.indexOf(".",pos);
        int end = machine.length();

        String wholeName =  armb.getMachine().substring(pos,end)+"-"+armb.getInstance();
        return wholeName;
    }

    private Pattern instanceNamePatternOMX =
        Pattern.compile("app[1-9]+-omx[1-9]+");
    private Pattern instanceNamePatternB2B =
        Pattern.compile("app[1-9]+-b2bshop[1-9]+");
    private Pattern instanceNameCorrectionPattern = Pattern.compile("a0[1-9][1-9]-.*");
    private DateTime lastMinute = new DateTime();
    private static DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyyMMddHHmm");

    public void didReceiveAccessRecordsBean(Message message, TransferBean obj) {
        AccessRecordsMinuteBean armb = (AccessRecordsMinuteBean) obj;
        DateTime dpdate = sdf.parseDateTime(armb.getTimeString()).withSecondOfMinute(0);

        if (dpdate.equals(lastMinute.plusMinutes(1))) {
            // set all columns to 0 at the first start of a minute
            for(int cc=0,tot = dataset.getColumnCount();cc<tot;cc++) {
                dataset.setValue(0,"Requests", dataset.getColumnKey(cc));
                dataset2.setValue(0,"Response Time", dataset.getColumnKey(cc));
            }
        }

        String machine = formatInstanceName(armb);
        //System.out.println("machine: "+machine);
        
        Matcher matcherOMX =
            instanceNamePatternOMX.matcher(machine);

        if ( matcherOMX.matches()) {
            machine = machine.replace("app","a0");
            machine = machine.replace("omx","");

            Matcher matcher2 = instanceNameCorrectionPattern.matcher(machine);
            if ( matcher2.matches()) {
                machine = machine.replace("a0","a");
            }
            dataset.setValue(armb.getTotalLoads(),"Requests", machine);
            dataset2.setValue(armb.getAverageLoadTime()/1000.,"Response Time", machine);
        }
        Matcher matcherB2B =
            instanceNamePatternB2B.matcher(machine);

        if ( matcherB2B.matches()) {
            machine = machine.replace("app","a0");
            machine = machine.replace("b2bshop","");

            Matcher matcher2 = instanceNameCorrectionPattern.matcher(machine);
            if ( matcher2.matches()) {
                machine = machine.replace("a0","a");
            }
            dataset.setValue(armb.getTotalLoads(),"Requests", machine);
            dataset2.setValue(armb.getAverageLoadTime()/1000.,"Response Time", machine);
        }
        lastMinute = dpdate;
    }

    public void didCompleteBagProcessing(Message message) {
    }

    class SymAction implements java.awt.event.ActionListener {

        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (event.getActionCommand().equals("ExpandGraph")) {

                GraphingFrame.getInstance().addGraphPanel(chartPanel, instance);
                GraphingFrame.getInstance().show();

            } else if (event.getActionCommand().equals("RemoveGraph")) {
                GraphingFrame.getInstance().returnPanel(instance);

                instance.updateUI();
            }
        }
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
        if (instance.getComponentCount() == 0) {
            expandGraphItem.setText("Remove Graph");
            expandGraphItem.setActionCommand("RemoveGraph");
        } else {
            expandGraphItem.setText("Maximize Graph");
            expandGraphItem.setActionCommand("ExpandGraph");
        }

    }

}
