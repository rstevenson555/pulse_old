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
<script>
    var date_params_url = ""
</script>
<div class="artLeftNav">
    <link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
    <script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js"></script>
    <script src="js/jquery.ui.timepicker.js"></script>
    <script>
        function getQueryStrings() { 
            var assoc  = {};
            var decode = function (s) { return decodeURIComponent(s.replace(/\+/g, " ")); };
            var queryString = location.search.substring(1); 
            var keyValues = queryString.split('&'); 

            for(var i in keyValues) { 
                var key = keyValues[i].split('=');
                if (key.length > 1) {
                assoc[decode(key[0])] = decode(key[1]);
                }
            } 

            return assoc; 
        } 
        $(document).ready(function(){                                                         
        	var sd = getQueryStrings()["selectedDate"]   
            $('#datepicker').datepicker( { 
                dateFormat: 'yymmdd',  
                changeMonth: true,
                changeYear: true,
                showOtherMonths: true,
                showButtonPanel: true,
                defaultDate: +0,
                onSelect: function(dateText, inst) { 
                    date_params_url =  "?selectedDate=" + dateText; //+ "&tY=" + dateText.substring(0,4) + "&cY=" + dateText.substring(0,4);                    
                	$('#selectedDate').val(dateText);
                	
                	// For click stream page only
                	/*if ($('#submitBtn').length == 1)
                		$('#submitBtn').click();             
                	else
                		window.location.href = date_params_url;
                        */
                	// For daily snapshot page only
                	//$('#hiddenform').submit();
                     window.location.href = date_params_url;
                } 
            });
            var myDate;
                            
            if (typeof sd == "undefined" || sd == "") {
                //myDate = new Date();
                //sd = $("#datepicker").datepicker('getDate');
                
            } else {
                //var dstr = $.datepicker.formatDate('M d, yy',
                //    $.datepicker.parseDate('yymmdd', sd
                //).toString());                        
                //20120409
                var year = sd.substring(0,4);
                var month = sd.substring(4,6);
                var day = sd.substring(6,8);
                // month in javascript is base-0
                myDate = new Date(year, month -1, day);
                $('#datepicker').datepicker('setDate', myDate);                     
            }
            
        });
     
    
    </script>
    <style type="text/css">
        .ui-datepicker table {
        box-shadow: none!important;
        }
    </style>

    <!-- Display ART logo -->
    <div style="margin-bottom:-3px">    
    <span class="fade">
        <a class="rollover" href="ViewDailySessionSummary.web" title="Return to home page" accesskey="1">
            <figure class="cube" style="margin: 0px">
                <img src="images/ekg.gif" class="front" width="206" style="height:155px;" alt="ART logo"/>
                <!--[if !IE]> -->
                <img src="images/ARTlogo_back.jpg" class="back" width="206">
                <!-- <![endif]-->
            </figure>
        </a>
    </span>
    </div>

    <div id="sideContent">
        <!-- Display calendar -->
        <div id="datepicker" style="font-size:62.5%;"></div> 

        <!-- Display remaining left nav content -->         
        <h2>Other Graphs</h2>
        <p>Navigate to other graphs and information...</p>
        <ul id="oN">
            <li>
                <a href="ViewRealTimeCharts.web">Realtime Charts</a> 
            </li>
            <li>
                <script >
                    function view_daily_session_summary() {
                    params = location.search;
                    window.location = "ViewDailySessionSummary.web" + params;
                    }
                </script>
                <a href="javascript:view_daily_session_summary()" >Daily Snapshot</a> 
            </li>
            <li>
                <script >
                    function view_click_stream_page_view() {
                    params = location.search;
                    window.location = "ClickStreamPageView.web" + params;
                    }
                </script>
                <a href="javascript:view_click_stream_page_view()" >Click Stream</a> 
            </li>
            <li>
                <script >
                    function view_daily_page_load() {
                    params = location.search;
                    window.location = "ViewDailyPageLoadCharts.web" + params;
                    }
                </script>
                <a href="javascript:view_daily_page_load()">Page Load Details</a>
            </li>
            <li>
                <script >
                    function view_current_deployments() {
                    params = location.search;
                    window.location = "ViewCurrentDeployments.web" + params;
                    }
                </script>
                <a href="javascript:view_current_deployments()">Current Deployments</a>
            </li>
            <li>
                <script >
                    function view_online_report_usage() {
                    params = location.search;
                    window.location = "OnlineReportUsageDetail.web" + params;
                    }
                </script>
                <a href="javascript:view_online_report_usage()">Online Reporting</a>
            </li>
            <li>
                <script >
                    function view_exceptions() {
                    params = location.search;
                    window.location = "ViewExceptions.web" + params;
                    }
                </script>
                <a href="javascript:view_exceptions()">Exceptions (LIVE!)</a>
            </li>
            <li>
                <script >
                    function view_historical() {
                    params = location.search;
                    window.location = "ViewHistoricalCharts.web" + params;
                    }
                </script>
                <a href="javascript:view_historical()">Financial (24hr-delay)</a>
            </li>
            <li>
                <script >
                    function view_time_slice() {
                    params = location.search;
                    window.location = "ViewTimeSliceDetail.web" + params;
                    }
                </script>
                <a href="javascript:view_time_slice()">Time Slice Report</a>
            </li>
            <li>
                <a href="http://java.sun.com/webapps/getjava/BrowserRedirect?locale=en&amp;host=www.java.com:80">
                    Install Java
                </a>
            </li>
        </ul>
    </div>
    <br />
</div>
<div id="toggleNav">
    <button onClick="toggleNav()" id="navButton" title="Hide menu">&#9668;</button>
</div>
<script>
	function toggleNav() {
		var navButton = document.getElementById('navButton');				
		if ($('.artLeftNav').css('display') != 'none') {					
			$('.artLeftNav').hide('fast');
			navButton.style.marginLeft = "0px";
			navButton.innerHTML = "&#9658;";
			navButton.title="Show menu";
		}
		else {
			$('.artLeftNav').show('fast');
			navButton.style.marginLeft = "-23px";
			navButton.innerHTML = "&#9668;";
			navButton.title="Hide menu";
		}
	}
</script>