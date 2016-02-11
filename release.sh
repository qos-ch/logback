# memory aid 

mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${VERSION_NUMBER} 

mvn clean
mvn install
mvn animal-sniffer:check
mvn site:site

#mvn javadoc:jar
mvn assembly:assembly
mvn deploy -P javadocjar,sign-artifacts

#uncomment diffie-hellman support in /etc/ssh/sshd_config
mvn site:deploy -N

git tag -m "tagging" -a v_${VERSION_NUMBER}
git push --tags

release version and add next version on jira
