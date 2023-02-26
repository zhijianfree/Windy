package com.zj.feature.executor.compare;

import java.util.Objects;

public enum  ParamValueType {
    String(0),
    Map(1),
    Boolean(2),
    Integer(3),
    Array(4),
    Float(5),
    Object(6)
    ;
    private int type;

    ParamValueType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static Object convertType(int type,java.lang.Object o){
        if (Objects.isNull(o)){
            return o;
        }

        java.lang.Object obj = null;
        switch (type){
            case 0:
                java.lang.String.valueOf(o);
                break;
            case 1:
                java.lang.String.valueOf(o);
                break;

        }
        return obj;
    }
}
