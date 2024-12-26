package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.feature.TestCaseBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface ITestCaseRepository {

    /**
     * 根据ID获取测试集
     * @param caseId 测试集ID
     * @return 测试集信息
     */
    TestCaseBO getTestCaseById(String caseId);

    /**
     * 保存测试集
     * @param testCaseBO 测试集信息
     * @return 是否成功
     */
    boolean saveCase(TestCaseBO testCaseBO);

    /**
     * 更新测试集
     * @param testCaseBO 测试集信息
     * @return 是否成功
     */
    Boolean updateCase(TestCaseBO testCaseBO);

    /**
     * 删除测试集
     * @param caseId 测试集ID
     * @return 是否成功
     */
    Boolean deleteTestCase(String caseId);

    /**
     * 获取服务所有测试集列表
     * @param serviceId 服务ID
     * @return 测试集列表
     */
    List<TestCaseBO> getServiceCases(String serviceId);

    /**
     * 分页获取服务下测试集列表
     * @param serviceId 服务ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 测试集列表
     */
    IPage<TestCaseBO> getCasePage(String serviceId, Integer page, Integer pageSize);

    /**
     * 获取e2e测试集
     * @return 测试集列表
     */
    IPage<TestCaseBO> getE2ECasesPage(Integer page, Integer pageSize);

    /**
     * 获取所有e2e测试集
     * @return 测试集列表
     */
    List<TestCaseBO> getE2ECases();
}
