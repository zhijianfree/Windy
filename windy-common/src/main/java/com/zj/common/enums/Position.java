package com.zj.common.enums;

import java.util.Objects;

public enum Position {
    Query,Path,Body,Header;

    public static boolean isBodyPosition(String type) {
        return Objects.equals(Position.Body.name(), type);
    }
}
