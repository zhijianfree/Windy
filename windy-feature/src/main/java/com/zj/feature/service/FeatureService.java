package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.BatchDeleteDto;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.feature.entity.dto.CopyFeatureDto;
import com.zj.feature.entity.dto.ExecutePointDto;
import com.zj.feature.entity.dto.FeatureInfoDto;
import com.zj.feature.entity.dto.FeatureNodeDto;
import com.zj.common.model.PageSize;
import com.zj.feature.entity.dto.TagFilterDto;
import com.zj.domain.entity.dto.feature.TestCaseConfigDto;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.entity.po.feature.ExecutePoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

  public List<FeatureNodeDto> getFeatureTreeList(String testCaseId) {
    List<com.zj.domain.entity.dto.feature.FeatureInfoDto> featureList = featureRepository.queryFeatureList(testCaseId);
    FeatureNodeDto root = new FeatureNodeDto();
    root = convertTree(featureList, root);
    return root.getChildren();
  }

  /**
   * 递归转为tree结构
   */
  public FeatureNodeDto convertTree(List<com.zj.domain.entity.dto.feature.FeatureInfoDto> featureList, FeatureNodeDto parent) {
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

  public boolean updateByFeatureId(com.zj.domain.entity.dto.feature.FeatureInfoDto featureInfo) {
    return featureRepository.deleteByFeatureId(featureInfo.getFeatureId());
  }

  public FeatureInfoDto getFeatureById(String featureId) {
    com.zj.domain.entity.dto.feature.FeatureInfoDto featureInfo = featureRepository.getFeatureById(featureId);
    if (Objects.isNull(featureInfo)) {
      throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
    }
    FeatureInfoDto featureInfoDTO = OrikaUtil.convert(featureInfo, FeatureInfoDto.class);
    List<String> featureTags = featureTagService.getFeatureTags(featureId);
    featureInfoDTO.setTags(featureTags);
    return featureInfoDTO;
  }

  public PageSize<com.zj.domain.entity.dto.feature.FeatureInfoDto> queryFeaturePage(String testCaseId, int page, int size) {
    IPage<com.zj.domain.entity.dto.feature.FeatureInfoDto> featurePage = featureRepository.queryFeaturePage(testCaseId, page, size);
    PageSize<com.zj.domain.entity.dto.feature.FeatureInfoDto> detailPageSize = new PageSize<>();
    detailPageSize.setTotal(featurePage.getTotal());
    detailPageSize.setData(featurePage.getRecords());
    return detailPageSize;
  }

  public List<com.zj.domain.entity.dto.feature.FeatureInfoDto> queryFeatureList(String testCaseId) {
    return featureRepository.queryFeatureList(testCaseId);
  }

  public List<com.zj.domain.entity.dto.feature.FeatureInfoDto> queryNotContainFolder(String testCaseId) {
    return featureRepository.queryNotContainFolder(testCaseId);
  }

  @Transactional
  public String createFeature(FeatureInfoDto featureInfoDTO) {
    com.zj.domain.entity.dto.feature.FeatureInfoDto featureInfo = OrikaUtil.convert(featureInfoDTO, com.zj.domain.entity.dto.feature.FeatureInfoDto.class);
    featureInfo.setFeatureId(uniqueIdService.getUniqueId());
    boolean result = featureRepository.createFeature(featureInfo);

    if (!CollectionUtils.isEmpty(featureInfoDTO.getTags())) {
      featureTagService.batchAddTag(featureInfo.getFeatureId(), featureInfoDTO.getTags());
    }

    log.info("create feature detail result = {}", result);
    return featureInfo.getFeatureId();
  }

  @Transactional
  public String updateFeatureInfo(FeatureInfoDto featureInfoDTO) {
    if (Objects.isNull(getFeatureById(featureInfoDTO.getFeatureId()))) {
      throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
    }

    com.zj.domain.entity.dto.feature.FeatureInfoDto featureInfo = OrikaUtil.convert(featureInfoDTO, com.zj.domain.entity.dto.feature.FeatureInfoDto.class);
    boolean result = featureRepository.updateFeatureInfo(featureInfo);

    List<ExecutePointDto> executePoints = featureInfoDTO.getTestFeatures();
    if (!CollectionUtils.isEmpty(executePoints)) {
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

  private int updateExecutePoint(List<ExecutePointDto> executePointDtos) {
    int result = 0;
    if (CollectionUtils.isEmpty(executePointDtos)) {
      return result;
    }

    return executePointDtos.stream().mapToInt(executePointDTO -> {
      if (Objects.isNull(executePointDTO.getPointId())) {
        executePointDTO.setPointId(uniqueIdService.getUniqueId());
        String pointId = executePointService.createExecutePoint(executePointDTO);
        return StringUtils.isEmpty(pointId) ? 0 : 1;
      }

      String pointId = executePointService.updateExecutePoint(executePointDTO);
      return StringUtils.isEmpty(pointId) ? 0 : 1;
    }).sum();
  }

  @Transactional
  public Boolean copyFeatures(CopyFeatureDto copyFeatureDTO) {
    TestCaseDto testCase = testCaseService.getTestCase(copyFeatureDTO.getTestCaseId());
    testCase.setTestCaseName(testCase.getTestCaseName() + COPY_STRING);
    String newCaseId = testCaseService.createTestCase(testCase);
    log.info("new caseId={} old case Id={}", newCaseId, copyFeatureDTO.getTestCaseId());

    List<com.zj.domain.entity.dto.feature.FeatureInfoDto> featureList = featureRepository.queryFeatureList(
        copyFeatureDTO.getFeatureIds());
    log.info("get feature list ={}", JSON.toJSONString(featureList));

    //1 先复制用例
    Long currentTime = System.currentTimeMillis();
    Map<String, String> idRecordMap = new HashMap<>();
    List<com.zj.domain.entity.dto.feature.FeatureInfoDto> newList = featureList.stream().peek(feature -> {
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
    List<ExecutePoint> executePointList = executePointService.getExecutePointByFeatureIds(
        featureIds);
    Map<String, List<ExecutePoint>> pointGroupMap = executePointList.stream()
        .collect(Collectors.groupingBy(ExecutePoint::getFeatureId, Collectors.toList()));
    log.info("get groupBy points={}", JSON.toJSONString(pointGroupMap));

    //将获取到的执行点根据featureId映射关系重新设置一次
    List<ExecutePoint> newExecutePoints = pointGroupMap.keySet().stream().map(oldId -> {
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
    List<com.zj.domain.entity.dto.feature.FeatureInfoDto> subFeatures = featureRepository.getSubFeatures(featureId);
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
    List<com.zj.domain.entity.dto.feature.FeatureInfoDto> featuresByTag = getFeaturesByTag(tagFilterDTO);
    FeatureNodeDto featureNode = new FeatureNodeDto();
    featureNode = convertTree(featuresByTag, featureNode);
    return featureNode.getChildren();
  }

  public List<com.zj.domain.entity.dto.feature.FeatureInfoDto> getFeaturesByTag(TagFilterDto filterDto) {
    List<com.zj.domain.entity.dto.feature.FeatureInfoDto> featureList = featureRepository.queryFeatureList(
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
    //todo 单个任务执行
    return null;
  }
}
