create table AccessRecords (
      RecordPK NUMBER(12) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Page_ID number(5),
      User_ID NUMBER(10),
      Time   Date,
      Session_ID NUMBER(10),
      Machine_ID   NUMBER(4),
      LoadTime NUMBER
      );
      
CREATE TABLE Users (
      User_ID NUMBER (10) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      userName VARCHAR(25)
      );
      
CREATE TABLE Pages (
      Page_ID NUMBER(6) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      pageName VARCHAR(25)
      );
      
CREATE TABLE Sessions(
      Session_ID NUMBER(10) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      IPAddress VARCHAR(20),
      sessionTXT VARCHAR(50)
      );
      
CREATE TABLE Machines(
      Machines_ID NUMBER(4) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      MachineName VARCHAR(50)
      );
      
CREATE TABLE Queries(
      Query_ID NUMBER UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Query    VARCHAR(255),
      User_ID NUMBER
      );
      
CREATE TABLE OPUser(
      OPUser_ID number(5) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      login VARCHAR(20)
      );
      
CREATE TABLE HistoricalRecords (
      HR_ID NUMBER(10) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      Machine_ID NUMBER(4),
      Time   Date,
      Query_ID NUMBER(5),
      Distinct_Hits NUMBER,
      Total_Hits NUMBER
      );

CREATE TABLE LoadTimes(
      LT_ID NUMBER(10) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
      HR_ID NUMBER(10),
      Page_ID NUMBER(6),
      AverageLoadTime NUMBER,
      TotalLoadTime NUMBER
      MaxLoadTime NUMBER
      );
      
      
