--create database artar;
 
drop table AccessRecords;
drop table Users;
drop table Pages;
drop table Sessions;
drop table Machines;
drop table Contexts;
drop table Apps;
drop table Rload_AccessRecords;
drop table Rload_Users;
drop table Rload_Pages;
drop table Rload_Sessions;
drop table Rload_Machines;
drop table Rload_Contexts;
drop table Rload_Apps;
drop table ExternalAccessRecords;
drop table ExternalStats;
drop table FiveSecondLoads;
drop table DailySummary;
drop table DailyLoadTimes;
drop table HourlyStatistics;
drop table MinuteStatistics;
drop table Historical_External_Statistics;
drop table Statistics;
drop table DailyPageLoadTimes;
drop table DailyContextStats;
drop table Browsers;
drop table BrowserStats;
drop table AccumulatorStats;
drop table Accumulator;

create table AccessRecords (
      RecordPK INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      insertTime         Timestamp,
      Page_ID      INT,
      User_ID      INT,
      Session_ID   INT,
      Machine_ID   INT,
      Context_ID   INT,
      App_ID       INT,
      Branch_Tag_ID INT,
      Time         Timestamp,
      LoadTime     INT,
      QueryParameter_ID INT,
      requestType INT,
      requestToken INT, 
      userServiceTime INT
      );

CREATE TABLE QueryParameters (
      QueryParameter_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      queryParams TEXT DEFAULT NULL,
      lastModTime  Timestamp
      );
      
CREATE TABLE Users (
      User_ID      INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      userName     VARCHAR(25),
      fullName     VARCHAR(64),
      companyName  VARCHAR(50),
      lastModTime         Timestamp
      );
      
CREATE TABLE Pages (
      Page_ID      INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      pageName     VARCHAR(75),
      isErrorPage  VARCHAR(1) DEFAULT "N",
      lastModTime         Timestamp
      );
      
CREATE TABLE Sessions(
      Session_ID   INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      IPAddress    VARCHAR(20),
      sessionTXT   VARCHAR(50),
      browserType  VARCHAR(255),
      lastModTime  Timestamp,
      insertTime   Timestamp,
      User_ID      INT,
      Context_ID   INT,
      sessionStartTime Timestamp,
      sessionEndTime   Timestamp,
      sessionHits  INT
      );
      
CREATE TABLE Machines(
      Machine_ID INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      MachineName VARCHAR(50),
      shortName VARCHAR(10),
      machineType VARCHAR(2),
      lastModTime         Timestamp
      );

CREATE TABLE Contexts(
      Context_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      contextName VARCHAR(50),
      lastModTime         Timestamp
      );
      
      
CREATE TABLE Apps(
      App_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      appName VARCHAR(50),
      lastModTime         Timestamp
      );

create table Rload_AccessRecords (
      RecordPK INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      lastModTime         Timestamp,
      Page_ID INT,
      User_ID INT,
      Session_ID INT,
      Machine_ID   INT,
      Context_ID INT,
      App_ID INT,
      Time   Timestamp,
      LoadTime INT
      );
      
CREATE TABLE Rload_Users (
      User_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      userName VARCHAR(25),
      fullName VARCHAR(64),
      companyName VARCHAR(50),
      lastModTime         Timestamp
      );
      
CREATE TABLE Rload_Pages (
      Page_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      pageName VARCHAR(75),
      isErrorPage VARCHAR(1) DEFAULT "N",
      lastModTime         Timestamp
      );
      
CREATE TABLE Rload_Sessions(
      Session_ID INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      IPAddress VARCHAR(20),
      sessionTXT VARCHAR(50),
      browserType VARCHAR(125),
      User_ID INT,
      lastModTime         Timestamp
      );
      
CREATE TABLE Rload_Machines(
      Machine_ID INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      MachineName VARCHAR(50),
      shortName VARCHAR(10),
      machineType VARCHAR(2),
      lastModTime         Timestamp
      );

CREATE TABLE Rload_Contexts(
      Context_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      contextName VARCHAR(50),
      lastModTime         Timestamp
      );
      
      
CREATE TABLE Rload_Apps(
      App_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      appName VARCHAR(50),
      lastModTime         Timestamp
      );


create table ExternalAccessRecords(
      RecordPK INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      lastModTime         Timestamp,
      Page_ID INT,
      User_ID INT,
      Session_ID INT,
      Machine_ID   INT,
      Context_ID INT,
      App_ID INT,
      Classification_ID INT,
      DataSection TEXT,
      Time   Timestamp,
      LoadTime INT
      );

create table ExternalStats (
      Classification_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Destination VARCHAR(75),
      Description VARCHAR(255),
      lastModTime         Timestamp
      );

----------------------------------------------------------------
----------------------------------------------------------------
-- Historical Information.
----------------------------------------------------------------
----------------------------------------------------------------


