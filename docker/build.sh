#!/bin/bash 

# Apache Maven Environment Variables
# MAVEN_HOME for Maven 1 - M2_HOME for Maven 2
export M2_HOME=/usr/local/apache-maven
export PATH=${M2_HOME}/bin:${PATH}

cd /pig
mvn --settings /home/jenkins/.m2/settings.xml -f amobee-pom.xml clean deploy
mvn --settings /home/jenkins/.m2/settings.xml -f amobee-pom.xml clean 

