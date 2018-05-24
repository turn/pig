#!/bin/bash 

docker rm -f pig
docker build -t pig docker/
docker run -v ${HOME}/.m2:/home/jenkins/.m2 -v $(pwd):/pig --name pig pig /home/jenkins/build.sh 
docker rm -f pig