CREATE TABLE FiveSecondLoads(
      RecordPK INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      lastModTime         Timestamp,
      Page_ID INT,
      User_ID INT,
      Session_ID INT,
      Machine_ID   INT,
      Context_ID INT, 
      App_ID INT,
      Time   Timestamp,
      LoadTime INT
      ); 

CREATE table DailySummary(
      Day DATE NOT NULL PRIMARY KEY,
      lastModTime         Timestamp,
      TotalLoads INT,
      AverageLoadTime INT,
      NinetiethPercentile INT,
      TwentyFifthPercentile INT,
      FiftiethPercentile INT,
      SeventyFifthPercentile INT,
      MaxLoadTime  INT,
      MinLoadTime  INT,
      DistinctUsers INT,
      ErrorPages INT,
      ThirtySecondLoads INT,
      TwentySecondLoads INT,
      FifteenSecondLoads INT,
      TenSecondLoads INT,
      FiveSecondLoads INT,
      MaxLoadTime_Page_ID INT,
      MaxLoadTime_User_ID INT,
      State VARCHAR(1) DEFAULT "O"
      );

Create table DailyPageLoadTimes(
    DailyLoadTime_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
    lastModTime         Timestamp,
    Day DATE,
    Page_ID INT,
    Context_ID INT,
    machineType VARCHAR(2),
    TotalLoads INT,
    AverageLoadTime INT,
    NinetiethPercentile INT,
    TwentyFifthPercentile INT,
    FiftiethPercentile INT,
    SeventyFifthPercentile INT,
    MaxLoadTime  INT,
    MinLoadTime  INT,
    DistinctUsers INT,
    ErrorPages INT,
    ThirtySecondLoads INT,
    TwentySecondLoads INT,
    FifteenSecondLoads INT,
    TenSecondLoads INT,
    FiveSecondLoads INT,
    State VARCHAR(1) DEFAULT "O"
    );

Create table HourlyStatistics(
    Machine_id INT,
    lastModTime         Timestamp,
    Time timestamp,
    TotalLoads INT,
    AverageLoadTime INT,
    NinetiethPercentile INT,
    TwentyFifthPercentile INT,
    FiftiethPercentile INT,
    SeventyFifthPercentile INT,
    MaxLoadTime INT,
    MinLoadTime INT,
    DistinctUsers INT,
    ErrorPages INT,
    ThirtySecondLoads INT,
    TwentySecondLoads INT,
    FifteenSecondLoads INT,
    TenSecondLoads INT,
    FiveSecondLoads INT,
    State VARCHAR(1) DEFAULT "O"
    );

Create table DailyContextStats(
    Day        Date,
    Context_ID INT,
    Count      INT,
    State      VARCHAR(1) DEFAULT "O",
    lastModTime         Timestamp
    );

Create table Browsers(
    Browser_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
    patternMatchString VARCHAR(40),
    Description VARCHAR(50),
    lastModTime         Timestamp
    );

Create table BrowserStats(
    Day     Date,
    Browser_ID INT,
    Count   INT,
    State   VARCHAR(1) DEFAULT "O",
    lastModTime         Timestamp
    );
    

Create table MinuteStatistics(
    Machine_id INT,
    lastModTime         Timestamp,
    Time timestamp,
    TotalLoads INT,
    AverageLoadTime INT,
    NinetiethPercentile INT,
    TwentyFifthPercentile INT,
    FiftiethPercentile INT,
    SeventyFifthPercentile INT,
    MaxLoadTime INT,
    MinLoadTime INT,
    DistinctUsers INT,
    ErrorPages INT,
    ThirtySecondLoads INT,
    TwentySecondLoads INT,
    FifteenSecondLoads INT,
    TenSecondLoads INT,
    FiveSecondLoads INT,
    State VARCHAR(1) DEFAULT "O"
    );

Create table Historical_External_Statistics(
    Statistics_ID INT,
    lastModTime         Timestamp,
    StartTime timestamp,
    SummaryPeriodMinutes INT,
    Count INT,
    AverageLoadTime INT,
    MaximumLoadTime INT,
    MinimumLoadTime INT,
    State VARCHAR(1) DEFAULT "O"
    );

Create table AccumulatorStats(
    AccumulatorStat_ID INT,
    lastModTime         Timestamp,
    Time               timestamp,
    Value              INT
    );

Create table AccumulatorEvent(
      AccumulatorEvent_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      AccumulatorStat_ID INT UNSIGNED,
      insertTime         Timestamp,
      Machine_ID   INT,
      Context_ID   INT,
      Branch_ID    INT,
      App_ID       INT,
      Time         Timestamp,
      intValue     INT,
      doubleValue DOUBLE,
      stringValue VARCHAR(40),
      dataType    VARCHAR(60)
      );

