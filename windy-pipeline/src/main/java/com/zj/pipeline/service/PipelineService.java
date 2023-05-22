package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.DispatchModel;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.entity.dto.pipeline.PipelineStageDto;
import com.zj.domain.entity.po.pipeline.Pipeline;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.pipeline.entity.enums.PipelineStatus;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Slf4j
@Service
public class PipelineService {

  public static final String WINDY_MASTER_DISPATCH_URL = "http://WindyMaster/v1/devops/dispatch/task";
  public static final String WINDY_MASTER_STOP_URL = "http://WindyMaster/v1/devops/dispatch/stop";
  @Autowired
  private PipelineNodeService pipelineNodeService;

  @Autowired
  private PipelineStageService pipelineStageService;

  @Autowired
  private PipelineActionService pipelineActionService;

  @Autowired
  private NodeRecordService nodeRecordService;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private IPipelineRepository pipelineRepository;

  @Autowired
  private RestTemplate restTemplate;

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
    List<PipelineStageDto> temp = JSON.parseArray(JSON.toJSONString(stageList),
        PipelineStageDto.class);
    addOrUpdateNode(pipelineId, stageList);

    //删除不存在的节点
    deleteNotExistStageAndNodes(oldPipeline, temp);
    return true;
  }

  private void deleteNotExistStageAndNodes(PipelineDto oldPipeline,
      List<PipelineStageDto> stageList) {
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

    stageList.forEach(stageDto -> {
      PipelineStageDto stage = pipelineStageService.getPipelineStage(stageDto.getStageId());
      if (Objects.isNull(stage)) {
        createNewStage(pipelineId, stageDto);
        return;
      }

      //修改stage节点
      pipelineStageService.updateStage(stageDto);

      //修改node节点
      List<PipelineNodeDto> stageDtoNodes = stageDto.getNodes();
      if (CollectionUtils.isNotEmpty(stageDtoNodes)) {
        stageDtoNodes.forEach(dto -> pipelineNodeService.updateNode(dto));
      }
    });
  }


  public List<PipelineDto> listPipelines(String serviceId) {
    return pipelineRepository.listPipelines(serviceId);
  }

  @Transactional
  public Boolean deletePipeline(String service, String pipelineId) {
    pipelineStageService.deleteStagesByPipelineId(pipelineId);
    pipelineNodeService.deleteByPipeline(pipelineId);
    return pipelineRepository.deletePipeline(pipelineId);
  }

  @Transactional
  public String createPipeline(PipelineDto pipelineDTO) {
    if (Objects.isNull(pipelineDTO)) {
      return "";
    }

    String pipelineId = uniqueIdService.getUniqueId();
    pipelineDTO.setPipelineId(pipelineId);
    pipelineDTO.setPipelineStatus(PipelineStatus.NORMAL.getType());
    boolean result = pipelineRepository.createPipeline(pipelineDTO);
    if (!result) {
      throw new ApiException(ErrorCode.CREATE_PIPELINE);
    }

    pipelineDTO.getStageList().forEach(stageDto -> createNewStage(pipelineId, stageDto));
    return pipelineId;
  }

  private void createNewStage(String pipelineId, PipelineStageDto stageDto) {

    String stageId = uniqueIdService.getUniqueId();
    PipelineStageDto pipelineStage = new PipelineStageDto();
    pipelineStage.setPipelineId(pipelineId);
    pipelineStage.setStageName(stageDto.getStageName());
    pipelineStage.setStageId(stageId);
    pipelineStage.setConfigId(stageDto.getConfigId());
    pipelineStage.setType(stageDto.getType());
    pipelineStageService.saveStage(pipelineStage);

    stageDto.getNodes().forEach(nodeDto -> {
      PipelineNodeDto pipelineNode = new PipelineNodeDto();
      pipelineNode.setNodeId(uniqueIdService.getUniqueId());
      pipelineNode.setPipelineId(pipelineId);
      pipelineNode.setStageId(stageId);
      pipelineNode.setType(nodeDto.getType());
      pipelineNode.setNodeName(nodeDto.getNodeName());
      pipelineNode.setConfigDetail(nodeDto.getConfigDetail());
      pipelineNodeService.saveNode(pipelineNode);
    });
  }

  public PipelineDto getPipeline(String pipelineId) {
    return pipelineRepository.getPipeline(pipelineId);
  }

  public Boolean execute(String pipelineId) {
    PipelineDto pipeline = getPipeline(pipelineId);
    if (Objects.isNull(pipeline)) {
      return false;
    }

    DispatchModel dispatchModel = new DispatchModel();
    dispatchModel.setSourceId(pipelineId);
    dispatchModel.setSourceName(pipeline.getPipelineName());
    dispatchModel.setType(LogType.PIPELINE.getType());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<DispatchModel> httpEntity = new HttpEntity<>(dispatchModel, headers);
    try {
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(WINDY_MASTER_DISPATCH_URL,
          httpEntity, String.class);
      log.info("get test result code= {} result={}", responseEntity.getStatusCode(),
          responseEntity.getBody());
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      log.error("request dispatch pipeline task error", e);
    }

    return false;
  }

  public PipelineDto getPipelineDetail(String pipelineId) {
    List<PipelineNodeDto> pipelineNodes = pipelineNodeService.getPipelineNodes(pipelineId);
    Map<String, List<PipelineNodeDto>> stageNodeMap = pipelineNodes.stream()
        .collect(Collectors.groupingBy(PipelineNodeDto::getStageId));

    List<PipelineStageDto> pipelineStages = pipelineStageService.sortPipelineNodes(pipelineId);
    List<PipelineStageDto> stageDTOList = pipelineStages.stream().peek(stage -> {
      stage.setNodes(stageNodeMap.get(stage.getStageId()));
    }).collect(Collectors.toList());

    PipelineDto pipeline = getPipeline(pipelineId);
    pipeline.setStageList(stageDTOList);
    return pipeline;
  }

  public Boolean pause(String historyId) {
    Optional<NodeRecordDto> optional = nodeRecordService.getNodeRecordsByHistory(historyId).stream()
        .filter(record -> Objects.equals(ProcessStatus.RUNNING.getType(), record.getStatus()))
        .findAny();
    if (!optional.isPresent()) {
      return false;
    }

    DispatchModel dispatchModel = new DispatchModel();
    dispatchModel.setSourceId(historyId);
    dispatchModel.setType(LogType.PIPELINE.getType());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<DispatchModel> httpEntity = new HttpEntity<>(dispatchModel, headers);
    try {
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(WINDY_MASTER_STOP_URL,
          httpEntity, String.class);
      log.info("get test result code= {} result={}", responseEntity.getStatusCode(),
          responseEntity.getBody());
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      log.error("request dispatch pipeline task error", e);
    }
    return true;
  }

  public List<PipelineDto> getServicePipelines(String serviceId) {
    return pipelineRepository.getServicePipelines(serviceId);
  }
}
