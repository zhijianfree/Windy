#!/bin/bash

# 设置项目名称和编译输出文件夹
PROJECT_NAME="$1"
OUTPUT_DIR="$MAIN_DIR_GO_PATH/docker"
VERSION=$2
MAIN_DIR_GO_PATH="$3"
GO_PATH=$4
ROOT_PATH=$5

echo "服务名称=$PROJECT_NAME 执行文件输出目录=$OUTPUT_DIR 构建版本=$VERSION main包目录=$MAIN_DIR_GO_PATH go运行目录=$GO_PATH"

# 下载依赖项
echo "开始下载依赖..."
"$GO_PATH/bin/go" mod tidy "-modfile=$ROOT_PATH/go.mod"

# 构建函数
os="linux"
arch="amd64"
output_name="${PROJECT_NAME}-${VERSION}"

echo "开始构建项目的执行文件 ${output_name}..."

# 设置环境变量以匹配目标平台
GOOS="$os" GOARCH="$arch" "$GO_PATH/bin/go" build -o "$OUTPUT_DIR/$output_name" -ldflags "-X main.version=$VERSION" "$MAIN_DIR_GO_PATH"

# 检查构建是否成功
if [ $? -ne 0 ]; then
  echo "构建执行文件失败 ${output_name}-${os}/${arch}. 退出构建."
  exit 1
fi

echo "go构建完成. 执行文件目录: $OUTPUT_DIR"