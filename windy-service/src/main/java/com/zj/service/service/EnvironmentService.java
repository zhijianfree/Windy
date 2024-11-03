package com.zj.service.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.pipeline.K8SAccessParams;
import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import com.zj.domain.entity.enums.EnvType;
import com.zj.domain.repository.service.IEnvironmentRepository;
import com.zj.service.entity.ResourceList;
import com.zj.service.entity.NodeInfo;
import com.zj.service.entity.SSHParams;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EnvironmentService {

    public static final String NAMESPACE_LIST_URI = "/api/v1/namespaces";
    public static final String NODE_LIST_URI = "/api/v1/nodes";
    public static final String INTERNAL_IP = "InternalIP";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder().sslSocketFactory(
                    SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier()).connectTimeout(10, TimeUnit.SECONDS).build();
    private final Map<Integer, Function<String, Boolean>> checkFuncMap = new ConcurrentHashMap<>();
    private final IEnvironmentRepository repository;
    private final UniqueIdService uniqueIdService;

    public EnvironmentService(IEnvironmentRepository repository, UniqueIdService uniqueIdService) {
        this.repository = repository;
        this.uniqueIdService = uniqueIdService;
        checkFuncMap.put(EnvType.SSH.getType(), this::checkSSH);
        checkFuncMap.put(EnvType.K8S.getType(), this::checkK8S);
    }

    public PageSize<DeployEnvironmentBO> getEnvironments(Integer page, Integer size, String name) {
        IPage<DeployEnvironmentBO> envPage = repository.getEnvPage(page, size, name);
        if (CollectionUtils.isEmpty(envPage.getRecords())) {
            return new PageSize<>();
        }

        PageSize<DeployEnvironmentBO> pageSize = new PageSize<>();
        pageSize.setData(envPage.getRecords());
        pageSize.setTotal(envPage.getTotal());
        return pageSize;
    }

    public Boolean createEnvironment(DeployEnvironmentBO deployEnvironment) {
        deployEnvironment.setEnvId(uniqueIdService.getUniqueId());
        return repository.createEnvironment(deployEnvironment);
    }

    public Boolean updateEnvironment(DeployEnvironmentBO deployEnvironment) {
        return repository.updateEnvironment(deployEnvironment);
    }

    public Boolean deleteEnvironment(String envId) {
        return repository.deleteEnvironment(envId);
    }

    public DeployEnvironmentBO getEnvironment(String envId) {
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
        try {
            K8SAccessParams k8SParams = JSON.parseObject(data, K8SAccessParams.class);
            Headers headers = getHeaders(k8SParams);
            String url = k8SParams.getApiService() + NAMESPACE_LIST_URI;
            Request request = new Request.Builder().url(url).get().headers(headers).build();
            Response response = okHttpClient.newCall(request).execute();
            ResourceList resourceList = JSON.parseObject(response.body().string(), ResourceList.class);
            return resourceList.getItems().stream().anyMatch(meta -> Objects.equals(k8SParams.getNamespace(), meta.getMetadata().getName()));
        } catch (Exception e) {
            log.info("test k8s connect error", e);
        }
        return false;
    }

    public boolean checkSSH(String data) {
        SSHParams sshParams = JSON.parseObject(data, SSHParams.class);
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(sshParams.getUser(), sshParams.getSshIp(), sshParams.getSshPort());
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

    public List<DeployEnvironmentBO> getAvailableEnvs() {
        return repository.getAvailableEnvs();
    }

    public List<NodeInfo> getNodeList(String envId) {
        try {
            DeployEnvironmentBO environment = repository.getEnvironment(envId);
            K8SAccessParams k8SParams = JSON.parseObject(environment.getEnvParams(), K8SAccessParams.class);
            Headers headers = getHeaders(k8SParams);
            String url = k8SParams.getApiService() + NODE_LIST_URI;
            Request request = new Request.Builder().url(url).get().headers(headers).build();
            Response response = okHttpClient.newCall(request).execute();
            ResourceList resourceList = JSON.parseObject(response.body().string(), ResourceList.class);

            return resourceList.getItems().stream().map(resource -> {
                String nodeName = resource.getMetadata().getName();
                Optional<ResourceList.Entry> optional = resource.getStatus().getAddresses().stream()
                        .filter(entry -> Objects.equals(entry.getType(), INTERNAL_IP)).findFirst();
                NodeInfo nodeInfo = new NodeInfo();
                nodeInfo.setNodeName(nodeName);
                nodeInfo.setNodeIp(optional.map(ResourceList.Entry::getAddress).orElse(""));
                return nodeInfo;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("test k8s connect error", e);
        }
        return Collections.emptyList();
    }

    private static Headers getHeaders(K8SAccessParams k8SParams) {
        return new Headers.Builder().add(AUTHORIZATION, BEARER + k8SParams.getToken()).build();
    }
}
