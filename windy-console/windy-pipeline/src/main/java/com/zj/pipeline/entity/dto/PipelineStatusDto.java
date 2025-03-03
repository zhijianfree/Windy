package com.zj.pipeline.entity.dto;

import lombok.Data;

@Data
public class PipelineStatusDto {

    /**
     * 流水线ID
     */
    private String pipelineId;

    /**
     * 流水线名称
     */
    private String pipelineName;

    /**
     * 流水线类型
     */
    private Integer pipelineType;

    /**
     * 流水线状态
     */
    private Integer status;
}
