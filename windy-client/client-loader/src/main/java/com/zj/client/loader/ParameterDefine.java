package com.zj.client.loader;


public class ParameterDefine {
    private String paramKey;
    private int type;
    private String description;
    private String value;
    private DefaultValue defaultValue;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
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
