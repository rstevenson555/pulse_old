SELECT aa, count(*) from (
       select DISTINCT sessionPK, DATE_FORMAT(Time, "%H") aa
       from accessrecords where DATE_FORMAT(Time,"%d")='26'
       )
group by aa;


Create  table xyz as 
       select DISTINCT Session_ID, DATE_FORMAT(Time, "%H") aa
       from accessrecords where DATE_FORMAT(Time,"%d")='23'


select aa, count(*)
in (Create  table xyz as 
       select DISTINCT Session_ID, DATE_FORMAT(Time, "%H") aa
       from accessrecords where DATE_FORMAT(Time,"%d")='23')
group by aa


select DATE_FORMAT(Time, "%d %H %M"), count(*)
from accessrecords 
group by DATE_FORMAT(Time, "%d %H %M");


select aa, count(*)
from xyz
group by aa

SELECT DATE_FORMAT(Time, "%H") as aa , count(distinct Session_ID) from AccessRecords
where DATE_FORMAT(Time,"%d")='23' GROUP BY aa;

SELECT CONCAT(DATE_FORMAT(Time,"%H"),TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15) as a, 
              count(distinct Session_ID) as b, 
              TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 + DATE_FORMAT(TIME,"%H")*60
              as MinSinceMidnight,  TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 as yyz
              from accessrecords 
              where DATE_FORMAT(Time,"%d")='23' 
              GROUP BY MinSinceMidnight;
              
              
              //TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15
              
              
SELECT DATE_ADD( DATE_FORMAT(Time,"%d%H"),  
                 INTERVAL 2  MINUTE ) as a, 
              count(distinct Session_ID) as b, 
              TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 + DATE_FORMAT(TIME,"%H")*60
              as MinSinceMidnight,  TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 as yyz
              from accessrecords 
              where DATE_FORMAT(Time,"%d")='23' 
              GROUP BY MinSinceMidnight;
              
SELECT CONCAT(DATE_FORMAT(Time,"%H"), TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15) as a, 
              count(distinct Session_ID) as b, 
              TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 + DATE_FORMAT(TIME,"%H")*60
              as MinSinceMidnight,  FORMAT(TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15,0) as yyz
              from accessrecords 
              where DATE_FORMAT(Time,"%d")='23' 
              GROUP BY MinSinceMidnight;
              
              
SELECT CONCAT(DATE_FORMAT(Time,"%H"),"YYY" )
	                    as a, 
	                    count(distinct Session_ID) as b, 
	                    TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 + DATE_FORMAT(TIME,"%H")*60
	                    as MinSinceMidnight,  FORMAT(TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15,0) as yyz
	                    from accessrecords 
	                    where DATE_FORMAT(Time,"%d")='23' 
              GROUP BY MinSinceMidnight;
           

SELECT CONCAT(DATE_FORMAT(Time,"%H"),DATE_FORMAT(DATE_ADD(Time,INTERVAL
              (15-TRUNCATE(DATE_FORMAT(Time,"%i")%15,0))
              MINUTE),"%i"))
	                    as timePeriod, 
	                    count(distinct Session_ID) as distSessions, 
	                    TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 + DATE_FORMAT(TIME,"%H")*60
	                    as MinSinceMidnight,  FORMAT(TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15,0) as yyz
	                    from accessrecords 
	                    where DATE_FORMAT(Time,"%d")='23' 
              GROUP BY MinSinceMidnight order by timePeriod;


SELECT p.PageName, COUNT( ar.Page_ID) as cnt, FORMAT(AVG(ar.loadTime),0) as AVE_LT, SUM(ar.loadTime) as TOT_LT from AccessRecords ar, Pages p
 Where DATE_FORMAT(Time,"%d")='23' AND p.Page_ID=ar.Page_ID 
 GROUP BY ar.Page_ID ORDER BY TOT_LT DESC;




SELECT TIME, CONCAT(DATE_FORMAT(Time,"%H"),
                   DATE_FORMAT(DATE_SUB(Time,INTERVAL
                   (TRUNCATE(DATE_FORMAT(Time,"%i")%15,0))
                   MINUTE),"%i"))
	           as timePeriod,
	           qt.Query_ID as qid,
	      ms.Machine_ID as Machine,
	      TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 + DATE_FORMAT(TIME,"%H")*60
	           as MinSinceMidnight,  
	      FORMAT(TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15,0) as yyz
	      from accessrecords ar, 
	           Machines ms,
	           Queries qt
	      where 
	      DATE_FORMAT(Time,"%d%H")='2912' and 
	      ar.Machine_ID=ms.Machine_ID and 
	      ms.MachineName='NAS3' and
	      qt.QueryName='QuarterHourlySession'



SELECT CONCAT(DATE_FORMAT(Time,"%H"),
                   DATE_FORMAT(DATE_SUB(Time,INTERVAL
                   (TRUNCATE(DATE_FORMAT(Time,"%i")%15,0))
                   MINUTE),"%i"))
	           as timePeriod,
	           qt.Query_ID as qid,
	      count(distinct Session_ID) as distSessions,
	      count(Session_ID) as totalSessions,
	      ms.Machine_ID as Machine,
	      TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 + DATE_FORMAT(TIME,"%H")*60
	           as MinSinceMidnight,  
	      FORMAT(TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15,0) as yyz
	      from accessrecords ar, 
	           Machines ms,
	           Queries qt
	      where 
	      DATE_FORMAT(Time,"%d")='30' and 
	      ar.Machine_ID=ms.Machine_ID and 
	      ms.MachineName='NAS3' and
	      qt.QueryName='QuarterHourlySession'
              GROUP BY timePeriod order by timePeriod ASC;


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






INSERT INTO Queries (Query,OpUser_ID,QueryName) Values('TEST',1,'TEST Query');

INSERT INTO OpUsers (login) Values ('Admin');

              DATE_FORMAT(Time,"%H"),DATE_FORMAT(Time,"%i")/15,0)*15)
              
              
              
              
              


SELECT CONCAT(DATE_FORMAT(Time,"%H"),
                   DATE_FORMAT(DATE_SUB(Time,INTERVAL
                   (TRUNCATE(DATE_FORMAT(Time,"%i")%15,0))
                   MINUTE),"%i"))
	           as timePeriod,
	           qt.Query_ID as qid,
	      count(distinct Session_ID) as distSessions,
	      count(Session_ID) as totalSessions,
	      ms.Machine_ID as Machine,
	      TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15 + DATE_FORMAT(TIME,"%H")*60
	           as MinSinceMidnight,  
	      FORMAT(TRUNCATE(DATE_FORMAT(Time,"%i")/15,0)*15,0) as yyz
	      from accessrecords ar, 
	           Machines ms,
	           Queries qt
	      where 
	      DATE_FORMAT(Time,"%d")='30' and 
	      ar.Machine_ID=ms.Machine_ID and 
	      ms.MachineName='NAS3' and
	      qt.QueryName='QuarterHourlySession'
              GROUP BY timePeriod order by timePeriod ASC;