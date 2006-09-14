
VERSION=0.4-SNAPSHOT
echo $VERSION

MVN=/java/maven-2.0.4/bin/mvn

BUNDLES_DIR=pixie:/var/www/logback.qos.ch/htdocs/dist/bundles/

update() {
 MODULE=$1
 pushd $MODULE
 $MVN repository:bundle-create 
 echo Maven exited with $?
 if [ $? != 0 ]
 then
   echo mvn command failed
   exit 1;
 fi
 scp target/$MODULE-$VERSION-bundle.jar $BUNDLES_DIR
 popd
}

update logback-core
update logback-classic
update logback-access

