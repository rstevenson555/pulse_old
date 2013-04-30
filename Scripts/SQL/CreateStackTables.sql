#d:\opt\mysql\bin\mysql -h 10.3.12.225 -u art_user --password=stream1 -t -vvv < d:\apps\logParser\SQL\CreateStackTables.sql

#create database artm
#GRANT ALL PRIVILEGES ON artm TO art_user@localhost IDENTIFIED BY 'stream1' WITH GRANT OPTION;
#GRANT ALL PRIVILEGES ON artm TO art_user@"%" IDENTIFIED BY 'stream1' WITH GRANT OPTION;

connect artm;

drop TABLE SequenceTable;
drop TABLE StackTraceBeanContainers;
drop TABLE StackTraceDetails;
drop TABLE StackTraces;
drop TABLE StackTraceRows;

create table StackTraces (
    Trace_id INT UNSIGNED NOT NULL PRIMARY KEY,
      Message_ID INT UNSIGNED,
    Trace_Key VARCHAR(50),
    Trace_Message VARCHAR(250),
      lastModTime  Timestamp,
    Trace_Time TIMESTAMP,
    ART_User_id INT,
      Session_ID   INT,
      Machine_ID   INT,
      Context_ID   INT,
      App_ID       INT,
      Branch_Tag_ID INT
  );

CREATE TABLE StackTraceRows (
      Row_id INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Row_Message VARCHAR(250)
      );

CREATE TABLE StackTraceMessages (
      Message_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Message    TEXT
      )
      
CREATE TABLE StackTraceDetails (
      Trace_id INT UNSIGNED NOT NULL,
      Row_id INT unsigned NOT NULL, 
      Stack_Depth INT,
      index trace_id_ind (Trace_id),
      index row_id_ind (Row_id),
      FOREIGN KEY (Trace_id) REFERENCES StackTraces(Trace_id),
      FOREIGN KEY (Row_id) REFERENCES StackTraceRows(Row_id),
      unique (Trace_id, Stack_Depth)
      );
      

CREATE TABLE StackTraceBeanContainers(
      Trace_id INT UNSIGNED NOT NULL,
      JspBeanContainer TEXT,
      index trace_id_ind (Trace_id),
      FOREIGN KEY (Trace_id) REFERENCES StackTraces(Trace_id)
      );
      
CREATE TABLE SequenceTable(
      SequenceName Varchar(255) NOT NULL PRIMARY KEY,
      count int
      );

CREATE TABLE PricingErrors(
      PricingError_id INT UNSIGNED NOT NULL,
      Error_Time TIMESTAMP,
      Class_Method VARCHAR(50),
      Class_Process VARCHAR(50),
      Error_Exception VARCHAR(125),
      Exception_Message VARCHAR(125),
      DataQueueSequence VARCHAR(14),
      PricingMachine_IP VARCHAR(15)
      );
CREATE TABLE NAPricings(
      NAPricing_id INT UNSIGNED NOT NULL,
      Event_Time TIMESTAMP,
      Class_Method VARCHAR(50),
      DataQueueSequence VARCHAR(16),
      NAPricing_Message VARCHAR(255),
      PricingMachine_IP VARCHAR(15),
      Pricing_Request TEXT,
      Pricing_Response TEXT,
      Request_Type VARCHAR(10),
      Detail_Error INT(3)
      );


CREATE TABLE CVSEvents(
      CVSEvent_id INT UNSIGNED NOT NULL,
      Event_Time TIMESTAMP,
      developer_id INT,
      tag_id INT,
      FileName VARCHAR(50), 
      Modified_Version VARCHAR(30),
      Commited_Version VARCHAR(30),
      LogMessage VARCHAR(255)
      );

CREATE TABLE Developers(
      Developer_id INT UNSIGNED NOT NULL,
      Developer_key VARCHAR(20),
      Full_Name VARCHAR(40)
      );

CREATE TABLE CVSTAGS(
      Tag_id INT UNSIGNED NOT NULL,
      Tag_Name VARCHAR(25),
      Tag_Description VARCHAR(255),
      Tag_Flag VARCHAR(1)
      );

CREATE TABLE CVSTagTypes(
      Tag_Flag VARCHAR(1),
      Flag_Description VARCHAR(25)
      );

CREATE TABLE ART_USERS(
      User_id INT UNSIGNED NOT NULL,
      login_id VARCHAR(10),
      passwd   VARCHAR(10),
      FirstName VARCHAR(20),
      LastName  VARCHAR(20),
      Email VARCHAR(50),
      IPAddress VARCHAR(16),
      LastAccess TIMESTAMP,
      LoginCount INT
      );


CREATE TABLE Deployments(
    Product VARCHAR(20),
    Machine VARCHAR(30),
    Server_Group VARCHAR(10),
    Properties_File VARCHAR(20),
    Release_Tag VARCHAR(30),
    Application_Context VARCHAR(20),
    Deploy_Time TIMESTAMP,
    IsCurrent VARCHAR(1),
    SomeComment VARCHAR(200)
    );

      

      


insert into SequenceTable (SequenceName, count) values ("CVSEvents_Sequence",501);
insert into SequenceTable (SequenceName, count) values ("CVSTag_Sequence",501);
insert into SequenceTable (SequenceName, count) values ("Developers_Sequence",501);
insert into SequenceTable (SequenceName, count) values ("PricingError_Sequence",1);
insert into SequenceTable (SequenceName, count) values ("NAPricing_Sequence",1);
insert into SequenceTable (SequenceName, count) values ("Trace_Sequence",1);


insert into SequenceTable (SequenceName, count) values ("ArtUsers_Sequence",501);



Alter table StackTraces  
      DailyPageLoadTimes
      Message_ID INT UNSIGNED,
      lastModTime  Timestamp,
      Session_ID   INT,
      Machine_ID   INT,
      Context_ID   INT,
      App_ID       INT,
      Branch_Tag_ID INT


