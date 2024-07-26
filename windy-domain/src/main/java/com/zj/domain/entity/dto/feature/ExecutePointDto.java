package com.zj.domain.entity.dto.feature;

import lombok.Data;

@Data
public class ExecutePointDto {

    private Long id;

    /**
     * 执行点Id
     * */
    private String pointId;

    /**
     *
     *
     * */
    private Integer executeType;

    /**
     * 用例Id
     * */
    private String featureId;

    private String templateId;

    /**
     * 执行点信息(包含参数信息)
     * */
    private String featureInfo;

    /**
     * 包含比较信息
     * */
    private String compareDefine;

    /**
     * 包含变量声明
     * */
    private String variables;

    /**
     * 执行点功能描述，自定义
     * */
    private String description;

    /**
     * 测试阶段
     * */
    private Integer testStage;

    /**
     * 执行顺序
     * */
    private Integer sortOrder;

    private Long createTime;
    private Long updateTime;
}
