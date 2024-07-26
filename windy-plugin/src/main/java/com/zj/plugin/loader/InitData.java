package com.zj.plugin.loader;

public class InitData {
    private String data;
    private Object range;
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
