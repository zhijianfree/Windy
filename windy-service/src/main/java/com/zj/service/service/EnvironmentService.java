package com.zj.service.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.service.DeployEnvironmentDto;
import com.zj.domain.entity.enums.EnvType;
import com.zj.domain.repository.service.IEnvironmentRepository;
import com.zj.service.entity.K8SParams;
import com.zj.service.entity.SSHParams;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EnvironmentService {

  private Map<Integer, Function<String, Boolean>> checkFuncMap = new ConcurrentHashMap<>();

  private IEnvironmentRepository repository;
  private UniqueIdService uniqueIdService;

  public EnvironmentService(IEnvironmentRepository repository, UniqueIdService uniqueIdService) {
    this.repository = repository;
    this.uniqueIdService = uniqueIdService;
    checkFuncMap.put(EnvType.SSH.getType(), this::checkSSH);
    checkFuncMap.put(EnvType.K8S.getType(), this::checkK8S);
  }

  public PageSize<DeployEnvironmentDto> getEnvironments(Integer page, Integer size,
      String name) {
    IPage<DeployEnvironmentDto> envPage = repository.getEnvPage(page, size, name);
    if (CollectionUtils.isEmpty(envPage.getRecords())) {
      return new PageSize<>();
    }

    PageSize<DeployEnvironmentDto> pageSize = new PageSize<>();
    pageSize.setData(envPage.getRecords());
    pageSize.setTotal(envPage.getTotal());
    return pageSize;
  }

  public Boolean createEnvironment(DeployEnvironmentDto deployEnvironment) {
    deployEnvironment.setEnvId(uniqueIdService.getUniqueId());
    return repository.createEnvironment(deployEnvironment);
  }

  public Boolean updateEnvironment(DeployEnvironmentDto deployEnvironment) {
    return repository.updateEnvironment(deployEnvironment);
  }

  public Boolean deleteEnvironment(String envId) {
    return repository.deleteEnvironment(envId);
  }

  public DeployEnvironmentDto getEnvironment(String envId) {
    return repository.getEnvironment(envId);
  }

  public Boolean checkStatus(Integer checkType, String data) {
    Function<String, Boolean> checkFunction = checkFuncMap.get(checkType);
    if (Objects.isNull(checkFunction)) {
      return false;
    }
    return checkFunction.apply(data);
  }

  public boolean checkK8S(String data) {
    K8SParams k8SParams = JSON.parseObject(data, K8SParams.class);
    // 创建Config对象并设置访问配置
    Config config = new ConfigBuilder()
        .withMasterUrl(k8SParams.getUrl())
        .withOauthToken(k8SParams.getToken())
        .build();
    try (KubernetesClient client = new DefaultKubernetesClient(config)) {
      // 获取Namespace列表
      List<Namespace> namespaces = client.namespaces().list().getItems();

      // 打印Namespace列表
      return namespaces.stream().anyMatch(
          namespace -> Objects.equals(k8SParams.getNamespace(), namespace.getSpec().toString()));
    } catch (Exception e) {
      log.info("test k8s connect error");
    }
    return false;
  }

  public boolean checkSSH(String data) {
    SSHParams sshParams = JSON.parseObject(data, SSHParams.class);
    JSch jsch = new JSch();
    Session session = null;
    try {
      session = jsch.getSession(sshParams.getUser(), sshParams.getHost(), sshParams.getPort());
      session.setPassword(sshParams.getPassword());
      // 配置SSH连接时不进行主机密钥检查
      session.setConfig("StrictHostKeyChecking", "no");
      session.connect();
      return session.isConnected();
    } catch (JSchException e) {
      log.info("test connect ssh server error", e);
    } finally {
      Optional.ofNullable(session).ifPresent(Session::disconnect);
    }
    return false;
  }
}
