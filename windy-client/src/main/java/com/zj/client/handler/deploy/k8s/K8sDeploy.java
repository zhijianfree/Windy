package com.zj.client.handler.deploy.k8s;

import com.zj.client.handler.deploy.IDeployMode;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

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
        .withNamespace(deployContext.getNamespace())
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
    List<EnvVar> envVars = new ArrayList<>();
    envVars.add(buildEnvItem("GATEWAY_SERVER_PORT", "8000"));

    Integer replicas = Optional.ofNullable(deployContext.getReplicas()).orElse(DEFAULT_REPLICAS);
    Deployment deployment = new DeploymentBuilder().withNewMetadata().withName(serviceName)
        .addToLabels("app", serviceName).endMetadata().withNewSpec().withNewSelector()
        .addToMatchLabels("app", serviceName).endSelector().withReplicas(replicas).withNewTemplate()
        .withNewMetadata().addToLabels("app", serviceName).endMetadata().withNewSpec()
        .addNewContainer().withEnv(envVars).withName(serviceName).withImage(deployContext.getImageName())
        .endContainer().endSpec().endTemplate().endSpec().build();
    log.info("image name={}", deployContext.getImageName());
    statusMap.put(deployContext.getRecordId(), ProcessStatus.RUNNING);
    client.apps().deployments().inNamespace(deployContext.getNamespace())
        .createOrReplace(deployment);
  }

  private static EnvVar buildEnvItem(String name, String value) {
    return new EnvVarBuilder().withName(name).withValue(value)
        .build();
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

  public static void main(String[] args) {
    Config config = new ConfigBuilder().withMasterUrl("https://10.202.244.10:6443")
        .withNamespace("default")
        .withTrustCerts(true).withOauthToken("eyJhbGciOiJSUzI1NiIsImtpZCI6IktKZjdLMkhGNUItc0pkeGhuWXZPM2p6MEt4ZlQyVWkxZWJZUjdGcXJsVnMifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6IndpbmR5LXRva2VuLW5ybW00Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6IndpbmR5Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiZDgxZWFlNGUtOTMyNS00Mzg1LTk3NzMtZGM0OTQ4YzQ3YWNlIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmRlZmF1bHQ6d2luZHkifQ.pHeurUoSg0IZyWxeeKwN5K4Mmp6AsqZEBUP92ALIbPxypf5A2O2YghegOMIgWow7U58i5jWvwCK8UhG4ZAN2QcPg3aWrxPZfQmsWd2H34IoPbofi1jfkK8SyKJn9HAkgINnCpP7Qs4sQnQ73dGUzR2a5S7f-lEMvnC6lr6kBPRhzXyjkMhVIlEiAZUqXGPY8w6P969hx1QzIb6AOMPvMerro1_Ju33MoqDWqWIG5hwF3gzTPE0dVjJO4ymOumk0_vZHk6PgQ7nECSA2B7_6UZNkyDg1lj-e2nyvR5f_2UWWPxGhIKXTsAAIya4vka1BLKQq6-JrwN-InismQKzhOZA").build();
    DefaultKubernetesClient client = new DefaultKubernetesClient(config);

    List<EnvVar> envVars = new ArrayList<>();
    envVars.add(buildEnvItem("GATEWAY_SERVER_PORT", "8000"));

    String imageName = "smartdo-registry-cn.tuya-inc.com:7799/gyl/edgedaemon:20230810143323";
    String serviceName = "edgedaemon";
    Deployment deployment = new DeploymentBuilder()
        .withNewMetadata().withName(serviceName)
        .addToLabels("app", serviceName).endMetadata()
        .withNewSpec().withNewSelector()
        .addToMatchLabels("app", serviceName).endSelector().withReplicas(1).withNewTemplate()
        .withNewMetadata().addToLabels("app", serviceName).endMetadata().withNewSpec()
        .addNewContainer().withEnv(envVars).withName(serviceName).withImage(imageName)
        .endContainer().endSpec().endTemplate().endSpec().build();
    client.apps().deployments().inNamespace("default")
        .createOrReplace(deployment);
  }
}
