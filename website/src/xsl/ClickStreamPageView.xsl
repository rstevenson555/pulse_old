<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="ARTPage.xsl"    />
    <xsl:import href="ViewHistoricalChartsLeftPanel.xsl"    />

    <xsl:template match="DashBoard">
        <a href="ViewDailySessionSummary.web" title="Return to home page" accesskey="1">
            <img src="images/ekg.gif" width="209" style="height:155px;" alt="Pulse logo"/>
        </a>
        <div id="tools">
            <ul id="mainNav" style="display:none;">
                <li>
                    <a href="download.html" title="Real time charts of boiseoffice.com performance">Production Environment</a>
                </li>
                <li>
                    <a href="products/" title="Historical charts of boiseoffice.com performance">Test Environment</a>
                </li>
                <li>
                    <a href="support/" title="Report a bug">Development Environment</a>
                </li>
            </ul>
        <!-- closes #textSize-->
            <div id="sf" style="height:155px;display:none">
        <!-- <label>boiseoffice.com dashboard:</label> -->
                <div id="realtimegraphs">
            
                </div>
            </div>
        </div>
    <!-- closes #tools-->
    </xsl:template>

    <xsl:template match="Body">
        <hr class="hide"/>

 
        <div id="mainContent" style="margin-top: -170px;position: relative;">

            <div id="which">

                <h2>Click Stream Playback</h2>
                <dl>
                    <dt class="im"/>
                    <dd>

                        <ul id="ftr">
                            <table class="detaildata" summary="Click Summary">
                                <tr>
                                    <th>Click #</th>
                                    <th>Page Name</th>
                    <!--<th>time</th>
                    <th>loadTime</th>
                    <th>pageName</th>
                    <th>branchName</th>
                    <th>contextName</th>
                    <th>sessionTxt</th>
                    <th>userName</th>-->
                                </tr>

                                <xsl:apply-templates select="$Payload" mode="applyFirst"/>
                            </table>
                        </ul>

                        <div style="clear: both"></div>
                    </dd>
                </dl>

                <h2>The users View  </h2>
                <dl>
                    <dt class="im">
                        <iframe> 
                            <xsl:attribute name="src">
                                <xsl:value-of select="concat('./iview.web?HtmlPage_ID=',HtmlPageResponse_ID)"/>
                            </xsl:attribute>
                            <xsl:attribute name="name">htmlpageviewframe</xsl:attribute>
                            <xsl:attribute name="width">1024</xsl:attribute>
                            <xsl:attribute name="height">600</xsl:attribute>
                            <xsl:attribute name="align">center</xsl:attribute>
                            <xsl:attribute name="noresize"/>
                            Your browser does not support IFrame consider using http://www.mozilla.org/firefox 
                        </iframe>
                    </dt>
                </dl>

            </div>
    <!-- closes #which -->
  
            <hr class="hide"/>
            <div id="footer">

                <ul id="bn">
                    <li>
                        <a href="sitemap.html">Site Map</a>
                    </li>
                    <li>
                        <a href="contact/">Contact Us</a>
                    </li>
                    <li>
                        <a href="foundation/donate.html">About ART</a>
                    </li>
                    <li>
                        <a href="http://java.sun.com/webapps/getjava/BrowserRedirect?locale=en&amp;host=www.java.com:80">Install Java</a>
                    </li>
                </ul>
                <p>Copyright ; 1998-2003 Officemax</p>
            </div>

        </div>
    <!-- closes #mainContent-->
    </xsl:template>

    <xsl:template match="Payload" mode="applyFirst">
        <xsl:apply-templates select="ClickElements/ClickElement" mode="data">
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="ClickElements/ClickElement" mode="data">
        <xsl:if test="position() mod 2 =0">
            <tbody class="two">
                <tr>
                    <td>

                        <xsl:value-of select="RecordID"/>
                    </td>
                    <td class="leftMessage">    
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="concat('./iview.web?HtmlPage_ID=',HtmlPageResponse_ID)"/>
                            </xsl:attribute>
                            <xsl:attribute name="target">htmlpageviewframe</xsl:attribute>
                            <xsl:value-of select="PageName"/>
                        </a>
                    </td>
                    <!--<td class="leftMessage">    
                        <xsl:value-of select="requestToken"/>
                    </td>-->
                </tr>
            </tbody>
        </xsl:if>
        <xsl:if test="position() mod 2 =1">
            <tbody class="one">
                <tr>
                    <td>

                        <xsl:value-of select="RecordID"/>
                    </td>
                    <td class="leftMessage">    
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="concat('./iview.web?HtmlPage_ID=',HtmlPageResponse_ID)"/>
                            </xsl:attribute>
                            <xsl:attribute name="target">htmlpageviewframe</xsl:attribute>
                            <xsl:value-of select="PageName"/>
                        </a>
                    </td>
                    <!--<td class="leftMessage">    
                        <xsl:value-of select="requestToken"/>
                    </td>-->
                </tr>
            </tbody>
        </xsl:if>
    
    </xsl:template>
</xsl:stylesheet>
