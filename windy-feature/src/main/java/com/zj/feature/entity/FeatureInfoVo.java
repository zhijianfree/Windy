package com.zj.feature.entity;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class FeatureInfoVo {

    /**
     * 用例Id
     */
    private String featureId;

    /**
     * 用例名称
     */
    @NotBlank(message = "用例名称不能为空")
    private String featureName;

    /**
     * 父节点Id
     */
    private String parentId;

    /**
     * 用例类型
     */
    @NotNull(message = "用例类型不能为空")
    private Integer featureType;

    /**
     * 用例标签
     */
    private List<String> tags;

    /**
     * 用例的测试步骤
     */
    private String testStep;

    /**
     * 测试集合Id
     */
    @NotBlank
    private String testCaseId;

    /**
     * 执行点列表
     */
    private List<@Valid ExecutePointVo> testFeatures;

    /**
     * 排序
     */
    private Integer sortOrder;

    private Long createTime;

    private Long updateTime;

    /**
     * 用例状态
     */
    private Integer status;

}
