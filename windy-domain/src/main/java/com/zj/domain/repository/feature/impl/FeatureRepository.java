package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.enums.FeatureType;
import com.zj.domain.entity.po.feature.FeatureInfo;
import com.zj.domain.mapper.feeature.FeatureMapper;
import com.zj.domain.repository.feature.IFeatureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Repository
public class FeatureRepository extends ServiceImpl<FeatureMapper, FeatureInfo> implements
        IFeatureRepository {

    @Override
    public List<FeatureInfoBO> queryFeatureList(String testCaseId) {
        List<FeatureInfo> featureList = list(
                Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getTestCaseId, testCaseId));
        return OrikaUtil.convertList(featureList, FeatureInfoBO.class);
    }

    @Override
    public FeatureInfoBO getFeatureById(String featureId) {
        FeatureInfo featureInfo = getOne(
                Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getFeatureId, featureId));
        return OrikaUtil.convert(featureInfo, FeatureInfoBO.class);
    }

    @Override
    public List<FeatureInfoBO> queryNotContainFolder(String testCaseId) {
        List<FeatureInfo> infoList = list(Wrappers.lambdaQuery(FeatureInfo.class)
                .eq(FeatureInfo::getFeatureType, FeatureType.ITEM.getType())
                .eq(FeatureInfo::getTestCaseId, testCaseId).orderByAsc(FeatureInfo::getSortOrder));
        return OrikaUtil.convertList(infoList, FeatureInfoBO.class);
    }

    @Override
    public IPage<FeatureInfoBO> queryFeaturePage(String testCaseId, int page, int size) {
        Page<FeatureInfo> pageSize = new Page<>(page, size);
        Page<FeatureInfo> infoPage = page(pageSize,
                Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getTestCaseId, testCaseId));
        List<FeatureInfoBO> featureInfoBOS = OrikaUtil.convertList(infoPage.getRecords(),
                FeatureInfoBO.class);

        Page<FeatureInfoBO> result = new Page<>();
        result.setRecords(featureInfoBOS);
        result.setTotal(infoPage.getTotal());
        return result;
    }

    @Override
    public boolean saveBatch(List<FeatureInfoBO> infoList) {
        List<FeatureInfo> list = OrikaUtil.convertList(infoList, FeatureInfo.class);
        return saveBatch(list);
    }

    @Override
    public boolean createFeature(FeatureInfoBO featureInfoBO) {
        FeatureInfo featureInfo = OrikaUtil.convert(featureInfoBO, FeatureInfo.class);
        featureInfo.setCreateTime(System.currentTimeMillis());
        featureInfo.setUpdateTime(System.currentTimeMillis());
        return save(featureInfo);
    }

    @Override
    public boolean updateFeatureInfo(FeatureInfoBO featureInfoBO) {
        FeatureInfo featureInfo = OrikaUtil.convert(featureInfoBO, FeatureInfo.class);
        featureInfo.setUpdateTime(System.currentTimeMillis());
        return update(featureInfo, Wrappers.lambdaUpdate(FeatureInfo.class)
                .eq(FeatureInfo::getFeatureId, featureInfoBO.getFeatureId()));
    }

    @Override
    public List<FeatureInfoBO> getSubFeatures(String featureId) {
        List<FeatureInfo> subFeatures = list(
                Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getParentId, featureId));

        return OrikaUtil.convertList(subFeatures, FeatureInfoBO.class);
    }

    @Override
    @Transactional
    public Boolean batchUpdate(List<FeatureInfoBO> features) {
        List<FeatureInfo> featureList = OrikaUtil.convertList(features, FeatureInfo.class);
        return updateBatchById(featureList);

    }

    @Override
    public boolean deleteByFeatureId(String featureId) {
        return remove(Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getFeatureId, featureId));
    }

    @Override
    public List<FeatureInfoBO> queryFeatureList(List<String> featureIds) {
        List<FeatureInfo> featureInfoList = list(Wrappers.lambdaQuery(FeatureInfo.class)
                .in(FeatureInfo::getFeatureId, featureIds));

        return OrikaUtil.convertList(featureInfoList, FeatureInfoBO.class);
    }

    @Override
    public Boolean batchDeleteByFeatureId(List<String> featureIds) {
        if (CollectionUtils.isEmpty(featureIds)) {
            return false;
        }

        return remove(Wrappers.lambdaQuery(FeatureInfo.class).in(FeatureInfo::getFeatureId, featureIds));
    }

    @Override
    public List<FeatureInfoBO> getCaseFeatures(String testCaseId) {
        List<FeatureInfo> featureInfoList = list(Wrappers.lambdaQuery(FeatureInfo.class)
                .eq(FeatureInfo::getTestCaseId, testCaseId));
        return OrikaUtil.convertList(featureInfoList, FeatureInfoBO.class);
    }

    @Override
    public List<FeatureInfoBO> getFeatureByCases(List<String> testCaseIds) {
        List<FeatureInfo> featureInfoList = list(Wrappers.lambdaQuery(FeatureInfo.class)
                .in(FeatureInfo::getTestCaseId, testCaseIds));
        return OrikaUtil.convertList(featureInfoList, FeatureInfoBO.class);
    }
}
