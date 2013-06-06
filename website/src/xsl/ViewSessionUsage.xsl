<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="ARTPage.xsl"    />
<xsl:import href="ViewHistoricalChartsLeftPanel.xsl"    />

  <xsl:template match="DashBoard">
   <a href="ViewDailySessionSummary.web" title="Return to home page" accesskey="1"><img src="images/ekg.gif" width="209" style="height:155px;" alt="Pulse logo"/></a>
    <div id="tools">
        <!--<ul id="mainNav" style="display:none;">
            <li><a href="download.html" title="Real time charts of boiseoffice.com performance">Production Environment</a></li>
            <li><a href="products/" title="Historical charts of boiseoffice.com performance">Test Environment</a></li>
            <li><a href="support/" title="Report a bug">Development Environment</a></li>
        </ul> -->
        <!-- closes #textSize-->
        <div id="sf" style="height:155px;display:none">
            <div id="realtimegraphs">
                <div id="chatdiv">
          
                <!--
                    <APPLET ARCHIVE="commons-logging.jar,log4j.jar,jfreechart-0.9.18.jar,commons-collections-3.0.jar,jcommon-0.9.3.jar,logParser-applets.jar,LiveLogParser.jar,jgroups-core.jar,bos-common-logging.jar" 
                        CODE="com.bos.applets.MessagingApplet"
                        width="325" height="120" ALT="You should see an applet, not this text.">
                        <param name="plot_image" VALUE = "/images/bg2.gif"/>
                        <param name="chart_bgcolor" VALUE = "#FFFFFF"/>
                    </APPLET> 
          
                    <APPLET ARCHIVE="commons-logging.jar,log4j.jar,jfreechart-0.9.18.jar,commons-collections-3.0.jar,jcommon-0.9.3.jar,logParser-applets.jar,LiveLogParser.jar,jgroups-core.jar,bos-common-logging.jar" 
                        CODE="com.bos.applets.LiveSessions"
                        width="220" height="120" ALT="You should see an applet, not this text.">
                        <param name="plot_image" VALUE = "/images/bg2.gif"/>
                        <param name="chart_bgcolor" VALUE = "#FFFFFF"/>
                    </APPLET> 
                    -->
                </div>
            </div>
        </div>
    </div>
    <!-- closes #tools-->
  </xsl:template>

  <xsl:template match="Body">
    <hr class="hide"/>

 
    <div id="mainContent" style="margin-top: -170px;position: relative;">

        <div id="which">

        <h2>Daily Summary:<xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDay"/><xsl:text>  </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentMonth"/><xsl:text> </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDate"/><xsl:text>, </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentYear"/>
                 <div id="subcomment"> <xsl:text>  (60 seconds delayed) </xsl:text> </div>
        </h2>
        <dl>
            <dt class="im"/>
            <dd>

            <ul id="ftr">
                <xsl:apply-templates select="$Payload"/>
            </ul>

            <div style="clear: both"></div>
            </dd>
        </dl>
