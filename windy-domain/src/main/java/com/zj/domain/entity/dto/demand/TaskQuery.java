package com.zj.domain.entity.dto.demand;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskQuery {
    private Integer page;
    private Integer size;
    private String name;
    private Integer status;
    private String userId;
}
