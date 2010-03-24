
# memory aid 
mvn clean
mvn install
mvn site:site
#mvn javadoc:jar
mvn assembly:assembly
mvn deploy -P javadocjar
mvn site:deploy -N