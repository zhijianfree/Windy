package com.zj.client.feature.executor.vo;

import com.zj.client.entity.dto.ExecutePointDTO;
import com.zj.client.entity.dto.ParamDefine;
import java.util.List;
import lombok.Data;

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

    /**
     * 执行方法
     * */
    private String method;

    /**
     * 方法参数
     * */
    private List<ParamDefine> params;

    /**
     * 针对for循环的需要特殊处理
     * */
    private List<ExecutePointDTO> executePoints;
}
