#!/bin/ksh
export ANT_HOME=/apps/artadmin/ant
export JAVA_HOME=/apps/artadmin/java
export PATH=$PATH:$ANT_HOME/bin:$JAVA_HOME/bin:/usr/local/bin
ant -f /apps/artadmin/art/scripts/getLogs.xml -logfile /apps/artadmin/art/scripts/antLogs/getLogfiles.log
