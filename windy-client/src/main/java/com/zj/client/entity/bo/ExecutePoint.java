package com.zj.client.entity.bo;

import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.entity.feature.VariableDefine;
import lombok.Data;

import java.util.List;

@Data
public class ExecutePoint {

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
    private ExecutorUnit executorUnit;
    /**
     * 包含比较信息
     * */
    private List<CompareDefine> compareDefines;

    /**
     * 包含变量声明
     * */
    private List<VariableDefine> variableDefines;

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
