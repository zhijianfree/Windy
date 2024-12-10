package com.zj.master.dispatch.pipeline.listener;

import com.alibaba.fastjson.JSON;
import com.zj.domain.entity.bo.demand.BugBO;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PublishBindBO;
import com.zj.domain.entity.enums.BugStatus;
import com.zj.domain.entity.enums.DemandStatus;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.entity.enums.RelationType;
import com.zj.domain.entity.enums.WorkTaskStatus;
import com.zj.domain.repository.demand.IBugRepository;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.demand.IWorkTaskRepository;
import com.zj.domain.repository.pipeline.ICodeChangeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import com.zj.master.entity.vo.NodeStatusChange;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流水线成功过完成之后删除发布绑定的分支、删除代码变更、完成(需求、缺陷、工作项)
 * @author guyuelan
 * @since 2023/6/29
 */
@Slf4j
@Component
public class PublishEndListener implements IPipelineEndListener {

    private final IPublishBindRepository publishBindRepository;
    private final IPipelineRepository pipelineRepository;
    private final ICodeChangeRepository codeChangeRepository;
    private final IDemandRepository demandRepository;
    private final IBugRepository bugRepository;
    private final IWorkTaskRepository workTaskRepository;


    public PublishEndListener(IPublishBindRepository publishBindRepository, IPipelineRepository pipelineRepository,
                              ICodeChangeRepository codeChangeRepository, IDemandRepository demandRepository,
                              IBugRepository bugRepository, IWorkTaskRepository workTaskRepository) {
        this.publishBindRepository = publishBindRepository;
        this.pipelineRepository = pipelineRepository;
        this.codeChangeRepository = codeChangeRepository;
        this.demandRepository = demandRepository;
        this.bugRepository = bugRepository;
        this.workTaskRepository = workTaskRepository;
    }

    @Override
    public void handleEnd(NodeStatusChange statusChange) {
        PipelineBO pipeline = pipelineRepository.getPipeline(statusChange.getPipelineId());
        if (Objects.isNull(pipeline)) {
            return;
        }

        log.info("pipeline info = {}", JSON.toJSONString(pipeline));
        if (Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType()) && statusChange.getProcessStatus().isSuccess()) {
            List<PublishBindBO> pipelinePublishes = publishBindRepository.getServicePublishes(pipeline.getServiceId());
            List<String> branches =
                    pipelinePublishes.stream().map(PublishBindBO::getBranch).collect(Collectors.toList());
            log.info("get publish branches ={}", branches);
            List<CodeChangeBO> serviceChangeList = codeChangeRepository.getServiceChanges(pipeline.getServiceId());
            finishCodeRelatedResource(serviceChangeList);
            List<String> serviceChanges =
                    serviceChangeList.stream().filter(codeChange -> branches.contains(codeChange.getChangeBranch())).map(CodeChangeBO::getChangeId).collect(Collectors.toList());
            log.info("code changes id ={}", serviceChanges);
            boolean batchDeleteCodeChange = codeChangeRepository.batchDeleteCodeChange(serviceChanges);
            boolean deletePublishLine = publishBindRepository.deleteServicePublishes(pipeline.getServiceId());
            log.info("delete code change result = {} delete publish result={}", batchDeleteCodeChange,
                    deletePublishLine);
        }
    }

    /**
     * 分支发布完成则会自动完成关联的需求、缺陷、工作项
     */
    private void finishCodeRelatedResource(List<CodeChangeBO> serviceChangeList) {
        if (CollectionUtils.isEmpty(serviceChangeList)) {
            log.info("service code change is empty not finish relation source");
            return;
        }

        Map<Integer, List<String>> relationMap =
                serviceChangeList.stream().collect(Collectors.groupingBy(CodeChangeBO::getRelationType,
                        Collectors.mapping(CodeChangeBO::getRelationId, Collectors.toList())));
        List<String> demandIds = relationMap.get(RelationType.DEMAND.getType());
        if (CollectionUtils.isNotEmpty(demandIds)) {
            List<String> notCompleteDemandIds = demandRepository.getNotCompleteDemandByIds(demandIds).stream()
                    .map(DemandBO::getDemandId).collect(Collectors.toList());
            boolean demandUpdate = demandRepository.batchUpdateStatus(notCompleteDemandIds, DemandStatus.PUBLISHED.getType());
            log.info("batch update demand result ={} id={}", demandUpdate, notCompleteDemandIds);
        }

        List<String> bugIds = relationMap.get(RelationType.BUG.getType());
        if (CollectionUtils.isNotEmpty(bugIds)) {
            List<String> notCompleteBugIds = bugRepository.getNotCompleteBugs(bugIds).stream()
                    .map(BugBO::getBugId).collect(Collectors.toList());
            boolean bugUpdate = bugRepository.batchUpdateStatus(notCompleteBugIds, BugStatus.PUBLISHED.getType());
            log.info("batch update bug result ={}", bugUpdate);
        }

        List<String> taskIds = relationMap.get(RelationType.WORK.getType());
        if (CollectionUtils.isNotEmpty(bugIds)) {
            List<String> notCompleteTaskIds =
                    workTaskRepository.getNotCompleteWorkTasks(taskIds).stream().map(WorkTaskBO::getTaskId).collect(Collectors.toList());
            boolean workTaskUpdate = workTaskRepository.batchUpdateStatus(notCompleteTaskIds, WorkTaskStatus.COMPLETE.getType());
            log.info("batch update work task result ={}", workTaskUpdate);
        }
    }
}
