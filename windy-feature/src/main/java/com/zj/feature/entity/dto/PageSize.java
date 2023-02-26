package com.zj.feature.entity.dto;

import java.util.List;
import lombok.Data;

@Data
public class PageSize<T> {
    private long total;
    private List<T> data;
}
