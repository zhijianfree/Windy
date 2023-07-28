package com.zj.client.handler.deploy.k8s;

import com.zj.client.handler.deploy.DeployContext;
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

  private String apiService;

  private String token;

  private String namespace;

  /**
   * 镜像名称
   * */
  private String imageName;

  /**
   * 副本数
   * */
  private Integer replicas;
}
