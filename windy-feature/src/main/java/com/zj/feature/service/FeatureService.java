package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.enums.LogType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.DispatchModel;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.BatchDeleteDto;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TaskInfoDto;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.feature.entity.dto.CopyFeatureDto;
import com.zj.feature.entity.dto.ExecutePointVo;
import com.zj.feature.entity.dto.FeatureInfoVo;
import com.zj.feature.entity.dto.FeatureNodeDto;
import com.zj.common.model.PageSize;
import com.zj.feature.entity.dto.TagFilterDto;
import com.zj.domain.entity.dto.feature.TestCaseConfigDto;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class FeatureService {

  public static final String COPY_STRING = " copy";
  @Autowired
  private ExecutePointService executePointService;

  @Autowired
  private TestCaseService testCaseService;

  @Autowired
  private TestCaseConfigService testCaseConfigService;

  @Autowired
  private FeatureTagService featureTagService;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private IFeatureRepository featureRepository;

  @Autowired
  private RestTemplate restTemplate;

  public static final String WINDY_MASTER_DISPATCH_URL = "http://WindyMaster/v1/devops/dispatch/task";

  public List<FeatureNodeDto> getFeatureTreeList(String testCaseId) {
    List<FeatureInfoDto> featureList = featureRepository.queryFeatureList(testCaseId);
    FeatureNodeDto root = new FeatureNodeDto();
    root = convertTree(featureList, root);
    return root.getChildren();
  }

  /**
   * 递归转为tree结构
   */
  public FeatureNodeDto convertTree(List<FeatureInfoDto> featureList, FeatureNodeDto parent) {
    if (CollectionUtils.isEmpty(featureList)) {
      return parent;
    }

    List<FeatureNodeDto> list = featureList.stream()
        .filter(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()))
        .map(FeatureNodeDto::toNode)
        .collect(Collectors.toList());
    parent.setChildren(list);

    featureList.removeIf(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()));
    list.forEach(node -> {
      convertTree(featureList, node);
    });

    return parent;
  }

  public boolean updateByFeatureId(FeatureInfoDto featureInfo) {
    return featureRepository.deleteByFeatureId(featureInfo.getFeatureId());
  }

  public FeatureInfoVo getFeatureById(String featureId) {
    FeatureInfoDto featureInfo = featureRepository.getFeatureById(featureId);
    if (Objects.isNull(featureInfo)) {
      throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
    }
    FeatureInfoVo featureInfoDTO = OrikaUtil.convert(featureInfo, FeatureInfoVo.class);
    List<String> featureTags = featureTagService.getFeatureTags(featureId);
    featureInfoDTO.setTags(featureTags);
    return featureInfoDTO;
  }

  public PageSize<FeatureInfoDto> queryFeaturePage(String testCaseId, int page, int size) {
    IPage<FeatureInfoDto> featurePage = featureRepository.queryFeaturePage(testCaseId, page, size);
    PageSize<FeatureInfoDto> detailPageSize = new PageSize<>();
    detailPageSize.setTotal(featurePage.getTotal());
    detailPageSize.setData(featurePage.getRecords());
    return detailPageSize;
  }

  public List<FeatureInfoDto> queryFeatureList(String testCaseId) {
    return featureRepository.queryFeatureList(testCaseId);
  }

  public List<FeatureInfoDto> queryNotContainFolder(String testCaseId) {
    return featureRepository.queryNotContainFolder(testCaseId);
  }

  @Transactional
  public String createFeature(FeatureInfoVo featureInfoDTO) {
    FeatureInfoDto featureInfo = OrikaUtil.convert(featureInfoDTO, FeatureInfoDto.class);
    featureInfo.setFeatureId(uniqueIdService.getUniqueId());
    boolean result = featureRepository.createFeature(featureInfo);

    if (CollectionUtils.isNotEmpty(featureInfoDTO.getTags())) {
      featureTagService.batchAddTag(featureInfo.getFeatureId(), featureInfoDTO.getTags());
    }

    log.info("create feature detail result = {}", result);
    return featureInfo.getFeatureId();
  }

  @Transactional
  public String updateFeatureInfo(FeatureInfoVo featureInfoDTO) {
    if (Objects.isNull(getFeatureById(featureInfoDTO.getFeatureId()))) {
      throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
    }

    FeatureInfoDto featureInfo = OrikaUtil.convert(featureInfoDTO, FeatureInfoDto.class);
    boolean result = featureRepository.updateFeatureInfo(featureInfo);

    List<ExecutePointVo> executePoints = featureInfoDTO.getTestFeatures();
    if (CollectionUtils.isNotEmpty(executePoints)) {
      int featureResult = updateExecutePoint(executePoints);
      if (featureResult < 1) {
        throw new ApiException(ErrorCode.ERROR);
      }
    }

    if (Objects.nonNull(featureInfoDTO.getTags())) {
      featureTagService.batchUpdateTag(featureInfo.getFeatureId(), featureInfoDTO.getTags());
    }

    log.info("update test case feature result={}", result);
    return featureInfo.getFeatureId();
  }

  private int updateExecutePoint(List<ExecutePointVo> executePointVos) {
    int result = 0;
    if (CollectionUtils.isEmpty(executePointVos)) {
      return result;
    }

    return executePointVos.stream().mapToInt(executePointVo -> {
      if (Objects.isNull(executePointVo.getPointId())) {
        executePointVo.setPointId(uniqueIdService.getUniqueId());
        String pointId = executePointService.createExecutePoint(executePointVo);
        return StringUtils.isEmpty(pointId) ? 0 : 1;
      }

      String pointId = executePointService.updateExecutePoint(executePointVo);
      return StringUtils.isEmpty(pointId) ? 0 : 1;
    }).sum();
  }

  @Transactional
  public Boolean copyFeatures(CopyFeatureDto copyFeatureDTO) {
    TestCaseDto testCase = testCaseService.getTestCase(copyFeatureDTO.getTestCaseId());
    testCase.setTestCaseName(testCase.getTestCaseName() + COPY_STRING);
    String newCaseId = testCaseService.createTestCase(testCase);
    log.info("new caseId={} old case Id={}", newCaseId, copyFeatureDTO.getTestCaseId());

    List<FeatureInfoDto> featureList = featureRepository.queryFeatureList(
        copyFeatureDTO.getFeatureIds());
    log.info("get feature list ={}", JSON.toJSONString(featureList));

    //1 先复制用例
    Long currentTime = System.currentTimeMillis();
    Map<String, String> idRecordMap = new HashMap<>();
    List<FeatureInfoDto> newList = featureList.stream().peek(feature -> {
      String uniqueId = uniqueIdService.getUniqueId();
      idRecordMap.put(feature.getFeatureId(), uniqueId);
      feature.setFeatureId(uniqueId);
      feature.setTestCaseId(newCaseId);
      feature.setCreateTime(currentTime);
      feature.setUpdateTime(currentTime);
    }).collect(Collectors.toList());
    newList.forEach(feature -> {
      //将之前的父节点换成新的父节点Id
      String oldParentId = feature.getParentId();
      String newParentId = idRecordMap.get(oldParentId);
      feature.setParentId(newParentId);
    });
    featureRepository.saveBatch(newList);
    log.info("batch save features ={} idMap={}", JSON.toJSONString(newList), idRecordMap);

    //2 复制执行点
    List<String> featureIds = new ArrayList<>(idRecordMap.keySet());
    List<ExecutePointDto> executePointList = executePointService.getExecutePointByFeatureIds(
        featureIds);
    Map<String, List<ExecutePointDto>> pointGroupMap = executePointList.stream()
        .collect(Collectors.groupingBy(ExecutePointDto::getFeatureId, Collectors.toList()));
    log.info("get groupBy points={}", JSON.toJSONString(pointGroupMap));

    //将获取到的执行点根据featureId映射关系重新设置一次
    List<ExecutePointDto> newExecutePoints = pointGroupMap.keySet().stream().map(oldId -> {
      String newFeatureId = idRecordMap.get(oldId);
      pointGroupMap.get(oldId).forEach(executePoint -> {
        executePoint.setId(null);
        executePoint.setFeatureId(newFeatureId);
        executePoint.setCreateTime(currentTime);
        executePoint.setUpdateTime(currentTime);
      });
      return pointGroupMap.get(oldId);
    }).flatMap(Collection::stream).collect(Collectors.toList());
    executePointService.saveBatch(newExecutePoints);
    log.info("batch save points={}", JSON.toJSONString(newExecutePoints));

    //3 复制全局变量
    List<TestCaseConfigDto> caseConfigs = testCaseConfigService.getTestCaseConfigs(
        copyFeatureDTO.getTestCaseId());
    caseConfigs.forEach(configDTO -> configDTO.setUnionId(newCaseId));
    testCaseConfigService.addCaseConfigs(caseConfigs);
    log.info("copy global configs={}", JSON.toJSONString(caseConfigs));
    return true;
  }

  @Transactional
  public boolean deleteByFeatureId(String featureId) {
    List<FeatureInfoDto> subFeatures = featureRepository.getSubFeatures(featureId);
    if (!CollectionUtils.isEmpty(subFeatures)) {
      throw new ApiException(ErrorCode.SUB_FEATURE_EXIST);
    }
    executePointService.deleteByFeatureId(featureId);
    return featureRepository.deleteByFeatureId(featureId);
  }

  public Boolean batchDeleteByFeatureId(BatchDeleteDto batchDeleteDto) {
    return featureRepository.batchDeleteByFeatureId(batchDeleteDto);
  }

  public List<FeatureNodeDto> filterFeaturesByTag(TagFilterDto tagFilterDTO) {
    List<FeatureInfoDto> featuresByTag = getFeaturesByTag(tagFilterDTO);
    FeatureNodeDto featureNode = new FeatureNodeDto();
    featureNode = convertTree(featuresByTag, featureNode);
    return featureNode.getChildren();
  }

  public List<FeatureInfoDto> getFeaturesByTag(TagFilterDto filterDto) {
    List<FeatureInfoDto> featureList = featureRepository.queryFeatureList(
        filterDto.getTestCaseId());
    if (CollectionUtils.isEmpty(featureList)) {
      return Collections.emptyList();
    }

    List<String> featuresTags = featureTagService.getFeaturesByTag(filterDto.getTags());
    if (CollectionUtils.isEmpty(featuresTags)) {
      return Collections.emptyList();
    }

    return featureList.stream().filter(feature -> featuresTags.contains(feature.getFeatureId()))
        .collect(Collectors.toList());
  }

  public Boolean executeFeature(String featureId) {
    FeatureInfoVo feature = getFeatureById(featureId);
    if (Objects.isNull(feature)) {
      log.info("can not find feature={}", featureId);
      return false;
    }

    DispatchModel dispatchModel = new DispatchModel();
    dispatchModel.setType(LogType.FEATURE.getType());
    dispatchModel.setSourceId(JSON.toJSONString(Collections.singletonList(featureId)));
    dispatchModel.setSourceName(feature.getFeatureName());

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
      log.error("request dispatch task error", e);
    }
    return false;
  }
}
