package com.zj.client.entity.po;

import java.io.Serializable;
import lombok.Data;

@Data
public class ExecutePoint implements Serializable {
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
