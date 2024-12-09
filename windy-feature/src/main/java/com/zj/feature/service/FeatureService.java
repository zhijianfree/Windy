package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.enums.LogType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import com.zj.domain.entity.bo.feature.TestCaseBO;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.feature.entity.BatchDeleteDto;
import com.zj.feature.entity.BatchUpdateFeatures;
import com.zj.feature.entity.CopyCaseFeatureDto;
import com.zj.feature.entity.ExecutePointVo;
import com.zj.feature.entity.FeatureInfoVo;
import com.zj.feature.entity.FeatureNodeDto;
import com.zj.feature.entity.FeatureOrder;
import com.zj.feature.entity.PasteFeatureDto;
import com.zj.feature.entity.TagFilterDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FeatureService {

    public static final String COPY_STRING = " copy";

    private final ExecutePointService executePointService;
    private final TestCaseService testCaseService;
    private final TestCaseConfigService testCaseConfigService;
    private final FeatureTagService featureTagService;
    private final UniqueIdService uniqueIdService;
    private final IFeatureRepository featureRepository;
    private final IMasterInvoker masterInvoker;

    public FeatureService(ExecutePointService executePointService, TestCaseService testCaseService,
                          TestCaseConfigService testCaseConfigService, FeatureTagService featureTagService,
                          UniqueIdService uniqueIdService, IFeatureRepository featureRepository, IMasterInvoker masterInvoker) {
        this.executePointService = executePointService;
        this.testCaseService = testCaseService;
        this.testCaseConfigService = testCaseConfigService;
        this.featureTagService = featureTagService;
        this.uniqueIdService = uniqueIdService;
        this.featureRepository = featureRepository;
        this.masterInvoker = masterInvoker;
    }

    public List<FeatureNodeDto> getFeatureTreeList(String testCaseId) {
        List<FeatureInfoBO> featureList = featureRepository.queryFeatureList(testCaseId).stream()
                .sorted(Comparator.comparing(FeatureInfoBO::getSortOrder)).collect(Collectors.toList());
        FeatureNodeDto root = new FeatureNodeDto();
        root = convertTree(featureList, root);
        return root.getChildren();
    }

    /**
     * 递归转为tree结构
     */
    public FeatureNodeDto convertTree(List<FeatureInfoBO> featureList, FeatureNodeDto parent) {
        if (CollectionUtils.isEmpty(featureList)) {
            return parent;
        }

        List<FeatureNodeDto> list = featureList.stream().filter(feature -> Objects.equals(feature.getParentId(),
                parent.getFeatureId())).map(FeatureNodeDto::toNode).collect(Collectors.toList());
        parent.setChildren(list);

        featureList.removeIf(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()));
        list.forEach(node -> convertTree(featureList, node));
        return parent;
    }

    public FeatureInfoVo getFeatureById(String featureId) {
        FeatureInfoBO featureInfo = featureRepository.getFeatureById(featureId);
        if (Objects.isNull(featureInfo)) {
            throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
        }
        FeatureInfoVo featureInfoDTO = OrikaUtil.convert(featureInfo, FeatureInfoVo.class);
        List<String> featureTags = featureTagService.getFeatureTags(featureId);
        featureInfoDTO.setTags(featureTags);
        return featureInfoDTO;
    }

    public PageSize<FeatureInfoBO> queryFeaturePage(String testCaseId, int page, int size) {
        IPage<FeatureInfoBO> featurePage = featureRepository.queryFeaturePage(testCaseId, page, size);
        PageSize<FeatureInfoBO> detailPageSize = new PageSize<>();
        detailPageSize.setTotal(featurePage.getTotal());
        detailPageSize.setData(featurePage.getRecords());
        return detailPageSize;
    }

    public List<FeatureInfoBO> queryFeatureList(String testCaseId) {
        return featureRepository.queryFeatureList(testCaseId);
    }

    @Transactional
    public String createFeature(FeatureInfoVo featureInfoDTO) {
        FeatureInfoBO featureInfo = OrikaUtil.convert(featureInfoDTO, FeatureInfoBO.class);
        featureInfo.setFeatureId(uniqueIdService.getUniqueId());
        boolean result = featureRepository.createFeature(featureInfo);

        if (CollectionUtils.isNotEmpty(featureInfoDTO.getTags())) {
            boolean batchAddTag = featureTagService.batchAddTag(featureInfo.getFeatureId(), featureInfoDTO.getTags());
            log.info("batch save tag result={}", batchAddTag);
        }

        log.info("create feature detail result = {}", result);
        return featureInfo.getFeatureId();
    }

    @Transactional
    public String updateFeatureInfo(FeatureInfoVo featureInfoDTO) {
        if (Objects.isNull(getFeatureById(featureInfoDTO.getFeatureId()))) {
            throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
        }

        FeatureInfoBO featureInfo = OrikaUtil.convert(featureInfoDTO, FeatureInfoBO.class);
        boolean result = featureRepository.updateFeatureInfo(featureInfo);
        log.info("update test case feature result={}", result);

        List<ExecutePointVo> executePoints = featureInfoDTO.getTestFeatures();
        if (CollectionUtils.isNotEmpty(executePoints)) {
            int featureResult = updateExecutePoint(executePoints);
            if (featureResult < 1) {
                log.info("update feature points result count less than 1");
                throw new ApiException(ErrorCode.BATCH_UPDATE_FEATURE_POINTS_ERROR);
            }
        }

        if (Objects.nonNull(featureInfoDTO.getTags())) {
            boolean batchUpdateTag = featureTagService.batchUpdateTag(featureInfo.getFeatureId(), featureInfoDTO.getTags());
            log.info("batch update tag result={}", batchUpdateTag);
        }
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
    public Boolean copyCaseFeatures(CopyCaseFeatureDto copyCaseFeature) {
        TestCaseBO testCase = testCaseService.getTestCase(copyCaseFeature.getTestCaseId());
        testCase.setTestCaseName(testCase.getTestCaseName() + COPY_STRING);
        String newCaseId = testCaseService.createTestCase(testCase);
        log.info("new caseId={} old case Id={}", newCaseId, copyCaseFeature.getTestCaseId());

        List<FeatureInfoBO> featureList = featureRepository.queryFeatureList(copyCaseFeature.getFeatureIds());
        log.info("get feature list ={}", JSON.toJSONString(featureList));

        //1 先复制用例
        Long currentTime = System.currentTimeMillis();
        Map<String, String> idRecordMap = new HashMap<>();
        List<FeatureInfoBO> newList = featureList.stream().peek(feature -> {
            String uniqueId = uniqueIdService.getUniqueId();
            idRecordMap.put(feature.getFeatureId(), uniqueId);
            feature.setFeatureId(uniqueId);
            feature.setId(null);
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
        List<ExecutePointBO> executePointList = executePointService.getExecutePointByFeatureIds(featureIds);
        Map<String, List<ExecutePointBO>> pointGroupMap =
                executePointList.stream().collect(Collectors.groupingBy(ExecutePointBO::getFeatureId,
                        Collectors.toList()));
        log.info("get groupBy points={}", JSON.toJSONString(pointGroupMap));

        //将获取到的执行点根据featureId映射关系重新设置一次
        List<ExecutePointBO> newExecutePoints = pointGroupMap.keySet().stream().map(oldId -> {
            String newFeatureId = idRecordMap.get(oldId);
            pointGroupMap.get(oldId).forEach(executePoint -> {
                executePoint.setId(null);
                executePoint.setPointId(uniqueIdService.getUniqueId());
                executePoint.setFeatureId(newFeatureId);
                executePoint.setCreateTime(currentTime);
                executePoint.setUpdateTime(currentTime);
            });
            return pointGroupMap.get(oldId);
        }).flatMap(Collection::stream).collect(Collectors.toList());
        boolean saveBatch = executePointService.saveBatch(newExecutePoints);
        log.info("batch save points result={}", saveBatch);

        //3 复制全局变量
        List<TestCaseConfigBO> caseConfigs = testCaseConfigService.getTestCaseConfigs(copyCaseFeature.getTestCaseId());
        caseConfigs.forEach(config -> {
            config.setUnionId(newCaseId);
            config.setId(null);
        });
        testCaseConfigService.addCaseConfigs(caseConfigs);
        log.info("copy global configs={}", JSON.toJSONString(caseConfigs));
        return true;
    }

    @Transactional
    public boolean deleteByFeatureId(String featureId) {
        List<FeatureInfoBO> subFeatures = featureRepository.getSubFeatures(featureId);
        if (!CollectionUtils.isEmpty(subFeatures)) {
            throw new ApiException(ErrorCode.SUB_FEATURE_EXIST);
        }
        executePointService.deleteByFeatureId(featureId);
        return featureRepository.deleteByFeatureId(featureId);
    }

    public Boolean batchDeleteByFeatureId(BatchDeleteDto batchDeleteDto) {
        return featureRepository.batchDeleteByFeatureId(batchDeleteDto.getFeatures());
    }

    public List<FeatureNodeDto> filterFeaturesByTag(TagFilterDto tagFilterDTO) {
        List<FeatureInfoBO> featuresByTag = getFeaturesByTag(tagFilterDTO);
        FeatureNodeDto featureNode = new FeatureNodeDto();
        featureNode = convertTree(featuresByTag, featureNode);
        return featureNode.getChildren();
    }

    public List<FeatureInfoBO> getFeaturesByTag(TagFilterDto filterDto) {
        List<FeatureInfoBO> featureList = featureRepository.queryFeatureList(filterDto.getTestCaseId());
        if (CollectionUtils.isEmpty(featureList)) {
            return Collections.emptyList();
        }

        List<String> featuresTags = featureTagService.getFeaturesByTag(filterDto.getTags());
        if (CollectionUtils.isEmpty(featuresTags)) {
            return Collections.emptyList();
        }

        // 过滤出符合标签的 feature
        List<FeatureInfoBO> matchedFeatures = featureList.stream()
                .filter(feature -> featuresTags.contains(feature.getFeatureId()))
                .collect(Collectors.toList());

        // 找到所有相关的父节点
        Set<FeatureInfoBO> resultSet = new HashSet<>(matchedFeatures);
        for (FeatureInfoBO feature : matchedFeatures) {
            findParents(featureList, feature.getParentId(), resultSet);
        }
        return new ArrayList<>(resultSet);
    }

    /**
     * 递归查找父节点
     */
    private void findParents(List<FeatureInfoBO> allFeatures, String parentId, Set<FeatureInfoBO> resultSet) {
        if (Objects.isNull(parentId)) {
            return;
        }
        for (FeatureInfoBO feature : allFeatures) {
            if (parentId.equals(feature.getFeatureId()) && !resultSet.contains(feature)) {
                resultSet.add(feature); // 添加父节点到结果集合
                findParents(allFeatures, feature.getParentId(), resultSet); // 递归查找父节点
            }
        }
    }

    public Boolean executeFeature(String featureId) {
        FeatureInfoVo feature = getFeatureById(featureId);
        if (Objects.isNull(feature)) {
            log.info("can not find feature={}", featureId);
            return false;
        }

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setType(LogType.FEATURE.getType());
        dispatchTaskModel.setSourceId(JSON.toJSONString(Collections.singletonList(featureId)));
        dispatchTaskModel.setSourceName(feature.getFeatureName());
        String recordId = masterInvoker.runFeatureTask(dispatchTaskModel);
        return Objects.nonNull(recordId);
    }

    public Boolean pasteFeatures(PasteFeatureDto copyFeature) {
        FeatureInfoBO targetFeature = featureRepository.getFeatureById(copyFeature.getTargetFeature());
        if (Objects.isNull(targetFeature)) {
            log.info("can not find target feature={}", copyFeature.getTargetFeature());
            throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
        }

        List<ExecutePointBO> executePoints =
                executePointService.getExecutePointByFeatureIds(copyFeature.getFeatureIds());
        Map<String, List<ExecutePointBO>> featurePointsMap =
                executePoints.stream().collect(Collectors.groupingBy(ExecutePointBO::getFeatureId));

        List<FeatureInfoBO> featureInfos = featureRepository.queryFeatureList(copyFeature.getFeatureIds());
        featureInfos.forEach(feature -> {
            //将用例关联的执行点也复制懂啊新的用例下
            String newFeatureId = uniqueIdService.getUniqueId();
            List<ExecutePointBO> existPoints = featurePointsMap.get(feature.getFeatureId());
            existPoints = existPoints.stream().map(point -> {
                point.setId(null);
                point.setPointId(uniqueIdService.getUniqueId());
                point.setFeatureId(newFeatureId);
                point.setUpdateTime(System.currentTimeMillis());
                point.setCreateTime(System.currentTimeMillis());
                return point;
            }).collect(Collectors.toList());
            boolean saveBatch = executePointService.saveBatch(existPoints);
            log.info("batch save execute point result ={}", saveBatch);

            feature.setId(null);
            feature.setFeatureName(feature.getFeatureName() + "  Copy");
            feature.setFeatureId(newFeatureId);
            feature.setUpdateTime(System.currentTimeMillis());
            feature.setCreateTime(System.currentTimeMillis());
            feature.setParentId(copyFeature.getTargetFeature());
            feature.setTestCaseId(targetFeature.getTestCaseId());
        });
        return featureRepository.saveBatch(featureInfos);
    }

    public Boolean batchUpdateFeatures(BatchUpdateFeatures batchUpdateFeatures) {
        List<String> featureIds =
                batchUpdateFeatures.getFeatureOrders().stream().map(FeatureOrder::getFeatureId).collect(Collectors.toList());
        Map<String, Long> featureMap =
                featureRepository.queryFeatureList(featureIds).stream().collect(Collectors.toMap(FeatureInfoBO::getFeatureId, FeatureInfoBO::getId));
        List<FeatureInfoBO> features =
                batchUpdateFeatures.getFeatureOrders().stream().map(featureOrder -> {
                    FeatureInfoBO feature = OrikaUtil.convert(featureOrder, FeatureInfoBO.class);
                    feature.setId(featureMap.get(feature.getFeatureId()));
                    return feature;
                }).collect(Collectors.toList());
        return featureRepository.batchUpdate(features);
    }
}
