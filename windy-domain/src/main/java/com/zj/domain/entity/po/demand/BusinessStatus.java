package com.zj.domain.entity.po.demand;

import lombok.Data;

@Data
public class BusinessStatus {

    private Long id;

    /**
     * 业务名称
     */
    private String statusName;

    /**
     * 业务类型
     */
    private String type;

    /**
     * 业务类型值
     */
    private Integer value;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 业务操作类型： 1 可变更 2 业务当前状态类型不允许变更
     */
    private Integer operateType;

    /**
     * 业务状态颜色值
     */
    private String statusColor;
}
