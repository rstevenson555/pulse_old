create table AccessRecords (
      RecordPK INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Page_ID INT,
      User_ID INT,
      Time   Timestamp,
      Session_ID INT,
      Machine_ID   INT,
      LoadTime INT
      );
      
CREATE TABLE Users (
      User_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      userName VARCHAR(25)
      );
      
CREATE TABLE Pages (
      Page_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      pageName VARCHAR(25)
      );
      
CREATE TABLE Sessions(
      Session_ID INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      IPAddress VARCHAR(20),
      sessionTXT VARCHAR(50),
      browserTXT VARCHAR(30)
      );
      
CREATE TABLE Machines(
      Machine_ID INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      MachineName VARCHAR(50)
      );
      
CREATE TABLE Queries(
      Query_ID INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Query    VARCHAR(255),
      User_ID INT
      );
      
CREATE TABLE OPUser(
      OpUser_ID INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      login VARCHAR(20)
      );
      
CREATE TABLE HistoricalRecords (
      HR_ID INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Machine_ID INT,
      Time   Date,
      Query_ID INT,
      Distinct_Hits INT,
      Total_Hits INT
      );

CREATE TABLE LoadTimes(
      LT_ID INT  UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      HR_ID INT,
      Page_ID INT,
      AverageLoadTime INT,
      TotalLoadTime INT,
      MaxLoadTime INT
      );
      
CREATE TABLE StagingHourly(
      Session_ID INT,
      aa Timestamp
      );
CREATE TABLE StagingQuarterHourly(
      Session_ID INT,
      aa Timestamp
      );
CREATE TABLE StagingMinute(
      Session_ID INT,
      aa Timestamp
      );
      
      

      
      
