package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface ITestCaseConfigRepository {

  List<TestCaseConfigBO> getCaseConfigs(String caseId);

  boolean saveConfig(TestCaseConfigBO caseConfig);

  boolean updateCaseConfig(TestCaseConfigBO configDto);

  boolean deleteCaseConfig(String configId);

  boolean batchUpdateCaseConfig(List<TestCaseConfigBO> updateList);
}
