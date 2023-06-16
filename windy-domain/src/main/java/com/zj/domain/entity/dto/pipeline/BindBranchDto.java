package com.zj.domain.entity.dto.pipeline;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author guyuelan
 * @since 2021/10/15
 */

@Data
public class BindBranchDto {

    /**
     * 绑定Id
     */
    private String bindId;

    /**
     * 绑定分支
     */
    @NotEmpty
    private String gitBranch;

    /**
     * git地址
     */
    @NotEmpty
    private String gitUrl;

    /**
     * 绑定类型： 0 未选中  1 选中
     */
    @NotNull
    private Boolean isChoose;

    /**
     * 流水线Id
     */
    @NotEmpty
    private String pipelineId;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 更新时间
     * */
    private Long updateTime;
}
