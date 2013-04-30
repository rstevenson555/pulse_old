#!/bin/bash
export JAVA_HOME=/opt/art/java/jdk1.6.0_29
export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$PATH
nohup ant start-prodArtEngine-omx -logfile ArtEngine-omx.log 1> temp.out 2> error.out &
#/etc/init.d/artEnine
sleep 2
#./art-omx.sh begin_command_mode
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.AccessRecordsDailyStats
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.AccessRecordsDailyPageStats
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.AccessRecordsMinuteStats
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.AccessRecordsHourlyStats
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.ExternalTimingMachineClassificationMinuteStats
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.LoadTestStats
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.AccumulatorDailyStats
#./art.sh loadstatunit=com.bos.art.logParser.statistics.OnlineReportingDailyStats
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.BrowserStats
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.JVMStatBroadcastStatisticsUnit
./art-omx.sh loadstatunit=com.bos.art.logParser.statistics.CriticalErrorBroadcastStatisticsUnit
./art-omx.sh STARTQUERYPARAMPROCESSOR 
./art-omx.sh STARTQUERYPARAMUNLOADER
./art-omx.sh STARTQUERYPARAMCLEANER
#./art-omx.sh end_command_mode
