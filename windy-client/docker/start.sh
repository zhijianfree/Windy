#!/bin/bash

service="Windy-client"
echo "start service $service"

nohup java -jar -Dwindy.pipeline.maven.path='/opt/windy-client/maven' *.jar

sleep 10

echo "服务结束"

# docker run -d -it --name devops --link gyl-mysql:gyl-mysql --link sso-redis:sso-redis -p 9768:9768 devops:v1