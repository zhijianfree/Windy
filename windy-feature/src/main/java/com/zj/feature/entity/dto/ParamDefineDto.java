package com.zj.feature.entity.dto;

import com.zj.feature.entity.vo.ParameterDefine;
import lombok.Data;

@Data
public class ParamDefineDto {
    private Object value;
    private String paramKey;
    private String description;
    private int type;
    private ParameterDefine.DefaultValue defaultValue;
}
