# memory aid 

mvn versions:set -DnewVersion=${VERSION_NUMBER} -DgenerateBackupPoms=false

mvn clean
mvn install
mvn site:site
#mvn javadoc:jar
mvn assembly:assembly
mvn deploy -P javadocjar,sign-artifacts
mvn site:deploy -N

git tag -a v_${VERSION_NUMBER}
git push --tags
git push github --tags
