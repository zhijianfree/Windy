package com.zj.common.model;

import lombok.Data;

@Data
public class K8SAccessParams {
    /**
     * K8S访问的api地址
     */
    private String apiService;

    /**
     * 访问k8s的token
     */
    private String token;

    /**
     * 镜像仓库地址
     */
    private String repository;

    /**
     * 命名空间
     */
    private String namespace;
}
