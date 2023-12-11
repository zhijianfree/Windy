package com.zj.client.handler.deploy.k8s;

import com.zj.client.handler.deploy.DeployContext;
import com.zj.common.model.K8SAccessParams;
import com.zj.common.model.K8SContainerParams;
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

  private K8SContainerParams k8SContainerParams;
}
