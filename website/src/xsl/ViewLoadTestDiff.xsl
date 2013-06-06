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



<!--
    
               <table class="detaildata" summary="Load Test Difference Summary">
                <tr>
                    <th>LoadTest:</th>
                    <th>Compare:</th>
                    <th>Test Date:</th>
                    <th>Branch:</th>
                    <th>Context:</th>
                    <th>StartTime:</th>
                    <th>EndTime:</th>
                </tr>
                -->

                <xsl:apply-templates select="$Payload" mode="applyFirst"/>
                <!--
                </table>
               <table class="detaildata" summary="Load Test Detail Difference">
                   <xsl:apply-templates select="$Payload" mode="applySecond"/>
                </table>
                -->






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
     <xsl:apply-templates select="LoadTests/LoadTest" mode="tableFirst">
     </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="LoadTests/LoadTest" mode="tableFirst">
     <h2><xsl:value-of select="ComparisonTest/CompareTest/TestName"/> 
      vs. Base Test: <xsl:value-of select="ComparisonTest/BaseTest/TestName"/> </h2>
     <div id="subcommentleft">   
       Load Test: 
       <xsl:value-of select="ComparisonTest/CompareTest/TestName"/>
       <BR/>
       Context: 
       <xsl:value-of select="ComparisonTest/CompareTest/ContextName"/>
       Branch: 
       <xsl:value-of select="ComparisonTest/CompareTest/BranchName"/>
       <BR/>
       Start Time: 
       <xsl:value-of select="ComparisonTest/CompareTest/startTime"/>
       End Time:
       <xsl:value-of select="ComparisonTest/CompareTest/endTime"/>
       <BR/>
       Link: 
       http://test-art/logparser-website/<xsl:value-of select="ComparisonTest/CompareTest/Link"/>
       </div>

     <div id="basesubcommentleft">   
      <BR/>
       Base Test:
       <xsl:value-of select="ComparisonTest/BaseTest/TestName"/>
       Context: 
       <xsl:value-of select="ComparisonTest/BaseTest/ContextName"/>
       Branch: 
       <xsl:value-of select="ComparisonTest/BaseTest/BranchName"/>
       
     </div>
     <table class="detaildata" summary="Load Test Difference Summary">
       <tr>
         <th>Script:</th>
         <th>AVG Diff:</th>
         <th>90% Diff:</th>
         <th>50% Diff:</th>
         <th>Avg:</th>
         <th>90%:</th>
         <th>50%:</th>
       </tr>
     <xsl:apply-templates select="ComparisonTest/Transaction" mode="dataFirst">
     </xsl:apply-templates>
     </table>
     <table class="detaildata" summary="Load Test Difference Summary">
       <tr>
         <th>Transaction:</th>
         <th>Desc:</th>
         <th>AVG Diff:</th>
         <th>90% Diff:</th>
         <th>50% Diff:</th>
         <th>Avg:</th>
         <th>90%:</th>
         <th>50%:</th>
       </tr>
       <tr>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
       </tr>

     <xsl:apply-templates select="ComparisonTestDetails/Transaction" mode="dataSecond">
     </xsl:apply-templates>
     </table>

     <table class="detaildata" summary="Load Runner Difference Summary">
       <tr>
         <th>Transaction:</th>
         <th>Desc:</th>
         <th>AVG Diff:</th>
         <th>Count Diff:</th>
         <th>Max Diff:</th>
         <th>LR Count:</th>
         <th>LR Avg:</th>
         <th>LR Max:</th>
       </tr>
       <tr>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
         <th>__________</th>
       </tr>
     <xsl:apply-templates select="LRComparisonTestDetails/LRTransaction" mode="dataThird">
     </xsl:apply-templates>
     </table>

  </xsl:template>

