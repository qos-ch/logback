mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${VERSION_NUMBER} 

mvn clean
mvn install
mvn animal-sniffer:check
mvn site:site

#mvn javadoc:jar
#mvn assembly:single

export GPG_TTY=$(tty)
password
mvn deploy -P javadocjar,sign-artifacts -Dgpg.passphrase=passwd



# cleanHistory dep
#uncomment diffie-hellman support in /etc/ssh/sshd_config

mvn site:deploy -N # with Java 8!!!

git tag -m "tagging" -a v_${VERSION_NUMBER}
git push --tags

release version and add next version on jira
