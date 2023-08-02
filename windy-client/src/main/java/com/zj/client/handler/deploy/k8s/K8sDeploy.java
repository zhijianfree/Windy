package com.zj.client.handler.deploy.k8s;

import com.zj.common.enums.DeployType;
import com.zj.client.handler.deploy.IDeployMode;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/7/26
 */
@Slf4j
@Component
public class K8sDeploy implements IDeployMode<K8sDeployContext> {

  public static final int DEFAULT_REPLICAS = 1;
  public static final int MAX_DEPLOY_TIME = 300 * 1000;
  private final Map<String, ProcessStatus> statusMap = new HashMap<>();

  @Override
  public Integer deployType() {
    return DeployType.K8S.getType();
  }

  @Override
  public void deploy(K8sDeployContext deployContext) {
    //部署代码
    Config config = new ConfigBuilder().withMasterUrl(deployContext.getApiService())
        .withNamespace(deployContext.getNamespace()) // Replace "default" with your namespace
        .withTrustCerts(true).withOauthToken(deployContext.getToken()).build();
    KubernetesClient client = null;
    try {
      client = new DefaultKubernetesClient(config);
      String serviceName = deployContext.getServiceName().toLowerCase();
      deployK8s(deployContext, client, serviceName);
      loopQueryDeployStatus(deployContext, client, serviceName);
      statusMap.put(deployContext.getRecordId(), ProcessStatus.SUCCESS);
    } catch (Exception e) {
      log.error("deploy k8s instance error", e);
      statusMap.put(deployContext.getRecordId(), ProcessStatus.FAIL);
    } finally {
      Optional.ofNullable(client).ifPresent(Client::close);
    }
  }

  private void deployK8s(K8sDeployContext deployContext, KubernetesClient client,
      String serviceName) {
    Integer replicas = Optional.ofNullable(deployContext.getReplicas()).orElse(DEFAULT_REPLICAS);
    Deployment deployment = new DeploymentBuilder()
        .withNewMetadata()
        .withName(serviceName)
        .addToLabels("app", serviceName)
        .endMetadata()
        .withNewSpec()
        .withNewSelector()
        .addToMatchLabels("app", serviceName)
        .endSelector()
        .withReplicas(replicas)
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
    log.info("image name={}", deployContext.getImageName());
    statusMap.put(deployContext.getRecordId(), ProcessStatus.RUNNING);
    client.apps().deployments().inNamespace(deployContext.getNamespace())
        .createOrReplace(deployment);
  }

  private static void loopQueryDeployStatus(K8sDeployContext deployContext, KubernetesClient client,
      String serviceName) throws InterruptedException {
    AtomicLong atomicLong = new AtomicLong(0);
    while (true) {
      long time = atomicLong.addAndGet(10000);
      Thread.sleep(time);
      Deployment deploy = client.apps().deployments().inNamespace(deployContext.getNamespace())
          .withName(serviceName).get();
      DeploymentStatus status = deploy.getStatus();
      if (Objects.equals(status.getReplicas(), status.getAvailableReplicas())) {
        break;
      }

      if (atomicLong.get() >= MAX_DEPLOY_TIME) {
        throw new ExecuteException("部署时间超时");
      }
    }
  }

  @Override
  public ProcessStatus getDeployStatus(String recordId) {
    return statusMap.get(recordId);
  }
}
