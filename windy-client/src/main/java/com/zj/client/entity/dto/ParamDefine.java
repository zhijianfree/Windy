package com.zj.client.entity.dto;

import lombok.Data;

@Data
public class ParamDefine {
    private Object value;
    private String paramKey;
    private String description;
    private int type;
    private ParameterDefine.DefaultValue defaultValue;
}
