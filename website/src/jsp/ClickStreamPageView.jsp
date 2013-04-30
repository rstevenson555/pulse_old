<!DOCTYPE html>
<html style="height: 100%">
    <head>
        <META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>ART Client - Online statistical reporting</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta content="Eric Van Geem" name="author">
        <link media="screen" title="default" href="css/default.css" type="text/css" rel="stylesheet" />
        <link media="print" title="print" href="css/default.css" type="text/css" rel="stylesheet /">   
        <link rel="stylesheet" href="css/rollover.css" />
        
        <%@ page import="java.lang.Math" %> 
        <%@ page import="java.util.List" %> 
        <%@ page import="java.util.ArrayList" %> 
        <%@ page import="java.util.HashMap" %> 
        <%@ page import="java.util.Calendar" %> 
        <%@ page import="java.util.Date" %> 
        <%@ page import="java.text.SimpleDateFormat" %> 
        <%@ page import="com.bos.servlets.ClickStreamServlet" %> 
        <%
           String selectedDate = request.getParameter("selectedDate");
           String sessionTXT = request.getParameter("SessionTxt");
           String userID = request.getParameter("userid");
           String fromTime = request.getParameter("from");
           String toTime = request.getParameter("to");
           String sessionOffset = request.getParameter("sessions");
           
           if (selectedDate == null) selectedDate = "";
           if (sessionTXT == null) sessionTXT = "";
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
            
           ClickStreamServlet css = new ClickStreamServlet(dateAsString, sessionTXT, userID, fromTime, toTime, sessionOffset);
           List clickElements = css.getClickStream();
           
           
           if (!userID.isEmpty() || searchAllSessions) 
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
                width: 600px;
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
                               <th>Session End Time</th>
                           </tr>
                       </tbody>
                       <%   HashMap userSession = null;
                            for (int i = 0; i < userSessions.size(); i++) { 
                                userSession = (HashMap) userSessions.get(i);
                                String sessionTxt = (String) userSession.get("session");
                            %>
                            <tbody class='<%=((i%2 == 0)?"one":"two")%>'>
                                <tr>
                                    <td><%= offset + i + 1 %></td>
                                    <% if (searchAllSessions) { %><td><%= userSession.get("user_id") %></td> <% } %>
                                    <td><a href="javascript:setSessionTxt('<%= sessionTxt %>')"><%= sessionTxt %></a></td>
                                    <td><%= userSession.get("starttime") %></td>
                                    <td><%= userSession.get("endtime") %></td>
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
                    <iframe src="http://qa.officemax.com" name="htmlpageviewframe" id="viewFrame" width="1024" height="800">
                        Your browser does not support iFrame. Consider using 
                        <a href="http://www.mozilla.org/firefox">Mozilla Firefox</a> or
                        <a href="http://www.google.com/chrome">Google Chrome</a>
                    </iframe>
                </div>
              </div>
              
              <div class="sidePanels inlinePanel">
                <h2 style="width:75%"><span class="shadow">Click Stream Playback</span>
                    <!-- Search box -->
                    <div id="searchContainer">
                        <a id="searchButton" class="greenBtn"><span>Search</span></a>                        
                        <div id="searchBox" style="display: none;">                
                            <form id="searchForm" method="GET">                                  
                                <fieldset id="body">
                                    <fieldset>
                                        <label for="userid">User ID</label>
                                        <input type="text" class="deletable" name="userid" id="userid" value="<%= userID %>" />
                                    </fieldset>
                                    <fieldset>
                                        <label for="sessionid">Session ID</label>
                                        <input type="text" class="deletable" name="SessionTxt" id="sessionid" style="font-size:11px" value="<%= sessionTXT %>" />
                                    </fieldset>                                    
                                    <fieldset>
                                        <label>Time frame</label>
                                        <input type="text" class="deletable" name="from" id="fromTime" placeholder="From" style="width:34%" value="<%= fromTime %>" />
                                        <em> - </em>
                                        <input type="text" class="deletable" name="to" id="toTime" placeholder="To" style="width:34%" value="<%= toTime %>" />                                        
                                    </fieldset>         
                                    <input type="button" id="submitBtn" value="Submit" class="submitSearch greenBtn">                           
                                </fieldset>
                                <input type="hidden" id="selectedDate" name="selectedDate" value="<%= selectedDate %>"/>
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
                                        <div class="rightCell">Page Name</div>
                                    </th>
                                </tr>
                            </table>
                    <% } %>
                    
                    <!-- Body for click stream table -->
                    <div class="artTableFrame clickStreamTableFrame">
                        <table class="artTable" id="click-stream-table" summary="Click Summary">
                          <%  HashMap clickElement = null; 
                              for (int i = 0; i < clickElements.size(); i++) { 
                                  clickElement = (HashMap) clickElements.get(i);                                      
                          %>
                                <tbody class='<%= (i%2==0)?"one":"two" %>' id='rec<%= i %>'>
                                    <tr>
                                        <td>
                                            <div class="leftCell">
                                                <%= i + 1 %>
                                            </div>
                                            <div class="rightCell">
                                                <% String htmlPageID = (String) clickElement.get("htmlPageID"); %>
                                                <% if (htmlPageID != null) { %>
                                                <a href="./iview.web?HtmlPage_ID=<%=htmlPageID%>" 
                                                    id="pagelink" 
                                                    onClick="setIndex(<%= i %>)" 
                                                    target="htmlpageviewframe"
                                                    data-queryparams="<%= (String) clickElement.get("queryParams") %>">
                                                    <%= (String) clickElement.get("pageName") %>
                                                </a>
                                                <% } else { %>
                                                    <a href="http://www.officemax.com/<%= (String) clickElement.get("pageName") %>"
                                                       id="pagelink" 
                                                       onClick="setIndex(<%= i %>)" 
                                                       target="htmlpageviewframe"
                                                       data-queryparams="<%= (String) clickElement.get("queryParams") %>">
                                                        <%= (String) clickElement.get("pageName") %>
                                                    </a>
                                                <% } %>
                                            </div>
                                        </td>
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
                    setIndex(currentIndex+1);
                }
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
                showQueryParams(); 
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
            	
            	// Pass saved query parameters to a parsing function, then display the parsed query parameters
            	$('#query-params-table').html( parseQueryParams(clickElementParams) );
            }
            
            function parseQueryParams(params) {
            	// Check if there are any parameters. If not, return a message saying so.
            	if (params === "" || params === "null") 
           			return "<tbody><tr><th>There are no query parameters for this page.</th></tr></tbody>";
            	
            	// Initialize table headers
            	var queryParamsHTML = "<tbody><tr><th style='padding-left:10px;text-align:left'>Name</th><th style='text-align:left'>Value</th></tr></tbody>";
            	
            	// Split key-value pairs            	
            	var paramArray = params.split("&");
            	            	
            	for (var i = 0; i < paramArray.length; i++) {
            		// Split name and value of each query parameter
            		var query = paramArray[i].split("=");
            		
            		// Build a table row for each query parameter            		
            		queryParamsHTML += "<tbody class='" + ((i%2 == 0)?"one":"two") + "'><tr>";
            		queryParamsHTML += "<td class='queryNameCell'>" + query[0] + "</td>";
            		queryParamsHTML += "<td>" + query[1] + "</td>";    
            		queryParamsHTML += "</tr></tbody>";
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
                    	if ($('#ui-timepicker-div').css('display') == 'none') {
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
                			$('#selectedDate').val(yyyy+""+mm+""+dd);
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
                	
                	form.submit();                	
                });             
            });
            
            $(function() { 
            	$('#fromTime').timepicker();
				$('#toTime').timepicker();         
				$('#ui-timepicker-div').addClass('timepicker-ovrd');
            });
            // End search form code
            
            function setSessionTxt(sessionid) {
            	$('#sessionid').val(sessionid);
            	$('#selectedDate').val('');
            	$('#fromTime').val('');
            	$('#toTime').val('');
            	$("#submitBtn").click();
            }
            
            // Find the browser's window size and adjust page elements accordingly to fit the screen.
            $(function() {
            	var w = document.body.clientWidth;
            	
            	if (w <= 1450 && w >= 1350) {
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
            	}
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