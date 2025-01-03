package com.zj.service.entity;

import com.zj.domain.entity.bo.service.ServiceApiBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStaticsDto {

    /**
     * 服务的API总数
     */
    private Integer apiCount;

    /**
     * 用例总数
     */
    private Integer featureCount;

    /**
     * 服务的API覆盖率
     */
    private Integer apiCoverage;

    /**
     * 未覆盖的API列表
     */
    private List<ServiceApiBO> notCoveredApi;
}
