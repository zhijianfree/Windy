package com.zj.domain.entity.dto.demand;

import lombok.Data;

@Data
public class BusinessStatusDTO {

    private Long id;

    private String statusName;

    private String type;

    private Integer value;

    private Integer sort;

    private String statusColor;
}
