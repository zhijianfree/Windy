package com.zj.client.handler.pipeline.deploy.k8s;

import com.zj.client.handler.pipeline.deploy.DeployContext;
import com.zj.common.entity.pipeline.K8SAccessParams;
import com.zj.common.entity.pipeline.ServiceConfig;
import lombok.Builder;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/7/26
 */
@Data
@Builder
public class K8sDeployContext extends DeployContext {

  private String serviceName;

  private K8SAccessParams k8SAccessParams;

  private ServiceConfig serviceConfig;
}
