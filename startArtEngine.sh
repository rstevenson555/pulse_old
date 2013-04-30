#!/bin/bash
export JAVA_HOME=/opt/art/java/jdk1.6.0_29
export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$PATH
nohup ant start-prodArtEngine-sol -logfile ArtEngine.log 1> temp.out 2> error.out &
#/etc/init.d/artEnine
sleep 2
#./art.sh begin_command_mode
./art.sh loadstatunit=com.bos.art.logParser.statistics.AccessRecordsDailyStats
./art.sh loadstatunit=com.bos.art.logParser.statistics.AccessRecordsDailyPageStats
./art.sh loadstatunit=com.bos.art.logParser.statistics.AccessRecordsMinuteStats
./art.sh loadstatunit=com.bos.art.logParser.statistics.AccessRecordsHourlyStats
./art.sh loadstatunit=com.bos.art.logParser.statistics.ExternalTimingMachineClassificationMinuteStats
./art.sh loadstatunit=com.bos.art.logParser.statistics.LoadTestStats
./art.sh loadstatunit=com.bos.art.logParser.statistics.AccumulatorDailyStats
#./art.sh loadstatunit=com.bos.art.logParser.statistics.OnlineReportingDailyStats
./art.sh loadstatunit=com.bos.art.logParser.statistics.BrowserStats
./art.sh loadstatunit=com.bos.art.logParser.statistics.JVMStatBroadcastStatisticsUnit
./art.sh loadstatunit=com.bos.art.logParser.statistics.CriticalErrorBroadcastStatisticsUnit
./art.sh STARTQUERYPARAMPROCESSOR 
./art.sh STARTQUERYPARAMUNLOADER
./art.sh STARTQUERYPARAMCLEANER
#./art.sh end_command_mode
