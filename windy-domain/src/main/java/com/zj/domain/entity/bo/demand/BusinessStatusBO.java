package com.zj.domain.entity.bo.demand;

import lombok.Data;

@Data
public class BusinessStatusBO {

    private Long id;

    private String statusName;

    private String type;

    private Integer value;

    private Integer sort;

    private String statusColor;
}
