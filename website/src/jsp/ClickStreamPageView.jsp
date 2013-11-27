<!DOCTYPE html>
<html style="height: 100%">
    <head>
        <META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Pulse</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta content="Eric Van Geem" name="author">
        <link media="screen" title="default" href="css/default.css" type="text/css" rel="stylesheet" />
        <link media="print" title="print" href="css/default.css" type="text/css" rel="stylesheet /">
        <link rel="stylesheet" href="css/rollover.css" />
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>

        <%@ page import="java.lang.Math" %>
        <%@ page import="java.util.List" %>
        <%@ page import="java.util.Map" %>
        <%@ page import="java.util.ArrayList" %>
        <%@ page import="java.util.HashMap" %>
        <%@ page import="java.util.Calendar" %>
        <%@ page import="java.util.Date" %>
        <%@ page import="java.text.SimpleDateFormat" %>
        <%@ page import="com.bos.servlets.ClickStreamServlet" %>
        <%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>
        <%@ page import="org.apache.commons.codec.binary.Base64" %>
        <%
           String selectedDate = request.getParameter("selectedDate");
           String sessionTXT = request.getParameter("SessionTxt");
           String sessionStart = request.getParameter("sessionStart");
           String advancedTXT= request.getParameter("AdvancedTxt");
           String userID = request.getParameter("userid");
           String fromTime = request.getParameter("from");
           String toTime = request.getParameter("to");
           String sessionOffset = request.getParameter("sessions");

           if (advancedTXT == null) advancedTXT = "";
           if (selectedDate == null) selectedDate = "";
           if (sessionTXT == null) sessionTXT = "";
           if (sessionStart == null) sessionStart = "";
           if (userID == null) userID = "";
           if (fromTime == null) fromTime = "";
           if (toTime == null) toTime = "";
           if (sessionOffset == null) sessionOffset = "";
           List userSessions = null;
           boolean searchAllSessions = (userID.isEmpty() && sessionTXT.isEmpty()
                                && !selectedDate.isEmpty()
                                && (!fromTime.isEmpty() || !toTime.isEmpty()));

           String dateAsString = "";
           if(selectedDate != null && !selectedDate.equals("")) {
                StringBuffer b = new StringBuffer(selectedDate);
                StringBuffer s = b.insert(4,'-');
                StringBuffer b2 = new StringBuffer(s.toString());
                StringBuffer s2 = b2.insert(7,'-');
                dateAsString = s2.toString();
            }

           System.out.println("sessionTXT: " + sessionTXT);
           System.out.println("dateAsString " + dateAsString);
           System.out.println("advancedTXT " + advancedTXT);
           System.out.println("userID " + userID);
           System.out.println("fromTime " + fromTime);
           System.out.println("toTime " + toTime);

           ClickStreamServlet css = new ClickStreamServlet(dateAsString, sessionTXT, sessionStart, advancedTXT,userID, fromTime, toTime, sessionOffset);
           List clickElements = css.getClickStream();

           if (!userID.isEmpty() || searchAllSessions)
               userSessions = css.getUserSessions();

           if (!advancedTXT.isEmpty() || searchAllSessions)
               userSessions = css.getUserSessions();

        %>

        <style>
            .debug {
                background: yellow;
                position: absolute;
                top: 0px;
                left: 0px;
                right: 0px;
                text-align: center;
                opacity: 0.55;
            }
            #page_screen {
               background-color: #000000;
               filter: alpha(opacity=80);
               opacity: 0.8;
               position: absolute;
               top: 0;
               left: 0;
               width: 100%;
               height: 100%;
               display: none;
            }

            #session-panel {
                width: 900px;
                background-color: #D2E0EA;
                border: 1px solid #899CAA;
                position: absolute;
                border-radius: 6px;
                padding: 6px;
                z-index: 1;
            }

            #select-sessions {
                margin: 6px 0px;
                position: absolute;
                top: 0px;
                right: 65px;
            }

            .sessionPanelClose {
                position: absolute;
                top: 6px;
                right: 6px;
                line-height: 1.3;
                border: 1px solid gray;
                border-radius: 3px;
                background-color: whiteSmoke;
            }

            .sessionPanelClose a {
                text-decoration: none;
                cursor: pointer;
                padding: 3px;
            }

            .sessionList {
                background: white;
                border-radius: 5px;
                border: 1px solid gray;
                max-height: 300px;
                overflow-y: auto;
                margin-top: 5px;
            }

            div.ui-datepicker, .ui-datepicker td{
                 font-size:10px;
            }
        </style>
    </head>
    <body id="artclient" style="height:100%" class="homepage">
        <!-- Import left panel -->
        <jsp:include page="LeftPanel.jsp" />

        <% if (userSessions != null && userSessions.size() > 0) {
        	/* Display session panel pop-up menu when the selected
             user ID has more than one session associated with it */
               int sessionCount = css.getSessionCount();
               int maxRows = ClickStreamServlet.MAX_SESSION_ROWS;
               int offset = css.getSessionOffset();
               int numPages = (int) Math.ceil((double) sessionCount / (double) maxRows);
               String optionSelected = ""; %>
           <div id="session-panel" style="display: none">
             <form name="sessionsForm" id="sessions-form" method="POST">
               <% if (searchAllSessions) { %>
                    <div><b>Sessions created within this time frame.</b></div>
               <% } else { %>
                    <div><b>Select session ID for user:</b> <%= userID %></div>
                 <% } %>
               <% if (!dateAsString.isEmpty()) { %><div><b>Selected date:</b> <%= dateAsString %></div> <% } %>
               <% if (!fromTime.isEmpty() && !toTime.isEmpty() && !dateAsString.isEmpty()) { %><div><b>Time:</b> <%=fromTime%> - <%=toTime%></div> <% } %>
               <span style="display: block">
                  <% if (numPages > 1) { %>
                     <select name="sessions" id="select-sessions">
                       <% for (int i = 0; i < numPages; i++) {
                          int startIndex = (i*maxRows)+1;
                          int endIndex = (i+1)*maxRows;

                          // For the last option, display the exact number of rows as the ending index
                          if (i == numPages-1)
                              endIndex = sessionCount;

                          // Determine which option was last selected and make sure it's still selected when page is refreshed
                          if (startIndex-1 == offset)
                              optionSelected = "selected";
                          else
                              optionSelected = "";
                       %>
                           <option value="<%=startIndex - 1%>" <%=optionSelected%> >
                             Sessions #<%= startIndex %> - <%= endIndex %>
                           </option>
                       <% } %>
                     </select>
                  <% } %>
                 </span>
               </form>
               <span class="sessionPanelClose"><a title="Close" onmouseup="closePanel()">X</a></span>
               <div class="sessionList">
                   <table class="artTable">
                       <tbody>
                           <tr>
                               <th>#</th>
                               <% if (searchAllSessions) { %><th>User ID</th> <% } %>
                               <th>Session ID</th>
                               <th>Session Start Time</th>
                               <!--<th>Session End Time</th>-->
                               <th>Browser</th>
                               <th>Views</th>
                               <th>Status</th>
                               <!--<th>Order</th>-->
                           </tr>
                       </tbody>
                       <%   HashMap<String,String> userSession = null;
                            for (int i = 0; i < userSessions.size(); i++) {
                                userSession = (HashMap) userSessions.get(i);
                                String sessionTxt = (String) userSession.get("session");
                            %>
                            <tbody class='<%=((i%2 == 0)?"one":"two")%>'>
                                <tr>
                                    <td><%= offset + i + 1 %></td>
                                    <% if (searchAllSessions) { %><td><%= userSession.get("user_id") %></td> <% } %>
                                    <td><a href="javascript:setSessionTxt('<%= sessionTxt %>','<%= userSession.get("starttime") %>')"><%= sessionTxt %></a></td>
                                    <td><%= userSession.get("starttime") %></td>
                                    <% String browser = "";
                                      browser = (userSession.get("browsertype")!=null)?userSession.get("browsertype"):"";
                                      if ( browser.length()>50) {
                                          browser = browser.substring(0,50);
                                      }
                                    %>
                                    <td><%= browser %></td>
                                    <td><%= userSession.get("sessionhits") %></td>
                                    <td>
                                    <%
                                    int error = 1;
                                    int order = 1<<1;
                                    int four_0_four = 1<<2;
                                    int no_search_results = 1<<3;
                                    int experience = 0;
                                    String img = null;

                                    String expstr = userSession.get("experience");
                                    if (expstr !=null) {
                                        experience = Integer.parseInt(expstr.trim());
                                    }
                                    if ( (experience & error) == error) {
                                        img = "<img src='images/reddot.png' style='width:20px;'></img><br/>";
                                    } else {
                                        img = "&nbsp;";
                                    }

                                    %>
                                    <!-- red dot -->
                                    <%= img %>

                                    <%
                                    if (( experience & order) == order) {
                                        img = "<span style='font-size:18pt;font-weight:bold;color:lightgreen'>$</span><br/>";
                                    } else {
                                        img = "&nbsp;";
                                    }

                                    %>
                                    <!-- dollar sign -->
                                    <%= img %>

                                    <%
                                    if (( experience & four_0_four) == four_0_four) {
                                        img = "<span style='font-size:18pt;font-weight:bold;color:#FF1919'>404</span><br/>";
                                    } else {
                                        img = "&nbsp;";
                                    }

                                    %>
                                    <!-- 404 sign -->
                                    <%= img %>

                                    <%
                                    if (( experience & no_search_results) == no_search_results) {
                                        img = "<img src='images/no-search-results.png' style='width:20px;'></img><br/>";

                                    } else {
                                        img = "&nbsp;";
                                    }

                                    %>
                                    <!-- 404 sign -->
                                    <%= img %>
                                    </td>
                                </tr>
                            </tbody>
                         <% } %>
                  </table>
               </div>
           </div>
           <div id="page_screen"></div>
           <script>
               // Invoke showSessionPanel() when page is loaded
               showSessionPanel();

               $('#select-sessions').change(function() {
            	  $('#sessions-form').submit();
               });

               function showSessionPanel()
               {
                darkenPage();
                   var session_panel = document.getElementById('session-panel');

                   w = 300;
                   h = 300;

                   // get the x and y coordinates to center the session panel
                   xc = Math.round(document.body.clientWidth/2)-(w/2)-150;
                   yc = Math.round(document.body.clientHeight/2)-(h/2);

                   // show the session panel
                   session_panel.style.left = xc + "px";
                   session_panel.style.top  = "50px";
                   session_panel.style.display = 'block';
               }

               function closePanel()
               {
                   // hide the newsletter panel
                   var session_panel = document.getElementById('session-panel');
                   session_panel.style.display = 'none';
                   // lighten the page again
                   lightenPage();
               }

                // this function puts the dark screen over the entire page
               function darkenPage()
               {
                   var page_screen = document.getElementById('page_screen');
                   //page_screen.style.height = document.body.parentNode.scrollHeight + 'px';
                   page_screen.style.display = 'block';
               }

               function lightenPage()
               {
                   var page_screen = document.getElementById('page_screen');
                   page_screen.style.display = 'none';
               }
           </script>
       <% } // End session pop-up menu %>

        <!-- Begin main body -->
        <div class="mainBody">
           <div class="innerBody">
              <div class="userViewFrame inlinePanel">
                <h2><span class="shadow">The User's View</span>
                    <span class="clickStreamPageNavLinks">
                    <a href="javascript:void(0)" id="navPrev" title="Previous view">
                        <img src="images/btn_arrow_left.gif" alt="Previous" width="5" height="7" border="0">
                        &nbsp;Previous</a>&nbsp;|
                    <a href="javascript:void(0)" id="navNext" title="Next view">Next&nbsp;
                        <img src="images/btn_arrow_right.gif" alt="Previous" width="5" height="7" border="0">
                    </a>
                    &nbsp;&nbsp;Click #: <span id="clickNum">1</span>
                    </span>
                </h2>

                <div>
                    <iframe src="http://maxbuyer.officemax.com" name="htmlpageviewframe" id="viewFrame" width="1024" height="800">
                        Your browser does not support iFrame. Consider using
                        <a href="http://www.mozilla.org/firefox">Mozilla Firefox</a> or
                        <a href="http://www.google.com/chrome">Google Chrome</a>
                    </iframe>
                </div>
              </div>

              <div class="sidePanels inlinePanel">
                  <h2><span class="shadow">Customer Info</span></h2>
                <div class="queryParamsPanel">
                    <div class="artTableFrame queryParamsTableFrame">
                        <table class="artTable" id="customer-params-table" summary="Customer Info">
                            <%
                                String customerInfoHTML = "";
                                HashMap fclickElement = null;
                                for (int i = 0; i < clickElements.size(); i++) {
                                  fclickElement = (HashMap) clickElements.get(i);
                                  break;
                                }
                                if ( fclickElement!=null) {
                                    //Map<String,String> cmap = css.getCustomerInfoByQueryParameters((String) fclickElement.get("queryParams"));
                                    Map<String,String> cmap = css.getCustomerInfoBySessionTxt( sessionTXT );
                                    int i = 0;
                                    for (Map.Entry<String, String> entry : cmap.entrySet())
                                    {
                                        String key = entry.getKey();
                                        String value = entry.getValue();
                                        //use key and value
                                        customerInfoHTML += "<tbody class='" + (((i++)%2 == 0)?"one":"two") + "'><tr>";
                                        customerInfoHTML += "<td class='queryNameCell'>" + key + "</td>";
                                        if ( key.equals("IP Address")) {
                                        //whois.arin.net?queryinput=5.104.241.178
                                            customerInfoHTML += "<td><a target='_blank' href='http://whois.arin.net?queryinput=" + value + "'>" + value + "</a></td>";
                                        } else {
                                            customerInfoHTML += "<td>" + value + "</td>";
                                        }
                                        customerInfoHTML += "</tr></tbody>";
                                    }
                                }
                            %>
                            <%= customerInfoHTML %>

                        </table>
                    </div>
                </div>

                <h2 style="width:75%"><span class="shadow">Click Stream Playback</span>
                    <!-- Search box -->
                    <div id="searchContainer">
                        <a id="searchButton" class="greenBtn"><span>Search</span></a>
                        <div id="searchBox" style="display: none;">
                            <form id="searchForm" method="GET">
                                <fieldset id="body">
                                    <fieldset>
                                        <label for="userid">User Key</label>
                                        <input type="text" class="deletable" name="userid" id="userid" value="<%= userID %>" />
                                    </fieldset>
                                    <fieldset>
                                        <label for="sessionid">Session ID</label>
                                        <input type="text" class="deletable" name="SessionTxt" id="sessionid" style="font-size:11px" value="<%= sessionTXT %>" />
                                    </fieldset>
                                    <fieldset>
                                        <label for="advanced">Advanced</label>
                                        <input type="text" class="deletable" name="AdvancedTxt" id="advancedid" style="font-size:11px" value="<%= StringEscapeUtils.unescapeJava(advancedTXT) %>" />
                                    </fieldset>
                                    <fieldset>
                                        <label for="selected">Date</label>
                                        <input type="text" class="deletable" name="selectedDate" id="selectedDate" style="font-size:11px" value="<%= selectedDate %>" />
                                    </fieldset>
                                    <!--Selected Date-->
                                    <fieldset>
                                        <label>Time frame</label>
                                        <input type="text" class="deletable" name="from" id="fromTime" placeholder="From" style="width:34%" value="<%= StringEscapeUtils.unescapeJava(fromTime) %>" />
                                        <em> - </em>
                                        <input type="text" class="deletable" name="to" id="toTime" placeholder="To" style="width:34%" value="<%= StringEscapeUtils.unescapeJava(toTime) %>" />
                                    </fieldset>
                                    <input type="button" id="submitBtn" value="Submit" class="submitSearch greenBtn">
                                    <input type="button" id="resetBtn" onClick="javascript:parent.location='ClickStreamPageView.jsp';return false;" value="Reset" class="reset greenBtn">
                                </fieldset>
                                <input type="hidden" id="selectedDate" name="selectedDate" value="<%= selectedDate %>"/>
                                <input type="hidden" id="fromTime" name="from" value="<%= StringEscapeUtils.unescapeJava(fromTime) %>"/>
                                <input type="hidden" id="toTime" name="to" value="<%= StringEscapeUtils.unescapeJava(toTime) %>"/>
                                <input type="hidden" id="sessionStartTime" name="sessionStart" value="<%= StringEscapeUtils.unescapeJava(sessionStart) %>"/>
                            </form>
                        </div>
                    </div>
                </h2>

                <div class="clickStreamPanel">
                    <!-- Header for click stream table -->
                    <% if (clickElements.size() == 0) { %>
                        <table class="clickStreamTableHeader">
                             <tr><th style="text-align: left">There are no clicks to display with these search terms.</th></tr>
                             <% if (!userID.isEmpty()) { %> <tr><th>User ID: <%= userID %></th></tr> <% } %>
                             <% if (!sessionTXT.isEmpty()) { %> <tr><th>Session ID: <%= sessionTXT %></th></tr> <% } %>
                             <% if (!dateAsString.isEmpty()) { %><tr><th>Selected date: <%= dateAsString %></th></tr> <% } %>
                             <% if (!fromTime.isEmpty() || !toTime.isEmpty()) { %><tr><th>Time range: <%=fromTime%> - <%=toTime%></th></tr> <% } %>
                        </table>
                    <% } else { %>
                            <table class="clickStreamTableHeader">
                                <tr>
                                    <th>
                                        <div class="leftCell">Click #</div>
                                    </th>
                                    <th style="width:239px;">
                                        <div class="rightCell" >Page Name</div>
                                    </th>
                                    <th>
                                        <div class="rightTimeCell">Time</div>
                                    </th>
                                    <th>
                                       <div class="rightStatusCell">Status</div>
                                    </th>
                                </tr>
                            </table>
                    <% } %>

                    <!-- Body for click stream table -->
                    <div class="artTableFrame clickStreamTableFrame">
                        <table class="artTable" id="click-stream-table" summary="Click Summary">
                          <%  HashMap clickElement = null;
                              boolean pagecapture = false;
                              for (int i = 0; i < clickElements.size(); i++) {
                                  clickElement = (HashMap) clickElements.get(i);
                                  if (!clickElement.get("htmlPageID").equals("0")) {
                                      pagecapture = true;
                                  }

                          %>
                                <tbody class='<%= (i%2==0)?"one":"two" %>' id='rec<%= i %>'>
                                    <tr>
                                        <td>
                                            <div class="leftCell">
                                                <%= i + 1 %>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="rightCell">
                                                <% String htmlPageID = (String) clickElement.get("htmlPageID");
                                                   Integer requestTokenCount = Integer.parseInt((String)clickElement.get("requestTokenCount"));
                                                   String elemStr = "";
                                                   String pageName = (String) clickElement.get("pageName");
                                                   // index.jsp is the name I give it so we can map the pagename to a name in the pages table
                                                   // change it back to the / root because index.jsp is not legal on officemax.com
                                                   if (pageName.equals("index.jsp")) {
                                                       pageName = "/";
                                                   }
                                                %>
                                                <% if (htmlPageID != null) {
                                                    elemStr = ((String) clickElement.get("queryParams"));
                                                    if ( elemStr!=null) {
                                                        elemStr = elemStr.replaceAll("&amp;","#amp;");
                                                        elemStr = elemStr.replaceAll("quot;","#quot;");

                                                        elemStr = elemStr.replaceAll("\"","#quot;");
                                                    }
                                                    if ( htmlPageID.equals("0") && pagecapture == true) {
                                                        htmlPageID = "-1";
                                                    }
                                                    //System.out.println(elemStr);
                                                %>
                                                <a href="./iview.web?HtmlPage_ID=<%=htmlPageID%>&requestTokenCount=<%=requestTokenCount%>"
                                                    id="pagelink"
                                                    onClick="setIndex(<%= i %>)"
                                                    target="htmlpageviewframe"
                                                    data-queryparams="<%= elemStr %>">
                                                    <%= pageName %>
                                                </a>
                                                <% } else { %>
                                                        <a href="./iview.web?nocontent=true"
                                                           id="pagelink"
                                                            onClick="setIndex(<%= i %>)"
                                                            target="htmlpageviewframe"
                                                            data-queryparams="<%= elemStr %>">
                                                            <%= pageName %>
                                                        </a>
                                                <% }%>
                                            </div>
                                            </td>
                                            <td><div class="rightTimeCell">
                                                <%= clickElement.get("time") %>
                                            </div>
                                            </td>
                                            <td>
                                            <div class="rightStatusCell">
                                                <%
                                                int error = 1;
                                                int order = 1<<1;
                                                int four_0_four = 1<<2;
                                                int no_search_results = 1<<3;
                                                int experience = 0;
                                                String img = null;

                                                String expstr = ((String)clickElement.get("experience"));
                                                if (expstr !=null && !expstr.equals("")) {
                                                    experience = Integer.parseInt(expstr.trim());
                                                }
                                                if ( (experience & error) == error) {
                                                    img = "<img src='images/reddot.png' style='width:20px;'></img><br/>";
                                                } else {
                                                    img = "&nbsp;";
                                                }

                                                %>
                                                <!-- red dot -->
                                                <%= img %>

                                                <%
                                                if (( experience & order) == order) {
                                                    img = "<span style='font-size:18pt;font-weight:bold;color:lightgreen'>$</span><br/>";
                                                } else {
                                                    img = "&nbsp;";
                                                }

                                                %>
                                                <!-- dollar sign -->
                                                <%= img %>

                                                <%
                                                if (( experience & four_0_four) == four_0_four) {
                                                    img = "<span style='font-size:18pt;font-weight:bold;color:#FF1919'>404</span><br/>";
                                                } else {
                                                    img = "&nbsp;";
                                                }

                                                %>
                                                <!-- 404 sign -->
                                                <%= img %>

                                                <%
                                                if (( experience & no_search_results) == no_search_results) {
                                                    img = "<img src='images/no-search-results.png' style='width:20px;'></img><br/>";

                                                } else {
                                                    img = "&nbsp;";
                                                }

                                                %>
                                                <!-- 404 sign -->
                                                <%= img %>
                                            </div>
                                            </td>

                                        <!--</td>-->
                                    </tr>
                                </tbody>
                             <% } %>
                        </table>
                    </div>
                </div>

                <h2><span class="shadow">Query Parameters</span></h2>
                <div class="queryParamsPanel">
                    <div class="artTableFrame queryParamsTableFrame">
                        <table class="artTable" id="query-params-table" summary="Click Summary">
                            <tbody><tr><th>There are no query parameters for this page.</th></tr></tbody>
                        </table>
                    </div>
                </div>

                <h2><span class="shadow">HTTP Headers</span></h2>
                <div class="queryParamsPanel">
                    <div class="artTableFrame queryParamsTableFrame">
                        <table class="artTable" id="http-header-table" summary="HTTP Header Summary">
                            <tbody><tr><th>There are no http headers for this page.</th></tr></tbody>
                        </table>
                    </div>
                </div>
             </div>
          </div>
          <jsp:include page="Footer.jsp"/>
        </div>
        <!-- End main body -->
        <script>
            var currentIndex = 0;
            var recordCount = <%= clickElements.size() %>;  // Save the number of records from this session ID
            var urls = new Array(); // Array to hold all click stream links

            $(document).ready(function(){
            	// Wait for page to load completely, then store all click stream links into an array
                for (var i = 0; i < recordCount; i++) {
                    urls[i] = $('#click-stream-table').find('#rec' + i + ' #pagelink').attr('href');
                    $("#viewFrame").attr("src", urls[0]); // Load the first link into iframe when page is loaded
                    setIndex(currentIndex);
                }

            	// Place delete icon inside input text boxes in search form
                $('input.deletable').wrap('<span class="deleteicon" />').after($('<span/>').click(function() {
                    $(this).prev('input').val('').focus();
                }));
            });

            // Attach click event handlers to navigation links
            $(function() {
                $("#navPrev").click(function() { previous(); });
                $("#navNext").click(function() { next(); });
            });

            function next() {
                if (currentIndex < urls.length - 1) {
					$("#viewFrame").attr("src", urls[currentIndex+1]);
                    console.log(urls[currentIndex+1]);
                    setIndex(currentIndex+1);
                }
            }

            function zeroPad(num, places) {
                        var zero = places - num.toString().length + 1;
                        return Array(+(zero > 0 && zero)).join("0") + num;
            }

            function previous(){
                if (currentIndex > 0) {
					$("#viewFrame").attr("src", urls[currentIndex-1]);
                    setIndex(currentIndex-1);
                }
            }


            function setIndex(newIndex) {
            	styleSelectedRecord(newIndex);
                currentIndex = newIndex; // Update current index with new index
                $('#clickNum').html(currentIndex+1); // Update click number display
                //console.log(element);
                //console.log($('#clickNum').scrollTop());
                //$('#clickNum').scrollTop(0);
                console.log(newIndex);
                console.log(currentIndex);

                var firstElement = $('#click-stream-table').find('#rec0' + ' #pagelink');
                var fpos = firstElement.position().top;;

                console.log("firstElement pos: " + fpos);
                var element = $('#click-stream-table').find('#rec' + currentIndex + ' #pagelink');
                console.log("table " + $('.clickStreamTableFrame').scrollTop());
                console.log("element: " + element.position().top);
                var frameTop = $('.clickStreamTableFrame').scrollTop();
                var epos = element.position().top - 282;

                var newpos = frameTop + epos;

                console.log("table new pos: " + newpos);

                $('.clickStreamTableFrame').animate({
                    scrollTop: newpos
                }, 0);


                showQueryParams();
                showHttpHeaders();
            }

            function styleSelectedRecord(newIndex) {
            	// Unstyle previous selected record
            	$('#rec' + currentIndex).removeAttr('style');
            	$('#rec' + currentIndex + " #pagelink").removeAttr('style');
            	$('#rec' + currentIndex + " td").css('font-size', '9px');

            	// Style current selected record
            	$('#rec' + newIndex).css('font-weight', 'bold');
            	$('#rec' + newIndex + " #pagelink").css('font-weight', 'bold');
            	$('#rec' + newIndex + " td").css('font-size', '10px');
            }

            function showQueryParams() {
            	// Save query paramaters of the current playback being viewed
            	var clickElementParams = $('#click-stream-table').find('#rec' + currentIndex + ' #pagelink').attr('data-queryparams');

                //var params = base64_decode(clickElementParams);
                var params = clickElementParams;

            	// Pass saved query parameters to a parsing function, then display the parsed query parameters
            	$('#query-params-table').html( parseQueryParams(params) );
            }

            function showHttpHeaders() {
            	// Save query paramaters of the current playback being viewed
            	var clickElementParams = $('#click-stream-table').find('#rec' + currentIndex + ' #pagelink').attr('data-queryparams');
                //var params = base64_decode(clickElementParams);
                var params = clickElementParams;

            	// Pass saved query parameters to a parsing function, then display the parsed query parameters
            	$('#http-header-table').html( parseHttpHeaders(params) );
            }

            /*
            Copyright Vassilis Petroulias [DRDigit]

            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
            You may obtain a copy of the License at

                http://www.apache.org/licenses/LICENSE-2.0

            Unless required by applicable law or agreed to in writing, software
            distributed under the License is distributed on an "AS IS" BASIS,
            WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            See the License for the specific language governing permissions and
            limitations under the License.
            */
            var B64 = {
                alphabet: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=',
                lookup: null,
                ie: /MSIE /.test(navigator.userAgent),
                ieo: /MSIE [67]/.test(navigator.userAgent),
                encode: function (s) {
                    var buffer = B64.toUtf8(s),
                        position = -1,
                        len = buffer.length,
                        nan1, nan2, enc = [, , , ];
                    if (B64.ie) {
                        var result = [];
                        while (++position < len) {
                            nan1 = buffer[position + 1], nan2 = buffer[position + 2];
                            enc[0] = buffer[position] >> 2;
                            enc[1] = ((buffer[position] & 3) << 4) | (buffer[++position] >> 4);
                            if (isNaN(nan1)) enc[2] = enc[3] = 64;
                            else {
                                enc[2] = ((buffer[position] & 15) << 2) | (buffer[++position] >> 6);
                                enc[3] = (isNaN(nan2)) ? 64 : buffer[position] & 63;
                            }
                            result.push(B64.alphabet[enc[0]], B64.alphabet[enc[1]], B64.alphabet[enc[2]], B64.alphabet[enc[3]]);
                        }
                        return result.join('');
                    } else {
                        result = '';
                        while (++position < len) {
                            nan1 = buffer[position + 1], nan2 = buffer[position + 2];
                            enc[0] = buffer[position] >> 2;
                            enc[1] = ((buffer[position] & 3) << 4) | (buffer[++position] >> 4);
                            if (isNaN(nan1)) enc[2] = enc[3] = 64;
                            else {
                                enc[2] = ((buffer[position] & 15) << 2) | (buffer[++position] >> 6);
                                enc[3] = (isNaN(nan2)) ? 64 : buffer[position] & 63;
                            }
                            result += B64.alphabet[enc[0]] + B64.alphabet[enc[1]] + B64.alphabet[enc[2]] + B64.alphabet[enc[3]];
                        }
                        return result;
                    }
                },
                decode: function (s) {
                    var buffer = B64.fromUtf8(s),
                        position = 0,
                        len = buffer.length;
                    if (B64.ieo) {
                        result = [];
                        while (position < len) {
                            if (buffer[position] < 128) result.push(String.fromCharCode(buffer[position++]));
                            else if (buffer[position] > 191 && buffer[position] < 224) result.push(String.fromCharCode(((buffer[position++] & 31) << 6) | (buffer[position++] & 63)));
                            else result.push(String.fromCharCode(((buffer[position++] & 15) << 12) | ((buffer[position++] & 63) << 6) | (buffer[position++] & 63)));
                        }
                        return result.join('');
                    } else {
                        result = '';
                        while (position < len) {
                            if (buffer[position] < 128) result += String.fromCharCode(buffer[position++]);
                            else if (buffer[position] > 191 && buffer[position] < 224) result += String.fromCharCode(((buffer[position++] & 31) << 6) | (buffer[position++] & 63));
                            else result += String.fromCharCode(((buffer[position++] & 15) << 12) | ((buffer[position++] & 63) << 6) | (buffer[position++] & 63));
                        }
                        return result;
                    }
                },
                toUtf8: function (s) {
                    var position = -1,
                        len = s.length,
                        chr, buffer = [];
                    if (/^[\x00-\x7f]*$/.test(s)) while (++position < len)
                    buffer.push(s.charCodeAt(position));
                    else while (++position < len) {
                        chr = s.charCodeAt(position);
                        if (chr < 128) buffer.push(chr);
                        else if (chr < 2048) buffer.push((chr >> 6) | 192, (chr & 63) | 128);
                        else buffer.push((chr >> 12) | 224, ((chr >> 6) & 63) | 128, (chr & 63) | 128);
                    }
                    return buffer;
                },
                fromUtf8: function (s) {
                    var position = -1,
                        len, buffer = [],
                        enc = [, , , ];
                    if (!B64.lookup) {
                        len = B64.alphabet.length;
                        B64.lookup = {};
                        while (++position < len)
                        B64.lookup[B64.alphabet[position]] = position;
                        position = -1;
                    }
                    len = s.length;
                    while (position < len) {
                        enc[0] = B64.lookup[s.charAt(++position)];
                        enc[1] = B64.lookup[s.charAt(++position)];
                        buffer.push((enc[0] << 2) | (enc[1] >> 4));
                        enc[2] = B64.lookup[s.charAt(++position)];
                        if (enc[2] == 64) break;
                        buffer.push(((enc[1] & 15) << 4) | (enc[2] >> 2));
                        enc[3] = B64.lookup[s.charAt(++position)];
                        if (enc[3] == 64) break;
                        buffer.push(((enc[2] & 3) << 6) | enc[3]);
                    }
                    return buffer;
                }
            };


            function base64_decode (data) {
                // http://kevin.vanzonneveld.net
                // +   original by: Tyler Akins (http://rumkin.com)
                // +   improved by: Thunder.m
                // +      input by: Aman Gupta
                // +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
                // +   bugfixed by: Onno Marsman
                // +   bugfixed by: Pellentesque Malesuada
                // +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
                // +      input by: Brett Zamir (http://brett-zamir.me)
                // +   bugfixed by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
                // *     example 1: base64_decode('S2V2aW4gdmFuIFpvbm5ldmVsZA==');
                // *     returns 1: 'Kevin van Zonneveld'
                // mozilla has this native
                // - but breaks in 2.0.0.12!
                //if (typeof this.window['atob'] == 'function') {
                //    return atob(data);
                //}
                var b64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
                var o1, o2, o3, h1, h2, h3, h4, bits, i = 0,
                    ac = 0,
                    dec = "",
                    tmp_arr = [];

                if (!data) {
                    return data;
                }

                data += '';

                do { // unpack four hexets into three octets using index points in b64
                    h1 = b64.indexOf(data.charAt(i++));
                    h2 = b64.indexOf(data.charAt(i++));
                    h3 = b64.indexOf(data.charAt(i++));
                    h4 = b64.indexOf(data.charAt(i++));

                    bits = h1 << 18 | h2 << 12 | h3 << 6 | h4;

                    o1 = bits >> 16 & 0xff;
                    o2 = bits >> 8 & 0xff;
                    o3 = bits & 0xff;

                    if (h3 == 64) {
                    tmp_arr[ac++] = String.fromCharCode(o1);
                    } else if (h4 == 64) {
                    tmp_arr[ac++] = String.fromCharCode(o1, o2);
                    } else {
                    tmp_arr[ac++] = String.fromCharCode(o1, o2, o3);
                    }
                } while (i < data.length);

                dec = tmp_arr.join('');

                return dec;
                }

            String.prototype.trim = function() {
                return this.replace(/^\s+|\s+$/g, "");
                };

            function parseQueryParams(params) {
            	// Check if there are any parameters. If not, return a message saying so.
            	if (params === "" || params === "null")
           			return "<tbody><tr><th>There are no query parameters for this page.</th></tr></tbody>";

            	// Initialize table headers
            	var queryParamsHTML = "<tbody><tr><th style='padding-left:10px;text-align:left'>Name</th><th style='text-align:left'>Value</th></tr></tbody>";

                //params = params.replace("&amp;","#amp;");

            	// Split key-value pairs
            	var paramArray = params.split("&");

            	for (var i = 0; i < paramArray.length; i++) {
            		// Split name and value of each query parameter
            		//var query = paramArray[i].split("=");
                    //var query = paramArray[i].match(/(^.*?)=(.*)/)
                    paramArray[i] = paramArray[i].trim();
                    var equal = paramArray[i].indexOf("=");

                    //if (query == null) {
                    if (equal == -1) {

                        // Build a table row for each query parameter
//                        queryParamsHTML += "<tbody class='" + ((i%2 == 0)?"one":"two") + "'><tr>";
//                        queryParamsHTML += "<td class='queryNameCell'>" + "" + "</td>";
//                        queryParamsHTML += "<td>" + "" + "</td>";
//                        queryParamsHTML += "</tr></tbody>";
                    } else {
                        var key = paramArray[i].substring(0,equal);
                        key = key.replace(/#amp;/g,"&amp;");
                        key = key.replace(/#quot;/g,"\"");

                        if ( key.indexOf("header.")!=-1) {
                            // has header. in it so skip it
                            continue;
                        }
                        if ( key == "")
                            continue;
                        // if key has header. skip it
                        //query[2] = query[2].replace("#amp;","&amp;");
                        // Build a table row for each query parameter
                        var value = paramArray[i].substring(equal+1,paramArray[i].length);
                        value = value.replace(/#amp;/g,"&amp;");
                        value = value.replace(/#quot;/g,"\"");

                        queryParamsHTML += "<tbody class='" + ((i%2 == 0)?"one":"two") + "'><tr>";
                        queryParamsHTML += "<td class='queryNameCell'>" + key + "</td>";
                        console.log(key);
                        if ( key == "searchTerm" || key == "search") {
                            //queryParamsHTML += "<td>" + base64_decode(value) + "</td>";
                            queryParamsHTML += "<td>" + B64.decode(value) + "</td>";
                        } else {
                            queryParamsHTML += "<td>" + value + "</td>";
                        }
                        queryParamsHTML += "</tr></tbody>";
                    }
            	}

            	// Return the HTML-formatted query parameters to be displayed
            	return queryParamsHTML;
            }

            function parseHttpHeaders(params) {
            	// Check if there are any parameters. If not, return a message saying so.
            	if (params === "" || params === "null")
           			return "<tbody><tr><th>There are no HTTP Headers for this page.</th></tr></tbody>";

            	// Initialize table headers
            	var queryParamsHTML = "<tbody><tr><th style='padding-left:10px;text-align:left'>Name</th><th style='text-align:left'>Value</th></tr></tbody>";

                //params = params.replace("&amp;","#amp;");

            	// Split key-value pairs
            	var paramArray = params.split("&");

            	for (var i = 0; i < paramArray.length; i++) {
            		// Split name and value of each query parameter
            		//var query = paramArray[i].split("=");
                    //var query = paramArray[i].match(/(^.*?)=(.*)/)
                    paramArray[i] = paramArray[i].trim();
                    var equal = paramArray[i].indexOf("=");

                    if (equal == -1) {
                        // Build a table row for each query parameter
//                        queryParamsHTML += "<tbody class='" + ((i%2 == 0)?"one":"two") + "'><tr>";
//                        queryParamsHTML += "<td class='queryNameCell'>" + "" + "</td>";
//                        queryParamsHTML += "<td>" + "" + "</td>";
//                        queryParamsHTML += "</tr></tbody>";
                    } else {
                        var key = paramArray[i].substring(0,equal);
                        key = key.replace(/#amp;/g,"&amp;");
                        key = key.replace(/#quot;/g,"\"");

                        if ( key.indexOf("header.")==-1) {
                            // does not have header. in it so skip it
                            continue;
                        }
                        key = key.replace("header.cookie.","");
                        key = key.replace("header.","");

                        if ( key == "")
                            continue;

                        var value = paramArray[i].substring(equal+1,paramArray[i].length);
                        //query[2] = query[2].replace("#amp;","&amp;");
                        value = value.replace(/#amp;/g,"&amp;");
                        value = value.replace(/#quot;/g,"\"");

                        //console.log("query[1] " + query[1]);
                        //console.log("query[2] " + query[2]);
                        // Build a table row for each query parameter
                        queryParamsHTML += "<tbody class='" + ((i%2 == 0)?"one":"two") + "'><tr>";
                        queryParamsHTML += "<td class='queryNameCell'>" + key + "</td>";
                        queryParamsHTML += "<td>" + value + "</td>";
                        queryParamsHTML += "</tr></tbody>";
                    }
            	}

            	// Return the HTML-formatted query parameters to be displayed
            	return queryParamsHTML;
            }

            // Search form
            $(function() {
                var button = $('#searchButton');
                var box = $('#searchBox');
                var form = $('#searchForm');

                button.removeAttr('href');
                button.mouseup(function(search) {
                    box.slideToggle('fast');
                    button.toggleClass('active');
                });
                form.mouseup(function() {
                    return false;
                });
                $(this).mouseup(function(search) {
                    if(!($(search.target).parent('#searchButton').length > 0)) {
                    	if ($('#ui-timepicker-div').css('display') == 'none' && $('#ui-datepicker-div').css('display') == 'none') {
                            button.removeClass('active');
                            box.hide();
                    	}
                    }
                });

                // Submit only the form fields that contain data
                $("#submitBtn").click(function() {

                	// Check if a date is selected when trying to search by time frame,
                	// unless we are searching for page clicks instead of sessions.
                	if ($('#fromTime').val() !== '' || $('#toTime').val() !== '') {
                		if ($('#selectedDate').val() === '' && $('#sessionid').val() === '') {
                			var today = new Date();
                			var dd = today.getDate();
                			var mm = today.getMonth()+1;
                			var yyyy = today.getFullYear();
                			$('#selectedDate').val(yyyy+""+zeroPad(mm,2)+""+zeroPad(dd,2));
                		}
                	}

                	// Disable any fields that do not contain data so
                	// that they are not submitted with the form
                	if ($("#userid").val() === '')
                		$("#userid").attr("disabled", "disabled")

                	if ($('#sessionid').val() === '')
                		$("#sessionid").attr("disabled", "disabled");

                	if ($("#fromTime").val() === '')
                		$("#fromTime").attr("disabled", "disabled");

                	if ($("#toTime").val() === '')
                		$("#toTime").attr("disabled", "disabled");

                	if ($('#selectedDate').val() === '')
                		$('#selectedDate').attr("disabled", "disabled");

                	if ($('#advancedid').val() === '')
                		$('#advancedid').attr("disabled", "disabled");

                	form.submit();
                });
            });

            $(function() {
            	$('#fromTime').timepicker({
                    onSelect: function(timeText, inst) {
                        console.log(timeText);
                        $('#fromTime').val(timeText);
                    }
                });
				$('#toTime').timepicker( {
                    onSelect: function(timeText, inst) {
                        console.log(timeText);
                        $('#toTime').val(timeText);
                    }
                });
				//$('#selectedDate').datepicker();
                $('#selectedDate').datepicker( {
                    dateFormat: 'yymmdd',
                    onSelect: function(dateText, inst) {
                        console.log(dateText);
                        $('#selectedDate').val(dateText);
                    }
                });
				$('#ui-timepicker-div').addClass('timepicker-ovrd');
            });
            // End search form code

            function setSessionTxt(sessionid,sessionStart) {
            	$('#sessionid').val(sessionid);
                $('#sessionStartTime').val(sessionStart);
            	$('#selectedDate').val('');
            	$('#fromTime').val('');
            	$('#toTime').val('');
                $('#from').val('');
            	$('#to').val('');
                $("#advancedTXT").val('');
                $("#advancedid").val('');
            	$("#submitBtn").click();

            }

            // Find the browser's window size and adjust page elements accordingly to fit the screen.
            $(function() {
            	var w = document.body.clientWidth;

            	/*if (w <= 1450 && w >= 1350) {
            		$('#viewFrame').attr('width', '900');
            		$('#viewFrame').attr('height', '700');
            		$('.clickStreamPageNavLinks').css('margin-left', '220px');
            	}

            	if (w <= 1300) {
            		$('#viewFrame').attr('width', '800');
            		$('#viewFrame').attr('height', '600');
            		$('.clickStreamPageNavLinks').css('margin-left', '155px');
            		$('.clickStreamTableFrame').css('max-height', '390px');
            		$('#footer').css('display', 'none');
            	}*/
            });

            // Hide left nav by default upon page load
            $(function() {
            	var navButton = document.getElementById('navButton');
        		if ($('.artLeftNav').css('display') != 'none') {
        			$('.artLeftNav').css('display', 'none');
        			navButton.style.marginLeft = "0px";
        			navButton.innerHTML = "&#9658;";
        			navButton.title="Show menu";
        		}
            });
        </script>
    </body>
</html>