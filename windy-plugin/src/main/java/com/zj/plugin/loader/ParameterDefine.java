package com.zj.plugin.loader;


public class ParameterDefine {
    private String paramKey;
    private String type;
    private String description;
    private Object value;
    private DefaultValue defaultValue;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DefaultValue getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(DefaultValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public static class  DefaultValue{
        private String defaultValue;
        private Object range;

        public DefaultValue() {
        }

        public DefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Object getRange() {
            return range;
        }

        public void setRange(Object range) {
            this.range = range;
        }
    }
}
