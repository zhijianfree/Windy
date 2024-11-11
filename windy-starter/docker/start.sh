#!/bin/bash

service="Windy-console"

SYSTEM_CACHE_SIZE=$(awk '($1 == "MemTotal:"){print $2}' /proc/meminfo)
echo "$SYSTEM_CACHE_SIZE"

echo "start service $service"

nohup java -jar *.jar

sleep 10

echo "服务结束"

# docker run -d -it --name devops --link gyl-mysql:gyl-mysql --link sso-redis:sso-redis -p 9768:9768 devops:v1