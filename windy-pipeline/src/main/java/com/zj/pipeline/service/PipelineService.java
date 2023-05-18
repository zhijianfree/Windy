package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.DispatchModel;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.entity.dto.pipeline.PipelineStageDTO;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.entity.po.pipeline.Pipeline;
import com.zj.domain.entity.po.pipeline.PipelineNode;
import com.zj.domain.entity.po.pipeline.PipelineStage;
import com.zj.domain.mapper.pipeline.PipelineMapper;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.pipeline.entity.enums.PipelineStatus;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class PipelineService extends ServiceImpl<PipelineMapper, Pipeline> {

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
  public boolean updatePipeline(String service, String pipelineId, PipelineDTO pipelineDTO) {
    Assert.notEmpty(service, "service can not be empty");
    PipelineDTO oldPipeline = getPipelineDetail(pipelineId);
    if (Objects.isNull(oldPipeline)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }

    boolean result = pipelineRepository.updatePipeline(pipelineDTO);
    if (!result) {
      throw new ApiException(ErrorCode.UPDATE_PIPELINE_ERROR);
    }

    List<PipelineStageDTO> stageList = pipelineDTO.getStageList();
    List<PipelineStageDTO> temp = JSON.parseArray(JSON.toJSONString(stageList),
        PipelineStageDTO.class);
    addOrUpdateNode(pipelineId, stageList);

    //删除不存在的节点
    deleteNotExistStageAndNodes(oldPipeline, temp);
    return true;
  }

  private void deleteNotExistStageAndNodes(PipelineDTO oldPipeline,
      List<PipelineStageDTO> stageList) {
    List<String> nodeIds = stageList.stream().map(PipelineStageDTO::getNodes)
        .flatMap(Collection::stream).filter(Objects::nonNull).map(PipelineNodeDto::getNodeId)
        .collect(Collectors.toList());

    List<String> notExistNodes = oldPipeline.getStageList().stream().map(PipelineStageDTO::getNodes)
        .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
        .map(PipelineNodeDto::getNodeId).filter(nodeId -> !nodeIds.contains(nodeId))
        .collect(Collectors.toList());

    //如果node节点未变更则直接退出
    if (CollectionUtils.isNotEmpty(notExistNodes)) {
      pipelineNodeService.deleteNodeIds(notExistNodes);
    }

    List<String> newStageIds = stageList.stream().map(PipelineStageDTO::getStageId)
        .collect(Collectors.toList());
    List<String> notExistStages = oldPipeline.getStageList().stream()
        .map(PipelineStageDTO::getStageId).filter(stageId -> !newStageIds.contains(stageId))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(notExistStages)) {
      pipelineStageService.remove(
          Wrappers.lambdaQuery(PipelineStage.class).in(PipelineStage::getStageId, notExistStages));
    }
  }

  private void addOrUpdateNode(String pipelineId, List<PipelineStageDTO> stageList) {
    if (CollectionUtils.isEmpty(stageList)) {
      return;
    }

    stageList.forEach(stageDto -> {
      PipelineStage stage = pipelineStageService.getOne(Wrappers.lambdaQuery(PipelineStage.class)
          .eq(PipelineStage::getStageId, stageDto.getStageId()));
      if (Objects.isNull(stage)) {
        createNewStage(pipelineId, stageDto);
        return;
      }

      //修改stage节点
      Long currentTime = System.currentTimeMillis();
      PipelineStage pipelineStage = OrikaUtil.convert(stageDto, PipelineStage.class);
      pipelineStage.setUpdateTime(currentTime);
      pipelineStageService.update(pipelineStage, Wrappers.lambdaUpdate(PipelineStage.class)
          .eq(PipelineStage::getStageId, pipelineStage.getStageId()));

      //修改node节点
      List<PipelineNodeDto> stageDtoNodes = stageDto.getNodes();
      if (CollectionUtils.isNotEmpty(stageDtoNodes)) {
        stageDtoNodes.forEach(dto -> pipelineNodeService.updateNode(dto));
      }
    });
  }


  public List<PipelineDTO> listPipelines(String serviceId) {
    return pipelineRepository.listPipelines(serviceId);
  }

  @Transactional
  public Boolean deletePipeline(String service, String pipelineId) {
    pipelineStageService.remove(
        Wrappers.lambdaQuery(PipelineStage.class).eq(PipelineStage::getPipelineId, pipelineId));
    pipelineNodeService.deleteByPipeline(pipelineId);
    return pipelineRepository.deletePipeline(pipelineId);
  }

  @Transactional
  public String createPipeline(PipelineDTO pipelineDTO) {
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

  private void createNewStage(String pipelineId, PipelineStageDTO stageDto) {
    Long currentTime = System.currentTimeMillis();
    String stageId = uniqueIdService.getUniqueId();
    PipelineStage pipelineStage = new PipelineStage();
    pipelineStage.setPipelineId(pipelineId);
    pipelineStage.setStageName(stageDto.getStageName());
    pipelineStage.setStageId(stageId);
    pipelineStage.setConfigId(stageDto.getConfigId());
    pipelineStage.setType(stageDto.getType());
    pipelineStage.setCreateTime(currentTime);
    pipelineStage.setUpdateTime(currentTime);
    pipelineStageService.save(pipelineStage);

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

  public PipelineDTO getPipeline(String pipelineId) {
    return pipelineRepository.getPipeline(pipelineId);
  }

  public Boolean execute(String pipelineId) {
    PipelineDTO pipeline = getPipeline(pipelineId);
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

  public PipelineDTO getPipelineDetail(String pipelineId) {
    List<PipelineNodeDto> pipelineNodes = pipelineNodeService.getPipelineNodes(pipelineId);
    Map<String, List<PipelineNodeDto>> stageNodeMap = pipelineNodes.stream()
        .collect(Collectors.groupingBy(PipelineNodeDto::getStageId));

    List<PipelineStage> pipelineStages = pipelineStageService.list(
        Wrappers.lambdaQuery(PipelineStage.class).eq(PipelineStage::getPipelineId, pipelineId)
            .orderByAsc(PipelineStage::getType));
    List<PipelineStageDTO> stageDTOList = pipelineStages.stream().map(stage -> {
      PipelineStageDTO stageDTO = OrikaUtil.convert(stage, PipelineStageDTO.class);
      stageDTO.setNodes(stageNodeMap.get(stage.getStageId()));
      return stageDTO;
    }).collect(Collectors.toList());

    PipelineDTO pipeline = getPipeline(pipelineId);
    pipeline.setStageList(stageDTOList);
    return pipeline;
  }

  public Boolean pause(String historyId) {
    List<NodeRecord> records = nodeRecordService.list(
        Wrappers.lambdaQuery(NodeRecord.class).eq(NodeRecord::getHistoryId, historyId)
            .eq(NodeRecord::getStatus, ProcessStatus.RUNNING.getType()));
    if (CollectionUtils.isEmpty(records)) {
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
}
