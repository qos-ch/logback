#!/bin/sh

##
# This script will add logback jars to your classpath.
##

CLASSPATH="${CLASSPATH};${PWD}/logback-classic-${project.version}.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-core-${project.version}.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-examples/logback-examples-${project.version}.jar"
CLASSPATH="${CLASSPATH};${PWD}/logback-examples/lib/slf4j-api-${slf4j.version}.jar"

export CLASSPATH

echo $CLASSPATH
