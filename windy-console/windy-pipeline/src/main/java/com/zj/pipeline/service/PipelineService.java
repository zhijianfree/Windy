package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import com.zj.domain.entity.bo.pipeline.PipelineStageBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.service.impl.MicroServiceRepository;
import com.zj.pipeline.entity.dto.PipelineDto;
import com.zj.pipeline.entity.dto.PipelineStatusDto;
import com.zj.pipeline.entity.enums.PipelineStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final INodeRecordRepository nodeRecordRepository;
    private final IMasterInvoker masterInvoker;

    public PipelineService(PipelineNodeService pipelineNodeService,
                           PipelineStageService pipelineStageService, PipelineHistoryService pipelineHistoryService,
                           UniqueIdService uniqueIdService, IPipelineRepository pipelineRepository,
                           IBindBranchRepository bindBranchRepository, MicroServiceRepository microServiceRepository,
                           INodeRecordRepository nodeRecordRepository, IMasterInvoker masterInvoker) {
        this.pipelineNodeService = pipelineNodeService;
        this.pipelineStageService = pipelineStageService;
        this.pipelineHistoryService = pipelineHistoryService;
        this.uniqueIdService = uniqueIdService;
        this.pipelineRepository = pipelineRepository;
        this.bindBranchRepository = bindBranchRepository;
        this.microServiceRepository = microServiceRepository;
        this.nodeRecordRepository = nodeRecordRepository;
        this.masterInvoker = masterInvoker;
    }

    @Transactional
    public boolean updatePipeline(String service, String pipelineId, PipelineDto pipelineDto) {
        Assert.notEmpty(service, "service can not be empty");
        PipelineBO oldPipeline = getPipelineDetail(pipelineId);
        if (Objects.isNull(oldPipeline)) {
            throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
        }

        PipelineBO pipelineBO = OrikaUtil.convert(pipelineDto, PipelineBO.class);
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
            boolean deleteNodeIds = pipelineNodeService.deleteNodeIds(notExistNodes);
            log.info("delete node result={}", deleteNodeIds);
        }

        List<String> newStageIds = stageList.stream().map(PipelineStageBO::getStageId)
                .collect(Collectors.toList());
        List<String> notExistStages = oldPipeline.getStageList().stream()
                .map(PipelineStageBO::getStageId).filter(stageId -> !newStageIds.contains(stageId))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notExistStages)) {
            boolean deletePipelineStages = pipelineStageService.deletePipelineStages(notExistStages);
            log.info("delete stage result={}", deletePipelineStages);
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
            boolean updateStage = pipelineStageService.updateStage(stageDto);
            log.info("update stage result={}", updateStage);

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
        return pipelineRepository.getServicePipelines(serviceId);
    }

    @Transactional
    public Boolean deletePipeline(String serviceId, String pipelineId) {
        try {
            checkPipelineAndService(serviceId, pipelineId);
            boolean deleteHistory = pipelineHistoryService.deleteByPipelineId(pipelineId);
            List<String> nodeIds = pipelineNodeService.getPipelineNodes(pipelineId).stream().map(PipelineNodeBO::getNodeId).collect(Collectors.toList());
            boolean deleteRecord = nodeRecordRepository.deleteRecordByNodeId(nodeIds);
            boolean deleteBind = bindBranchRepository.deleteByPipelineId(pipelineId);
            boolean deleteStage = pipelineStageService.deleteStagesByPipelineId(pipelineId);
            boolean deleteNode = pipelineNodeService.deleteByPipeline(pipelineId);
            log.info("delete stage and node result stage = {} node={} bind={} deleteHistory={} deleteRecord={}",
                    deleteStage, deleteNode, deleteBind, deleteHistory, deleteRecord);
            return pipelineRepository.deletePipeline(pipelineId);
        } catch (Exception e) {
            log.info("delete pipeline error serviceId={} pipelineId={}", serviceId, pipelineId, e);
            throw new ApiException(ErrorCode.DELETE_PIPELINE_ERROR);
        }
    }

    private void checkPipelineAndService(String serviceId, String pipelineId) {
        checkService(serviceId);

        PipelineBO pipeline = pipelineRepository.getPipeline(pipelineId);
        if (Objects.isNull(pipeline)) {
            log.info("can not find pipeline = {}", pipelineId);
            throw new ApiException(ErrorCode.PIPELINE_NOT_BIND);
        }

        if (!Objects.equals(pipeline.getServiceId(), serviceId)) {
            log.info("pipeline not belong service pipelineId={} serviceId={}", pipelineId, serviceId);
            throw new ApiException(ErrorCode.PIPELINE_NOT_BELONG_SERVICE);
        }
    }

    @Transactional
    public String createPipeline(PipelineDto pipelineDto) {
        PipelineBO pipelineBO = OrikaUtil.convert(pipelineDto, PipelineBO.class);
        String serviceId = pipelineBO.getServiceId();
        MicroserviceBO serviceDetail = checkService(serviceId);

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

    private MicroserviceBO checkService(String serviceId) {
        MicroserviceBO serviceDetail = microServiceRepository.queryServiceDetail(serviceId);
        if (Objects.isNull(serviceDetail)) {
            log.info("can not find service ={}", serviceId);
            throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
        }
        return serviceDetail;
    }

    private boolean createMasterBranchBind(String pipelineId, MicroserviceBO serviceDetail) {
        BindBranchBO bindBranchBO = new BindBranchBO();
        bindBranchBO.setBindId(uniqueIdService.getUniqueId());
        bindBranchBO.setPipelineId(pipelineId);
        bindBranchBO.setGitBranch(serviceDetail.getServiceConfig().getServiceContext().getMainBranch());
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
            log.info("can not find pipeline pipelineId={}", pipelineId);
            throw new ApiException(ErrorCode.NOT_FIND_PIPELINE);
        }

        PipelineHistoryBO latestHistory = pipelineHistoryService.getLatestPipelineHistory(pipeline.getServiceId(), pipeline.getPipelineId());
        if (Objects.nonNull(latestHistory) && Objects.equals(latestHistory.getPipelineStatus(),
                ProcessStatus.RUNNING.getType())) {
            log.info("pipeline is running , can not run again={}", pipeline.getPipelineId());
            throw new ApiException(ErrorCode.PIPELINE_IS_RUNNING);
        }

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setSourceId(pipelineId);
        dispatchTaskModel.setSourceName(pipeline.getPipelineName());
        dispatchTaskModel.setType(LogType.PIPELINE.getType());
        String taskId = masterInvoker.startPipelineTask(dispatchTaskModel);
        if (StringUtils.isBlank(taskId)) {
            log.info("execute pipeline error pipelineId={}", pipelineId);
            throw new ApiException(ErrorCode.RUN_PIPELINE_ERROR);
        }
        return taskId;
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

    public List<PipelineStatusDto> getServicePipelineStatus(String serviceId) {
        List<PipelineBO> servicePipelines = pipelineRepository.getServicePipelines(serviceId);
        return servicePipelines.stream().filter(pipelineBO -> Objects.equals(pipelineBO.getPipelineType(),
                PipelineType.CUSTOM.getType())).map(pipelineBO -> {
            PipelineHistoryBO latestPipelineHistory = pipelineHistoryService.getLatestPipelineHistory(serviceId,
                    pipelineBO.getPipelineId());
            PipelineStatusDto pipelineStatusDto = new PipelineStatusDto();
            pipelineStatusDto.setPipelineId(pipelineBO.getPipelineId());
            pipelineStatusDto.setPipelineName(pipelineBO.getPipelineName());
            pipelineStatusDto.setPipelineType(pipelineBO.getPipelineType());
            Optional.ofNullable(latestPipelineHistory).ifPresent(history ->
                    pipelineStatusDto.setStatus(history.getPipelineStatus()));
            return pipelineStatusDto;
        }).collect(Collectors.toList());
    }

    public PipelineStatusDto getPipelineStatus(String pipelineId) {
        PipelineBO pipelineBO = pipelineRepository.getPipeline(pipelineId);
        PipelineStatusDto pipelineStatusDto = new PipelineStatusDto();
        pipelineStatusDto.setPipelineId(pipelineBO.getPipelineId());
        pipelineStatusDto.setPipelineName(pipelineBO.getPipelineName());
        pipelineStatusDto.setPipelineType(pipelineBO.getPipelineType());
        PipelineHistoryBO latestPipelineHistory = pipelineHistoryService.getLatestPipelineHistory(pipelineBO.getServiceId(),
                pipelineBO.getPipelineId());
        Optional.ofNullable(latestPipelineHistory).ifPresent(history ->
                pipelineStatusDto.setStatus(history.getPipelineStatus()));
        return pipelineStatusDto;
    }
}
