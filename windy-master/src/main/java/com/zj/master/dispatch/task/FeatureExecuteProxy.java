package com.zj.master.dispatch.task;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.DispatchType;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.adapter.invoker.IClientInvoker;
import com.zj.common.utils.IpUtils;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.feature.ITestCaseConfigRepository;
import com.zj.master.dispatch.feature.FeatureDispatch;
import com.zj.master.dispatch.listener.IStopEventListener;
import com.zj.master.dispatch.listener.InternalEvent;
import com.zj.master.dispatch.listener.InternalEventFactory;
import com.zj.master.entity.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class FeatureExecuteProxy implements IStopEventListener {

    public static final String TASK_FEATURE_TIPS = "no task need run";
    private final Map<String, FeatureTask> featureTaskMap = new ConcurrentHashMap<>();
    private final Executor executorService;
    private final TaskEndProcessor taskEndProcessor;
    private final IExecutePointRepository executePointRepository;
    private final ITaskRecordRepository taskRecordRepository;
    private final IFeatureHistoryRepository featureHistoryRepository;
    private final IExecuteTemplateRepository executeTemplateRepository;
    private final ITestCaseConfigRepository caseConfigRepository;
    private final IFeatureRepository featureRepository;
    private final IClientInvoker clientInvoker;

    public FeatureExecuteProxy(@Qualifier("featureExecutorPool") Executor executorService,
                               TaskEndProcessor taskEndProcessor,
                               IExecutePointRepository executePointRepository,
                               ITaskRecordRepository taskRecordRepository,
                               IFeatureHistoryRepository featureHistoryRepository,
                               IExecuteTemplateRepository executeTemplateRepository,
                               ITestCaseConfigRepository caseConfigRepository,
                               IFeatureRepository featureRepository, IClientInvoker clientInvoker) {
        this.executorService = executorService;
        this.taskEndProcessor = taskEndProcessor;
        this.executePointRepository = executePointRepository;
        this.taskRecordRepository = taskRecordRepository;
        this.featureHistoryRepository = featureHistoryRepository;
        this.executeTemplateRepository = executeTemplateRepository;
        this.caseConfigRepository = caseConfigRepository;
        this.featureRepository = featureRepository;
        this.clientInvoker = clientInvoker;
    }

    public void execute(FeatureTask featureTask) {
        featureTaskMap.put(featureTask.getTaskRecordId(), featureTask);
        CompletableFuture.supplyAsync(() -> {
            LinkedBlockingQueue<String> featureIds = featureTask.getFeatureIds();
            String featureId = featureIds.poll();
            String taskRecordId = featureTask.getTaskRecordId();
            if (StringUtils.isBlank(featureId)) {
                featureTaskMap.remove(taskRecordId);
                return null;
            }

            TaskRecordBO taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
            if (!taskRecordId.startsWith(FeatureDispatch.TEMP_KEY) && (Objects.isNull(taskRecord)
                    || ProcessStatus.isCompleteStatus(taskRecord.getStatus()))) {
                log.info("task record is done not execute status={}", taskRecord.getStatus());
                return null;
            }

            FeatureExecuteParam featureExecuteParam = getFeatureExecuteParam(featureTask, featureId);
            featureExecuteParam.setDispatchType(DispatchType.FEATURE.name());
            featureExecuteParam.setMasterIp(IpUtils.getLocalIP());
            boolean result = clientInvoker.runFeatureTask(featureExecuteParam);
            log.info("run feature task result = {}", result);
            //todo 执行用例失败的情况下需要修改用例的状态
            return featureId;
        }, executorService).whenComplete((featureId, e) -> {
            String recordId = Optional.ofNullable(featureId).orElse(TASK_FEATURE_TIPS);
            log.info("complete trigger action recordId = {}", recordId);
        }).exceptionally(e -> {
            log.error("handle task error", e);
            //todo 执行用例失败的情况下需要修改用例的状态
            return null;
        });
    }

    public boolean isExitTask(String recordId) {
        if (StringUtils.isBlank(recordId)) {
            return false;
        }
        return featureTaskMap.containsKey(recordId);
    }

    private FeatureExecuteParam getFeatureExecuteParam(FeatureTask featureTask, String featureId) {
        FeatureExecuteParam featureExecuteParam = new FeatureExecuteParam();
        featureExecuteParam.setFeatureId(featureId);
        featureExecuteParam.setExecuteContext(featureTask.getExecuteContext().toMap());
        featureExecuteParam.setTaskRecordId(featureTask.getTaskRecordId());
        List<ExecutePointBO> executePoints = executePointRepository.getExecutePointByFeatureId(featureId);
        //将用例关联模版信息也添加到用例信息中
        executePoints.forEach(executePoint -> {
            ExecutorUnit executorUnit = wrapExecutorUnitTemplate(executePoint);
            executePoint.setExecutorUnit(executorUnit);
        });
        featureExecuteParam.setExecutePointList(executePoints);
        return featureExecuteParam;
    }

    private ExecutorUnit wrapExecutorUnitTemplate(ExecutePointBO executePoint) {
        ExecutorUnit executorUnit = executePoint.getExecutorUnit();
        wrapSubExecutePoints(executorUnit);
        if (StringUtils.isBlank(executorUnit.getRelatedId())) {
            return executorUnit;
        }
        ExecuteTemplateBO executeTemplate = executeTemplateRepository.getExecuteTemplate(executorUnit.getRelatedId());
        if (Objects.isNull(executeTemplate)) {
            return executorUnit;
        }
        ExecutorUnit related = OrikaUtil.convert(executeTemplate, ExecutorUnit.class);
        related.setParams(executeTemplate.getParameterDefines());
        related.setHeaders((Map<String, String>) JSON.parse(executeTemplate.getHeader()));
        executorUnit.setRelatedTemplate(related);
        return executorUnit;
    }

    /**
     * 针对if或者for循环的嵌套子执行点需要设置关联的模版
     */
    private void wrapSubExecutePoints(ExecutorUnit executorUnit) {
        if (Objects.isNull(executorUnit) || CollectionUtils.isEmpty(executorUnit.getExecutePoints())) {
            return;
        }
        executorUnit.getExecutePoints().forEach(point -> {
            String relatedId = point.getExecutorUnit().getRelatedId();
            if (StringUtils.isBlank(relatedId)) {
                return;
            }
            ExecuteTemplateBO executeTemplate = executeTemplateRepository.getExecuteTemplate(relatedId);
            if (Objects.isNull(executeTemplate)) {
                return;
            }
            ExecutorUnit related = OrikaUtil.convert(executeTemplate, ExecutorUnit.class);
            related.setParams(executeTemplate.getParameterDefines());
            related.setHeaders((Map<String, String>) JSON.parse(executeTemplate.getHeader()));
            point.getExecutorUnit().setRelatedTemplate(related);
        });
    }

    public void featureStatusChange(String taskRecordId, FeatureHistoryBO history, Map<String, Object> context) {
        //每个用例执行完成之后都需要判断下是整个任务是否执行完成
        FeatureTask featureTask = featureTaskMap.get(taskRecordId);
        if (Objects.isNull(featureTask)) {
            log.info("can not find task record");
            return;
        }

        boolean isTaskEnd = taskEndProcessor.process(taskRecordId, featureTask.getLogId());
        if (isTaskEnd) {
            featureTaskMap.remove(taskRecordId);
            return;
        }

        if (MapUtils.isNotEmpty(context)) {
            featureTask.getExecuteContext().toMap().putAll(context);
            //异步将上下文替换
            InternalEventFactory.sendNotifyEvent(new InternalEvent(EventType.RECOVER_CONTEXT, history.getFeatureId(),
                    context));
        }

        log.info("feature task start cycle run");
        execute(featureTask);
    }

    @Subscribe
    @AllowConcurrentEvents
    public void recoverGlobalContext(InternalEvent event) {
        if (!Objects.equals(event.getEventType().getType(), EventType.RECOVER_CONTEXT.getType())) {
            return;
        }
        FeatureInfoBO feature = featureRepository.getFeatureById(event.getTargetId());
        if (Objects.isNull(feature)) {
            log.info("can not find feature, not recover global context ={}", event.getTargetId());
            return;
        }
        List<TestCaseConfigBO> caseConfigs = caseConfigRepository.getCaseConfigs(
                feature.getTestCaseId());
        List<TestCaseConfigBO> updateList =
                caseConfigs.stream().filter(config -> event.getContext().containsKey(config.getParamKey())
                        && !Objects.equals(event.getContext().get(config.getParamKey()), config.getValue()))
                        .map(config -> {
                            config.setValue(String.valueOf(event.getContext().get(config.getParamKey())));
                            return config;
                        }).collect(Collectors.toList());
        boolean result = caseConfigRepository.batchUpdateCaseConfig(updateList);
        log.info("batch recover global context result={} context={}", result, JSON.toJSONString(updateList));
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void stopEvent(InternalEvent event) {
        if (Objects.isNull(event.getLogType()) || !Objects.equals(event.getLogType().getType(),
                LogType.FEATURE_TASK.getType())) {
            return;
        }

        String taskRecordId = event.getTargetId();
        FeatureTask featureTask = featureTaskMap.remove(taskRecordId);
        if (Objects.nonNull(featureTask)) {
            featureTaskMap.remove(taskRecordId);
        }

        log.info("stop pipeline task taskId={} taskRecordId={}", featureTask.getTaskId(),
                featureTask.getTaskRecordId());
        taskRecordRepository.updateRecordStatus(taskRecordId, ProcessStatus.STOP.getType());
        featureHistoryRepository.stopTaskFeatures(taskRecordId, ProcessStatus.STOP);
    }

    public Integer getTaskSize() {
        return featureTaskMap.values().stream().mapToInt(task -> task.getFeatureIds().size()).sum();
    }
}
