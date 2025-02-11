package com.zj.feature.service;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import com.zj.domain.repository.feature.ITestCaseConfigRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2022/12/19
 */
@Service
public class TestCaseConfigService {

  private final UniqueIdService uniqueIdService;
  private final ITestCaseConfigRepository testCaseConfigRepository;

  public TestCaseConfigService(UniqueIdService uniqueIdService,
      ITestCaseConfigRepository testCaseConfigRepository) {
    this.uniqueIdService = uniqueIdService;
    this.testCaseConfigRepository = testCaseConfigRepository;
  }

  public List<TestCaseConfigBO> getTestCaseConfigs(String caseId) {
    return testCaseConfigRepository.getCaseConfigs(caseId);
  }

  public Integer addCaseConfigs(List<TestCaseConfigBO> configs) {
    if (CollectionUtils.isEmpty(configs)) {
      return 0;
    }

    List<TestCaseConfigBO> caseConfigs = configs.stream().map(testCaseConfig -> {
      testCaseConfig.setConfigId(uniqueIdService.getUniqueId());
      testCaseConfig.setCreateTime(System.currentTimeMillis());
      testCaseConfig.setUpdateTime(System.currentTimeMillis());
      return testCaseConfig;
    }).collect(Collectors.toList());
    return caseConfigs.stream()
        .mapToInt(caseConfig -> testCaseConfigRepository.saveConfig(caseConfig) ? 1 : 0).sum();
  }

  public boolean updateCaseConfigs(TestCaseConfigBO configDto) {
    if (Objects.isNull(configDto)) {
      return false;
    }
    return testCaseConfigRepository.updateCaseConfig(configDto);
  }

  public boolean deleteCaseConfig(String configId) {
    return testCaseConfigRepository.deleteCaseConfig(configId);
  }
}
