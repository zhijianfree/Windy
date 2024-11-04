#!/bin/bash

# 设置项目名称和编译输出文件夹
PROJECT_NAME="$1"
OUTPUT_DIR="./docker"
VERSION=$2
TARGET_FILE="$3"

# 下载依赖项
echo "Downloading dependencies..."
go mod tidy

# 构建函数
os="linux"
arch="amd64"
output_name="${PROJECT_NAME}-${os}/${arch}"

echo "Building for ${os}/${arch}..."

# 设置环境变量以匹配目标平台
GOOS="$os" GOARCH="$arch" go build -o "$OUTPUT_DIR/$output_name" -ldflags "-X main.version=$VERSION" "$TARGET_FILE"

# 检查构建是否成功
if [ $? -ne 0 ]; then
  echo "Failed to build for ${output_name}-${os}/${arch}. Exiting."
  exit 1
fi

echo "Build complete. Executables are located in the $OUTPUT_DIR directory."