package com.zj.domain.entity.po.demand;

import lombok.Data;

@Data
public class BusinessStatus {

    private Long id;

    private String statusName;

    private String type;

    private Integer value;

    private Integer sort;
}
