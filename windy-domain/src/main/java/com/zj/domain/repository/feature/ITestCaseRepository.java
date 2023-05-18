package com.zj.domain.repository.feature;

import com.zj.domain.entity.dto.feature.TestCaseDto;

/**
 * @author falcon
 * @since 2023/5/18
 */
public interface ITestCaseRepository {

  TestCaseDto getTestCaseByTaskId(String caseId);
}
