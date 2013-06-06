<?xml version="1.0"?>
<!DOCTYPE xsl:stylesheet [<!ENTITY nbsp "&#160;">]>

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
                <div id="realtimegraphs">
                </div>
            </div>
        </div>
    <!-- closes #tools-->
    </xsl:template>

    <xsl:template match="Body">
        <hr class="hide"/>

        <script type="text/javascript">
            function submitformdata()
            {
            var page=document.forms["timeslice"]["page"].value;
            var context=document.forms["timeslice"]["context"].value;
            var start=document.forms["timeslice"]["start"].value;
            var end=document.forms["timeslice"]["end"].value;

            var params = window.location.href;
            var pos = params.indexOf("?");
            if ( params!=null &amp;&amp; pos!=-1) {
            params = params.substring(params.indexOf("?")+1);
            } else {
            params = "";
            }
         
            if ( params!="")  {
            params += "&amp;page=" + page;
            } else {
            params += "page=" + page;
            }
            params += "&amp;context=" + context;
            params += "&amp;start=" + start;
            params += "&amp;end=" + end;
            //params += "&amp;selectedDate=" + "20120404";
        
            window.location.href = "?" + params; 
                
            return true;
            }
        </script>

 
        <div id="mainContent" style="margin-top: -170px;position: relative;">
            <div id="which" style="margin-top: 10px;">

                <div class="input_criteria" style="background-color: #EEECF6;">
                    <form name="timeslice" action="" onSubmit="return submitformdata()" >
                        Partial Page Name (*Optional): 
                        <input type="text" name="page" size="30" maxlength="30"/>&nbsp;&nbsp;
                        Context (*Optional): 
                        <input type="text" name="context" size="10" maxlength="10"/>
                        <br/>
                        Start Time (HH:MM): 
                        <input type="text" name="start" size="5" maxlength="5"/>&nbsp;&nbsp;
                        End Time (HH:MM): 
                        <input type="text" name="end" size="5" maxlength="5" />
                        <br/>            
                        Submit: 
                        <input type="submit" value="Submit" name="submit"/>
                        <input type="hidden" name="selectedDate" value=""/>
                    </form>
                </div>
                <script>
                    function pad2(number) {
                    return (number &lt; 10 ? '0' : '') + number;
                    }
                    function getQueryStrings() { 
                    var assoc  = {};
                    var decode = function (s) { return decodeURIComponent(s.replace(/\+/g, " ")); };
                    var queryString = location.search.substring(1); 
                    var keyValues = queryString.split('&amp;'); 

                    for(var i in keyValues) { 
                    var key = keyValues[i].split('=');
                    if (key.length > 1) {
                    assoc[decode(key[0])] = decode(key[1]);
                    }
                    } 

                    return assoc; 
                    } 
                
                    var dt = getQueryStrings()['selectedDate'];
                    if ( typeof dt == "undefined" || dt == "undefined" || dt == null || dt == "") {
                    dt = "";
                    var now = new Date();
                    //alert(now);
                    //alert(now.getFullYear().toString());
                    dt = now.getFullYear().toString() + pad2((now.getMonth()+1).toString()) + pad2((now.getDate()).toString());
                    }
                    document.forms["timeslice"]["selectedDate"].value = dt;
                    var pg = getQueryStrings()['page'];
                    if ( typeof pg == "undefined" || pg == "undefined" || pg == null || pg == "") {
                    pg = ""
                    }                  
                    document.forms["timeslice"]["page"].value =  pg;
                
                    var ct = getQueryStrings()['context'];
                    if ( typeof ct == "undefined" || ct == "undefined" || ct == null || ct == "") {
                    ct = ""
                    }                  
                    document.forms["timeslice"]["context"].value =  ct;
                
                    var s = getQueryStrings()['start'];
                    if ( typeof s == "undefined" || s == "undefined" || s == null || s == "") {
                    s = ""
                    }                  
                    document.forms["timeslice"]["start"].value =  unescape(s);
                
                    var e = getQueryStrings()['end'];
                    if ( typeof e == "undefined" || e == "undefined" || e == null || e == "") {
                    e = ""
                    }                  
                    document.forms["timeslice"]["end"].value =  unescape(e);
                </script>
                <h2>Time Slice Report:
                    <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDay"/>
                    <xsl:text>  </xsl:text>
                    <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentMonth"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentDate"/>
                    <xsl:text>, </xsl:text>
                    <xsl:value-of select="/Page/Payload/Calendar/CalendarBean/CurrentYear"/>
                </h2>
                <dl>
                    <dt class="im"/>
                    <dd>

                        <ul id="ftr">
                            <xsl:apply-templates select="$Payload/TimeSlice" mode="Details"/>
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
                    <dt>
                        <a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">Bugzilla</a>
                    </dt>
                    <dd>

                        <p>Report a bug for ART 2.0.[
                            <a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">more</a>]
                        </p>
                    </dd>
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
                <p>Copyright ; 1998-2012 OfficeMax </p>
            </div>
        </div>
    <!-- closes #mainContent-->
    </xsl:template>

    <xsl:template match="HighLineStats" mode="Details">
        <table summary="Overall Summary Stats">
            <thead>
                <tr>
                    <th scope="col">Users</th>
                    <th scope="col">Orders</th>
                    <th scope="col">Sessions</th>
                </tr>
            </thead>

            <xsl:apply-templates select="highRecord" mode="HighSessionData">
                <!--<xsl:sort select="Hits" data-type="number" order="descending"/>-->
            </xsl:apply-templates>
        </table>            
    </xsl:template>
  
    <xsl:template match="TimeSlice" mode="Details">
        <table summary="Time Slice Summary">
            <thead>
                <tr>
                    <th scope="col">Hour</th>
                    <th scope="col">Context</th>
                    <th scope="col">Page Name</th>
                    <th scope="col">No. Hits</th>
                    <th scope="col">Avg. Load Time</th>
                    <th scope="col">Max Load Time</th>
                    <th scope="col">Distinct Users</th>
                    <th scope="col">Distinct Sessions</th>
                </tr>
            </thead>

            <xsl:apply-templates select="pageRecord" mode="SessionData">
                <!--<xsl:sort select="Hits" data-type="number" order="descending"/>-->
            </xsl:apply-templates>
        </table>            
    </xsl:template>
  
    <xsl:template match="highRecord" mode="HighSessionData">
        <xsl:if test="position() mod 2 =0">
            <tbody class="two">
                <TR>
                    <TD>
                        <xsl:value-of select="format-number(Users,'##,##0')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Orders,'##,##0')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Sessions,'##,##0')"/>
                    </TD>
                </TR>
            </tbody>
        </xsl:if>
        <xsl:if test="position() mod 2 =1">
            <tbody class="one">
                <TR>
                    <TD>
                        <xsl:value-of select="format-number(Users,'##,##0')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Orders,'##,##0')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Sessions,'##,##0')"/>
                    </TD>
                </TR>
            </tbody>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="pageRecord" mode="SessionData">
        <xsl:if test="position() mod 2 =0">
            <tbody class="two">
                <TR>
                    <TD>
                        <xsl:value-of select="format-number(Hour,'##')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="ContextName"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="PageName"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Hits,'##,##0')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(AvgLoadTime ,'##,##0.00')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(MaxLoadTime,'##,##0.00')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Users,'##,##0')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Sessions,'##,##0')"/>
                    </TD>
                </TR>
            </tbody>
        </xsl:if>
        <xsl:if test="position() mod 2 =1">
            <tbody class="one">
                <TR>
                    <TD>
                        <xsl:value-of select="format-number(Hour,'##')"/>
                    </TD>                    
                    <TD>
                        <xsl:value-of select="ContextName"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="PageName"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Hits,'##,##0')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(AvgLoadTime ,'##,##0.00')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(MaxLoadTime,'##,##0.00')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Users,'##,##0')"/>
                    </TD>
                    <TD>
                        <xsl:value-of select="format-number(Sessions,'##,##0')"/>
                    </TD>
                </TR>
            </tbody>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
