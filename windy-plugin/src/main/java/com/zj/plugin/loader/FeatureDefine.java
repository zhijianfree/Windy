package com.zj.plugin.loader;

import java.util.List;

public class FeatureDefine {
    private String method;
    private String name;
    private String source;
    private String description;
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
