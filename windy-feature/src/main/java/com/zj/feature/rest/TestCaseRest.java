package com.zj.feature.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.common.PageSize;
import com.zj.feature.entity.dto.TestCaseConfigDTO;
import com.zj.feature.entity.dto.TestCaseDTO;
import com.zj.feature.service.TestCaseConfigService;
import com.zj.feature.service.TestCaseService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/v1/devops/feature")
@RestController
public class TestCaseRest {

  @Autowired
  private TestCaseService testCaseService;

  @Autowired
  private TestCaseConfigService testCaseConfigService;

  @RequestMapping("/{serviceId}/cases")
  public ResponseMeta<PageSize<TestCaseDTO>> getTestCases(@PathVariable("serviceId") String serviceId, @RequestParam("page") Integer page,
      @RequestParam("pageSize") Integer pageSize) {
    PageSize<TestCaseDTO> testCaseDTOS = testCaseService.getTestCaseList(serviceId, page, pageSize);
    return new ResponseMeta<>(ErrorCode.SUCCESS, testCaseDTOS);
  }

  @PostMapping("/case")
  public ResponseMeta<String> createTestCases(@RequestBody TestCaseDTO testCaseDTO) {
    String testCaseId = testCaseService.createTestCase(testCaseDTO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, testCaseId);
  }

  @PutMapping("/case")
  public ResponseMeta<Boolean> updateTestCase(@RequestBody TestCaseDTO testCaseDTO) {
    Boolean result = testCaseService.updateTestCase(testCaseDTO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @GetMapping("/case/{caseId}")
  public ResponseMeta<TestCaseDTO> updateTestCase(@PathVariable("caseId") String caseId) {
    TestCaseDTO testCase = testCaseService.getTestCase(caseId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, testCase);
  }

  @DeleteMapping("/case/{caseId}")
  public ResponseMeta<Boolean> deleteTestCase(@PathVariable("caseId") String caseId) {
    Boolean result = testCaseService.deleteTestCase(caseId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @GetMapping("/case/{caseId}/configs")
  public ResponseMeta<List<TestCaseConfigDTO>> getTestCaseConfigs(@PathVariable("caseId") String caseId) {
    List<TestCaseConfigDTO> result = testCaseConfigService.getTestCaseConfigs(caseId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @PostMapping("/case/config")
  public ResponseMeta<Integer> addCaseConfig(@RequestBody List<TestCaseConfigDTO> configs) {
    Integer result = testCaseConfigService.addCaseConfigs(configs);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @PutMapping("/case/config")
  public ResponseMeta<Boolean> updateCaseConfig(@RequestBody TestCaseConfigDTO configDTO) {
    Boolean result = testCaseConfigService.updateCaseConfigs(configDTO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @DeleteMapping("/case/config/{configId}")
  public ResponseMeta<Boolean> deleteCaseConfig(@PathVariable("configId") String configId) {
    Boolean result = testCaseConfigService.deleteCaseConfig(configId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }
}
