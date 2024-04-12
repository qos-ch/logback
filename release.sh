mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${VERSION_NUMBER} 

# mvn  -Dmaven.javadoc.skippedModules=logback-core-blackbox,logback-classic-blackbox,logback-examples javadoc:aggregate

# mvn -Ddoclint=none -Dmaven.javadoc.skippedModules=slf4j-ext,log4j-over-slf4j,log4j-over-slf4j-blackbox,jul-to-slf4j-blackbox,slf4j-migrator,osgi-over-slf4j javadoc:aggregate

#mvn -Ddoclint=none  -DXXadditionalparam=-Xdoclint:none -Dmaven.javadoc.skippedModules=osgi-over-slf4j,slf4j-ext,log4j-over-slf4j-blackbox,log4j-over-slf4j javadoc:aggregate





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
