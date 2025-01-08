package com.zj.plugin.loader;

public class InitData {
    /**
     * 默认值
     */
    private String data;

    /**
     * 默认值范围
     */
    private Object range;

    /**
     * 范围值的类型，如果默认值为Array时使用
     */
    private String rangeType;
    public InitData() {
    }

    public InitData(String data) {
        this.data = data;
    }

    public String getRangeType() {
        return rangeType;
    }

    public void setRangeType(String rangeType) {
        this.rangeType = rangeType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Object getRange() {
        return range;
    }

    public void setRange(Object range) {
        this.range = range;
    }
}
