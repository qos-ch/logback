#!/bin/sh

##
# This script will add logback jars to your classpath.
# It will preserve the initial classpath.
##

oldClassPath=$CLASSPATH
#echo $oldClassPath

NEW_CLASSPATH=${PWD}/logback-access-0.6-SNAPSHOT.jar
NEW_CLASSPATH=${NEW_CLASSPATH}:${PWD}/logback-classic-0.6-SNAPSHOT.jar
NEW_CLASSPATH=${NEW_CLASSPATH}:${PWD}/logback-classic-0.6-SNAPSHOT-tests.jar
NEW_CLASSPATH=${NEW_CLASSPATH}:${PWD}/logback-core-0.6-SNAPSHOT.jar
NEW_CLASSPATH=${NEW_CLASSPATH}:${PWD}/logback-core-0.6-SNAPSHOT-tests.jar
NEW_CLASSPATH=${NEW_CLASSPATH}:${PWD}/logback-examples/logback-examples-0.6-SNAPSHOT.jar

#echo $NEW_CLASSPATH

if [ "$oldClassPath" != "" ]; then
    export CLASSPATH=${NEW_CLASSPATH}:${oldClassPath}
else
   	export CLASSPATH=${NEW_CLASSPATH}
fi