package com.zj.plugin.loader;


public class ParameterDefine {
    /**
     * 参数名称
     */
    private String paramKey;

    /**
     * 参数的数据类型 {@link ParamValueType}
     */
    private String type;

    /**
     * 参数描述
     */
    private String description;

    /**
     * 参数值
     */
    private Object value;

    /**
     * 参数位置，只有在调用类型为HTTP时使用
     */
    private String position;

    /**
     * 参数默认值
     */
    private DefaultValue defaultValue;

    public Object getValue() {
        return value;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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
