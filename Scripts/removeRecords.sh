#!/usr/bin/ksh
export ANT_HOME=/apps/artadmin/ant
export JAVA_HOME=/apps/artadmin/java
export PATH=$PATH:$ANT_HOME/bin:$JAVA_HOME/bin
ant -f /apps/artadmin/art/scripts/storedProcedure.xml runProc  -logfile /apps/artadmin/art/scripts/antLogs/removeRecords.log
