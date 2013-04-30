#!/bin/bash
ps -auxwwwww |grep com.bos.art.logParser.server.Engine | grep -v grep | awk {'print "kill -9 "$2 '} > exfile1.sh 
ps -auxwwwww |grep "start-prodArtEngine" | grep -v grep | awk {'print "kill -9 "$2 '} >> exfile1.sh 
ps -auxwwwww |grep "start-prodArtCollector" | grep -v grep | awk {'print "kill -9 "$2 '} >> exfile1.sh 
ps -auxwwwww |grep com.bos.art.logServer | grep -v grep | awk {'print "kill -9 "$2 '} >> exfile1.sh 
cat exfile1.sh
source exfile1.sh
rm -f esfile1.sh

