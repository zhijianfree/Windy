package com.zj.common.model;

import lombok.Data;

@Data
public class DeployParams {

    private K8SAccessParams k8SAccessParams;

    private K8SContainerParams k8SContainerParams;
}
