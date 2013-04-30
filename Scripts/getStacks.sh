#!/bin/ksh
export ANT_HOME=/opt/ant
export JAVA_HOME=/opt/java
export PATH=$PATH:$ANT_HOME/bin:$JAVA_HOME/bin:/usr/local/bin:/usr/bin
ant getStacks -f /home/artadmin/art/scripts/getLogs.xml -logfile /home/artadmin/art/scripts/antLogs/getLogfiles.log
