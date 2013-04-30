<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="ARTPage.xsl"    />
<xsl:import href="ViewHistoricalChartsLeftPanel.xsl"    />

  <xsl:template match="DashBoard">
   <a href="/logparser-website/ViewDailySessionSummary.web" title="Return to home page" accesskey="1"><img src="images/ARTlogo.jpg" width="209" alt="ART logo"/></a>
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
<!--
        <h2>Daily Summary Report</h2>
        <dl>
            <dt class="im"/>
            <dd>

            <ul id="ftr">
                <xsl:apply-templates select="$Payload"/>
            </ul>

            <div style="clear: both"></div>
            </dd>
        </dl>
        -->


        <h2>Financial Data for :  
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDay"/><xsl:text>  </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentMonth"/><xsl:text> </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDate"/><xsl:text>, </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentYear"/>
        </h2>
        <dl>
            <dt class="im"/>
            <dd>

            <ul id="ftr">
                <xsl:apply-templates select="$Payload" mode="financial"/>
            </ul>

            <div style="clear: both"></div>
            </dd>
        </dl>


        <!--
        <dl>
            <dt class="im"/>
            <dd>
                <p>Customer Validation Response Time</p>
                <img src="images/graph.jpg"/>
            </dd>

            <dt class="im"/>
            <dd>
                <p>Customer Pricing Response Time</p>
                <img src="images/graph.jpg"/>
            </dd>
    
            <dt class="im"/>
            <dd>
                <p>Campaigns Response Time</p>
                <img src="images/graph.jpg"/>
            </dd>
        </dl>
        -->

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

  <xsl:template match="Payload">
    <table summary="Daily Summary">
        <thead>
            <tr>
                <th scope="col">Summary</th>
                <th scope="col">Value</th>
            </tr>
        </thead>
        <tbody class="one">
            <tr>
                <td class="fl">Day</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/day"/></td>
            </tr>
        </tbody>

        <tbody class="two">
            <tr>
                <td class="fl">30 Second Loads</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/thirtySecondLoads"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td class="fl">15 Second Loads</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/fifteenSecondLoads"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td class="fl">Total Pages Loaded</td>
                <td><xsl:value-of select="format-number(DailySummary/hibernate-custom/DailySummaryBean/totalLoads,'#,##0')"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td class="fl">Average Page Load Time</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/averageLoadTime div 1000"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td class="fl">Ninetieth Percentile</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/ninetiethPercentile div 1000"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td class="fl">Number of Distinct Users</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/distinctUsers"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td class="fl">No. of times error pages displayed</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/errorPages"/></td>
            </tr>
        </tbody>
        <!--
        <tbody class="one">
            <tr>
                <td>TwentyFifthPercentile</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/twentyFifthPercentile"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td>FiftiethPercentile</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/fiftiethPercentile"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td>SeventyFifthPercentile</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/seventyFifthPercentile"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td>MaxLoadTime</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/maxLoadTime"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td>20 Second Loads</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/twentySecondLoads"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td>TenSecondLoads</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/tenSecondLoads"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td>FiveSecondLoads</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/fiveSecondLoads"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td>MaxLoadTime_Page_ID</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/maxLoadTime_Page_ID"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td>MaxLoadTime_User_ID</td>
                <td><xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/maxLoadTime_User_ID"/></td>
            </tr>
        </tbody>
        -->
    </table>            

  </xsl:template>
  <xsl:template match="Payload" mode="financial">
    <table summary="Daily Summary">
        <thead>
            <tr>
                <th scope="col"></th>
                <th scope="col">Orders</th>
                <th scope="col">Lines</th>
                <th scope="col">Dollars</th>
            </tr>
        </thead>
        <tbody class="one">
            <tr>
                <td class="fl">Total Internet:</td>
                <xsl:variable name="totald" select="OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/dollarTotal"/>
                <xsl:variable name="totall" select="OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/lineCount"/>
                <xsl:variable name="totalo" select="OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/orderCount"/>
                <td><xsl:value-of select="format-number($totalo,'###,###,###')"/></td>
                <td><xsl:value-of select="format-number($totall,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number($totald,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>

        <tbody class="two">
            <tr>
                <td class="fl">MaxBuyer</td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opOrderCount,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opLineCount,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opDollarTotal,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td class="fl">MaxBuyer -- Guest:</td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/bxOrders,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/bxLines,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/bxDollars,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td class="fl">Call Center Imprint:</td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/cscImprintOrderCount,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/cscImprintLineCount,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/cscImprintDollarTotal,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td class="fl">In-Progress:</td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/currentOrders,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/currentOrdersTlines,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/currentOrdersTdollars,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>
        <tbody class="two">
            <tr>
                <td class="fl">Deleted &amp; Punchout Deleted:</td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/deletedOrders,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/deletedOrdersTlines,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/deletedOrdersTdollars,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>
    </table>            
  </xsl:template>
  
</xsl:stylesheet>
