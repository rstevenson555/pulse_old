ANT_HOME=/apps/artadmin/ant
JAVA_HOME=/apps/artadmin/java
PATH=$PATH:$ANT_HOME/bin:$JAVA_HOME/bin
ant -f getLogs.xml store.javaErrors -logfile ~/art/scripts/antLogs/storeJavaError.lo

