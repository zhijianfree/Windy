package com.zj.domain.entity.po.demand;

import lombok.Data;

@Data
public class BusinessStatus {

    private Long id;

    private Integer statusId;

    private String statusName;

    private Integer type;

    private Integer sort;
}
