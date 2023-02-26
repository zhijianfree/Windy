package com.zj.feature.ability;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParameterDefine {
    private String paramKey;
    private int type;
    private String description;
    private DefaultValue defaultValue;

    @Data
    public static class  DefaultValue{

        public DefaultValue() {
        }

        private String defaultValue;
        private Object range;
    }
}
