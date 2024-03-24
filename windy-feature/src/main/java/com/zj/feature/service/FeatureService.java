package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.enums.LogType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.model.PageSize;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.feature.BatchDeleteDto;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TestCaseConfigDto;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.feature.entity.dto.CopyCaseFeatureDto;
import com.zj.feature.entity.dto.CopyFeatureDto;
import com.zj.feature.entity.dto.ExecutePointVo;
import com.zj.feature.entity.dto.FeatureInfoVo;
import com.zj.feature.entity.dto.FeatureNodeDto;
import com.zj.feature.entity.dto.TagFilterDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final RequestProxy requestProxy;

    public FeatureService(ExecutePointService executePointService, TestCaseService testCaseService,
                          TestCaseConfigService testCaseConfigService, FeatureTagService featureTagService,
                          UniqueIdService uniqueIdService, IFeatureRepository featureRepository,
                          RequestProxy requestProxy) {
        this.executePointService = executePointService;
        this.testCaseService = testCaseService;
        this.testCaseConfigService = testCaseConfigService;
        this.featureTagService = featureTagService;
        this.uniqueIdService = uniqueIdService;
        this.featureRepository = featureRepository;
        this.requestProxy = requestProxy;
    }

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

        List<FeatureNodeDto> list = featureList.stream().filter(feature -> Objects.equals(feature.getParentId(),
                parent.getFeatureId())).map(FeatureNodeDto::toNode).collect(Collectors.toList());
        parent.setChildren(list);

        featureList.removeIf(feature -> Objects.equals(feature.getParentId(), parent.getFeatureId()));
        list.forEach(node -> convertTree(featureList, node));

        return parent;
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
    public Boolean copyCaseFeatures(CopyCaseFeatureDto copyCaseFeature) {
        TestCaseDto testCase = testCaseService.getTestCase(copyCaseFeature.getTestCaseId());
        testCase.setTestCaseName(testCase.getTestCaseName() + COPY_STRING);
        String newCaseId = testCaseService.createTestCase(testCase);
        log.info("new caseId={} old case Id={}", newCaseId, copyCaseFeature.getTestCaseId());

        List<FeatureInfoDto> featureList = featureRepository.queryFeatureList(copyCaseFeature.getFeatureIds());
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
        List<ExecutePointDto> executePointList = executePointService.getExecutePointByFeatureIds(featureIds);
        Map<String, List<ExecutePointDto>> pointGroupMap =
                executePointList.stream().collect(Collectors.groupingBy(ExecutePointDto::getFeatureId,
                        Collectors.toList()));
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
        List<TestCaseConfigDto> caseConfigs = testCaseConfigService.getTestCaseConfigs(copyCaseFeature.getTestCaseId());
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
        List<FeatureInfoDto> featureList = featureRepository.queryFeatureList(filterDto.getTestCaseId());
        if (CollectionUtils.isEmpty(featureList)) {
            return Collections.emptyList();
        }

        List<String> featuresTags = featureTagService.getFeaturesByTag(filterDto.getTags());
        if (CollectionUtils.isEmpty(featuresTags)) {
            return Collections.emptyList();
        }

        return featureList.stream().filter(feature -> featuresTags.contains(feature.getFeatureId())).collect(Collectors.toList());
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
        return requestProxy.runTask(dispatchTaskModel);
    }

    public Boolean copyFeature(CopyFeatureDto copyFeature) {
        FeatureInfoDto targetFeature = featureRepository.getFeatureById(copyFeature.getTargetFeature());
        if (Objects.isNull(targetFeature)) {
            log.info("can not find target feature={}", copyFeature.getTargetFeature());
            throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
        }

        List<ExecutePointDto> executePoints = executePointService.getExecutePointByFeatureIds(copyFeature.getFeatureIds());
        Map<String, List<ExecutePointDto>> featurePointsMap =
                executePoints.stream().collect(Collectors.groupingBy(ExecutePointDto::getFeatureId));

        List<FeatureInfoDto> featureInfos = featureRepository.queryFeatureList(copyFeature.getFeatureIds());
        featureInfos.forEach(feature -> {
            //将用例关联的执行点也复制懂啊新的用例下
            String newFeatureId = uniqueIdService.getUniqueId();
            List<ExecutePointDto> existPoints = featurePointsMap.get(feature.getFeatureId());
            existPoints.forEach(point ->{
                point.setId(null);
                point.setPointId(uniqueIdService.getUniqueId());
                point.setFeatureId(newFeatureId);
                point.setUpdateTime(System.currentTimeMillis());
                point.setCreateTime(System.currentTimeMillis());
            });
            executePointService.saveBatch(existPoints);

            feature.setFeatureId(newFeatureId);
            feature.setUpdateTime(System.currentTimeMillis());
            feature.setCreateTime(System.currentTimeMillis());
            feature.setParentId(copyFeature.getTargetFeature());
            feature.setTestCaseId(targetFeature.getTestCaseId());
        });
        return featureRepository.saveBatch(featureInfos);
    }
}
