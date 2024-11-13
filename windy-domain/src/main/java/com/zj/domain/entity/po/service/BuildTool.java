package com.zj.domain.entity.po.service;

import lombok.Data;

@Data
public class BuildTool {

    private Long id;

    /**
     * 工具ID
     */
    private String toolId;

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
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
