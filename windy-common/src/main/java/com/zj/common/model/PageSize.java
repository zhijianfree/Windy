package com.zj.common.model;

import java.util.List;
import lombok.Data;

@Data
public class PageSize<T> {
    private long total;
    private List<T> data;
}
