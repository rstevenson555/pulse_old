package com.bos.applets;

import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JApplet;
import org.apache.oro.text.regex.*;

/**
 * A basic extension of the javax.swing.JApplet class
 */
public class PageInputQuery extends JApplet {

    public void init() {
        // This code is automatically generated by Visual Cafe when you add
        // components to the visual environment. It instantiates and initializes
        // the components. To modify the code, only use code syntax that matches
        // what Visual Cafe can generate, or Visual Cafe may be unable to back
        // parse your Java file into its visual environment.
        //{{INIT_CONTROLS
        getContentPane().setLayout(null);
        getContentPane().setBackground(new java.awt.Color(242, 217, 161));
        setSize(187, 216);
        getContentPane().add(startTime);
        startTime.setFont(new Font("Dialog", Font.PLAIN, 11));
        startTime.setBounds(24, 24, 75, 16);
        getContentPane().add(endTime);
        endTime.setFont(new Font("Dialog", Font.PLAIN, 11));
        endTime.setBounds(24, 64, 75, 16);
        getContentPane().add(context);
        context.setFont(new Font("Dialog", Font.PLAIN, 11));
        context.setBounds(24, 104, 120, 16);
        getContentPane().add(pageName);
        pageName.setFont(new Font("Dialog", Font.PLAIN, 11));
        pageName.setBounds(24, 144, 144, 16);
        JLabel1.setText("Start Time");
        getContentPane().add(JLabel1);
        JLabel1.setFont(new Font("Dialog", Font.BOLD, 11));
        JLabel1.setBounds(24, 8, 92, 16);
        JLabel2.setText("End Time");
        getContentPane().add(JLabel2);
        JLabel2.setFont(new Font("Dialog", Font.BOLD, 11));
        JLabel2.setBounds(24, 48, 92, 16);
        JLabel3.setText("Page(s)");
        getContentPane().add(JLabel3);
        JLabel3.setFont(new Font("Dialog", Font.BOLD, 11));
        JLabel3.setBounds(24, 128, 92, 16);
        JLabel4.setText("Context(s)");
        getContentPane().add(JLabel4);
        JLabel4.setFont(new Font("Dialog", Font.BOLD, 11));
        JLabel4.setBounds(24, 88, 92, 16);
        submit.setBorderPainted(false);
        submit.setText("Submit");
        getContentPane().add(submit);
        submit.setBackground(new java.awt.Color(242, 217, 161));
        submit.setFont(new Font("Dialog", Font.BOLD, 11));
        submit.setBounds(48, 180, 80, 16);
        //}}

        //{{REGISTER_LISTENERS
        SymAction lSymAction = new SymAction();
        submit.addActionListener(lSymAction);
        //}}
    }
    //{{DECLARE_CONTROLS
    javax.swing.JTextField startTime = new javax.swing.JTextField();
    javax.swing.JTextField endTime = new javax.swing.JTextField();
    javax.swing.JTextField context = new javax.swing.JTextField();
    javax.swing.JTextField pageName = new javax.swing.JTextField();
    javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
    javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
    javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
    javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
    javax.swing.JButton submit = new javax.swing.JButton();
    //}}
    static private PatternCompiler compiler = new Perl5Compiler();
    static private Pattern time1Pattern = null;
    static private Pattern time2Pattern = null;
    static private Pattern time3Pattern = null;

    static {
        try {
            // accept a time format
            time1Pattern = compiler.compile("([0-9][0-9]):([0-9][0-9])");
            time2Pattern = compiler.compile("([1-9]):([0-9][0-9])");
            time3Pattern = compiler.compile("([1-9]):([0-9])");
        } catch (MalformedPatternException e) {
            System.out.println("Bad Pattern: " + e);
        }
    }

    class SymAction implements java.awt.event.ActionListener {

        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == submit) {
                submit_actionPerformed(event);
            }
        }
    }

    void submit_actionPerformed(java.awt.event.ActionEvent event) {
        String host = getCodeBase().getHost();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

        String docBase = getDocumentBase().toExternalForm();
        if (docBase.indexOf("?") != -1) {
            docBase = docBase.substring(0, docBase.indexOf("?"));
        }
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        Integer hour = null;
        Integer min = null;
        PatternMatcher matcher = new Perl5Matcher();
        PatternMatcherInput startTimeInput = null;
        
        startTimeInput = new PatternMatcherInput(startTime.getText());
        if (matcher.contains(startTimeInput, time1Pattern)) {
            MatchResult result = matcher.getMatch();
            int groups = result.groups();
            hour = new Integer(result.group(1));
            min = new Integer(result.group(2));
        } else {
            startTimeInput = new PatternMatcherInput(startTime.getText());
            if (matcher.contains(startTimeInput, time2Pattern)) {
                MatchResult result = matcher.getMatch();
                int groups = result.groups();
                hour = new Integer(result.group(1));
                min = new Integer(result.group(2));
            } else {
                startTimeInput = new PatternMatcherInput(startTime.getText());
                if (matcher.contains(startTimeInput, time3Pattern)) {
                    MatchResult result = matcher.getMatch();
                    int groups = result.groups();
                    hour = new Integer(result.group(1));
                    min = new Integer(result.group(2));
                }
            }
        }
        if (hour == null || min == null) {
            System.out.println("Error parsing start time input");
            return;
        }
        start.set(Calendar.HOUR_OF_DAY, hour.intValue());
        start.set(Calendar.MINUTE, min.intValue());
        start.set(Calendar.SECOND, 0);
        System.out.println("start time is : " + start);
        System.out.println("hour: " + hour.intValue());
        System.out.println("min: " + min.intValue());
        String startTimeString = format.format(start.getTime());
        // now do the end -time
        hour = null;
        min = null;
        PatternMatcherInput endTimeInput = null;
        matcher = new Perl5Matcher();
        endTimeInput = new PatternMatcherInput(endTime.getText());
        if (matcher.contains(endTimeInput, time1Pattern)) {
            MatchResult result = matcher.getMatch();
            int groups = result.groups();
            hour = new Integer(result.group(1));
            min = new Integer(result.group(2));
        } else {
            endTimeInput = new PatternMatcherInput(endTime.getText());
            if (matcher.contains(endTimeInput, time2Pattern)) {
                MatchResult result = matcher.getMatch();
                int groups = result.groups();
                hour = new Integer(result.group(1));
                min = new Integer(result.group(2));
            } else {
                endTimeInput = new PatternMatcherInput(endTime.getText());
                if (matcher.contains(endTimeInput, time3Pattern)) {
                    MatchResult result = matcher.getMatch();
                    int groups = result.groups();
                    hour = new Integer(result.group(1));
                    min = new Integer(result.group(2));
                }
            }
        }
        if (hour == null || min == null) {
            System.out.println("Error parsing end time input");
            return;
        }
        end.set(Calendar.HOUR_OF_DAY, hour.intValue());
        end.set(Calendar.MINUTE, min.intValue());
        end.set(Calendar.SECOND, 0);
        String endTimeString = format.format(end.getTime());
        String url = docBase + "?start=" + startTimeString + "&end=" + endTimeString
                + "&context=" + context.getText() + "&page=" + pageName.getText();

        java.applet.AppletContext context = this.getAppletContext();
        try {
            System.out.println("requesting url: " + url);
            context.showDocument(new java.net.URL(url));
        } catch (java.net.MalformedURLException mfue) {
            System.err.println("bad url" + mfue);
        }
    }
}
