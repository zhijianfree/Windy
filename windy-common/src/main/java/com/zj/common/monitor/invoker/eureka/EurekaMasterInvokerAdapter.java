package com.zj.common.monitor.invoker.eureka;

import com.alibaba.fastjson.JSON;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.model.MasterCollect;
import com.zj.common.model.PluginInfo;
import com.zj.common.model.ResponseMeta;
import com.zj.common.model.ResponseStatusModel;
import com.zj.common.model.ResultEvent;
import com.zj.common.model.StopDispatch;
import com.zj.common.monitor.discover.DiscoverService;
import com.zj.common.monitor.discover.ServiceInstance;
import com.zj.common.monitor.invoker.IMasterInvoker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class EurekaMasterInvokerAdapter extends BaseEurekaAdapter implements IMasterInvoker {
    private static final String DISPATCH_TASK = "http://WindyMaster/v1/devops/dispatch/task";
    private static final String NOTIFY_TASK_RESULT = "http://WindyMaster/v1/devops/dispatch/notify";
    private static final String QUERY_APPROVAL_STATUS = "http://WindyMaster/v1/devops/master/record/%s";
    public static final String STOP_DISPATCH_TASK = "http://WindyMaster/v1/devops/dispatch/stop";
    public static final String MASTER_MONITOR_URL = "http://%s/v1/devops/master/instance";
    public static final String GET_PLUGIN_LIST = "http://WindyMaster/v1/devops/master/plugins";
    private final DiscoverService discoverService;

    public EurekaMasterInvokerAdapter(RestTemplate restTemplate, DiscoverService discoverService) {
        super(restTemplate);
        this.discoverService = discoverService;
    }

    @Override
    public String runFeatureTask(DispatchTaskModel dispatchTaskModel) {
        ResponseEntity<String> response = requestPost(DISPATCH_TASK, dispatchTaskModel);
        if (Objects.isNull(response)) {
            return null;
        }
        ResponseMeta responseMeta = JSON.parseObject(response.getBody(), ResponseMeta.class);
        return Optional.ofNullable(responseMeta).map(res -> String.valueOf(res.getData())).orElse(null);
    }

    @Override
    public Boolean runGenerateTask(DispatchTaskModel dispatchTaskModel) {
        ResponseEntity<String> response = requestPost(DISPATCH_TASK, dispatchTaskModel);
        if (Objects.isNull(response)) {
            return false;
        }
        ResponseMeta responseMeta = JSON.parseObject(response.getBody(), ResponseMeta.class);
        return Optional.ofNullable(responseMeta).map(res -> Boolean.parseBoolean(String.valueOf(res.getData()))).orElse(null);
    }

    @Override
    public String runPipelineTask(DispatchTaskModel dispatchTaskModel) {
        ResponseEntity<String> response = requestPost(DISPATCH_TASK, dispatchTaskModel);
        if (Objects.isNull(response)) {
            return null;
        }
        ResponseMeta responseMeta = JSON.parseObject(response.getBody(), ResponseMeta.class);
        return Optional.ofNullable(responseMeta).map(res -> String.valueOf(res.getData())).orElse(null);
    }

    @Override
    public ResponseStatusModel getFeatureTaskStatus(String taskRecordId) {
        return null;
    }

    @Override
    public boolean notifyExecuteEvent(ResultEvent resultEvent) {
        List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(DiscoverService.WINDY_MASTER);
        boolean notifySuccess = serviceInstances.stream()
                .filter(serviceInstance -> Objects.equals(serviceInstance.getHost(), resultEvent.getMasterIP()))
                .findFirst().map(serviceInstance -> {
                    String url = NOTIFY_TASK_RESULT.replace(DiscoverService.WINDY_MASTER, serviceInstance.getHost());
                    log.info("start notify master ip={} result", serviceInstance.getHost());
                    // 如果触发任务执行的master节点存在那么优先访问触发任务的master节点
                    return postWithIp(url, resultEvent);
                }).orElse(false);
        if (notifySuccess) {
            return true;
        }

        //master节点不可达时，尝试使用其他的master节点
        ResponseEntity<String> responseEntity = requestPost(NOTIFY_TASK_RESULT, resultEvent);
        log.info("notify event code={} result={}", responseEntity.getStatusCode(), responseEntity.getBody());
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public ResponseStatusModel getApprovalRecord(String recordId) {
        String url = String.format(QUERY_APPROVAL_STATUS , recordId);
        ResponseEntity<String> response = requestGet(url);
        return Optional.ofNullable(response).map(res -> JSON.parseObject(res.getBody(), ResponseStatusModel.class))
                .orElse(null);
    }

    @Override
    public List<PluginInfo> getAvailablePlugins() {
        ResponseEntity<String> response = requestGet(GET_PLUGIN_LIST);
        if (Objects.isNull(response)) {
            return Collections.emptyList();
        }
        ResponseMeta responseMeta = JSON.parseObject(response.getBody(), ResponseMeta.class);
        return Optional.ofNullable(responseMeta).map(res -> JSON.parseArray(JSON.toJSONString(responseMeta.getData()),
                PluginInfo.class)).orElseGet(ArrayList::new);
    }

    @Override
    public boolean stopDispatchTask(DispatchTaskModel dispatchTaskModel) {
        ResponseEntity<String> response = requestPost(STOP_DISPATCH_TASK, dispatchTaskModel);
        if (Objects.isNull(response)) {
            return false;
        }
        log.info("get test result code= {} result={}", response.getStatusCode(), response.getBody());
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public List<MasterCollect> requestMasterMonitor() {
        List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(DiscoverService.WINDY_MASTER);
        return serviceInstances.stream().map(service -> {
            String url = String.format(MASTER_MONITOR_URL, service.getHost());
            Response response = getWithIp(url);
            if (Objects.isNull(response)) {
                return null;
            }
            String resultString = response.body().toString();
            log.info("request master monitor result={}", resultString);
            ResponseMeta result = JSON.parseObject(resultString, ResponseMeta.class);
            return JSON.parseObject(JSON.toJSONString(result.getData()), MasterCollect.class);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
