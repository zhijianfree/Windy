package com.zj.domain.entity.po.feature;

import lombok.Data;

@Data
public class FeatureInfo {
    private Long id;

    /**
     * 用例关联的测试集Id
     */
    private String testCaseId;

    /**
     * 用例Id
     */
    private String featureId;

    /**
     * 用例名称
     */
    private String featureName;

    /**
     * 父节点Id
     */
    private String parentId;

    /**
     * 用例类型用来识别是目录还是用例
     */
    private Integer featureType;

    /**
     * 用例状态
     */
    private Integer status;

    /**
     * 用例的测试步骤
     */
    private String testStep;

    /**
     * 排序
     */
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
