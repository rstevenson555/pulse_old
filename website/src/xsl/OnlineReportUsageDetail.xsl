<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:import href="ARTPage.xsl"    />
<xsl:import href="ViewHistoricalChartsLeftPanel.xsl"    />

  <xsl:template match="DashBoard">
   <a href="ViewDailySessionSummary.web" title="Return to home page" accesskey="1"><img src="images/ekg.gif" width="209" style="height:155px;" alt="Pulse logo"/></a>
    <div id="tools">
        <ul id="mainNav" style="display:none;">
            <li><a href="download.html" title="Real time charts of boiseoffice.com performance">Production Environment</a></li>
            <li><a href="products/" title="Historical charts of boiseoffice.com performance">Test Environment</a></li>
            <li><a href="support/" title="Report a bug">Development Environment</a></li>
        </ul>
        <!-- closes #textSize-->
        <div id="sf" style="height:155px;display:none">
        <!-- <label>boiseoffice.com dashboard:</label> -->
            <div id="realtimegraphs">
            <!--
                <img src="images/newtechnom1.jpg" alt="Server 1 - Wait Queue: 3.2"/>
                <img src="images/newtechnom2.jpg" alt="Server 2 - Wait Queue: 3.0"/>
                <img src="images/newtechnom3.jpg" alt="Server 3 - Wait Queue: 2.2"/>
                <img src="images/newtechnom4.jpg" alt="Server 4 - Wait Queue: 1.9"/>
            -->
                <!--<APPLET ARCHIVE="jfreechart-0.9.16.jar,jcommon-0.9.1.jar,logParser-applets.jar" 
                    CODE="com.bos.applets.SpeedoMeterApplet"
                    width="70" height="70" ALT="You should see an applet, not this text.">
                </APPLET> -->
            </div>
        </div>
    </div>
    <!-- closes #tools-->
  </xsl:template>

  <xsl:template match="Body">
    <hr class="hide"/>

 
    <div id="mainContent" style="margin-top: -170px;position: relative;">

        <div id="which">

        <h2>Daily Page Load Time Reports:</h2>
        <dl>
            <dt class="im"/>
            <dd>

            <ul id="ftr">
               <table class="detaildata" summary="Daily Summary">
               <xsl:apply-templates select="$Payload" mode="tableHeader"/>
                <!--
                <tr>
                    <th>ServerGroup</th>
                    <th>ApplicationContext</th>
                    <th>ReleaseTag</th>
                    <th>DeployTime</th>
                    <th>PropertiesFile</th>
                    <th>ChangeControll</th>
                    <th>Machine</th>
                    <th>UserID</th>
                </tr>
                -->

                <xsl:apply-templates select="$Payload" mode="applyFirst"/>
                </table>
            </ul>

            <div style="clear: both"></div>
            </dd>
        </dl>

        <h2>Image of OR Performance</h2>
        <dl>
            <dt class="im"/>
            <dd>
                <img src="./MonthlyTrendChart.web?width=800&amp;height=375&amp;start=20050101000000&amp;end=20051231235959&amp;context=onlinereporting"/>
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

  <xsl:template match="Payload" mode="tableHeader">
    
    <tr>
     <th>report type:</th>
     <xsl:apply-templates select="MonthlyOnlineReports/yearMonths" mode="printHeader"/>
     </tr>
  </xsl:template>
  <xsl:template match="MonthlyOnlineReports/yearMonths" mode="printHeader">
       <xsl:for-each select="yearMonth">
          <th>
             <xsl:value-of select="."/>
          </th>
       </xsl:for-each>
  </xsl:template>

  <xsl:template match="Payload" mode="applyFirst">
     <xsl:apply-templates select="MonthlyOnlineReports/ReportTypes/ReportType" mode="data"/>
  </xsl:template>

  <xsl:template match="MonthlyOnlineReports/ReportTypes/ReportType" mode="data">
    <xsl:if test="position() mod 2 =0">
        <tbody class="two">
            <tr>
                <td class="left">
                    <xsl:value-of select="Description"/>
               </td>
               <xsl:for-each select="Records/Count">
                 <td class="left">
                    <xsl:value-of select="."/>
                 </td>
               </xsl:for-each>  
            </tr>
        </tbody>
    </xsl:if>
    <xsl:if test="position() mod 2 =1">
        <tbody class="one">
          <tr>
             <td class="left">
                <xsl:value-of select="Description"/>
             </td>
             <xsl:for-each select="Records/Count">
               <td class="left">
                  <xsl:value-of select="."/>
               </td>
             </xsl:for-each>  
          </tr>
        </tbody>
    </xsl:if>
    
  </xsl:template>
</xsl:stylesheet>
