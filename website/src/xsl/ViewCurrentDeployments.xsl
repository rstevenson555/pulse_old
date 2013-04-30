<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:import href="ARTPage.xsl"    />
<xsl:import href="ViewHistoricalChartsLeftPanel.xsl"    />

  <xsl:template match="DashBoard">
      <a href="/logparser-website/ViewDailySessionSummary.web" title="Return to home page" accesskey="1">
          <img src="images/ARTlogo.jpg" width="209" alt="ART logo"/> 
      </a>
    <div id="tools">
        <!-- closes #textSize-->
        <div id="sf" style="height:155px;display:none">
        <!-- <label>boiseoffice.com dashboard:</label> -->
            <div id="realtimegraphs">
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
                    <th>Site</th>
                    <th>Instance</th>
                    <th>Application Context</th>
                    <th>Release Tag</th>
                    <th>Deploy Time</th>
                    <th>Properties File</th>
                    <th>Change Control</th>
                    <th>Machine</th>
                    <th>UserID</th>
                </tr>

                <xsl:apply-templates select="$Payload" mode="applyFirst"/>
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
     <xsl:apply-templates select="CurrentDeployments/CurrentElement" mode="data">
     </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="CurrentDeployments/CurrentElement" mode="data">
    <xsl:if test="position() mod 2 =0">
        <tbody class="two">
            <tr>
                <td class="left">
                    <xsl:value-of select="Product"/>
               </td>
                <td class="left">
                    <xsl:value-of select="ServerGroup"/>
               </td>
                 <td class="left">
                    <xsl:value-of select="ApplicationContext"/>
                </td>
                <td class="left">
                    <xsl:value-of select="ReleaseTag"/>
                </td>
                <td class="left">
                    <xsl:value-of select="DeployTime"/>
                </td>
                <td class="left">
                    <xsl:value-of select="PropertiesFile"/>
                </td>
                <td class="left">
                    <xsl:value-of select="ChangeControll"/>
                </td>
                <td class="left">
                    <xsl:value-of select="Machine"/>
                </td>
                <td class="left">
                    <xsl:value-of select="UserID"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
    <xsl:if test="position() mod 2 =1">
        <tbody class="one">
            <tr>
                <td class="left">
                    <xsl:value-of select="Product"/>
               </td>
                <td class="left">
                    <xsl:value-of select="ServerGroup"/>
               </td>
                 <td class="left">
                    <xsl:value-of select="ApplicationContext"/>
                </td>
                <td class="left">
                    <xsl:value-of select="ReleaseTag"/>
                </td>
                <td class="left">
                    <xsl:value-of select="DeployTime"/>
                </td>
                <td class="left">
                    <xsl:value-of select="PropertiesFile"/>
                </td>
                <td class="left">
                    <xsl:value-of select="ChangeControll"/>
                </td>
                <td class="left">
                    <xsl:value-of select="Machine"/>
                </td>
                <td class="left">
                    <xsl:value-of select="UserID"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
    
  </xsl:template>
</xsl:stylesheet>
