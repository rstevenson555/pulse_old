
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
              
              
//**************************************************
//Daily Load Time Queries
//**************************************************
SELECT a.Page_ID as pageid, a.Machine_ID as machineid,
        Max(a.loadTime) as maxlt, Min(a.loadTime) as minlt, 
        AVG(a.loadTime) as alt, count(a.loadTime) as tothits, 
        DATE_FORMAT(Time,"%Y%m%d") as timePeriod
 from  AccessRecords a
 WHERE DATE_FORMAT(Time,"%Y%m%d")=?
 GROUP BY a.Page_ID, a.Machine_ID ORDER BY maxlt
              
//**************************************************
//ORACLE ORACLE ORACLE ORACLE ORACLE ORACLE ORACLE 
//Daily Load Time Queries
//**************************************************
SELECT a.Page_ID as pageid, a.Machine_ID as machineid,
        Max(a.loadTime) as maxlt, Min(a.loadTime), AVG(a.loadTime),
        count(a.loadTime) as tothits, 
        TO_CHAR(Time,'yyyymmdd') as timePeriod
 from  AccessRecords a
 WHERE TO_CHAR(Time,'yyyymmdd')='20010430'
 GROUP BY a.Page_ID, a.Machine_ID, TO_CHAR(Time,'yyyymmdd') ORDER BY maxlt
 
 
 //*************************************************
 //ORACLE 
 //QuarterHourlyDaily Load Times
 //*************************************************
 SELECT TO_CHAR(Time,'hh24')||
                     TO_CHAR(TO_NUMBER(TO_CHAR(Time,'mi'),'09') - 
                     MOD(TO_NUMBER(TO_CHAR(Time,'mi'),'09'),15), '09')
  	           as timePeriod,
  	      count(distinct Session_ID) as distSessions,
  	      count(Session_ID) as totalSessions,
  	      Machine_ID as Machine
  	      from accessrecords ar 
  	      where 
  	      TO_CHAR(Time,'dd')='30' 
                group by TO_CHAR(Time,'hh24')||
                     TO_CHAR(TO_NUMBER(TO_CHAR(Time,'mi'),'09') - 
                     MOD(TO_NUMBER(TO_CHAR(Time,'mi'),'09'),15), '09'),
                     Machine_ID
               order by timePeriod ASC;
               
              
              
              
              
              


              