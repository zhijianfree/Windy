package com.zj.common.entity.pipeline;

import com.zj.common.enums.ToolType;
import com.zj.common.enums.DeployType;
import lombok.Data;

@Data
public class ServiceContext {

    /**
     * 服务部署方式: {@link DeployType}
     */
    private Integer deployType;

    /**
     * 代码开发语言:{@link ToolType}
     */
    private String code;

    /**
     * 开发语言的构建版本
     */
    private String buildVersion;
}
