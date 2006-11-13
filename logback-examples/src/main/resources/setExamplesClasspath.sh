#!/bin/sh

##
# This script will add logback jars to your classpath.
##

CLASSPATH="$CLASSPATH;${PWD}/logback-access-0.6-SNAPSHOT.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-classic-0.6-SNAPSHOT.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-classic-0.6-SNAPSHOT-tests.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-core-0.6-SNAPSHOT.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-core-0.6-SNAPSHOT-tests.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-examples/logback-examples-0.6-SNAPSHOT.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-examples/lib/slf4j-api-1.1.0-beta0.jar"

export CLASSPATH

echo $CLASSPATH
