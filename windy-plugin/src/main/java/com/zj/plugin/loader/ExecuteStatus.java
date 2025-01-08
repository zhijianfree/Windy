package com.zj.plugin.loader;

public enum ExecuteStatus {
    SUCCESS(1, "处理成功"),
    FAIL(2, "处理失败"),
    TIMEOUT(3, "处理超时"),
    RUNNING(4, "运行中"),
    ;

    private final int type;
    private final String desc;
    ExecuteStatus(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }
}
