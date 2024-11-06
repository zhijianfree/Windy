#!/bin/bash

# 设置项目名称和编译输出文件夹
PROJECT_NAME="$1"
VERSION=$2
MAIN_DIR_GO_PATH="$3"
GO_PATH=$4
ROOT_PATH=$5
OUTPUT_DIR="$MAIN_DIR_GO_PATH/docker"

echo "服务名称=$PROJECT_NAME"
echo "执行文件输出目录=$OUTPUT_DIR"
echo "构建版本=$VERSION"
echo "main包目录=$MAIN_DIR_GO_PATH"
echo "go运行目录=$GO_PATH"
echo "项目目录=$ROOT_PATH"

# 下载依赖项
echo "开始下载依赖..."
cd "$ROOT_PATH" || { echo "无法进入目录 $ROOT_PATH. 退出构建."; exit 2; }

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