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
                <tr>
                    <th>Stack Line:</th>
                    <th>Call Stack Trace: </th>
                </tr>

                <xsl:apply-templates select="$Payload" mode="applyFirst"/>
            </table>
            </ul>

            <ul id="ftr">
            <table class="detaildata" summary="Daily Summary">
                <tr>
                    <th>Time</th>
                    <th>Context</th>
                    <th>Branch</th>
                    <th>Page</th>
                    <th>QueryParams</th>
                    <th>RequestParams</th>
                </tr>

                <xsl:apply-templates select="$Payload" mode="applySecond"/>
            </table>
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
     <xsl:apply-templates select="ExceptionRecord/StackTrace/TraceLine" mode="data"/>
  </xsl:template>

  <xsl:template match="Payload" mode="applySecond">
     <xsl:apply-templates select="ClickStream/Request" mode="ClickData"/>
  </xsl:template>

  <xsl:template match="ExceptionRecord/StackTrace/TraceLine" mode="data">
    <xsl:if test="position() mod 2 =0">
        <tbody class="two">
            <tr>
                <td>
                    <xsl:value-of select="Depth"/>
                </td>
                <td class="leftMessage">    
                    <xsl:value-of select="RowMessage"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
    <xsl:if test="position() mod 2 =1">
        <tbody class="one">
            <tr>
                <td>
                    <xsl:value-of select="Depth"/>
                </td>
                <td class="leftMessage">    
                    <xsl:value-of select="RowMessage"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
    
  </xsl:template>

  <xsl:template match="ClickStream/Request" mode="ClickData">
    <xsl:choose>
      <xsl:when test="RequestType =1">
        <tbody class="two">
            <tr>
                <td>
                    <xsl:value-of select="Time/Hour"/>
                </td>
                <td>
                    <xsl:value-of select="ContextName"/>
                </td>
                <td>
                    <xsl:value-of select="BranchName"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="PageName"/>
                </td>
                <td class="leftMessage">
                   Forward 
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="QueryParams"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="RequestParams"/>
                </td>
            </tr>
        </tbody>
      </xsl:when>
      <xsl:when test="RequestType =2">
        <tbody class="one">
            <tr>
                <td>
                    <xsl:value-of select="Time/Hour"/>
                </td>
                <td>
                    <xsl:value-of select="ContextName"/>
                </td>
                <td>
                    <xsl:value-of select="BranchName"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="PageName"/>
                </td>
                <td class="leftMessage">
                    User Click
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="QueryParams"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="RequestParams"/>
                </td>
            </tr>
        </tbody>
      </xsl:when>
      <xsl:when test="position() mod 2 =1">
        <tbody class="one">
            <tr>
                <td>
                    <xsl:value-of select="Time/Hour"/>
                </td>
                <td>
                    <xsl:value-of select="ContextName"/>
                </td>
                <td>
                    <xsl:value-of select="BranchName"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="PageName"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="RequestType"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="QueryParams"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="RequestParams"/>
                </td>
            </tr>
        </tbody>
      </xsl:when>
      <xsl:when test="position() mod 2 =0">
        <tbody class="two">
            <tr>
                <td>
                    <xsl:value-of select="Time/Hour"/>
                </td>
                <td>
                    <xsl:value-of select="ContextName"/>
                </td>
                <td>
                    <xsl:value-of select="BranchName"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="PageName"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="RequestType"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="QueryParams"/>
                </td>
                <td class="leftMessage">
                    <xsl:value-of select="RequestParams"/>
                </td>
            </tr>
        </tbody>
    </xsl:when>
    </xsl:choose>
    
  </xsl:template>
</xsl:stylesheet>
