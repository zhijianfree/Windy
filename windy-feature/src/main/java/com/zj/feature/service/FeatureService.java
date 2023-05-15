package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.feature.entity.dto.BatchDeleteDTO;
import com.zj.feature.entity.dto.CopyFeatureDTO;
import com.zj.feature.entity.dto.ExecutePointDTO;
import com.zj.feature.entity.dto.FeatureInfoDTO;
import com.zj.feature.entity.dto.FeatureNodeDTO;
import com.zj.common.PageSize;
import com.zj.feature.entity.dto.TagFilterDTO;
import com.zj.feature.entity.dto.TestCaseConfigDTO;
import com.zj.feature.entity.dto.TestCaseDTO;
import com.zj.domain.entity.po.feature.ExecutePoint;
import com.zj.domain.entity.po.feature.FeatureInfo;
import com.zj.feature.entity.type.FeatureType;
import com.zj.domain.mapper.feeature.FeatureMapper;
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
public class FeatureService extends ServiceImpl<FeatureMapper, FeatureInfo> {

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

  public List<FeatureNodeDTO> getFeatureTreeList(String testCaseId) {
    List<FeatureInfo> featureList = queryFeatureList(testCaseId);
    FeatureNodeDTO root = new FeatureNodeDTO();
    root = convertTree(featureList, root);
    return root.getChildren();
  }

  /**
   * 递归转为tree结构
   */
  public FeatureNodeDTO convertTree(List<FeatureInfo> featureList, FeatureNodeDTO parent) {
    if (CollectionUtils.isEmpty(featureList)) {
      return parent;
    }

    List<FeatureNodeDTO> list = featureList.stream()
        .filter(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()))
        .map(FeatureNodeDTO::toNode)
        .collect(Collectors.toList());
    parent.setChildren(list);

