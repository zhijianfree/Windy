package com.zj.feature.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.feature.entity.dto.TestCaseConfigDTO;
import com.zj.feature.entity.po.TestCaseConfig;
import com.zj.feature.mapper.TestCaseConfigMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author falcon
 * @since 2022/12/19
 */
@Service
public class TestCaseConfigService extends ServiceImpl<TestCaseConfigMapper, TestCaseConfig> {

  public List<TestCaseConfigDTO> getTestCaseConfigs(String caseId) {
    List<TestCaseConfig> testCaseConfigs = list(
        Wrappers.lambdaQuery(TestCaseConfig.class).eq(TestCaseConfig::getUnionId, caseId));
    if (CollectionUtils.isEmpty(testCaseConfigs)) {
      return Collections.emptyList();
    }
    return testCaseConfigs.stream().map(TestCaseConfigDTO::toTestCaseConfigDTO)
        .collect(Collectors.toList());
  }

  public Integer addCaseConfigs(List<TestCaseConfigDTO> configs) {
    if (CollectionUtils.isEmpty(configs)) {
      return 0;
    }

    List<TestCaseConfig> caseConfigs = configs.stream().map(configDTO -> {
      TestCaseConfig testCaseConfig = TestCaseConfigDTO.toTestCaseConfig(configDTO);
      testCaseConfig.setConfigId(UUID.randomUUID().toString().replace("-", ""));
      testCaseConfig.setCreateTime(System.currentTimeMillis());
      testCaseConfig.setUpdateTime(System.currentTimeMillis());
      return testCaseConfig;
    }).collect(Collectors.toList());
    return caseConfigs.stream().mapToInt(caseConfig -> save(caseConfig) ? 1 : 0).sum();
  }

  public boolean updateCaseConfigs(TestCaseConfigDTO configDTO) {
    if (Objects.isNull(configDTO)) {
      return false;
    }
    TestCaseConfig testCaseConfig = TestCaseConfigDTO.toTestCaseConfig(configDTO);

    return update(testCaseConfig, Wrappers.lambdaUpdate(TestCaseConfig.class)
        .eq(TestCaseConfig::getConfigId, configDTO.getConfigId()));
  }

  public boolean deleteCaseConfig(String configId) {
    return remove(
        Wrappers.lambdaQuery(TestCaseConfig.class).eq(TestCaseConfig::getConfigId, configId));
  }
}
