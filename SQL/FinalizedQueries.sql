
//********************************************
// QuarterHourlySession
//********************************************
// This is the Quarter Hourly Query so far.
// There may be a change in the future.
SELECT  DATE_FORMAT(DATE_SUB(Time,INTERVAL
                   (TRUNCATE(DATE_FORMAT(Time,"%i")%15,0))
                   MINUTE),"%Y%m%d%H%i")
	           as timePeriod,
	           qt.Query_ID as qid,
	      count(distinct Session_ID) as distSessions,
	      count(Session_ID) as totalSessions,
	      ms.Machine_ID as Machine
	      from accessrecords ar, 
	           Machines ms,
	           Queries qt
	      where 
	      DATE_FORMAT(Time,"%d")='30' and 
	      ar.Machine_ID=ms.Machine_ID and 
	      ms.MachineName='NAS3' and
	      qt.QueryName='QuarterHourlySession'
              GROUP BY timePeriod order by timePeriod ASC;
              
              
              
              
              


              