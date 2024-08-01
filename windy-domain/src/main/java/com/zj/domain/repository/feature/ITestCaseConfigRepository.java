package com.zj.domain.repository.feature;

import com.zj.domain.entity.dto.feature.TestCaseConfigDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface ITestCaseConfigRepository {

  List<TestCaseConfigDto> getCaseConfigs(String caseId);

  boolean saveConfig(TestCaseConfigDto caseConfig);

  boolean updateCaseConfig(TestCaseConfigDto configDto);

  boolean deleteCaseConfig(String configId);

  boolean batchUpdateCaseConfig(List<TestCaseConfigDto> updateList);
}
