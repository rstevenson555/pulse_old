#export ANT_HOME=/opt/ant/apache-ant-1.5.4
#export JAVA_HOME=/opt/java/jdk15
export JAVA_HOME=/opt/art/java/jdk1.6.0_29
export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$PATH
export PATH=$ANT_HOME/bin:$JAVA_HOME/bin:$PATH

cd /opt/art/logParser/Scripts
jruby AccessRecordsCleaner-sol.rb
jruby AccessRecordsCleaner-omx.rb
#ant run-AccessRecordsCleaner-sol
#ant run-AccessRecordsCleaner-omx

cd /opt/art/logParser
ant run-AccumulatorRecordsCleaner-sol
ant run-ExternalAccessRecordsCleaner-sol
ant run-StackTraceCleaners-sol

ant run-AccumulatorRecordsCleaner-omx
ant run-ExternalAccessRecordsCleaner-omx
ant run-StackTraceCleaners-omx


