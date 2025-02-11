package com.zj.feature.entity;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class RelatedTemplateDto {

    /**
     * 服务ID
     */
    @NotBlank
    private String serviceId;

    /**
     * 关联的服务ID
     */
    @NotEmpty
    private List<@Valid @NotBlank String> relatedServiceIds;
}
