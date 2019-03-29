#!/bin/bash 

# Apache Maven Environment Variables
# MAVEN_HOME for Maven 1 - M2_HOME for Maven 2
export M2_HOME=/usr/local/apache-maven
export PATH=${M2_HOME}/bin:${PATH}
REPO_MVN="http://repo2.maven.org/maven2"
REPO_ROOT="https://repository.cloudera.com/artifactory/cloudera-repos/"
MVN_CMD="mvn --settings /home/jenkins/.m2/settings.xml -Dmvnrepo=\"$REPO_MVN\" -Drepository.root=\"$REPO-ROOT\" -f amobee-pom.xml"
echo "172.19.112.132 maven.turn.com" >> /etc/hosts

cd /pig
$MVN_CMD clean deploy 
$MVN_CMD clean 