    featureList.removeIf(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()));
    list.forEach(node -> {
      convertTree(featureList, node);
    });

    return parent;
  }

  public boolean updateByFeatureId(FeatureInfo featureInfo) {
    return update(featureInfo, Wrappers.lambdaUpdate(FeatureInfo.class)
        .eq(FeatureInfo::getFeatureId, featureInfo.getFeatureId()));
  }

  public FeatureInfoDTO getFeatureById(String featureId) {
    FeatureInfo featureInfo = getOne(
        Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getFeatureId, featureId));
    if (Objects.isNull(featureInfo)) {
      throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
    }
    FeatureInfoDTO featureInfoDTO = OrikaUtil.convert(featureInfo, FeatureInfoDTO.class);
    List<String> featureTags = featureTagService.getFeatureTags(featureId);
    featureInfoDTO.setTags(featureTags);
    return featureInfoDTO;
  }

  public PageSize<FeatureInfo> queryFeaturePage(String testCaseId, int page, int size) {
    Page<FeatureInfo> pageSize = new Page<>(page, size);
    Page<FeatureInfo> result = page(pageSize,
        Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getTestCaseId, testCaseId));

    PageSize<FeatureInfo> detailPageSize = new PageSize<>();
    detailPageSize.setTotal(result.getTotal());
    detailPageSize.setData(result.getRecords());
    return detailPageSize;
  }

  public List<FeatureInfo> queryFeatureList(String testCaseId) {
    return list(Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getTestCaseId, testCaseId));
  }

  public List<FeatureInfo> queryNotContainFolder(String testCaseId) {
    return list(Wrappers.lambdaQuery(FeatureInfo.class)
        .eq(FeatureInfo::getFeatureType, FeatureType.ITEM.getType())
        .eq(FeatureInfo::getTestCaseId, testCaseId));
  }

  @Transactional
  public String createFeature(FeatureInfoDTO featureInfoDTO) {
    FeatureInfo featureInfo = OrikaUtil.convert(featureInfoDTO, FeatureInfo.class);
    featureInfo.setFeatureId(uniqueIdService.getUniqueId());
    featureInfo.setCreateTime(System.currentTimeMillis());
    featureInfo.setUpdateTime(System.currentTimeMillis());
    boolean result = save(featureInfo);

    if (!CollectionUtils.isEmpty(featureInfoDTO.getTags())) {
      featureTagService.batchAddTag(featureInfo.getFeatureId(), featureInfoDTO.getTags());
    }

    log.info("create feature detail result = {}", result);
    return featureInfo.getFeatureId();
  }

  @Transactional
  public String updateFeatureInfo(FeatureInfoDTO featureInfoDTO) {
    if (Objects.isNull(getFeatureById(featureInfoDTO.getFeatureId()))) {
      throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
    }

    FeatureInfo featureInfo = OrikaUtil.convert(featureInfoDTO, FeatureInfo.class);
    featureInfo.setCreateTime(System.currentTimeMillis());
    featureInfo.setUpdateTime(System.currentTimeMillis());
    featureInfo.setFeatureId(featureInfoDTO.getFeatureId());
    boolean result = updateByFeatureId(featureInfo);

    List<ExecutePointDTO> executePoints = featureInfoDTO.getTestFeatures();
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

  private int updateExecutePoint(List<ExecutePointDTO> executePointDTOS) {
    int result = 0;
    if (CollectionUtils.isEmpty(executePointDTOS)) {
      return result;
    }

    return executePointDTOS.stream().mapToInt(executePointDTO -> {
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
  public Boolean copyFeatures(CopyFeatureDTO copyFeatureDTO) {
    TestCaseDTO testCase = testCaseService.getTestCase(copyFeatureDTO.getTestCaseId());
    testCase.setTestCaseName(testCase.getTestCaseName() + COPY_STRING);
    String newCaseId = testCaseService.createTestCase(testCase);
    log.info("new caseId={} old case Id={}", newCaseId, copyFeatureDTO.getTestCaseId());

    List<FeatureInfo> featureList = list(Wrappers.lambdaQuery(FeatureInfo.class)
        .in(FeatureInfo::getFeatureId, copyFeatureDTO.getFeatureIds()));
    log.info("get feature list ={}", JSON.toJSONString(featureList));

    //1 先复制用例
    Long currentTime = System.currentTimeMillis();
    Map<String, String> idRecordMap = new HashMap<>();
    List<FeatureInfo> newList = featureList.stream().peek(feature -> {
      String uniqueId = uniqueIdService.getUniqueId();
      idRecordMap.put(feature.getFeatureId(), uniqueId);
      feature.setId(null);
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
    saveBatch(newList);
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
    List<TestCaseConfigDTO> caseConfigs = testCaseConfigService.getTestCaseConfigs(
        copyFeatureDTO.getTestCaseId());
    caseConfigs.forEach(configDTO -> configDTO.setUnionId(newCaseId));
    testCaseConfigService.addCaseConfigs(caseConfigs);
    log.info("copy global configs={}", JSON.toJSONString(caseConfigs));
    return true;
  }

  @Transactional
  public boolean deleteByFeatureId(String featureId) {
    List<FeatureInfo> subFeatures = list(
        Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getParentId, featureId));
    if (!CollectionUtils.isEmpty(subFeatures)) {
      throw new ApiException(ErrorCode.SUB_FEATURE_EXIST);
    }
    executePointService.deleteByFeatureId(featureId);
    return remove(Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getFeatureId, featureId));
  }

  public Boolean batchDeleteByFeatureId(BatchDeleteDTO batchDeleteDTO) {
    if (Objects.isNull(batchDeleteDTO) || CollectionUtils.isEmpty(batchDeleteDTO.getFeatures())) {
      return false;
    }

    return remove(Wrappers.lambdaQuery(FeatureInfo.class)
        .in(FeatureInfo::getFeatureId, batchDeleteDTO.getFeatures()));
  }

  public List<FeatureNodeDTO> filterFeaturesByTag(TagFilterDTO tagFilterDTO) {
    List<FeatureInfo> featuresByTag = getFeaturesByTag(tagFilterDTO);
    FeatureNodeDTO featureNode = new FeatureNodeDTO();
    featureNode = convertTree(featuresByTag, featureNode);
    return featureNode.getChildren();
  }

  public List<FeatureInfo> getFeaturesByTag(TagFilterDTO filterDTO) {
    List<FeatureInfo> featureList = list(Wrappers.lambdaQuery(FeatureInfo.class)
        .eq(FeatureInfo::getTestCaseId, filterDTO.getTestCaseId())).stream().collect(Collectors.toList());
    if (CollectionUtils.isEmpty(featureList)) {
      return Collections.emptyList();
    }

    List<String> featuresTags = featureTagService.getFeaturesByTag(filterDTO.getTags());
    if (CollectionUtils.isEmpty(featuresTags)) {
      return Collections.emptyList();
    }

    return featureList.stream().filter(feature -> featuresTags.contains(feature.getFeatureId()))
        .collect(Collectors.toList());
  }
}
