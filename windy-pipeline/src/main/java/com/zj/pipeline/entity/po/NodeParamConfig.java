package com.zj.pipeline.entity.po;

import lombok.Data;

@Data
public class NodeParamConfig {

    private Long id;

    /**
     * 配置Id
     * */
    private String configId;

    /**
     * 配置名称
     * */
    private String configName;

    /**
     * 配置类型
     * */
    private String type;

    /**
     * 配置信息
     * */
    private String configDetail;

    /**
     * 排序
     * */
    private Integer sort;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 修改时间
     * */
    private Long updateTime;
}
