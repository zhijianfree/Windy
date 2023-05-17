package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.BatchDeleteDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.FeatureType;
import com.zj.domain.entity.po.feature.FeatureInfo;
import com.zj.domain.mapper.feeature.FeatureMapper;
import com.zj.domain.repository.feature.IFeatureRepository;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * @author falcon
 * @since 2023/5/17
 */
@Slf4j
@Repository
public class FeatureRepository extends ServiceImpl<FeatureMapper, FeatureInfo> implements
    IFeatureRepository {

  @Override
  public List<FeatureInfoDto> queryFeatureList(String testCaseId) {
    List<FeatureInfo> featureList = list(
        Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getTestCaseId, testCaseId));
    return OrikaUtil.convertList(featureList, FeatureInfoDto.class);
  }

  @Override
  public FeatureInfoDto getFeatureById(String featureId) {
    FeatureInfo featureInfo = getOne(
        Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getFeatureId, featureId));
    return OrikaUtil.convert(featureInfo, FeatureInfoDto.class);
  }

  @Override
  public List<FeatureInfoDto> queryNotContainFolder(String testCaseId) {
    List<FeatureInfo> infoList = list(Wrappers.lambdaQuery(FeatureInfo.class)
        .eq(FeatureInfo::getFeatureType, FeatureType.ITEM.getType())
        .eq(FeatureInfo::getTestCaseId, testCaseId));

    return OrikaUtil.convertList(infoList, FeatureInfoDto.class);
  }

  @Override
  public IPage<FeatureInfoDto> queryFeaturePage(String testCaseId, int page, int size) {
    Page<FeatureInfo> pageSize = new Page<>(page, size);
    Page<FeatureInfo> infoPage = page(pageSize,
        Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getTestCaseId, testCaseId));
    List<FeatureInfoDto> featureInfoDtos = OrikaUtil.convertList(infoPage.getRecords(),
        FeatureInfoDto.class);

    Page<FeatureInfoDto> result = new Page<>();
    result.setRecords(featureInfoDtos);
    result.setTotal(infoPage.getTotal());
    return result;
  }

  @Override
  public void saveBatch(List<FeatureInfoDto> infoList) {
    List<FeatureInfo> list = OrikaUtil.convertList(infoList, FeatureInfo.class);
    saveBatch(list);
  }

  @Override
  public boolean createFeature(FeatureInfoDto featureInfoDto) {
    FeatureInfo featureInfo = OrikaUtil.convert(featureInfoDto, FeatureInfo.class);
    featureInfo.setCreateTime(System.currentTimeMillis());
    featureInfo.setUpdateTime(System.currentTimeMillis());
    return save(featureInfo);
  }

  @Override
  public boolean updateFeatureInfo(FeatureInfoDto featureInfoDto) {
    FeatureInfo featureInfo = OrikaUtil.convert(featureInfoDto, FeatureInfo.class);
    featureInfo.setUpdateTime(System.currentTimeMillis());
    return update(featureInfo, Wrappers.lambdaUpdate(FeatureInfo.class)
        .eq(FeatureInfo::getFeatureId, featureInfoDto.getFeatureId()));
  }

  @Override
  public List<FeatureInfoDto> getSubFeatures(String featureId) {
    List<FeatureInfo> subFeatures = list(
        Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getParentId, featureId));

    return OrikaUtil.convertList(subFeatures, FeatureInfoDto.class);
  }

  @Override
  public boolean deleteByFeatureId(String featureId) {
    return remove(Wrappers.lambdaQuery(FeatureInfo.class).eq(FeatureInfo::getFeatureId, featureId));
  }

  @Override
  public List<FeatureInfoDto> queryFeatureList(List<String> featureIds) {
    List<FeatureInfo> featureInfoList = list(Wrappers.lambdaQuery(FeatureInfo.class)
        .in(FeatureInfo::getFeatureId, featureIds));

    return OrikaUtil.convertList(featureInfoList, FeatureInfoDto.class);
  }

  @Override
  public Boolean batchDeleteByFeatureId(BatchDeleteDto batchDeleteDto) {
    if (Objects.isNull(batchDeleteDto) || CollectionUtils.isEmpty(batchDeleteDto.getFeatures())) {
      return false;
    }

    return remove(Wrappers.lambdaQuery(FeatureInfo.class)
        .in(FeatureInfo::getFeatureId, batchDeleteDto.getFeatures()));
  }
}
