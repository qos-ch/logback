echo "File name $1"
echo "run length $2"



#On windows
#CLASSPATH="${CLASSPATH};./target/classes/"
#CLASSPATH="${CLASSPATH};./target/test-classes/"
#CLASSPATH="${CLASSPATH};../logback-core/target/classes"
#CLASSPATH="${CLASSPATH};../logback-examples/lib/slf4j-api-1.5.5.jar"

# On Unix
#CLASSPATH="${CLASSPATH}:./target/classes/"
#CLASSPATH="${CLASSPATH}:./target/test-classes/"
#CLASSPATH="${CLASSPATH}:../logback-core/target/classes"
#CLASSPATH="${CLASSPATH}:../logback-examples/lib/slf4j-api-1.5.5.jar"


if [ $# -lt 3 ]
then 
  echo "Usage: generateLogsInSafeMode.sh filename runLen stamp0 ... stampN"
  exit 1;
fi

FILENAME=$1
LEN=$2

shift 2

for stamp in $@
do 
  echo running safe mode with $stamp
  java ch.qos.logback.classic.multiJVM.SafeModeFileAppender $stamp $LEN $FILENAME &
done

wait

echo "To test the results issue the following command"
echo "./checkResults.sh $FILENAME $LEN $*"

