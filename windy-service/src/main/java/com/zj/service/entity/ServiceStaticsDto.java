package com.zj.service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStaticsDto {

    /**
     * 服务的API总数
     */
    private Integer apiCount;

    /**
     * 服务的API覆盖率
     */
    private Integer apiCoverage;
}
