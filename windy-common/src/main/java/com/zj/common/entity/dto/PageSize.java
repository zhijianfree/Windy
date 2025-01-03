package com.zj.common.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageSize<T> {
    private long total;
    private List<T> data;

    public PageSize(List<T> data) {
        this.data = data;
    }

}
