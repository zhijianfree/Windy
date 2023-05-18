package com.zj.feature.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.feature.TestCaseConfigDto;
import com.zj.domain.entity.po.feature.TestCaseConfig;
import com.zj.domain.mapper.feeature.TestCaseConfigMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2022/12/19
 */
@Service
public class TestCaseConfigService extends ServiceImpl<TestCaseConfigMapper, TestCaseConfig> {

  @Autowired
  private UniqueIdService uniqueIdService;

  public List<TestCaseConfigDto> getTestCaseConfigs(String caseId) {
    List<TestCaseConfig> testCaseConfigs = list(
        Wrappers.lambdaQuery(TestCaseConfig.class).eq(TestCaseConfig::getUnionId, caseId));
    if (CollectionUtils.isEmpty(testCaseConfigs)) {
      return Collections.emptyList();
    }
    return testCaseConfigs.stream().map(TestCaseConfigDto::toTestCaseConfigDTO)
        .collect(Collectors.toList());
  }

  public Integer addCaseConfigs(List<TestCaseConfigDto> configs) {
    if (CollectionUtils.isEmpty(configs)) {
      return 0;
    }

    List<TestCaseConfig> caseConfigs = configs.stream().map(configDTO -> {
      TestCaseConfig testCaseConfig = TestCaseConfigDto.toTestCaseConfig(configDTO);
      testCaseConfig.setConfigId(uniqueIdService.getUniqueId());
      testCaseConfig.setCreateTime(System.currentTimeMillis());
      testCaseConfig.setUpdateTime(System.currentTimeMillis());
      return testCaseConfig;
    }).collect(Collectors.toList());
    return caseConfigs.stream().mapToInt(caseConfig -> save(caseConfig) ? 1 : 0).sum();
  }

  public boolean updateCaseConfigs(TestCaseConfigDto configDTO) {
    if (Objects.isNull(configDTO)) {
      return false;
    }
    TestCaseConfig testCaseConfig = TestCaseConfigDto.toTestCaseConfig(configDTO);

    return update(testCaseConfig, Wrappers.lambdaUpdate(TestCaseConfig.class)
        .eq(TestCaseConfig::getConfigId, configDTO.getConfigId()));
  }

  public boolean deleteCaseConfig(String configId) {
    return remove(
        Wrappers.lambdaQuery(TestCaseConfig.class).eq(TestCaseConfig::getConfigId, configId));
  }
}
