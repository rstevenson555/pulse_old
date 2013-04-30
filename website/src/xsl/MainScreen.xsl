<?xml version="1.0"?>



<xsl:stylesheet version="1.0"

 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">



<xsl:import href="ARTPage.xsl"    />



  <xsl:template match="DashBoard">

   <a href="/logparser-website/ViewDailySessionSummary.web" title="Return to home page" accesskey="1"><img src="images/ARTlogo.jpg" width="209" alt="ART logo"/></a>

    <div id="tools">

        <ul id="mainNav" style="display:none;">

            <li><a href="download.html" title="Real time charts of boiseoffice.com performance">Production Environment</a></li>

            <li><a href="products/" title="Historical charst of boiseoffice.com performance">Test Environment</a></li>

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



        <h2>Average LoadTime Performance</h2>

        <dl>

            <dt class="im"/>

            <dd>



            <ul id="ftr">

                <p class="d1">The following are realtime graphs of the Average Page Load Time.</p>

                    <APPLET ARCHIVE="jfreechart-0.9.16.jar,jcommon-0.9.1.jar,logParser-applets.jar,LiveLogParser.jar,javagroups-all.jar" 

                            CODE="com.bos.applets.AvgLoadTime"

                            width="540" height="270" ALT="Your browser has java disabled, please install a recent version of Java">

                        <!-- <param name="plot_bgcolor" VALUE = "#A57B46"/> -->

                        <param name="plot_image" VALUE = "/images/bg6.gif"/>

                        <param name="chart_bgcolor" VALUE = "#FFFFFF"/>

                    </APPLET>

                <!--

                <img src="images/graph.jpg"/>

                <img src="images/graph.jpg"/>

                <img src="images/graph.jpg"/>

                <img src="images/graph.jpg"/>

                -->

            </ul>



            <div style="clear: both"></div>

            </dd>

        </dl>





        <h2>AS400 Connectivity Performance</h2>

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



        <h2>Contact us on issues identified in ART</h2>

        <dl class="footerbg">

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

            <a href="http://support.decisionone.com/mozilla/mozilla_help_main.htm">Realtime Charts</a>

        </li>

        <li>

            <a href="foundation/donate.html">Historical Charts</a>

        </li>

        <li>

            <a href="http://newsletter.mozdev.org/">About ART</a>

        </li>

        <li>

            <a href="http://java.sun.com/webapps/getjava/BrowserRedirect?locale=en&amp;host=www.java.com:80">Install Java</a>

        </li>

    </ul>

    

    </div>

    <!-- closes #side -->

  </xsl:template>





</xsl:stylesheet>

