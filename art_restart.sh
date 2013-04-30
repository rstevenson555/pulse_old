#!/bin/sh
######################################################################################
#    restart ART
#    (restarting reallocates the ArtCollector.log file which tends to get very big.
######################################################################################
export ANT_HOME=/opt/ant/apache-ant-1.5.4
export JAVA_HOME=/opt/java/jdk14
export PATH=$ANT_HOME/bin:$JAVA_HOME/bin:$PATH
cd /opt/art/logParser
sudo -u artadmin ./removeProcess.sh
sudo -u artadmin ./startArtEngine.sh
sudo -u artadmin ./startArtCollector.sh

