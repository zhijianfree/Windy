package com.zj.client.entity.enuns;

public enum ParamTypeEnum {
    STRING(0),
    MAP(1),
    LIST(2),

    INTEGER(3),

    FLOAT(4),

    DOUBLE(5)
    ;
    private int type;

    ParamTypeEnum(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
