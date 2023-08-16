package com.zj.client.handler.feature.executor.vo;

import com.zj.client.entity.dto.ExecutePointDTO;
import com.zj.plugin.loader.ParameterDefine;
import lombok.Data;

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
    private String service;

    private Integer invokeType;

    /**
     * 执行方法
     * */
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
     * 针对for循环的需要特殊处理
     * */
    private List<ExecutePointDTO> executePoints;
}
