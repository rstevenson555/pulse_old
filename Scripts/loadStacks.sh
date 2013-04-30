#!/usr/bin/ksh
export ANT_HOME=/opt/ant
export JAVA_HOME=/opt/java
export PATH=$PATH:$ANT_HOME/bin:$JAVA_HOME/bin:/usr/bin
ant load.all.stacks -f /apps/artadmin/art/scripts/dbLoad.xml -logfile /home/artadmin/art/scripts/antLogs/antLoadLog.txt
