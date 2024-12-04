package com.zj.common.entity.feature;

import com.zj.plugin.loader.ParameterDefine;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class ExecutorUnit {
    /**
     * 用例名称
     * */
    private String name;

    /**
     * 执行类名称
     * */
    @NotBlank
    private String service;

    @NotNull
    private Integer invokeType;

    private String description;

    /**
     * 执行方法
     * */
    @NotBlank
    private String method;

    /**
     * http类型时使用
     * */
    private Map<String, String> headers;

    /**
     * 方法参数
     * */
    private List<ParameterDefine> params;

    /**
     * 针对if、for循环的需要特殊处理
     * */
    private List<ExecutePointDto> executePoints;

    /**
     * 关联的模版Id
     */
    private String relatedId;

    /**
     * 关联的模版Id
     */
    private ExecutorUnit relatedTemplate;
}
