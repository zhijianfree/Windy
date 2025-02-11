package com.zj.service.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ServiceMemberDto {

    /**
     * 服务ID
     */
    @NotBlank(message = "服务ID不能为空")
    private String serviceId;

    /**
     * 关联的用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
}
