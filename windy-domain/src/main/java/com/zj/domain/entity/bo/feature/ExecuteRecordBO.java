package com.zj.domain.entity.bo.feature;

import com.zj.common.entity.feature.FeatureResponse;
import lombok.Data;

import java.util.List;

@Data
public class ExecuteRecordBO {

    /**
     * 执行Id
     * */
    private String executeRecordId;

    /**
     * 执行点名称
     * */
    private String executePointName;

    /**
     * 执行点Id
     * */
    private String executePointId;

    /**
     * 执行类型
     * */
    private Integer executeType;

    /**
     * 测试阶段
     * */
    private Integer testStage;

    /**
     * 历史Id
     */
    private String historyId;

    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 执行结果
     */
    private List<FeatureResponse> recordResult;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}
