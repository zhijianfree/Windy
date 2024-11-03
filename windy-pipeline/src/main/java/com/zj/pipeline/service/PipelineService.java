package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.zj.common.enums.LogType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import com.zj.domain.entity.bo.pipeline.PipelineStageBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.service.impl.MicroServiceRepository;
import com.zj.pipeline.entity.enums.PipelineStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Slf4j
@Service
public class PipelineService {

    private final PipelineNodeService pipelineNodeService;
    private final PipelineStageService pipelineStageService;
    private final PipelineHistoryService pipelineHistoryService;
    private final UniqueIdService uniqueIdService;
    private final IPipelineRepository pipelineRepository;
    private final IBindBranchRepository bindBranchRepository;
    private final MicroServiceRepository microServiceRepository;
    private final IMasterInvoker masterInvoker;

    public PipelineService(PipelineNodeService pipelineNodeService,
                           PipelineStageService pipelineStageService, PipelineHistoryService pipelineHistoryService,
                           UniqueIdService uniqueIdService, IPipelineRepository pipelineRepository,
                           IBindBranchRepository bindBranchRepository, MicroServiceRepository microServiceRepository, IMasterInvoker masterInvoker) {
        this.pipelineNodeService = pipelineNodeService;
        this.pipelineStageService = pipelineStageService;
        this.pipelineHistoryService = pipelineHistoryService;
        this.uniqueIdService = uniqueIdService;
        this.pipelineRepository = pipelineRepository;
        this.bindBranchRepository = bindBranchRepository;
        this.microServiceRepository = microServiceRepository;
        this.masterInvoker = masterInvoker;
    }

    @Transactional
    public boolean updatePipeline(String service, String pipelineId, PipelineBO pipelineBO) {
        Assert.notEmpty(service, "service can not be empty");
        PipelineBO oldPipeline = getPipelineDetail(pipelineId);
        if (Objects.isNull(oldPipeline)) {
            throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
        }

        boolean result = pipelineRepository.updatePipeline(pipelineBO);
        if (!result) {
            throw new ApiException(ErrorCode.UPDATE_PIPELINE_ERROR);
        }

        List<PipelineStageBO> stageList = pipelineBO.getStageList();
        List<PipelineStageBO> temp = JSON.parseArray(JSON.toJSONString(stageList), PipelineStageBO.class);
        addOrUpdateNode(pipelineId, stageList);

        //删除不存在的节点
        deleteNotExistStageAndNodes(oldPipeline, temp);
        return true;
    }

