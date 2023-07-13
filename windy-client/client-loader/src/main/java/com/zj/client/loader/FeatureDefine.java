package com.zj.client.loader;

import java.util.List;
import lombok.Data;

@Data
public class FeatureDefine {
    private String method;
    private String name;
    private String source;
    private String description;
    private List<ParameterDefine> params;
}
