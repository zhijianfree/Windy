package com.zj.common.entity.service;

import lombok.Data;

@Data
public class ToolVersionDto {

    /**
     * 工具版本名称
     */
    private String name;

    /**
     * 构建工具类型
     */
    private String type;

    /**
     * 安装路径
     */
    private String installPath;

    /**
     * 构建配置
     */
    private String buildConfig;
}
