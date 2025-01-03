package com.zj.pipeline.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ApprovalInfo {

    @NotBlank
    private String historyId;

    @NotBlank
    private String nodeId;

    @NotNull
    private Integer type;

    private String message;
}
