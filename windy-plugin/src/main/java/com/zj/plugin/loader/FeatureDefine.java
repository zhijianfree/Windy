package com.zj.plugin.loader;

import java.util.List;

public class FeatureDefine {
    /**
     * 模版执行的方法
     */
    private String method;

    /**
     * 模版名称
     */
    private String name;

    /**
     * 模版的完整类名，包括累的包路径
     */
    private String source;

    /**
     * 模版描述
     */
    private String description;

    /**
     * 模版参数列表
     */
    private List<ParameterDefine> params;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ParameterDefine> getParams() {
        return params;
    }

    public void setParams(List<ParameterDefine> params) {
        this.params = params;
    }
}
