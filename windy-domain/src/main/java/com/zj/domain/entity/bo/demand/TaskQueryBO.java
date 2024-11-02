package com.zj.domain.entity.bo.demand;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskQueryBO {
    /**
     * 分页号
     */
    private Integer page;

    /**
     * 分页大小
     */
    private Integer size;

    /**
     * 工作项名称
     */
    private String name;

    /**
     * 工作项状态
     */
    private Integer status;

    /**
     * 用户ID
     */
    private String userId;
}