    private void deleteNotExistStageAndNodes(PipelineBO oldPipeline, List<PipelineStageBO> stageList) {
        if (CollectionUtils.isEmpty(stageList)) {
           return;
        }
        List<String> nodeIds = stageList.stream().map(PipelineStageBO::getNodes)
                .flatMap(Collection::stream).filter(Objects::nonNull).map(PipelineNodeBO::getNodeId)
                .collect(Collectors.toList());

        List<String> notExistNodes = oldPipeline.getStageList().stream().map(PipelineStageBO::getNodes)
                .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
                .map(PipelineNodeBO::getNodeId).filter(nodeId -> !nodeIds.contains(nodeId))
                .collect(Collectors.toList());

        //如果node节点未变更则直接退出
        if (CollectionUtils.isNotEmpty(notExistNodes)) {
            pipelineNodeService.deleteNodeIds(notExistNodes);
        }

        List<String> newStageIds = stageList.stream().map(PipelineStageBO::getStageId)
                .collect(Collectors.toList());
        List<String> notExistStages = oldPipeline.getStageList().stream()
                .map(PipelineStageBO::getStageId).filter(stageId -> !newStageIds.contains(stageId))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notExistStages)) {
            pipelineStageService.deletePipelineStages(notExistStages);
        }
    }

    private void addOrUpdateNode(String pipelineId, List<PipelineStageBO> stageList) {
        if (CollectionUtils.isEmpty(stageList)) {
            return;
        }

        AtomicInteger sortOrder = new AtomicInteger(0);
        stageList.forEach(stageDto -> {
            PipelineStageBO stage = pipelineStageService.getPipelineStage(stageDto.getStageId());
            if (Objects.isNull(stage)) {
                createNewStage(pipelineId, stageDto, sortOrder);
                return;
            }

            //修改stage节点
            stageDto.setSortOrder(sortOrder.incrementAndGet());
            pipelineStageService.updateStage(stageDto);

            //修改node节点
            List<PipelineNodeBO> stageDtoNodes = stageDto.getNodes();
            if (CollectionUtils.isNotEmpty(stageDtoNodes)) {
                stageDtoNodes.forEach(dto -> {
                    dto.setSortOrder(sortOrder.incrementAndGet());
                    pipelineNodeService.updateNode(dto);
                });
            }
        });
    }


    public List<PipelineBO> listPipelines(String serviceId) {
        return pipelineRepository.listPipelines(serviceId);
    }

    @Transactional
    public Boolean deletePipeline(String service, String pipelineId) {
        try {
            pipelineStageService.deleteStagesByPipelineId(pipelineId);
            pipelineNodeService.deleteByPipeline(pipelineId);
            return pipelineRepository.deletePipeline(pipelineId);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.DELETE_PIPELINE_ERROR);
        }
    }

    @Transactional
    public String createPipeline(PipelineBO pipelineBO) {
        String serviceId = pipelineBO.getServiceId();
        MicroserviceBO serviceDetail = microServiceRepository.queryServiceDetail(serviceId);
        if (Objects.isNull(serviceDetail)) {
            log.info("can not find service ={}", serviceId);
            throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
        }

        checkPublishPipelineExist(pipelineBO);

        String pipelineId = uniqueIdService.getUniqueId();
        pipelineBO.setPipelineId(pipelineId);
        pipelineBO.setPipelineStatus(PipelineStatus.NORMAL.getType());
        boolean result = pipelineRepository.createPipeline(pipelineBO);
        if (!result) {
            throw new ApiException(ErrorCode.CREATE_PIPELINE);
        }

        AtomicInteger atomicInteger = new AtomicInteger(0);
        int sum = pipelineBO.getStageList().stream()
                .mapToInt(stageDto -> createNewStage(pipelineId, stageDto, atomicInteger)).sum();
        log.info("log pipeline create stage count={} pipelineId={}", sum, pipelineId);

        if (Objects.equals(pipelineBO.getPipelineType(), PipelineType.PUBLISH.getType())) {
            boolean saveGitBranch = createMasterBranchBind(pipelineId, serviceDetail);
            log.info("save bind git master branch result={}", saveGitBranch);
        }
        return pipelineId;
    }

    private boolean createMasterBranchBind(String pipelineId, MicroserviceBO serviceDetail) {
        BindBranchBO bindBranchBO = new BindBranchBO();
        bindBranchBO.setBindId(uniqueIdService.getUniqueId());
        bindBranchBO.setPipelineId(pipelineId);
        bindBranchBO.setGitBranch("master");
        bindBranchBO.setGitUrl(serviceDetail.getGitUrl());
        bindBranchBO.setIsChoose(true);
        return bindBranchRepository.saveGitBranch(bindBranchBO);
    }

    private void checkPublishPipelineExist(PipelineBO pipeline) {
        if (!Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType())) {
            return;
        }
        PipelineBO publishPipeline = pipelineRepository.getPublishPipeline(pipeline.getServiceId());
        if (Objects.nonNull(publishPipeline)) {
            throw new ApiException(ErrorCode.PUBLISH_PIPELINE_EXIST);
        }
    }

    private Integer createNewStage(String pipelineId, PipelineStageBO stageDto,
                                   AtomicInteger atomicOrder) {
        String stageId = uniqueIdService.getUniqueId();
        PipelineStageBO pipelineStage = new PipelineStageBO();
        pipelineStage.setPipelineId(pipelineId);
        pipelineStage.setStageName(stageDto.getStageName());
        pipelineStage.setStageId(stageId);
        pipelineStage.setConfigId(stageDto.getConfigId());
        pipelineStage.setType(stageDto.getType());
        pipelineStage.setSortOrder(atomicOrder.incrementAndGet());
        pipelineStageService.saveStage(pipelineStage);

        stageDto.getNodes().forEach(nodeDto -> {
            PipelineNodeBO pipelineNode = new PipelineNodeBO();
            pipelineNode.setNodeId(uniqueIdService.getUniqueId());
            pipelineNode.setPipelineId(pipelineId);
            pipelineNode.setStageId(stageId);
            pipelineNode.setType(nodeDto.getType());
            pipelineNode.setNodeName(nodeDto.getNodeName());
            pipelineNode.setConfigDetail(nodeDto.getConfigDetail());
            pipelineNode.setSortOrder(atomicOrder.incrementAndGet());
            pipelineNodeService.saveNode(pipelineNode);
        });
        return atomicOrder.get();
    }

    public PipelineBO getPipeline(String pipelineId) {
        return pipelineRepository.getPipeline(pipelineId);
    }

    public String execute(String pipelineId) {
        PipelineBO pipeline = getPipeline(pipelineId);
        if (Objects.isNull(pipeline)) {
            return null;
        }

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setSourceId(pipelineId);
        dispatchTaskModel.setSourceName(pipeline.getPipelineName());
        dispatchTaskModel.setType(LogType.PIPELINE.getType());
        return masterInvoker.startPipelineTask(dispatchTaskModel);
    }

    public PipelineBO getPipelineDetail(String pipelineId) {
        PipelineBO pipeline = getPipeline(pipelineId);
        if (Objects.isNull(pipeline)) {
            throw new ApiException(ErrorCode.NOT_FIND_PIPELINE);
        }

        List<PipelineNodeBO> pipelineNodes = pipelineNodeService.getPipelineNodes(pipelineId);
        Map<String, List<PipelineNodeBO>> stageNodeMap = pipelineNodes.stream()
                .collect(Collectors.groupingBy(PipelineNodeBO::getStageId));

        List<PipelineStageBO> pipelineStages = pipelineStageService.sortPipelineNodes(pipelineId);
        List<PipelineStageBO> stageDTOList =
                pipelineStages.stream().peek(stage -> stage.setNodes(stageNodeMap.get(stage.getStageId())))
                        .collect(Collectors.toList());

        pipeline.setStageList(stageDTOList);
        return pipeline;
    }

    public Boolean pause(String historyId) {
        PipelineHistoryBO pipelineHistory = pipelineHistoryService.getPipelineHistory(historyId);
        if (Objects.isNull(pipelineHistory)) {
            return false;
        }

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setSourceId(historyId);
        dispatchTaskModel.setType(LogType.PIPELINE.getType());
        return masterInvoker.stopDispatchTask(dispatchTaskModel);
    }

    public List<PipelineBO> getServicePipelines(String serviceId) {
        return pipelineRepository.getServicePipelines(serviceId);
    }
}
