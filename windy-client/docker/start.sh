#!/bin/bash

service="Windy"
JAR_NAME="*.jar"

SYSTEM_CACHE_SIZE=$(awk '($1 == "MemTotal:"){print $2}' /proc/meminfo)
echo "$SYSTEM_CACHE_SIZE"

JVM_CACHE_SIZE=256
NEW_CACHE_SIZE=128

if [ $SYSTEM_CACHE_SIZE -gt 524288 ] ; then
  JVM_CACHE_SIZE=512
  NEW_CACHE_SIZE=256
fi

if [  $SYSTEM_CACHE_SIZE -gt 1048576 ] ; then
  JVM_CACHE_SIZE=1024
  NEW_CACHE_SIZE=512
fi

if [  $SYSTEM_CACHE_SIZE -gt 2097152 ] ; then
  JVM_CACHE_SIZE=2048
  NEW_CACHE_SIZE=1024
fi

if [ $SYSTEM_CACHE_SIZE -gt 3151872 ] ; then
  JVM_CACHE_SIZE=3078
  NEW_CACHE_SIZE=1536
fi

JVM_OPTION="-Xms${JVM_CACHE_SIZE}m -Xmx${JVM_CACHE_SIZE}m -XX:MaxNewSize=${NEW_CACHE_SIZE}m -XX:SurvivorRatio=6"

echo "start service $service"
echo "$JVM_OPTION"

nohup java -jar $JVM_OPTION $JAR_NAME

sleep 10

echo "服务结束"

# docker run -d -it --name devops --link gyl-mysql:gyl-mysql --link sso-redis:sso-redis -p 9768:9768 devops:v1