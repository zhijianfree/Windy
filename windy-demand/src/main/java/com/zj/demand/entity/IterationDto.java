package com.zj.demand.entity;

import com.zj.domain.entity.vo.Create;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class IterationDto {

    @Length(min = 10, max = 50)
    @NotBlank(groups = Create.class)
    private String name;

    /**
     * 迭代描述
     */
    @Length(max = 256)
    private String description;

    /**
     * 开始时间
     */
    @NotNull(groups = Create.class)
    private Long startTime;

    /**
     * 结束时间
     */
    @NotNull(groups = Create.class)
    private Long endTime;

    /**
     * 迭代状态
     */
    private Integer status;

    /**
     * 创建迭代用户
     */
    private String userId;

    /**
     * 空间ID
     */
    @NotBlank(groups = Create.class)
    private String spaceId;
}
