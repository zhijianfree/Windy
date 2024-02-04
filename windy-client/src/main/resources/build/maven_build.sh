#!/bin/bash
# 二方包发布脚本

#---------环境变量-----------------------------
# 脚本目录
SCRIPT=`pwd`
# 基础目录
BASE_HOME=`pwd`
# 工作目录
WORK_HOME=${BASE_HOME}/workspace
# 编译工作目录
CODE_PATH=${WORK_HOME}/compile
# 应用名称
APP_NAME=$1
# git仓库
GIT_URL=$2
# 分支名称
BRANCH=$3
# 打包版本
PACKAGE_VERSION=$4
# 发布的模块名称
MODULE_NAME=$5

#---------环境变量-----------------------------

#--------- 打印环境变量 ------------------------
# 开始时间
date '+%Y-%m-%d %H:%M:%S'
echo "===================环境变量============================="
echo "应用名: "  ${APP_NAME}
echo "代码仓库: "  ${GIT_URL}
echo "分支名: "  ${BRANCH}
echo "版本号: "  ${PACKAGE_VERSION}
echo "模块名称: "  ${MODULE_NAME}
echo "======================================================="