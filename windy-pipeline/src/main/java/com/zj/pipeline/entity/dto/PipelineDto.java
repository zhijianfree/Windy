package com.zj.pipeline.entity.dto;

import com.zj.common.entity.pipeline.PipelineConfig;
import com.zj.domain.entity.bo.pipeline.PipelineStageBO;
import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PipelineDto {

    /**
     * 流水线Id
     */
    @NotBlank(groups = {Update.class})
    private String pipelineId;

    /**
     * 流水线名称
     */
    @NotEmpty(message = "流水线名称不能为空", groups = {Create.class})
    private String pipelineName;

    /**
     * 服务Id
     */
    @NotEmpty(message = "服务Id不能为空", groups = {Create.class})
    private String serviceId;

    /**
     * 服务名
     */
    private String ServiceName;

    /**
     * 流水线类型
     */
    @Min(1)
    @Max(3)
    @NotNull(message = "流水线类型不能为空", groups = {Create.class})
    private Integer pipelineType;

    /**
     * 执行方式
     * */
    @Min(1)
    @Max(3)
    @NotNull(message = "执行方式不能为空", groups = {Create.class})
    private Integer executeType;

    /**
     * 流水线配置
     */
    private PipelineConfig pipelineConfig;

    /**
     * 流水线状态
     */
    private Integer pipelineStatus;

    @NotEmpty(groups = {Create.class})
    private List<PipelineStageBO> stageList;
}
