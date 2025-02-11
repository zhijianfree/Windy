package com.zj.demand.entity;

import com.zj.domain.entity.vo.Create;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class SpaceDto {

    /**
     * 空间名称
     */
    @Length(min = 4, max = 50)
    @NotBlank(groups = Create.class)
    private String spaceName;

    /**
     * 描述
     */
    @Length(max = 256)
    private String description;

    /**
     * 创建空间用户Id
     */
    private String userId;
}
