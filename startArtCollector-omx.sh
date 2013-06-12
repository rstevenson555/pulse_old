#!/bin/bash
export JAVA_HOME=/opt/art/java/jdk1.6.0_29
export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$PATH

nohup ant start-collector-omx -Dparam=-server -Dparam2=-encode_input -logfile ./omx-ArtCollectorStdout.log &
