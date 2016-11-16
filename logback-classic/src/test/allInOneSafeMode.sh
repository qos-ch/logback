# Use this script both generate and check the results.  It only works
# if there is only one instance of this script

if [ $# -lt 3 ]
then 
  echo "Usage: allInOneSafeMode.sh filename runLen stamp0 ... stampN"
  exit 1;
fi

echo "File name $1"
echo "run length $2"




#On windows
#CLASSPATH="${CLASSPATH}\;./target/classes/"
#CLASSPATH="${CLASSPATH}\;./target/test-classes/"
#CLASSPATH="${CLASSPATH}\;../logback-core/target/classes"
#LASSPATH="${CLASSPATH}\;../logback-examples/lib/slf4j-api-1.5.10.jar"
echo $CLASSPATH

# On Unix
#CLASSPATH="${CLASSPATH}:./target/classes/"
#CLASSPATH="${CLASSPATH}:./target/test-classes/"
#CLASSPATH="${CLASSPATH}:../logback-core/target/classes"
#CLASSPATH="${CLASSPATH}:../logback-examples/lib/slf4j-api-1.5.10.jar"

if [ -f $1 ]
then
 echo Removing $1 before tests
 rm $1;
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

echo Checking results...
java ch.qos.logback.classic.multiJVM.Checker $LEN $FILENAME $*
