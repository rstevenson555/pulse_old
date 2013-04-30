<!DOCTYPE html>
<html style="height: 100%">
    <head>
        <META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>ART Client - Online statistical reporting</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta content="Eric Van Geem" name="author">
        <link media="screen" title="default" href="css/default.css" type="text/css" rel="stylesheet">
        <link media="print" title="print" href="css/default.css" type="text/css" rel="stylesheet">   
        <link rel="stylesheet" href="css/rollover.css">
        
        <%@ page import="java.util.Date" %> 
        <%@ page import="java.util.Calendar" %>
        <%@ page import="java.util.HashMap" %>
        <%@ page import="java.util.ArrayList" %>
        <%@ page import="java.text.SimpleDateFormat" %> 
        <%@ page import="java.text.DecimalFormat" %> 
        <%@ page import="com.bos.art.model.jdo.DailySummaryBean" %> 
        <%@ page import="com.bos.servlets.SessionUsageServlet" %>      
        <%@ page import="org.joda.time.DateTime" %>      
        <%@ page import="org.joda.time.format.DateTimeFormat" %>      
        <%@ page import="org.joda.time.format.DateTimeFormatter" %>      
          
        
        <%             
             Date theDate = new Date();
             SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEEEE MMMMM d, yyyy");
             SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd MMMMM yyyy");
             SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyyMMdd");
             SimpleDateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM-dd");
             DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

             String selectedDate = request.getParameter("selectedDate");
             SessionUsageServlet sus = null;
             
             if(selectedDate != null && !selectedDate.equals("")) {            	
         		DateTime selectedDateTime = formatter.parseDateTime(selectedDate);             	
             	sus = new SessionUsageServlet(selectedDateTime.toString("yyyy-MM-dd"));
             } else { 
                DateTime selectedDateTime = new DateTime();
                selectedDate = selectedDateTime.toString("yyyyMMdd");
             	sus = new SessionUsageServlet(selectedDateTime.toString("yyyy-MM-dd"));
             }
            
             DailySummaryBean dsb = sus.getDailySummaryBean();             
             ArrayList<HashMap<String, String>> dollarsAndLinesMaps = sus.getDollarsAndLinesMaps();
             ArrayList<HashMap<String, String>> sessionSummaryMaps = sus.getSessionSummaryMaps();              
        %>
    </head>
    <body id="artclient" style="height:100%" class="homepage">    
        <!-- Import left panel -->    
        <jsp:include page="LeftPanel.jsp" />  
        
        <!-- Begin main body -->
        <div class="mainBody">
           <div class="innerBody" style="white-space: normal">
               <div style="display: inline-block">
                   <div id="draggable1">
                        <h2 style="width:inherit">Daily Summary: <%= dateFormat1.format(dsb.getDay()) %></h2>
                        <div id="subcomment">(60 seconds delayed)</div>
                        <div class="artTableFrame">
                            <table class="artTable" style="width:650px;" summary="Daily Summary">
                                <thead>
                                    <tr>
                                        <th scope="col">Summary</th>
                                        <th scope="col" style="text-align:right">Value</th>
                                    </tr>
                                </thead>
                                <tbody class="bigFont one">
                                    <tr>
                                        <td class="fl">Day</td>
                                        <td><%= dateFormat2.format(dsb.getDay()) %></td>
                                    </tr>
                                </tbody>
                                <tbody class="bigFont two">
                                    <tr>
                                        <td class="fl"><a href="View30SecondLoads.web?selectedDate=<%=dateFormat3.format(dsb.getDay()) %>" class="nav"><b>30 Second Loads</b></a></td>
                                        <td><%= dsb.getThirtySecondLoads() %></td>
                                    </tr>
                                </tbody>
                                <tbody class="bigFont one">
                                    <tr>
                                        <td class="fl">15 Second Loads</td>
                                        <td><%= dsb.getFifteenSecondLoads() %></td>
                                    </tr>
                                </tbody>
                                <tbody class="bigFont two">
                                    <tr>
                                        <% DecimalFormat df = new DecimalFormat("#,##0"); %>
                                        <td class="fl">Total Pages Loaded</td>
                                        <td><%= df.format( dsb.getTotalLoads() ) %></td>
                                    </tr>
                                </tbody>
                                <tbody class="bigFont one">
                                    <tr>
                                        <td class="fl">Average Page Load Time</td>
                                        <td><%= dsb.getAverageLoadTime() / 1000.0 %></td>
                                    </tr>
                                </tbody>
                                <tbody class="bigFont two">
                                    <tr>
                                        <td class="fl">Ninetieth Percentile</td>
                                        <td><%= dsb.getNinetiethPercentile() / 1000.0 %></td>
                                    </tr>
                                </tbody>                            
                            </table>
                        </div>
                   </div>
                   
                   <div id="draggable2">
                    <h2 style="width:inherit">Dollars and Lines Data: <%= dateFormat1.format(dsb.getDay()) %></h2>
                    <div id="subcomment">(60 seconds delayed)</div>
                    <div class="artTableFrame">
                        <table class="artTable" style="width:650px;" summary="Dollars and Lines by Context">
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
                            <%  HashMap<String, String> dollarsAndLinesMap = null;
                                for (int i = 0; i < dollarsAndLinesMaps.size() ; i++) {                                 	
                                   dollarsAndLinesMap = (HashMap) dollarsAndLinesMaps.get(i); %>
                                   
                                   <tbody class='bigFont <%= (i%2==0)? "one":"two" %>'>
                                       <tr>
                                           <td><%=dollarsAndLinesMap.get("contextName")%></td> 
                                           <td>Dollars:</td> 
                                           <td><%=dollarsAndLinesMap.get("WMDollars")%></td> 
                                           <td><%=dollarsAndLinesMap.get("SAPDollars")%></td> 
                                           <td><%=dollarsAndLinesMap.get("ARIBADollars")%></td> 
                                           <td><%=dollarsAndLinesMap.get("BOSDollars")%></td> 
                                           <td><%=dollarsAndLinesMap.get("GUESTDollars")%></td> 
                                       </tr>
                                       <tr>
                                           <td><%=dollarsAndLinesMap.get("contextName")%></td> 
                                           <td>Lines:</td> 
                                           <td><%=dollarsAndLinesMap.get("WMLines")%></td> 
                                           <td><%=dollarsAndLinesMap.get("SAPLines")%></td> 
                                           <td><%=dollarsAndLinesMap.get("ARIBALines")%></td> 
                                           <td><%=dollarsAndLinesMap.get("BOSLines")%></td> 
                                           <td><%=dollarsAndLinesMap.get("GUESTLines")%></td> 
                                       </tr>
                                       <tr>
                                           <td><%=dollarsAndLinesMap.get("contextName")%></td> 
                                           <td>Orders:</td> 
                                           <td><%=dollarsAndLinesMap.get("WMOrders")%></td> 
                                           <td><%=dollarsAndLinesMap.get("SAPOrders")%></td> 
                                           <td><%=dollarsAndLinesMap.get("ARIBAOrders")%></td> 
                                           <td><%=dollarsAndLinesMap.get("BOSOrders")%></td> 
                                           <td><%=dollarsAndLinesMap.get("GUESTOrders")%></td> 
                                       </tr>
                                   </tbody>
                            <% } %>
                        </table> 
                    </div>
                  </div>
                  
                  <div id="draggable3">
                      <div class="artTableFrame" style="margin-top: 20px">
                          <img src="./DollarsAndOrdersChart.web?width=650&amp;height=375&graphType=dao&selectedDate=<%=selectedDate%>" style="margin-bottom: -5px" />
                      </div>
                  </div>
                  
                  <div id="draggable4">
                      <div class="artTableFrame" style="margin-top: 20px">
                          <img src="./DollarsAndOrdersChart.web?width=650&amp;height=375&graphType=avg&selectedDate=<%=selectedDate%>" style="margin-bottom: -5px" />
                      </div>
                  </div>
                  
                  <div id="draggable5">
                    <h2 style="width:inherit">Session Usage Data: <%= dateFormat1.format(dsb.getDay()) %></h2>
                    <div id="subcomment">(30 minutes delayed)</div>
                    <div class="artTableFrame">
                        <table class="artTable" style="width:650px;" summary="Session Usage">
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
                            <% HashMap<String, String> sessionSummaryMap = null;
                               for (int i = 0; i < sessionSummaryMaps.size(); i++) { 
                                   sessionSummaryMap = (HashMap) sessionSummaryMaps.get(i); %>
                                   <tbody class='bigFont <%=(i%2==0?"one":"two")%>'>
                                       <tr>
                                         <td><%= sessionSummaryMap.get("contextName") %></td>
                                         <td><%= sessionSummaryMap.get("CountSessions") %></td>
                                         <td><%= sessionSummaryMap.get("distinctUsers") %></td>
                                         <td><%= sessionSummaryMap.get("AvgSessionDuration") %></td>
                                         <td><%= sessionSummaryMap.get("AvgSessionHits") %></td>   
                                         <td><%= sessionSummaryMap.get("AvgSecondsBetweenClick") %></td>                                         
                                       </tr>
                                   </tbody>
                            <% } %>                            
                        </table>
                    </div>
                 </div>
               </div>
           </div>
           <form id="hiddenform" method="get">
               <input type="hidden" name="selectedDate" id="selectedDate" />
           </form>
           
           <jsp:include page="Footer.jsp"/>  
        </div>
        <script>
        	$('.artTable th').css('text-align', 'center');
        	$('.mainBody h2').css('cursor', 'move');
        	$( "#draggable1" ).draggable({ grid: [ 5,5 ] });
        	$( "#draggable2" ).draggable({ grid: [ 5,5 ] });
        	$( "#draggable3" ).draggable({ grid: [ 5,5 ] });
        	$( "#draggable4" ).draggable({ grid: [ 5,5 ] });
        	$( "#draggable5" ).draggable({ grid: [ 5,5 ] });
        </script>
    </body>
</html>
            