Create table Accumulator(
    AccumulatorStat_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
    AccumulatorName VARCHAR(40),
    AccumulatorDescription TEXT,
    AccumulatorType VARCHAR(20),
    DataUnits       VARCHAR(40),
    lastModTime         Timestamp
    );


Create table LoadTestTransactions(
    Transaction_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
    Script_ID      INT UNSIGNED NOT NULL,
    transactionName VARCHAR(20),
    lastModTime         Timestamp
    );

Create table LoadTestScript(
    Script_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
    scriptName VARCHAR(20),
    lastModTime         Timestamp
    );

Create table LoadTests(
    LoadTest_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
    testName VARCHAR(255),
    Context_ID   INT,
    Branch_ID    INT,
    lastModTime  Timestamp,
    startTime    Timestamp,
    endTime      Timestamp
    );


Create table LoadTestTransactionMinuteRecords(
    LoadTest_ID INT UNSIGNED NOT NULL,
    insertTime  Timestamp,
    Time        Timestamp,
    Transaction_ID INT UNSIGNED,
    count       INT,
    avg         INT,
    max         INT,
    min         INT,
    NinetiethPercentile INT,
    FiftiethPercentile INT,
    );

Create table LoadTestTransactionSummary(
    LoadTest_ID INT UNSIGNED NOT NULL,
    insertTime  Timestamp,
    Transaction_ID INT UNSIGNED,
    count       INT,
    avg         INT,
    max         INT,
    min         INT,
    NinetiethPercentile INT,
    FiftiethPercentile INT,
    );

Create table LoadTestSummary(
    LoadTest_ID INT UNSIGNED NOT NULL,
    insertTime  Timestamp,
    Script_ID INT UNSIGNED,
    count       INT,
    avg         INT,
    max         INT,
    min         INT,
    NinetiethPercentile INT,
    FiftiethPercentile INT,
    );


CREATE TABLE HtmlPageResponse (
   HtmlPageResponse_ID   INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
   insertTime Timestamp,
   Branch_ID  INT,
   Machine_ID INT,
   Context_ID INT,
   Page_ID    INT,
   Time       Timestamp,
   sessionTXT VARCHAR(100),
   requestToken INT,
   requestTokenCount INT,
   encodedPage MEDIUMTEXT
);
 CREATE INDEX HTMLPAGERESPONSE_TIME_SESSION on HtmlPageResponse (Session_ID,Time);

--DROP TABLE IF EXISTS Deployments;
CREATE TABLE Deployments (
  Product varchar(20) default NULL,
  Machine varchar(30) default NULL,
  Server_Group varchar(10) default NULL,
  Properties_File varchar(20) default NULL,
  Release_Tag varchar(30) default NULL,
  Application_Context varchar(20) default NULL,
  Deploy_Time varchar(15) NOT NULL default '',
  IsCurrent char(1) default NULL,
  SomeComment varchar(200) default NULL,
  NovellUserId varchar(10) default NULL,
  ChangeControllNumber varchar(20) default NULL
) TYPE=MyISAM DEFAULT CHARSET=latin1;


 CREATE INDEX USERS_USER_NAME_INDEX      on Users (userName);
 CREATE INDEX PAGES_PAGE_NAME_INDEX      on Pages (pageName);
 CREATE INDEX SESSIONS_SESSION_TXT_INDEX on Sessions (sessionTXT,IPAddress);
 CREATE INDEX ACCUMULATOR_TIME_INDEX     on AccumulatorEvent(Time);
 CREATE INDEX ACCESSRECORDS_TIME2     on AccessRecords(Time);
 CREATE INDEX ACCESSRECORDS_TIME3     on AccessRecords(Time);

CREATE INDEX QUERYPARAMS_INDEX     on QueryParameters(queryParams(255)); 

 CREATE UNIQUE INDEX ACCUMULATORSTATS_ID_TIME_INDEX     on AccumulatorStats(AccumulatorStat_ID,Time);
 CREATE UNIQUE INDEX BROWSERSTATS_ID_DAY_BROWSER_ID     on BrowserStats(Day,Browser_ID);
 CREATE UNIQUE INDEX DAILYLOADTIMES_ID_DAY_PAGE_CONTEXT on BrowserStats(Day,Page_ID, Context_ID);

      -- alter table Sessions add sessionStartTime Timestamp;
      -- alter table Sessions add sessionEndTime   Timestamp;
 ALTER table AccessRecords add Branch_Tag_ID INT after App_ID;

ALTER table AccessRecords add  queryParams TEXT default NULL;

//ALTER table AccessRecords add  (requestType VARCHAR(40), requestToken INT, userServiceTime INT);

//requestType, requestToken, userServiceTime 



ALTER table AccessRecords DROP requestType;
ALTER table AccessRecords add (requestType INT);
