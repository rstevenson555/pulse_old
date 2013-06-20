#!/bin/bash
export JAVA_HOME=/opt/art/java/jdk1.6.0_29
export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$PATH

nohup ant start-collector-sol -Dparam=-server -Dparam2=-encode_input -logfile ./ArtCollectorStdout.log &
