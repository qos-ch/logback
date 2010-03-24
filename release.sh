
# memeory aid 
mvn clean
mvn iinstall
mvn site:site
#mvn javadoc:jar
mvn assembly:assembly
mvn deploy -P javadocjar
mvn site:deploy -N