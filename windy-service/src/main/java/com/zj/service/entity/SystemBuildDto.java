package com.zj.service.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class SystemBuildDto {

    /**
     * 工具版本名称
     */
    @NotBlank
    @Length(max = 50)
    private String name;

    /**
     * 构建工具类型
     */
    @NotBlank
    private String type;

    /**
     * 安装路径
     */
    @NotBlank
    @Length(max = 100)
    private String installPath;

    /**
     * 描述
     */
    @Length(max = 256)
    private String description;
}
