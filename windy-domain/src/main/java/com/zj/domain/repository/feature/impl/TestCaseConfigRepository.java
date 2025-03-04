package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import com.zj.domain.entity.po.feature.TestCaseConfig;
import com.zj.domain.mapper.feeature.TestCaseConfigMapper;
import com.zj.domain.repository.feature.ITestCaseConfigRepository;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Repository
public class TestCaseConfigRepository extends
    ServiceImpl<TestCaseConfigMapper, TestCaseConfig> implements ITestCaseConfigRepository {

  @Override
  public List<TestCaseConfigBO> getCaseConfigs(String caseId) {
    List<TestCaseConfig> configs = list(
        Wrappers.lambdaQuery(TestCaseConfig.class).eq(TestCaseConfig::getUnionId, caseId));
    return OrikaUtil.convertList(configs, TestCaseConfigBO.class);
  }

  @Override
  public boolean saveConfig(TestCaseConfigBO caseConfig) {
    TestCaseConfig testCaseConfig = OrikaUtil.convert(caseConfig, TestCaseConfig.class);
    long dateNow = System.currentTimeMillis();
    testCaseConfig.setUpdateTime(dateNow);
    testCaseConfig.setCreateTime(dateNow);
    return save(testCaseConfig);
  }

  @Override
  public boolean updateCaseConfig(TestCaseConfigBO configDto) {
    TestCaseConfig testCaseConfig = OrikaUtil.convert(configDto, TestCaseConfig.class);
    return update(testCaseConfig, Wrappers.lambdaUpdate(TestCaseConfig.class)
        .eq(TestCaseConfig::getConfigId, configDto.getConfigId()));
  }

  @Override
  public boolean deleteCaseConfig(String configId) {
    return remove(
        Wrappers.lambdaQuery(TestCaseConfig.class).eq(TestCaseConfig::getConfigId, configId));
  }

  @Override
  public boolean deleteByCaseId(String caseId) {
    return remove(
            Wrappers.lambdaQuery(TestCaseConfig.class).eq(TestCaseConfig::getUnionId, caseId));
  }

  @Override
  @Transactional
  public boolean batchUpdateCaseConfig(List<TestCaseConfigBO> updateList) {
    if (CollectionUtils.isEmpty(updateList)) {
      return false;
    }
    List<TestCaseConfig> list = OrikaUtil.convertList(updateList, TestCaseConfig.class);
    return updateBatchById(list);
  }
}
