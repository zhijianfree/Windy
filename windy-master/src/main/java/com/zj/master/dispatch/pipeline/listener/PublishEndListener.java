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
        if (Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType()) &&
                statusChange.getProcessStatus().isSuccess()) {
            //获取流水线所有的发布分支
            List<PublishBindBO> pipelinePublishes = publishBindRepository.getServicePublishes(pipeline.getServiceId());
            List<String> branches =
                    pipelinePublishes.stream().map(PublishBindBO::getBranch).collect(Collectors.toList());
            log.info("get publish branches ={}", branches);

            //根据发布分支查询的变更列表，然后更新需求或缺陷状态
            List<CodeChangeBO> serviceChangeList = codeChangeRepository.getServiceChanges(pipeline.getServiceId());
            finishCodeRelatedResource(serviceChangeList, branches);

            //删除已发布的变更列表
            List<String> serviceChanges =
                    serviceChangeList.stream().filter(codeChange -> branches.contains(codeChange.getChangeBranch())).map(CodeChangeBO::getChangeId).collect(Collectors.toList());
            log.info("code changes id ={}", serviceChanges);
            boolean batchDeleteCodeChange = codeChangeRepository.batchDeleteCodeChange(serviceChanges);

            //删除发布绑定的分支
            boolean deletePublishLine = publishBindRepository.deleteServicePublishes(pipeline.getServiceId());
            log.info("delete code change result = {} delete publish result={}", batchDeleteCodeChange,
                    deletePublishLine);
        }
    }

    /**
     * 分支发布完成则会自动修改关联的需求、缺陷、工作项状态
     */
    private void finishCodeRelatedResource(List<CodeChangeBO> serviceChangeList, List<String> publishBranches) {
        if (CollectionUtils.isEmpty(serviceChangeList)) {
            log.info("service code change is empty not finish relation source");
            return;
        }

        //获取已发布的分支关联的变更
        Map<Integer, List<String>> relationMap = serviceChangeList.stream().filter(codeChangeBO ->
                        publishBranches.contains(codeChangeBO.getChangeBranch()))
                        .collect(Collectors.groupingBy(CodeChangeBO::getRelationType,
                                Collectors.mapping(CodeChangeBO::getRelationId, Collectors.toList())));
        List<String> demandIds = relationMap.get(RelationType.DEMAND.getType());
        if (CollectionUtils.isNotEmpty(demandIds)) {
            batchUpdateDemands(demandIds, publishBranches);
        }

        List<String> bugIds = relationMap.get(RelationType.BUG.getType());
        if (CollectionUtils.isNotEmpty(bugIds)) {
            batchUpdateBugs(bugIds, publishBranches);
        }

        List<String> taskIds = relationMap.get(RelationType.WORK.getType());
        if (CollectionUtils.isNotEmpty(taskIds)) {
            List<String> notCompleteTaskIds =
                    workTaskRepository.getNotCompleteWorkTasks(taskIds).stream().map(WorkTaskBO::getTaskId).collect(Collectors.toList());
            boolean workTaskUpdate = workTaskRepository.batchUpdateStatus(notCompleteTaskIds, WorkTaskStatus.COMPLETE.getType());
            log.info("batch update work task result ={}", workTaskUpdate);
        }
    }

    private void batchUpdateBugs(List<String> bugIds, List<String> publishBranches) {
        //将缺陷列表转化成变更分支是否全部发布的map
        Map<Boolean, List<String>> partitionMap = bugIds.stream().collect(Collectors.partitioningBy(bugId ->
                isDemandAllBranchPublish(bugId, RelationType.BUG, publishBranches)));

        //将需求变更为发布状态
        List<String> completeBugIds = partitionMap.get(true);
        List<String> notPublishBugIds = bugRepository.getNotCompleteBugs(completeBugIds).stream()
                .map(BugBO::getBugId).collect(Collectors.toList());
        boolean bugUpdate = bugRepository.batchUpdateStatus(notPublishBugIds, BugStatus.PUBLISHED.getType());
        log.info("batch update complete bug result ={} id={}", bugUpdate, notPublishBugIds);

        //将未开始的需求变更未研发中状态
        List<String> notCompleteBugIds = partitionMap.get(false);
        boolean updateStatus = bugRepository.batchUpdateProcessing(notCompleteBugIds);
        log.info("update bug processing result ={} id={}", updateStatus, notCompleteBugIds);
    }

    private void batchUpdateDemands(List<String> demandIds, List<String> branches) {
        //将需求列表转化成变更分支是否全部发布的map
        Map<Boolean, List<String>> partitionMap = demandIds.stream().collect(Collectors.partitioningBy(demandId ->
                isDemandAllBranchPublish(demandId, RelationType.DEMAND, branches)));

        //将需求变更为发布状态
        List<String> completeDemandIds = partitionMap.get(true);
        List<String> notPublishDemandIds = demandRepository.getNotCompleteDemandByIds(completeDemandIds).stream()
                .map(DemandBO::getDemandId).collect(Collectors.toList());
        boolean demandUpdate = demandRepository.batchUpdateStatus(notPublishDemandIds, DemandStatus.PUBLISHED.getType());
        log.info("batch update complete demand result ={} id={}", demandUpdate, notPublishDemandIds);

        //将未开始的需求变更未研发中状态
        List<String> notCompleteDemandIds = partitionMap.get(false);
        boolean updateStatus = demandRepository.batchUpdateProcessing(notCompleteDemandIds);
        log.info("update demand processing result ={} id={}", updateStatus, notCompleteDemandIds);
    }

    private boolean isDemandAllBranchPublish(String relationId, RelationType relationType, List<String> branches) {
        List<CodeChangeBO> codeChanges = codeChangeRepository.getCodeChangeByRelationId(relationId, relationType.getType());
        return codeChanges.stream().allMatch(codeChangeBO -> branches.contains(codeChangeBO.getChangeBranch()));
    }
}
