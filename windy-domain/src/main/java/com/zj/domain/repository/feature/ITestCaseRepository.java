package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.feature.TestCaseBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface ITestCaseRepository {

    TestCaseBO getTestCaseById(String caseId);

    boolean saveCase(TestCaseBO testCaseBO);

    Boolean updateCase(TestCaseBO testCaseBO);

    Boolean deleteTestCase(String caseId);

    List<TestCaseBO> getServiceCases(String serviceId);

    IPage<TestCaseBO> getCasePage(String serviceId, Integer page, Integer pageSize);

    IPage<TestCaseBO> getE2ECasesPage(Integer page, Integer pageSize);

    List<TestCaseBO> getE2ECases();
}
