#!/bin/bash

# 检查参数个数
if [ "$#" -ne 3 ]; then
  echo "脚本执行错误缺失执行参数，执行格式如下: "
  echo " sh docker_start.sh 数据库IP:数据库端口 数据库用户 数据库用户密码"
  exit 1
fi

# 获取传入的参数
DB_HOST_PORT=$1
DB_USERNAME=$2
DB_PASSWORD=$3


# 定义 Eureka 地址（固定为 windy-master 容器）
EUREKA_ZONE="http://windy-master:9888/eureka"

# 定义网络名称
NETWORK_NAME="windy-bridge"

# 获取当前主机的 IP 地址（适配 macOS 和 Linux）
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS: 使用 ifconfig 过滤出 IPv4 地址
  HOST_IP=$(ifconfig en0 | grep inet | awk '$1=="inet" {print $2}' | head -n 1)
else
  # Linux: 使用 hostname -I 获取 IPv4 地址
  HOST_IP=$(hostname -I | awk '{print $1}')
  # 确保排除 IPv6 地址
  HOST_IP=$(echo $HOST_IP | grep -oP '(\d+\.\d+\.\d+\.\d+)')
fi

# 检查是否获取到有效的 IP 地址
if [ -z "$HOST_IP" ]; then
  echo "无法获取主机的 IP 地址，请检查网络配置"
  exit 1
fi

# 创建自定义桥接网络（如果不存在）
docker network inspect $NETWORK_NAME > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "创建自定义网络: $NETWORK_NAME"
  docker network create --driver bridge $NETWORK_NAME
else
  echo "网络 $NETWORK_NAME 已存在，跳过创建"
fi

# 函数：删除并重新部署容器
redeploy_container() {
  CONTAINER_NAME=$1
  IMAGE_NAME=$2
  PORT_MAPPING=$3
  ENV_VARS=$4

  # 检查容器是否存在
  CONTAINER_EXISTS=$(docker ps -a --filter "name=$CONTAINER_NAME" --format "{{.Names}}")

  if [ "$CONTAINER_EXISTS" == "$CONTAINER_NAME" ]; then
    # 容器已存在，删除并重新创建
    echo "$CONTAINER_NAME 容器已存在，正在删除并重新部署..."
    docker rm -f $CONTAINER_NAME
  fi

  # 重新创建并启动容器
  echo "$CONTAINER_NAME 容器部署中..."
  docker run \
    $ENV_VARS \
    --name $CONTAINER_NAME \
    --network $NETWORK_NAME \
    -p $PORT_MAPPING \
    -d \
    $IMAGE_NAME
}

# 部署 windy-master 容器
redeploy_container "windy-master" "guyuelan/windy-master:latest" "9888:9888" \
  "--env=DB_HOST=$DB_HOST_PORT --env=DB_USERNAME=$DB_USERNAME --env=DB_PASSWORD=$DB_PASSWORD --env=EUREKA_ZONE=$EUREKA_ZONE"

# 部署 windy-console 容器
redeploy_container "windy-console" "guyuelan/windy-console:latest" "9768:9768" \
  "--env=DB_HOST=$DB_HOST_PORT --env=DB_USERNAME=$DB_USERNAME --env=DB_PASSWORD=$DB_PASSWORD --env=EUREKA_ZONE=$EUREKA_ZONE"

# 部署 windy-client 容器
redeploy_container "windy-client" "guyuelan/windy-client:latest" "8070:8070" \
  "--env=EUREKA_ZONE=$EUREKA_ZONE --privileged --env=TZ=Asia/Shanghai -v /var/run/docker.sock:/var/run/docker.sock"

# 检查是否安装 jq 工具，如果未安装则自动安装
if ! command -v jq &> /dev/null; then
  echo "jq 工具未安装，正在安装 jq..."

  if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS 安装 jq
    brew install jq
  elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux 安装 jq
    if command -v apt-get &> /dev/null; then
      sudo apt-get update && sudo apt-get install -y jq
    elif command -v yum &> /dev/null; then
      sudo yum install -y jq
    else
      echo "无法识别的 Linux 发行版，无法安装 jq"
      jq_install_status=1
    fi
  else
    echo "不支持的操作系统类型，无法安装 jq"
    jq_install_status=1
  fi

  # 如果安装 jq 失败，提示并继续执行
  if [ "$jq_install_status" -ne 0 ]; then
    echo "jq安装失败无法自动探测，请等待2分钟后打开下面地址访问Windy"
  fi
else
  jq_install_status=0
fi

# 函数：检查容器是否返回期望的数据
wait_for_service() {
  HOST=$1
  PORT=$2
  TIMEOUT=120
  INTERVAL=2
  ELAPSED_TIME=0

  echo "等待服务启动..."

  while true; do
    # 发送请求到容器的路径 /v1/devops/system/version 并获取响应
    RESPONSE=$(curl -s http://$HOST:$PORT/v1/devops/system/version)

    if [ $? -eq 0 ]; then
      # 使用 jq 提取 consoleVersion
      CONSOLE_VERSION=$(echo $RESPONSE | jq -r '.data.consoleVersion')
      if [ "$CONSOLE_VERSION" != "null" ]; then
        echo "服务启动成功，windy-console版本: $CONSOLE_VERSION"
        break
      fi
    fi

    if [ $ELAPSED_TIME -ge $TIMEOUT ]; then
      echo "在 $TIMEOUT 秒内未能探测成功，退出脚本"
      exit 1
    fi

    sleep $INTERVAL
    ELAPSED_TIME=$((ELAPSED_TIME + INTERVAL))
  done
}

# 如果 jq 安装成功，进行探测
if [ "$jq_install_status" -eq 0 ]; then
  # 最后检查服务是否已启动成功
  wait_for_service $HOST_IP 9768
fi

# 最后提示：Windy 安装完成，提供访问地址
echo "Windy 已安装完成，请访问: http://localhost:9768"
echo "或者访问当前主机的 IP 地址: http://$HOST_IP:9768"
