package com.zj.service.entity;

import lombok.Data;

import java.util.List;

@Data
public class GenerateTemplate {

    /**
     * api列表
     */
    private List<String> apiIds;

    /**
     * 服务Id
     */
    private String serviceId;

}
