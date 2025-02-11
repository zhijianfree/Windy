package com.zj.service.entity;

import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class SystemBuildDto {

    /**
     * 工具ID
     */
    @NotBlank(groups = Update.class, message = "构建工具ID不能为空")
    private String toolId;

    /**
     * 工具版本名称
     */
    @Length(max = 50)
    @NotBlank(groups = Create.class)
    private String name;

    /**
     * 构建工具类型
     */
    @NotBlank(groups = Create.class)
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

    /**
     * 构建配置
     */
    private String buildConfig;
}
