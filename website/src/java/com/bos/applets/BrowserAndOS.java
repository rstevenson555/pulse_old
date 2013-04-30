package com.bos.applets;

import com.bos.applets.arch.AppletMessageListener;
import com.bos.art.logParser.broadcast.beans.BrowserBean;
import com.bos.art.logParser.broadcast.beans.TransferBean;
import com.bos.art.logParser.broadcast.beans.delegate.BrowserDelegate;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jgroups.Message;

public class BrowserAndOS extends JPanel implements BrowserDelegate {

    private DefaultPieDataset piedataset = new DefaultPieDataset();
    private DefaultPieDataset displayedPieDataset = new DefaultPieDataset();
    private JFreeChart chart;
    private int graphType;
    private int displayType;
    private JMenuItem expandGraphItem = new JMenuItem();
    private JMenuItem showAsPercentageItem = new JMenuItem();
    private JPopupMenu popupMenu;
    private ChartPanel chartPanel;
    private BrowserAndOS instance;
    public static final int BROWSER_GRAPH = 1;
    public static final int OS_GRAPH = 2;
    public static final int WHOLE_NUMBERS = 1;
    public static final int PERCENTAGE = 2;

    public JPanel getMainPanel() {
        return this;
    }

    public JFreeChart createChart() {
        JFreeChart jfreechart = null;
        if (graphType == BROWSER_GRAPH) {
            jfreechart = ChartFactory.createPieChart("Browser Stats", displayedPieDataset, false, true, false);
        } else {
            jfreechart = ChartFactory.createPieChart("OS Stats", displayedPieDataset, false, true, false);
        }
        PiePlot pieplot = (PiePlot) jfreechart.getPlot();
        pieplot.setLabelFont(new Font("SansSerif", 0, 12));
        pieplot.setNoDataMessage("No data received yet...");
        pieplot.setCircular(false);
        pieplot.setLabelGap(0.02D);

        return jfreechart;
    }

    public BrowserAndOS(int graphType) {
        AppletMessageListener.getInstance().setBrowserDelegate(this);

        instance = this;

        setLayout(new BorderLayout(0, 0));
        this.graphType = graphType;
        chart = createChart();
        chartPanel = new ChartPanel(chart);

        popupMenu = chartPanel.getPopupMenu();

        expandGraphItem.setText("Maximize Graph");
        expandGraphItem.setActionCommand("ExpandGraph");
        popupMenu.add(expandGraphItem);

        showAsPercentageItem.setText("Show As Percentage");
        showAsPercentageItem.setActionCommand("ShowAsPercentage");
        popupMenu.add(showAsPercentageItem);

        displayType = WHOLE_NUMBERS;

        SymPopupMenu lSymPopupMenu = new SymPopupMenu();
        popupMenu.addPopupMenuListener(lSymPopupMenu);

        SymAction lSymAction = new SymAction();
        expandGraphItem.addActionListener(lSymAction);
        showAsPercentageItem.addActionListener(lSymAction);

        add(BorderLayout.CENTER, chartPanel);
    }

    public JFreeChart getChart() {
        return chart;
    }

    public void didCompleteBagProcessing(org.jgroups.Message msg) {
    }

    public void didReceiveBrowserBean(Message msg, TransferBean obj) {
        BrowserBean bean = (BrowserBean) obj;
        if (graphType == BROWSER_GRAPH && !bean.isOs()) {
            if (displayType == WHOLE_NUMBERS) {
                piedataset.setValue(bean.getDesc(), bean.getCount());
                displayedPieDataset.setValue(bean.getDesc(), bean.getCount());

            } else if (displayType == PERCENTAGE) {
                // total up all the values
                double pieTotal = 0.0f;
                for (int i = 0, tot = piedataset.getItemCount(); i < tot; i++) {
                    Number value = piedataset.getValue(i);
                    pieTotal += value.doubleValue();
                }
                piedataset.setValue(bean.getDesc(), bean.getCount());
                displayedPieDataset.setValue(bean.getDesc(), ((double) bean.getCount() / (double) pieTotal) * 100);
            }
        } else if (graphType == OS_GRAPH && bean.isOs()) {

            if (displayType == WHOLE_NUMBERS) {
                piedataset.setValue(bean.getDesc(), bean.getCount());
                displayedPieDataset.setValue(bean.getDesc(), bean.getCount());

            } else if (displayType == PERCENTAGE) {
                // total up all the values
                double pieTotal = 0.0f;
                for (int i = 0, tot = piedataset.getItemCount(); i < tot; i++) {
                    Number value = piedataset.getValue(i);
                    pieTotal += value.doubleValue();
                }
                piedataset.setValue(bean.getDesc(), bean.getCount());
                displayedPieDataset.setValue(bean.getDesc(), ((double) bean.getCount() / (double) pieTotal) * 100);
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
        if (getComponentCount() == 0) {
            expandGraphItem.setText("Remove Graph");
            expandGraphItem.setActionCommand("RemoveGraph");
        } else if (getComponentCount() > 0) {
            expandGraphItem.setText("Maximize Graph");
            expandGraphItem.setActionCommand("ExpandGraph");
        }

        if (displayType == WHOLE_NUMBERS) {
            showAsPercentageItem.setText("Show As Percentage");
            showAsPercentageItem.setActionCommand("ShowAsPercentage");
        } else if (displayType == PERCENTAGE) {
            showAsPercentageItem.setText("Show As Whole Numbers");
            showAsPercentageItem.setActionCommand("ShowAsWholeNumbers");
        }

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

            if (event.getActionCommand().equals("ShowAsPercentage")) {
                displayType = PERCENTAGE;

                // first total up the real values
                double pieTotal = 0.0f;
                for (int i = 0, tot = piedataset.getItemCount(); i < tot; i++) {
                    Number value = piedataset.getValue(i);
                    pieTotal += value.doubleValue();
                }

                // then redisplay the pieces as percentages
                for (int i = 0, tot = piedataset.getItemCount(); i < tot; i++) {
                    Number value = piedataset.getValue(i);
                    Comparable comparable = piedataset.getKey(i);
                    displayedPieDataset.setValue(comparable, ((double) value.doubleValue() / (double) pieTotal) * 100);
                }

            } else if (event.getActionCommand().equals("ShowAsWholeNumbers")) {
                displayType = WHOLE_NUMBERS;

                // then redisplay the real values
                for (int i = 0, tot = piedataset.getItemCount(); i < tot; i++) {
                    Number value = piedataset.getValue(i);
                    Comparable comparable = piedataset.getKey(i);
                    displayedPieDataset.setValue(comparable, ((double) value.doubleValue()));
                }


            }
        }
    }
}