<!--
  <xsl:template match="Payload" mode="applySecond">
     <xsl:apply-templates select="LoadTests/LoadTest/ComparisonTestDetails/Transaction" mode="dataSecond">
     </xsl:apply-templates>
  </xsl:template>
  -->

  <xsl:template match="Transaction" mode="dataSecond">
    <xsl:if test="position() mod 2 =0">
        <tbody class="two">
            <tr>
                <td>
                    <xsl:value-of select="transactionName"/>
                </td>
                <td>
                    <xsl:value-of select="transactionDesc"/>
                </td>
                <xsl:if test="AvgDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(AvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="AvgDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(AvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                
                <xsl:if test="NPDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(NPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="NPDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(NPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>

                <xsl:if test="FPDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(FPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="FPDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(FPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <td>
                    <xsl:value-of select="format-number(testAvg div 1000,'##,##0.00')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(testNP div 1000,'##,##0.0')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(testFP div 1000,'##,##0.0')"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
    <xsl:if test="position() mod 2 =1">
        <tbody class="one">
            <tr>
                <td>
                    <xsl:value-of select="transactionName"/>
                </td>
                <td>
                    <xsl:value-of select="transactionDesc"/>
                </td>
                
                <xsl:if test="AvgDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(AvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="AvgDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(AvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                
                <xsl:if test="NPDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(NPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="NPDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(NPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>

                <xsl:if test="FPDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(FPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="FPDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(FPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <td>
                    <xsl:value-of select="format-number(testAvg div 1000,'##,##0.00')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(testNP div 1000,'##,##0.0')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(testFP div 1000,'##,##0.0')"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Transaction" mode="dataFirst">
    <xsl:if test="position() mod 2 =0">
        <tbody class="two">
            <tr>
                <td>
                    <xsl:value-of select="scriptName"/>
                </td>
                <xsl:if test="AvgDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(AvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="AvgDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(AvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                
                <xsl:if test="NPDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(NPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="NPDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(NPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>

                <xsl:if test="FPDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(FPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="FPDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(FPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <td>
                    <xsl:value-of select="format-number(testAvg div 1000,'##,##0.00')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(testNP div 1000,'##,##0.0')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(testFP div 1000,'##,##0.0')"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
    <xsl:if test="position() mod 2 =1">
        <tbody class="one">
            <tr>
                <td>
                    <xsl:value-of select="scriptName"/>
                </td>
                <xsl:if test="AvgDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(AvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="AvgDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(AvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                
                <xsl:if test="NPDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(NPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="NPDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(NPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>

                <xsl:if test="FPDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(FPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="FPDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(FPDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <td>
                    <xsl:value-of select="format-number(testAvg div 1000,'##,##0.00')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(testNP div 1000,'##,##0.0')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(testFP div 1000,'##,##0.0')"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
  </xsl:template>


  <xsl:template match="LRTransaction" mode="dataThird">
    <xsl:if test="position() mod 2 =0">
        <tbody class="two">
            <tr>
                <td>
                    <xsl:value-of select="LRtransactionName"/>
                </td>
                <td>
                    <xsl:value-of select="LRtransactionDesc"/>
                </td>
                <xsl:if test="LRAvgDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(LRAvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="LRAvgDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(LRAvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                
                <xsl:if test="LRCountDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(LRCountDiff,'##,##0')"/>
                  </td>
                </xsl:if>
                <xsl:if test="LRCountDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(LRCountDiff,'##,##0')"/>
                  </td>
                </xsl:if>

                <xsl:if test="LRMaxDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(LRMaxDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="LRMaxDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(LRMaxDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <td>
                    <xsl:value-of select="format-number(LRTestCnt,'##,##0')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(LRTestAvg div 1000,'##,##0.000')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(LRTestMax div 1000,'##,##0.000')"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
    <xsl:if test="position() mod 2 =1">
        <tbody class="one">
            <tr>
                <td>
                    <xsl:value-of select="LRtransactionName"/>
                </td>
                <td>
                    <xsl:value-of select="LRtransactionDesc"/>
                </td>
                <xsl:if test="LRAvgDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(LRAvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="LRAvgDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(LRAvgDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                
                <xsl:if test="LRCountDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(LRCountDiff,'##,##0')"/>
                  </td>
                </xsl:if>
                <xsl:if test="LRCountDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(LRCountDiff,'##,##0')"/>
                  </td>
                </xsl:if>

                <xsl:if test="LRMaxDiff &lt; 0">
                  <td class="negative">
                    <xsl:value-of select="format-number(LRMaxDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <xsl:if test="LRMaxDiff &gt;= 0">
                  <td class="positive">
                    <xsl:value-of select="format-number(LRMaxDiff div 1000,'##,##0.000')"/>
                  </td>
                </xsl:if>
                <td>
                    <xsl:value-of select="format-number(LRTestCnt,'##,##0')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(LRTestAvg div 1000,'##,##0.000')"/>
                </td>
                <td>
                    <xsl:value-of select="format-number(LRTestMax div 1000,'##,##0.000')"/>
                </td>
            </tr>
        </tbody>
    </xsl:if>
  </xsl:template>


 </xsl:stylesheet>
