package com.zj.common.entity.pipeline;

import lombok.Data;

@Data
public class DeployParams {

    private SSHParams sshParams;

    private K8SAccessParams k8SAccessParams;

    private ServiceConfig serviceConfig;
}
