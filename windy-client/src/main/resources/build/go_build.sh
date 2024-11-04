#!/bin/bash

# 设置项目名称和编译输出文件夹
PROJECT_NAME="my_go_project"
OUTPUT_DIR="./docker"
VERSION="v1.0.0"

# 支持的平台和架构列表，可以添加更多
PLATFORMS=("linux/amd64")

# 下载依赖项
echo "Downloading dependencies..."
go mod tidy

# 构建函数
build() {
  local os="$1"
  local arch="$2"
  local output_name="${PROJECT_NAME}-${os}-${arch}"

  echo "Building for ${os}/${arch}..."

  # 设置环境变量以匹配目标平台
  GOOS="$os" GOARCH="$arch" go build -o "$OUTPUT_DIR/$output_name" -ldflags "-X main.version=$VERSION" main.go

  # 检查构建是否成功
  if [ $? -ne 0 ]; then
    echo "Failed to build for ${os}/${arch}. Exiting."
    exit 1
  fi
}

# 针对每个平台进行构建
build "$os" "$arch"

echo "Build complete. Executables are located in the $OUTPUT_DIR directory."