<h2>Dollars and Lines Data:<xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDay"/><xsl:text>  </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentMonth"/><xsl:text> </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDate"/><xsl:text>, </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentYear"/> <div id="subcomment"> <xsl:text>  (60 seconds delayed) </xsl:text> </div>
        </h2>
        <dl>
            <dt class="im"/>
            <dd>

            <ul id="ftr">
                <xsl:apply-templates select="$Payload/DollarsLines" mode="DollarsLines"/>
            </ul>

            <div style="clear: both"></div>
            </dd>
        </dl>

        <h2>Session Usage Data:<xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDay"/><xsl:text>  </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentMonth"/><xsl:text> </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDate"/><xsl:text>, </xsl:text>
                <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentYear"/> <div id="subcomment"> <xsl:text>  (30 Min delayed) </xsl:text> </div>
        </h2>
        <dl>
            <dt class="im"/>
            <dd>

            <ul id="ftr">
                <xsl:apply-templates select="$Payload/SessionElement" mode="Sessions"/>
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
        <p>Copyright ; 1998-2003 Officemax</p>
    </div>

    </div>
    <!-- closes #mainContent-->
  </xsl:template>

  <xsl:template match="Payload">
    <table style="width:650px;" summary="Daily Summary">
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
                <td class="fl">
                
              <a>
                    <xsl:attribute name="href">View30SecondLoads.web?selectedDate=<xsl:value-of select="format-number($Payload/Calendar/CalendarBean/CurrentYear,'00')"/><xsl:value-of select="format-number($Payload/Calendar/CalendarBean/CurrentMonthNumber,'00')"/><xsl:value-of select="format-number($Payload/Calendar/CalendarBean/CurrentDate,'00')"/>
                    </xsl:attribute>
                    <xsl:attribute name="class">nav</xsl:attribute> 
                30 Second Loads
                </a> 
                </td>
                <td>
                <xsl:value-of select="DailySummary/hibernate-custom/DailySummaryBean/thirtySecondLoads"/>
                </td>
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
        <!--
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
    <table style="width:650px" summary="Daily Summary">
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
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opOrderCount,'###,###,###')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opLineCount,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opDollarTotal,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>

        <tbody class="two">
            <tr>
                <td class="fl">maxbuyer:</td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opOrderCount,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opLineCount,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/opDollarTotal,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>
        <tbody class="one">
            <tr>
                <td class="fl">maxbuyer-- Guest:</td>
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
                <td class="fl">Deleted:</td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/deletedOrders,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/deletedOrdersTlines,'###,###,##0')"/></td>
                <td><xsl:value-of select="format-number(OrderStatsSummaryElement/hibernate-custom/OrderStatsSummary/deletedOrdersTdollars,'$###,###,##0.00')"/></td>
            </tr>
        </tbody>
    </table>            
  </xsl:template>
  <xsl:template match="SessionElement" mode="Sessions">
    <table style="width:650px;" summary="Daily Summary">
        <thead>
            <tr>
                <th scope="col">Context</th>
                <th scope="col">Distinct Sessions</th>
                <th scope="col">Distinct Users</th>
                <th scope="col">Avg. Session Duration (min)</th>
                <th scope="col">Avg. Pages Displayed per Session</th>
                <th scope="col">Avg. Seconds between User click</th>
            </tr>
        </thead>

        <xsl:apply-templates select="SessionRecord" mode="SessionData">
            <xsl:sort select="CountSessions" data-type="number" order="descending"/>
        </xsl:apply-templates>
    </table>            
  </xsl:template>

  <xsl:template match="DollarsLines" mode="DollarsLines">
    <table style="width:650px;" summary="Dollars and Lines by Context">
        <thead>
            <tr>
                <th scope="col">Context</th>
                <th scope="col">Data</th>
                <th scope="col">WebMethods</th>
                <th scope="col">SAP</th>
                <th scope="col">ARIBA Direct</th>
                <th scope="col">MaxBuyer</th>
                <th scope="col">Guest at MaxBuyer</th>
            </tr>
        </thead>
        <xsl:apply-templates select="Context" mode="DollarsAndLinesData">
        </xsl:apply-templates>
    </table>            
  </xsl:template>

  <xsl:template match="SessionRecord" mode="SessionData">
      <xsl:if test="position() mod 2 =0">
       <tbody class="two">
            <TR>
                <TD>
                <xsl:value-of select="ContextName"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(CountSessions,'##,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(distinctUsers,'##,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(AvgSessionDuration div 60000,'##,##0.0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(AvgSessionHits,'##,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(AvgSecondsBetweenClick div 250,'##,##0')"/>
                </TD>
           </TR>
       </tbody>
              </xsl:if>
       <xsl:if test="position() mod 2 =1">
       <tbody class="one">
            <TR>
                <TD>
                <xsl:value-of select="ContextName"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(CountSessions,'##,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(distinctUsers,'##,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(AvgSessionDuration div 60000,'##,##0.0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(AvgSessionHits,'##,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(AvgSecondsBetweenClick div 250,'##,##0')"/>
                </TD>
           </TR>
       </tbody>
       </xsl:if>
  </xsl:template>
  
  <xsl:template match="Context" mode="DollarsAndLinesData">
      <xsl:if test="position() mod 2 =0">
       <tbody class="two">
            <TR>
                <TD>
                <xsl:value-of select="ContextName"/>
                </TD>
                <TD>Dollars:</TD>
                <TD>
                <xsl:value-of select="format-number(WMDollars,'#,###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(SAPDollars,'#,###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(ARIBADollars,'#,###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(BOSDollars,'#,###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(GUESTDollars,'#,###,##0.00')"/>
                </TD>
           </TR>
            <TR>
                <TD>
                <xsl:value-of select="ContextName"/>
                </TD>
                <TD>Lines:</TD>
                <TD>
                <xsl:value-of select="format-number(WMLines,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(SAPLines,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(ARIBALines,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(BOSLines,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(GUESTLines,'#,###,##0')"/>
                </TD>
           </TR>
           <TR>
                <TD>
                <xsl:value-of select="ContextName"/>
                </TD>
                <TD>Orders:</TD>
                <TD>
                <xsl:value-of select="format-number(WMOrders,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(SAPOrders,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(ARIBAOrders,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(BOSOrders,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(GUESTOrders,'#,###,##0')"/>
                </TD>
           </TR> 
           
       </tbody>
              </xsl:if>
       <xsl:if test="position() mod 2 =1">
       <tbody class="one">
            <TR>
                <TD>
                <xsl:value-of select="ContextName"/>
                </TD>
                <TD>Dollars:</TD>
                <TD>
                <xsl:value-of select="format-number(WMDollars,'#,###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(SAPDollars,'#,###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(ARIBADollars,'#,###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(BOSDollars,'#,###,##0.00')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(GUESTDollars,'#,###,##0.00')"/>
                </TD>
           </TR>
            <TR>
                <TD>
                <xsl:value-of select="ContextName"/>
                </TD>
                <TD>Lines:</TD>
                <TD>
                <xsl:value-of select="format-number(WMLines,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(SAPLines,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(ARIBALines,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(BOSLines,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(GUESTLines,'#,###,##0')"/>
                </TD>
           </TR>
           <TR>
                <TD>
                <xsl:value-of select="ContextName"/>
                </TD>
                <TD>Orders:</TD>
                <TD>
                <xsl:value-of select="format-number(WMOrders,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(SAPOrders,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(ARIBAOrders,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(BOSOrders,'#,###,##0')"/>
                </TD>
                <TD>
                <xsl:value-of select="format-number(GUESTOrders,'#,###,##0')"/>
                </TD>
           </TR> 
       </tbody>
       </xsl:if>
  </xsl:template>
</xsl:stylesheet>
