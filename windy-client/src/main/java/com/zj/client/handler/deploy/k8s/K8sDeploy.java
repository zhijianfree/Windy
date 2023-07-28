package com.zj.client.handler.deploy.k8s;

import com.zj.client.entity.enuns.DeployType;
import com.zj.client.handler.deploy.IDeployMode;
import com.zj.common.enums.ProcessStatus;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.Client;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/7/26
 */
@Slf4j
@Component
public class K8sDeploy implements IDeployMode<K8sDeployContext> {

  private final Map<String, ProcessStatus> statusMap = new HashMap<>();

  @Override
  public Integer deployType() {
    return DeployType.K8S.getType();
  }

  @Override
  public void deploy(K8sDeployContext deployContext) {
    //部署代码
    Config config = new ConfigBuilder()
        .withMasterUrl(deployContext.getApiService())
        .withNamespace(deployContext.getNamespace()) // Replace "default" with your namespace
        .withTrustCerts(true)
        .withOauthToken(deployContext.getToken())
        .build();
    KubernetesClient client = null;
    try {
      client = new DefaultKubernetesClient(config);
      String serviceName = deployContext.getServiceName().toLowerCase();
      Deployment deployment = new DeploymentBuilder()
          .withNewMetadata()
          .withName(serviceName)
          .addToLabels("app", serviceName)
          .endMetadata()
          .withNewSpec()
          .withNewSelector()
          .addToMatchLabels("app", serviceName)
          .endSelector()
          .withReplicas(deployContext.getReplicas())
          .withNewTemplate()
          .withNewMetadata()
          .addToLabels("app", serviceName)
          .endMetadata()
          .withNewSpec()
          .addNewContainer()
          .withName(serviceName)
          .withImage(deployContext.getImageName())
          .endContainer()
          .endSpec()
          .endTemplate()
          .endSpec()
          .build();
      statusMap.put(deployContext.getRecordId(), ProcessStatus.RUNNING);
      Deployment deploymentResult = client.apps().deployments()
          .inNamespace(deployContext.getNamespace())
          .createOrReplace(deployment);

      DeploymentStatus status = deploymentResult.getStatus();
      ProcessStatus result = status.getUnavailableReplicas() < 1 ? ProcessStatus.SUCCESS : ProcessStatus.FAIL;
      statusMap.put(deployContext.getRecordId(), result);
    } catch (Exception e) {
      log.error("deploy k8s instance error", e);
      statusMap.put(deployContext.getRecordId(), ProcessStatus.FAIL);
    } finally {
      Optional.ofNullable(client).ifPresent(Client::close);
    }
  }

  @Override
  public ProcessStatus getDeployStatus(String recordId) {
    return statusMap.get(recordId);
  }
}
