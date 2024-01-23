package com.zj.client.handler.deploy.k8s;

import com.zj.client.handler.deploy.IDeployMode;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
import com.zj.common.model.K8SAccessParams;
import com.zj.common.model.K8SContainerParams;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategy;
import io.fabric8.kubernetes.client.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    K8SAccessParams k8SAccess = deployContext.getK8SAccessParams();
    //部署代码
    Config config = new ConfigBuilder().withMasterUrl(k8SAccess.getApiService())
        .withNamespace(k8SAccess.getNamespace())
        .withTrustCerts(true).withOauthToken(k8SAccess.getToken()).build();
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
    K8SContainerParams containerParams = deployContext.getK8SContainerParams();
    List<EnvVar> envVars = buildEnvParams(containerParams.getEnvParams());
    //宿主机路径
    List<Volume> volumes = buildVolumes(containerParams.getVolumes());
    //容器路径
    List<VolumeMount> volumeMounts = buildVolumeMounts(containerParams.getVolumes());
    //端口映射
    List<ContainerPort> containerPorts = buildContainerPorts(containerParams.getPorts());
    Integer replicas = Optional.ofNullable(containerParams.getReplicas()).orElse(DEFAULT_REPLICAS);

    DeploymentStrategy strategy = new DeploymentStrategy();
    strategy.setType(containerParams.getStrategy().getType());

    Deployment deployment = new DeploymentBuilder()
            .withNewMetadata()
                .withName(serviceName)
                .addToLabels("app", serviceName)
            .endMetadata()
            .withNewSpec()
                .withNewSelector()
                  .addToMatchLabels("app", serviceName)
                .endSelector().withReplicas(replicas)
                .withNewTemplate()
                  .withNewSpec()
                      .withVolumes(volumes)
                      .addNewContainer()
                            .withName(serviceName)
                            .withImage(containerParams.getImageName())
                            .withEnv(envVars)
                            .withPorts(containerPorts)
                            .withVolumeMounts(volumeMounts)
                      .endContainer()
                  .endSpec()
                .endTemplate()
            .endSpec()
            .build();
    log.info("image name={}", containerParams.getImageName());
    statusMap.put(deployContext.getRecordId(), ProcessStatus.RUNNING);
    String namespace = deployContext.getK8SAccessParams().getNamespace();
    client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
  }

  private static void loopQueryDeployStatus(K8sDeployContext deployContext, KubernetesClient client,
      String serviceName) throws InterruptedException {
    AtomicLong atomicLong = new AtomicLong(0);
    while (true) {
      long time = atomicLong.addAndGet(10000);
      Thread.sleep(time);
      String namespace = deployContext.getK8SAccessParams().getNamespace();
      Deployment deploy = client.apps().deployments().inNamespace(namespace)
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

  private List<EnvVar> buildEnvParams(List<K8SContainerParams.ContainerEnv> envParams) {
    if (CollectionUtils.isEmpty(envParams)) {
      return Collections.emptyList();
    }
    return envParams.stream().filter(K8SContainerParams.ContainerEnv::notExistEmpty).map(this::buildEnvItem)
            .collect(Collectors.toList());
  }

  private EnvVar buildEnvItem(K8SContainerParams.ContainerEnv containerEnv) {
    if (containerEnv.isRelated()) {
      return new EnvVarBuilder().withName(containerEnv.getName()).withNewValueFrom()
              .withNewFieldRef().withFieldPath(containerEnv.getValue()).endFieldRef().endValueFrom()
              .build();
    }
    return new EnvVarBuilder().withName(containerEnv.getName()).withValue(containerEnv.getValue())
            .build();
  }

  private List<VolumeMount> buildVolumeMounts(List<K8SContainerParams.ContainerVolume> volumes) {
    if (CollectionUtils.isEmpty(volumes)) {
      return Collections.emptyList();
    }
    return volumes.stream().filter(K8SContainerParams.ContainerVolume::notExistEmpty).map(
            volume -> new VolumeMountBuilder().withName(volume.getName())
                    .withMountPath(volume.getVolume()).build()).collect(Collectors.toList());
  }

  private List<Volume> buildVolumes(List<K8SContainerParams.ContainerVolume> volumes) {
    if (CollectionUtils.isEmpty(volumes)) {
      return Collections.emptyList();
    }
    return volumes.stream().filter(K8SContainerParams.ContainerVolume::notExistEmpty).map(
            volume -> new VolumeBuilder().withName(volume.getName())
                    .withNewHostPath(volume.getHostVolume(), "").build()).collect(Collectors.toList());
  }

  private List<ContainerPort> buildContainerPorts(List<K8SContainerParams.ContainerPort> ports) {
    if (CollectionUtils.isEmpty(ports)) {
      return Collections.emptyList();
    }
    return ports.stream().filter(K8SContainerParams.ContainerPort::notExistEmpty).map(
                    port -> new ContainerPortBuilder().withContainerPort(port.getPort())
                            .withHostPort(port.getHostPort()).withProtocol(port.getProtocol()).build())
            .collect(Collectors.toList());
  }
}
