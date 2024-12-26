package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface ITestCaseConfigRepository {

  /**
   * 获取测试集配置
   * @param caseId 测试集ID
   * @return 配置列表
   */
  List<TestCaseConfigBO> getCaseConfigs(String caseId);

  /**
   * 保存测试集配置
   * @param caseConfig 配置信息
   * @return 是否成功
   */
  boolean saveConfig(TestCaseConfigBO caseConfig);

  /**
   * 更新测试集配置
   * @param configDto 配置信息
   * @return 是否成功
   */
  boolean updateCaseConfig(TestCaseConfigBO configDto);

  /**
   * 删除测试集配置
   * @param configId 配置ID
   * @return 是否成功
   */
  boolean deleteCaseConfig(String configId);

  /**
   * 批量更新测试集配置
   * @param updateList 配置列表
   * @return 是否成功
   */
  boolean batchUpdateCaseConfig(List<TestCaseConfigBO> updateList);
}
