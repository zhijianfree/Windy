package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.zj.common.enums.LogType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.monitor.invoker.IMasterInvoker;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.entity.dto.pipeline.PipelineStageDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
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
    public boolean updatePipeline(String service, String pipelineId, PipelineDto pipelineDTO) {
        Assert.notEmpty(service, "service can not be empty");
        PipelineDto oldPipeline = getPipelineDetail(pipelineId);
        if (Objects.isNull(oldPipeline)) {
            throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
        }

        boolean result = pipelineRepository.updatePipeline(pipelineDTO);
        if (!result) {
            throw new ApiException(ErrorCode.UPDATE_PIPELINE_ERROR);
        }

        List<PipelineStageDto> stageList = pipelineDTO.getStageList();
        List<PipelineStageDto> temp = JSON.parseArray(JSON.toJSONString(stageList), PipelineStageDto.class);
        addOrUpdateNode(pipelineId, stageList);

        //删除不存在的节点
        deleteNotExistStageAndNodes(oldPipeline, temp);
        return true;
    }

    private void deleteNotExistStageAndNodes(PipelineDto oldPipeline, List<PipelineStageDto> stageList) {
        if (CollectionUtils.isEmpty(stageList)) {
           return;
        }
        List<String> nodeIds = stageList.stream().map(PipelineStageDto::getNodes)
                .flatMap(Collection::stream).filter(Objects::nonNull).map(PipelineNodeDto::getNodeId)
                .collect(Collectors.toList());

        List<String> notExistNodes = oldPipeline.getStageList().stream().map(PipelineStageDto::getNodes)
                .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
                .map(PipelineNodeDto::getNodeId).filter(nodeId -> !nodeIds.contains(nodeId))
                .collect(Collectors.toList());

        //如果node节点未变更则直接退出
        if (CollectionUtils.isNotEmpty(notExistNodes)) {
            pipelineNodeService.deleteNodeIds(notExistNodes);
        }

        List<String> newStageIds = stageList.stream().map(PipelineStageDto::getStageId)
                .collect(Collectors.toList());
        List<String> notExistStages = oldPipeline.getStageList().stream()
                .map(PipelineStageDto::getStageId).filter(stageId -> !newStageIds.contains(stageId))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notExistStages)) {
            pipelineStageService.deletePipelineStages(notExistStages);
        }
    }

    private void addOrUpdateNode(String pipelineId, List<PipelineStageDto> stageList) {
        if (CollectionUtils.isEmpty(stageList)) {
            return;
        }

        AtomicInteger sortOrder = new AtomicInteger(0);
        stageList.forEach(stageDto -> {
            PipelineStageDto stage = pipelineStageService.getPipelineStage(stageDto.getStageId());
            if (Objects.isNull(stage)) {
                createNewStage(pipelineId, stageDto, sortOrder);
                return;
            }

            //修改stage节点
            stageDto.setSortOrder(sortOrder.incrementAndGet());
            pipelineStageService.updateStage(stageDto);

            //修改node节点
            List<PipelineNodeDto> stageDtoNodes = stageDto.getNodes();
            if (CollectionUtils.isNotEmpty(stageDtoNodes)) {
                stageDtoNodes.forEach(dto -> {
                    dto.setSortOrder(sortOrder.incrementAndGet());
                    pipelineNodeService.updateNode(dto);
                });
            }
        });
    }


    public List<PipelineDto> listPipelines(String serviceId) {
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
    public String createPipeline(PipelineDto pipelineDTO) {
        String serviceId = pipelineDTO.getServiceId();
        MicroserviceDto serviceDetail = microServiceRepository.queryServiceDetail(serviceId);
        if (Objects.isNull(serviceDetail)) {
            log.info("can not find service ={}", serviceId);
            throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
        }

        checkPublishPipelineExist(pipelineDTO);

        String pipelineId = uniqueIdService.getUniqueId();
        pipelineDTO.setPipelineId(pipelineId);
        pipelineDTO.setPipelineStatus(PipelineStatus.NORMAL.getType());
        boolean result = pipelineRepository.createPipeline(pipelineDTO);
        if (!result) {
            throw new ApiException(ErrorCode.CREATE_PIPELINE);
        }

        AtomicInteger atomicInteger = new AtomicInteger(0);
        int sum = pipelineDTO.getStageList().stream()
                .mapToInt(stageDto -> createNewStage(pipelineId, stageDto, atomicInteger)).sum();
        log.info("log pipeline create stage count={} pipelineId={}", sum, pipelineId);

        if (Objects.equals(pipelineDTO.getPipelineType(), PipelineType.PUBLISH.getType())) {
            boolean saveGitBranch = createMasterBranchBind(pipelineId, serviceDetail);
            log.info("save bind git master branch result={}", saveGitBranch);
        }
        return pipelineId;
    }

    private boolean createMasterBranchBind(String pipelineId, MicroserviceDto serviceDetail) {
        BindBranchDto bindBranchDto = new BindBranchDto();
        bindBranchDto.setBindId(uniqueIdService.getUniqueId());
        bindBranchDto.setPipelineId(pipelineId);
        bindBranchDto.setGitBranch("master");
        bindBranchDto.setGitUrl(serviceDetail.getGitUrl());
        bindBranchDto.setIsChoose(true);
        return bindBranchRepository.saveGitBranch(bindBranchDto);
    }

    private void checkPublishPipelineExist(PipelineDto pipeline) {
        if (!Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType())) {
            return;
        }
        PipelineDto publishPipeline = pipelineRepository.getPublishPipeline(pipeline.getServiceId());
        if (Objects.nonNull(publishPipeline)) {
            throw new ApiException(ErrorCode.PUBLISH_PIPELINE_EXIST);
        }
    }

    private Integer createNewStage(String pipelineId, PipelineStageDto stageDto,
                                   AtomicInteger atomicOrder) {
        String stageId = uniqueIdService.getUniqueId();
        PipelineStageDto pipelineStage = new PipelineStageDto();
        pipelineStage.setPipelineId(pipelineId);
        pipelineStage.setStageName(stageDto.getStageName());
        pipelineStage.setStageId(stageId);
        pipelineStage.setConfigId(stageDto.getConfigId());
        pipelineStage.setType(stageDto.getType());
        pipelineStage.setSortOrder(atomicOrder.incrementAndGet());
        pipelineStageService.saveStage(pipelineStage);

        stageDto.getNodes().forEach(nodeDto -> {
            PipelineNodeDto pipelineNode = new PipelineNodeDto();
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

    public PipelineDto getPipeline(String pipelineId) {
        return pipelineRepository.getPipeline(pipelineId);
    }

    public String execute(String pipelineId) {
        PipelineDto pipeline = getPipeline(pipelineId);
        if (Objects.isNull(pipeline)) {
            return null;
        }

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setSourceId(pipelineId);
        dispatchTaskModel.setSourceName(pipeline.getPipelineName());
        dispatchTaskModel.setType(LogType.PIPELINE.getType());
        return masterInvoker.runPipelineTask(dispatchTaskModel);
    }

    public PipelineDto getPipelineDetail(String pipelineId) {
        PipelineDto pipeline = getPipeline(pipelineId);
        if (Objects.isNull(pipeline)) {
            throw new ApiException(ErrorCode.NOT_FIND_PIPELINE);
        }

        List<PipelineNodeDto> pipelineNodes = pipelineNodeService.getPipelineNodes(pipelineId);
        Map<String, List<PipelineNodeDto>> stageNodeMap = pipelineNodes.stream()
                .collect(Collectors.groupingBy(PipelineNodeDto::getStageId));

        List<PipelineStageDto> pipelineStages = pipelineStageService.sortPipelineNodes(pipelineId);
        List<PipelineStageDto> stageDTOList =
                pipelineStages.stream().peek(stage -> stage.setNodes(stageNodeMap.get(stage.getStageId())))
                        .collect(Collectors.toList());

        pipeline.setStageList(stageDTOList);
        return pipeline;
    }

    public Boolean pause(String historyId) {
        PipelineHistoryDto pipelineHistory = pipelineHistoryService.getPipelineHistory(historyId);
        if (Objects.isNull(pipelineHistory)) {
            return false;
        }

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setSourceId(historyId);
        dispatchTaskModel.setType(LogType.PIPELINE.getType());
        return masterInvoker.stopDispatchTask(dispatchTaskModel);
    }

    public List<PipelineDto> getServicePipelines(String serviceId) {
        return pipelineRepository.getServicePipelines(serviceId);
    }
}
