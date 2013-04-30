<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="ARTPage.xsl"    />

  <xsl:template match="DashBoard">
   <a href="/logparser-website/ViewDailySessionSummary.web" title="Return to home page" accesskey="1"><img src="images/ARTlogo.jpg" width="209" alt="ART logo"/></a>
    <div id="tools">

        <br/>

        <div id="sf" style="height:135px;">
            <div id="realtimegraphs">
                <div id="chatdiv">

                    <APPLET ARCHIVE="commons-logging.jar,log4j.jar,jfreechart-1.0.14.jar,commons-collections-3.0.jar,jcommon-1.0.17.jar,logParser-applets.jar,LiveLogParser.jar,jgroups.jar,concurrent.jar,bos-common-logging.jar,jaws.jar,commons-codec-1.4.jar,joda-time-2.1.jar" 
                        CODE="com.bos.applets.LiveSessions"
                        mayscript="true" 
                        width="745" height="130" ALT="You should see an applet, not this text.">
                        <param name="plot_image" VALUE = "/images/bg2.gif"/>
                        <param name="java_version" value="1.5+"/>
                        <param name="chart_bgcolor" VALUE = "#FFFFFF"/>
                    </APPLET>  
                </div>
            </div>
        </div>
     </div>
  </xsl:template>

  <xsl:template match="Body">
    <hr class="hide"/>
 
    <div id="mainContent" >
        <div id="which">

        <h2>Average LoadTime Performance</h2>
        <dl class="artTableFrame">
            <dt class="im"/>
            <dd>

            <ul id="ftr">
                <p class="d1">Realtime graph of the Average Page Load Time by minute for the entire cluster</p>
                    <APPLET ARCHIVE="commons-logging.jar,log4j.jar,jfreechart-1.0.14.jar,commons-collections-3.0.jar,jcommon-1.0.17.jar,logParser-applets.jar,LiveLogParser.jar,jgroups.jar,concurrent.jar,bos-common-logging.jar,jaws.jar,commons-codec-1.4.jar,joda-time-2.1.jar" 
                            CODE="com.bos.applets.AvgLoadTime"
                            mayscript="true" 
                            width="720" height="200" ALT="Your browser has java disabled, please install a recent version of Java">
                        <!-- <param name="plot_bgcolor" VALUE = "#A57B46"/> -->
                        <param name="plot_image" VALUE = "/images/bg6.gif"/>
                        <param name="java_version" value="1.5+"/>
                        <param name="chart_bgcolor" VALUE = "#FFFFFF"/>
                    </APPLET>
            </ul>

            <div style="clear: both"></div>
            </dd>
        </dl>


        <h2>OffBox Performance</h2>
        <dl class="artTableFrame">
            <ul id="ftr">
                <p class="d1">The following graphs are realtime views of all off-box transactions</p>
                <APPLET ARCHIVE="commons-logging.jar,log4j.jar,jfreechart-1.0.14.jar,commons-collections-3.0.jar,jcommon-1.0.17.jar,logParser-applets.jar,LiveLogParser.jar,jgroups.jar,concurrent.jar,bos-common-logging.jar,jaws.jar,commons-codec-1.4.jar,joda-time-2.1.jar" 
                            CODE="com.bos.applets.OffBoxGraphingApplet"
                            mayscript="true" 
                            width="740" height="600" ALT="Your browser has java disabled, please install a recent version of Java">
                        <param name="plot_image" VALUE = "/images/bg6.gif"/>
                        <param name="java_version" value="1.5+"/>
                        <param name="chart_bgcolor" VALUE = "#FFFFFF"/>
                    </APPLET>
            </ul>
       
        </dl>

        <h2>Contact us on issues identified in ART</h2>
        <dl class="artTableFrame footerbg" >
            <dt class="im">
                <a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">
                    <img src="images/ico-bugz.gif" width="34" height="34" alt="Bugzilla"/>
                </a>
            </dt>
            <dt><a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">Bugzilla</a></dt>
            <dd>

                <p>Enterprise-grade bug tracking software [<a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">more</a>]</p>
            </dd>
        </dl>
    </div>
    <!-- closes #which -->
  
    <hr class="hide"/>
    <div id="footer">

        <ul id="bn">
            <li><a href="sitemap.html">Site Map</a></li>
            <li><a href="contact/">Contact Us</a></li>
            <li><a href="foundation/donate.html">About ART</a></li>
            <li><a href="http://java.sun.com/webapps/getjava/BrowserRedirect?locale=en&amp;host=www.java.com:80">Install Java</a></li>
        </ul>
        <p>Copyright ; 1998-2003 BoiseOffice Solutions</p>
    </div>

    </div>
    <!-- closes #mainContent-->
  </xsl:template>

  <xsl:template match="LeftPanel">
    <div id="side">
        <h2>Other Graphs</h2>
        <p>Navigate to other graphs and information...</p>

    <ul id="oN">
        <li>
          <a href="ViewRealTimeCharts.web">Realtime Charts</a>
        </li>
        <li>
          <a href="ViewDailySessionSummary.web">Daily Snapshot</a>
        </li>
         <li>
          <a href="ClickStreamPageView.web">Click Stream</a>
        </li>
        <li>
          <a href="ViewDailyPageLoadCharts.web">Page Load Details</a>
        </li>
        <li>
          <a href="ViewCurrentDeployments.web">Current Deployments</a>
        </li>
        <li>
          <a href="OnlineReportUsageDetail.web">Online Reporting</a>
        </li>
        <li>
          <a href="ViewExceptions.web">Exceptions (LIVE!)</a>
        </li>
        <li>
          <a href="ViewHistoricalCharts.web">Financial (24hr-delay)</a>
        </li>
        <li>
            <a href="ViewTimeSliceDetail.web">Time Slice Report</a>
        </li>
        <li>
          <a href="http://java.sun.com/webapps/getjava/BrowserRedirect?locale=en&amp;host=www.java.com:80">
          Install Java</a>
        </li>
      </ul>
    
    </div>
    <!-- closes #side -->
  </xsl:template>


</xsl:stylesheet>
