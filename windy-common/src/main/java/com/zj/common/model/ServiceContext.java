package com.zj.common.model;

import com.zj.common.enums.CodeType;
import com.zj.common.enums.DeployType;
import lombok.Data;

@Data
public class ServiceContext {

    /**
     * 服务部署方式: {@link DeployType}
     */
    private Integer deployType;

    /**
     * 代码开发语言:{@link CodeType}
     */
    private String code;
}
