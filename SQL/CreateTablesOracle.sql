drop table DailyLoadTimes;
drop table AccessRecords;
drop table LoadTimes;
drop table HistoricalRecords;
drop table Uptimes;
drop table Machines;
drop table QUERIES;
drop table OPUSERS;
drop table Pages;
drop table sessions;
drop table users;
drop sequence AccessRecordsSequence;
drop sequence UsersSequence;
drop sequence PagesSequence;
drop sequence SessionsSequence;
drop sequence MachinesSequence;
drop sequence QueriesSequence;
drop sequence OpUsersSequence;
drop sequence UptimesSequence;

CREATE TABLE Users (
      User_ID NUMBER(10) NOT NULL,
      userName VARCHAR2(25),
      CONSTRAINT Users_User_ID_pk PRIMARY KEY (User_ID)
      );
      
CREATE TABLE Pages (
      Page_ID NUMBER(4) NOT NULL,
      pageName VARCHAR2(25),
      CONSTRAINT Pages_Page_ID_pk PRIMARY KEY (Page_ID)
      );
      
CREATE TABLE Sessions(
      Session_ID NUMBER(10) NOT NULL,
      IPAddress VARCHAR2(20),
      sessionTXT VARCHAR2(50),
      CONSTRAINT Sessions_Session_ID_pk PRIMARY KEY (Session_ID)
      );
      
CREATE TABLE Machines(
      Machine_ID NUMBER(4) NOT NULL,
      MachineName VARCHAR2(50),
      CONSTRAINT Machines_Machine_ID_pk PRIMARY KEY (Machine_ID)
      );
      
create table AccessRecords (
      RecordPK NUMBER(12) NOT NULL,
      Page_ID NUMBER(4),
      User_ID NUMBER(10),
      Time   Date,
      Session_ID NUMBER(10),
      Machine_ID   NUMBER(4),
      LoadTime NUMBER(12),
      CONSTRAINT AccessRecords_RecordPK_pk PRIMARY KEY (RecordPK),
      CONSTRAINT Users_fk FOREIGN KEY (User_ID) 
                 REFERENCES Users (User_ID),
      CONSTRAINT Pages_fk FOREIGN KEY (Page_ID) 
                 REFERENCES Pages (Page_ID),
      CONSTRAINT Sessions_fk FOREIGN KEY (Session_ID) 
                 REFERENCES Sessions (Session_ID),
      CONSTRAINT Machines_fk FOREIGN KEY (Machine_ID) 
                 REFERENCES Machines (Machine_ID)
      );
      

CREATE TABLE OPUsers(
      OpUser_ID NUMBER(10) NOT NULL,
      login VARCHAR2(20),
      password VARCHAR2(20),
      CONSTRAINT OPUsers_OpUser_ID_pk PRIMARY KEY (OPUser_ID)
      );

CREATE TABLE Queries(
      Query_ID NUMBER(10) NOT NULL,
      Query    VARCHAR2(2000),
      OpUser_ID NUMBER(10),
      QueryName VARCHAR2(20),
      CONSTRAINT Queries_Query_ID_pk PRIMARY KEY (Query_ID),
      CONSTRAINT OpUser_fk FOREIGN KEY (OpUser_ID) 
                 REFERENCES OPUsers (OpUser_ID)
      );
      
CREATE TABLE HistoricalRecords (
      HR_ID NUMBER(10) NOT NULL,
      Machine_ID NUMBER(10),
      Time   Date,
      Query_ID NUMBER(10),
      Distinct_Hits NUMBER(12),
      Total_Hits NUMBER(12),
      CONSTRAINT  HistoricalRecords_HR_ID_pk PRIMARY KEY (HR_ID),
      CONSTRAINT Machine_HR_fk FOREIGN KEY (Machine_ID) 
                 REFERENCES Machines (Machine_ID),
      CONSTRAINT Query_fk FOREIGN KEY (Query_ID) 
                 REFERENCES Queries (Query_ID)
      );

CREATE TABLE LoadTimes(
      LT_ID NUMBER(10) NOT NULL,
      HR_ID NUMBER(10),
      Page_ID NUMBER(4),
      AverageLoadTime NUMBER(12),
      TotalLoadTime NUMBER(12),
      MaxLoadTime NUMBER(12),
      CONSTRAINT LoadTimes_LT_ID_pk PRIMARY KEY (LT_ID),
      CONSTRAINT HR_ID_fk FOREIGN KEY (HR_ID) 
                 REFERENCES HistoricalRecords (HR_ID),
      CONSTRAINT Pages_LT_fk FOREIGN KEY (Page_ID) 
                 REFERENCES Pages (Page_ID)
      );

CREATE TABLE Uptimes(
	     Uptime_id NUMBER(10),
	     startTime Date,
	     endTime Date,
	     Machine_ID NUMBER(10),
	     Filename VARCHAR2(30),
	     Archive VARCHAR2(30),
	     CONSTRAINT Uptimes_Uptime_ID_pk PRIMARY KEY (Uptime_id),
             CONSTRAINT Machine_ID_Uptime_machines_fk FOREIGN KEY (Machine_ID)
                        REFERENCES Machines (Machine_ID)
             );

CREATE TABLE DailyLoadTimes(
      DailyLoadTimes_ID NUMBER(10),
      Page_ID NUMBER(10),
      Machine_ID NUMBER(4),
      AverageLoadTime NUMBER(10),
      MaxLoadTime NUMBER(10),
      MinLoadTime NUMBER(10),
      TotalLoads NUMBER(10),
      Day DATE
      );

ALTER TABLE HistoricalRecords ADD CONSTRAINT 
HR_Machine_ID_Time_Query_ID_UK UNIQUE
(Machine_ID, Time, Query_ID);

ALTER TABLE DailyLoadTimes ADD CONSTRAINT
DLT_Page_ID_Machine_ID_Day_UK UNIQUE 
(Page_ID, Machine_ID, Day);



CREATE SEQUENCE AccessRecordsSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE UsersSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE PagesSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SessionsSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE MachinesSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE QueriesSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE OpUsersSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE UptimesSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE DailyLoadTimesSequence INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE HistoricalRecordsSequence INCREMTNT BY 1 START WITH 1;

CREATE INDEX TIME_ACCESSRECORDS_INDEX ON ACCESSRECORDS (Time);




