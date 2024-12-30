package com.zj.client.handler.pipeline.build;

import lombok.Data;

@Data
public class CodeBuildContext {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 构建的文件
     */
    private String buildFile;

    /**
     * 服务的目录
     */
    private String targetDir;

    /**
     * 构建工具的安装目录
     */
    private String buildPath;

    /**
     * 构建的版本
     */
    private String version;
}
