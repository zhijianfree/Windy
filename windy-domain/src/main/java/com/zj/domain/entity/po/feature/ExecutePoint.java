package com.zj.domain.entity.po.feature;

import lombok.Data;

@Data
public class ExecutePoint {
    private Long id;
    /**
     * 执行点Id
     * */
    private String pointId;

    /**
     * 执行类型
     * */
    private Integer executeType;

    /**
     * 用例Id
     * */
    private String featureId;

    /**
     * 执行点信息(包含参数信息)
     * */
    private String featureInfo;

    private String templateId;

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

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
