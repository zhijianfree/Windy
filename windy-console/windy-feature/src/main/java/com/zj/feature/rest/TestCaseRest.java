package com.zj.feature.rest;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.feature.TestCaseConfigBO;
import com.zj.domain.entity.bo.feature.TestCaseBO;
import com.zj.feature.entity.BatchExecuteFeature;
import com.zj.feature.service.TestCaseConfigService;
import com.zj.feature.service.TestCaseService;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@RestController
@RequestMapping("/v1/devops/feature")
public class TestCaseRest {

  private final TestCaseService testCaseService;
  private final TestCaseConfigService testCaseConfigService;

  public TestCaseRest(TestCaseService testCaseService, TestCaseConfigService testCaseConfigService) {
    this.testCaseService = testCaseService;
    this.testCaseConfigService = testCaseConfigService;
  }

  @GetMapping("/{serviceId}/cases")
  public ResponseMeta<PageSize<TestCaseBO>> getTestCases(@PathVariable("serviceId") String serviceId, @RequestParam("page") Integer page,
                                                         @RequestParam("pageSize") Integer pageSize) {
    PageSize<TestCaseBO> testCaseDTOS = testCaseService.getTestCaseList(serviceId, page, pageSize);
    return new ResponseMeta<>(ErrorCode.SUCCESS, testCaseDTOS);
  }

  @GetMapping("/e2e/page")
  public ResponseMeta<PageSize<TestCaseBO>> getE2ECases(@RequestParam("page") Integer page,
                                                        @RequestParam("pageSize") Integer pageSize) {
    PageSize<TestCaseBO> testCaseDTOS = testCaseService.getE2ECases(page, pageSize);
    return new ResponseMeta<>(ErrorCode.SUCCESS, testCaseDTOS);
  }

  @GetMapping("/e2e/cases")
  public ResponseMeta<List<TestCaseBO>> getE2ECases() {
    List<TestCaseBO> e2eCases = testCaseService.getE2ECases();
    return new ResponseMeta<>(ErrorCode.SUCCESS, e2eCases);
  }

  @PostMapping("/case")
  public ResponseMeta<String> createTestCases(@RequestBody TestCaseBO testCaseBO) {
    String testCaseId = testCaseService.createTestCase(testCaseBO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, testCaseId);
  }

  @PostMapping("/cases/{caseId}/batch")
  public ResponseMeta<Boolean> startBatchFeatureTask(@PathVariable("caseId") String caseId,
                                                    @Validated @RequestBody BatchExecuteFeature batchExecute) {
    return new ResponseMeta(ErrorCode.SUCCESS, testCaseService.executeFeature(caseId, batchExecute));
  }

  @PutMapping("/case")
  public ResponseMeta<Boolean> updateTestCase(@RequestBody TestCaseBO testCaseBO) {
    Boolean result = testCaseService.updateTestCase(testCaseBO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @GetMapping("/case/{caseId}")
  public ResponseMeta<TestCaseBO> getTestCase(@PathVariable("caseId") String caseId) {
    TestCaseBO testCase = testCaseService.getTestCase(caseId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, testCase);
  }

  @DeleteMapping("/case/{caseId}")
  public ResponseMeta<Boolean> deleteTestCase(@PathVariable("caseId") String caseId) {
    Boolean result = testCaseService.deleteTestCase(caseId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @GetMapping("/case/{caseId}/configs")
  public ResponseMeta<List<TestCaseConfigBO>> getTestCaseConfigs(@PathVariable("caseId") String caseId) {
    List<TestCaseConfigBO> result = testCaseConfigService.getTestCaseConfigs(caseId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @PostMapping("/case/config")
  public ResponseMeta<Integer> addCaseConfig(@RequestBody List<TestCaseConfigBO> configs) {
    Integer result = testCaseConfigService.addCaseConfigs(configs);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @PutMapping("/case/config")
  public ResponseMeta<Boolean> updateCaseConfig(@RequestBody TestCaseConfigBO configDTO) {
    Boolean result = testCaseConfigService.updateCaseConfigs(configDTO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @DeleteMapping("/case/config/{configId}")
  public ResponseMeta<Boolean> deleteCaseConfig(@PathVariable("configId") String configId) {
    Boolean result = testCaseConfigService.deleteCaseConfig(configId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }




}
