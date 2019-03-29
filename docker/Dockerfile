FROM centos:6

# Build environment for hadoop-common 
# need java7, c++, mvn and protoc 2.5.0

USER root 
RUN yum -y install which java-1.7.0-openjdk-devel ant ant-nodeps wget && yum clean all


RUN cd /usr/local && \
    wget http://www-us.apache.org/dist/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.tar.gz && \
    tar -xf apache-maven-3.1.1-bin.tar.gz && rm -f apache-maven-3.1.1-bin.tar.gz && \
    mv apache-maven-3.1.1/ apache-maven/ 

RUN groupadd --gid 516 jenkins && useradd -m -d /home/jenkins -g jenkins --uid 516 jenkins

ADD build.sh /home/jenkins/
RUN chown jenkins:jenkins /home/jenkins/build.sh && chmod u+x /home/jenkins/build.sh 

CMD ["tail", "-f", "/dev/null"]

