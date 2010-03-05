
# memeory aid 
mvn clean
mvn -P javadocjar install
mvn site:site
mvn javadoc:jar
mvn assembly:assembly