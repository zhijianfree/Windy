package com.zj.feature.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.uuid.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITestCaseRepository;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Service
public class TestCaseService {
  private final UniqueIdService uniqueIdService;
  private final ITestCaseRepository testCaseRepository;
  private final IFeatureRepository featureRepository;
  private final IExecutePointRepository executePointRepository;

  public TestCaseService(UniqueIdService uniqueIdService, ITestCaseRepository testCaseRepository, IFeatureRepository featureRepository, IExecutePointRepository executePointRepository) {
    this.uniqueIdService = uniqueIdService;
    this.testCaseRepository = testCaseRepository;
    this.featureRepository = featureRepository;
    this.executePointRepository = executePointRepository;
  }

  public PageSize<TestCaseDto> getE2ECases(Integer page, Integer pageSize) {
    IPage<TestCaseDto> pageObj = testCaseRepository.getE2ECasesPage(page, pageSize);
    return convertPageSize(pageObj);
  }

  public PageSize<TestCaseDto> convertPageSize(IPage<TestCaseDto> page){
    List<TestCaseDto> records = page.getRecords();
    PageSize<TestCaseDto> dtoPageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(records)) {
      dtoPageSize.setTotal(0);
      return dtoPageSize;
    }


    long total = page.getTotal();
    dtoPageSize.setTotal(total);
    dtoPageSize.setData(records);
    return dtoPageSize;
  }

  public PageSize<TestCaseDto> getTestCaseList(String serviceId, Integer page, Integer pageSize) {
    IPage<TestCaseDto> pageObj = testCaseRepository.getCasePage(serviceId, page, pageSize);
    return convertPageSize(pageObj);
  }

  public String createTestCase(TestCaseDto testCaseDto) {
    String testCaseId = uniqueIdService.getUniqueId();
    testCaseDto.setTestCaseId(testCaseId);
    testCaseRepository.saveCase(testCaseDto);
    return testCaseId;
  }

  public Boolean updateTestCase(TestCaseDto testCaseDto) {
    long dateNow = System.currentTimeMillis();
    testCaseDto.setUpdateTime(dateNow);
    return testCaseRepository.updateCase(testCaseDto);
  }

  public TestCaseDto getTestCase(String caseId) {
    return testCaseRepository.getTestCaseById(caseId);
  }

  @Transactional
  public Boolean deleteTestCase(String caseId) {
    List<FeatureInfoDto> featureList = featureRepository.queryFeatureList(caseId);
    if (CollectionUtils.isEmpty(featureList)) {
      return testCaseRepository.deleteTestCase(caseId);
    }

    List<String> featureIds = featureList.stream().map(FeatureInfoDto::getFeatureId).collect(Collectors.toList());
    List<ExecutePointDto> executePoints = executePointRepository.getPointsByFeatureIds(featureIds);

    boolean deleteFeatures = featureRepository.batchDeleteByFeatureId(featureIds);
    if (CollectionUtils.isEmpty(executePoints)) {
      return deleteFeatures && testCaseRepository.deleteTestCase(caseId);
    }

    boolean deletePoints = executePointRepository.deleteByFeatureIds(featureIds);
    return deleteFeatures && deletePoints && testCaseRepository.deleteTestCase(caseId);
  }

  public List<TestCaseDto> getE2ECases() {
    return testCaseRepository.getE2ECases();
  }
}
