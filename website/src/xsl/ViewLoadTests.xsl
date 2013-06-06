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



    <form name="loadTestDiffingForm" method="post" action="ViewLoadTestDiff.web">

    <table class="detaildata" summary="Daily Summary">
                <tr>



                    <th>LoadTest:</th>
                    <th>Compare:</th>
                    <th>Test Date:</th>
                    <th>Branch:</th>
<th>Context:</th>
<th>StartTime:</th>
<th>EndTime:</th>
<th>remove:</th>
<th>save:</th>
                </tr>

                <xsl:apply-templates select="$Payload" mode="applyFirst"/>
                </table>

                      <input>
                          <xsl:attribute name="type">submit</xsl:attribute>
                          <xsl:attribute name="name">CompareName</xsl:attribute>
                      </input>

      </form>




            </ul>

            <div style="clear: both"></div>
            </dd>
        </dl>

        <h2>Contact us on issues identified in ART</h2>
        <dl class="footerbg">
            <dt class="im">
                <a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">
                    <img src="images/ico-bugz.gif" width="34" height="34" alt="Bugzilla"/>
                </a>
            </dt>
            <dt><a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">Bugzilla</a></dt>
            <dd>

                <p>Report a bug for ART 2.0.[<a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">more</a>]</p>
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

  <xsl:template match="Payload" mode="applyFirst">
     <xsl:apply-templates select="LoadTests/LoadTest" mode="data">
     </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="LoadTests/LoadTest" mode="data">
    <xsl:if test="position() mod 2 =0">
        <tbody class="two">
            <tr>
            <!--
LoadTest
LoadTestName
Context
Branch
StartTime
EndTime

-->
                <td>
                <a>
                    <xsl:attribute name="href">ViewLoadTestDetail.web?loadTestID=<xsl:value-of select="LoadTestID"/>
                    </xsl:attribute>
                    <xsl:attribute name="class">nav</xsl:attribute> 
                    <xsl:value-of select="LoadTestName"/>
                </a>
                </td>
                <td>
                      <input>
                          <xsl:attribute name="type">radio</xsl:attribute>
                          <xsl:attribute name="name">baseReport</xsl:attribute>
                          <xsl:attribute name="value"><xsl:value-of select="LoadTestID"/></xsl:attribute>
                      </input>
                      <input>
                          <xsl:attribute name="type">checkbox</xsl:attribute>
                          <xsl:attribute name="name"><xsl:value-of select="LoadTestID"/></xsl:attribute>
                      </input>
                </td>
                <td>
                    <xsl:value-of select="substring(StartTime,1,10)"/>
                </td>
                <td>
                    <xsl:value-of select="Branch"/>
                </td>
                <td>
                    <xsl:value-of select="Context"/>
                </td>
                <td>
                    <xsl:value-of select="substring(StartTime,11)"/>
                </td>
                <td>
                    <xsl:value-of select="substring(EndTime,11)"/>
                </td>
                <td class="leftMessage">
                <a>
                    <xsl:attribute name="href">LoadTestStatus.web?status=Remove&amp;LoadTestID=<xsl:value-of select="LoadTestID"/>
                    </xsl:attribute>
                    <xsl:attribute name="class">nav</xsl:attribute> 
                    remove
                </a>
                </td>
                <td class="leftMessage">
                <a>
                    <xsl:attribute name="href">LoadTestStatus.web?status=Keep&amp;LoadTestID=<xsl:value-of select="LoadTestID"/>
                    </xsl:attribute>
                    <xsl:attribute name="class">nav</xsl:attribute> 
                    save
                </a>
                </td>
            </tr>
        </tbody>
    </xsl:if>
    <xsl:if test="position() mod 2 =1">
        <tbody class="one">
            <tr>
                <td>
                <a>
                    <xsl:attribute name="href">ViewLoadTestDetail.web?loadTestID=<xsl:value-of select="LoadTestID"/>
                    </xsl:attribute>
                    <xsl:attribute name="class">nav</xsl:attribute> 
                    <xsl:value-of select="LoadTestName"/>
                </a>
                </td>
                <td>
                      <input>
                          <xsl:attribute name="type">radio</xsl:attribute>
                          <xsl:attribute name="name">baseReport</xsl:attribute>
                          <xsl:attribute name="value"><xsl:value-of select="LoadTestID"/></xsl:attribute>
                      </input>
                      <input>
                          <xsl:attribute name="type">checkbox</xsl:attribute>
                          <xsl:attribute name="name"><xsl:value-of select="LoadTestID"/></xsl:attribute>
                      </input>
                </td>
                <td>
                    <xsl:value-of select="substring(StartTime,1,10)"/>
                </td>
                <td>
                    <xsl:value-of select="Branch"/>
                </td>
                <td>
                    <xsl:value-of select="Context"/>
                </td>
                <td>
                    <xsl:value-of select="substring(StartTime,11)"/>
                </td>
                <td>
                    <xsl:value-of select="substring(EndTime,11)"/>
                </td>
                <td class="leftMessage">
                <a>
                    <xsl:attribute name="href">LoadTestStatus.web?status=Remove&amp;LoadTestID=<xsl:value-of select="LoadTestID"/>
                    </xsl:attribute>
                    <xsl:attribute name="class">nav</xsl:attribute> 
                    remove
                </a>
                </td>
                <td class="leftMessage">
                <a>
                    <xsl:attribute name="href">LoadTestStatus.web?status=Keep&amp;LoadTestID=<xsl:value-of select="LoadTestID"/>
                    </xsl:attribute>
                    <xsl:attribute name="class">nav</xsl:attribute> 
                    save
                </a>
                </td>

            </tr>
        </tbody>
    </xsl:if>
    
  </xsl:template>
</xsl:stylesheet>
