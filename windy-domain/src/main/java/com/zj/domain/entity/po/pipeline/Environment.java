package com.zj.domain.entity.po.pipeline;

import lombok.Data;

@Data
public class Environment {
    /**
     * 环境名称
     * */
    private String envName;

    /**
     * 环境地址
     * */
    private String envHost;

    /**
     * 环境端口
     * */
    private String envPort;

    /**
     * 环境状态
     * */
    private String envStatus;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     * */
    private Long updateTime;
}
