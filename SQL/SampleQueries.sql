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
              GROUP BY MinSinceMidnight;











              DATE_FORMAT(Time,"%H"),DATE_FORMAT(Time,"%i")/15,0)*15)