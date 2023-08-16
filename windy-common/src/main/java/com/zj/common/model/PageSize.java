package com.zj.common.model;

import lombok.Data;

import java.util.List;

@Data
public class PageSize<T> {
    private long total;
    private List<T> data;
}
