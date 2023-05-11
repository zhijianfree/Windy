package com.zj.pipeline.rest;

import com.zj.common.ResponseMeta;
import com.zj.pipeline.entity.dto.CodeChangeDto;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.entity.dto.RelationDemandBug;
import com.zj.pipeline.service.CodeChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author guyuelan
 * @since 2021/10/15
 */

@RequestMapping("/v1/devops/pipeline")
@RestController
public class CodeChangeRest {

  @Autowired
  private CodeChangeService codeChangeService;

  @ResponseBody
  @GetMapping("/{serviceId}/change/{codeChangeId}")
  public ResponseMeta<CodeChangeDto> queryCodeChange(
      @NotNull @PathVariable("codeChangeId") String codeChangeId,
      @NotNull @PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<CodeChangeDto>(ErrorCode.SUCCESS, codeChangeService.getCodeChange(serviceId, codeChangeId));
  }

  @ResponseBody
  @GetMapping("/relations")
  public ResponseMeta<List<RelationDemandBug>> queryRelationIds(
      @RequestParam("name") String queryName) {
    return new ResponseMeta<List<RelationDemandBug>>(ErrorCode.SUCCESS, codeChangeService.queryRelationIds(queryName));
  }

  @ResponseBody
  @PostMapping("/change")
  public ResponseMeta<String> createCodeChange(
      @NotNull @Validated @RequestBody CodeChangeDto codeChangeDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS, codeChangeService.createCodeChange(codeChangeDto));
  }

  @ResponseBody
  @PutMapping("/{serviceId}/change/{codeChangeId}")
  public ResponseMeta<Boolean> updateCodeChange(
      @NotNull @PathVariable("serviceId") String serviceId,
      @NotNull @PathVariable("codeChangeId") String codeChangeId,
      @NotNull @RequestBody CodeChangeDto codeChangeDto) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, codeChangeService.updateCodeChange(serviceId, codeChangeId, codeChangeDto));
  }

  @ResponseBody
  @GetMapping("/{serviceId}/changes")
  public ResponseMeta<List<CodeChangeDto>> listCodeChanges(
      @NotNull @PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<List<CodeChangeDto>>(ErrorCode.SUCCESS,
        codeChangeService.listCodeChanges(serviceId));
  }

  @ResponseBody
  @DeleteMapping("/{serviceId}/change/{codeChangeId}")
  public ResponseMeta<Boolean> deleteCodeChange(
      @NotNull @PathVariable("serviceId") String serviceId,
      @NotNull @PathVariable("codeChangeId") String codeChangeId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
        codeChangeService.deleteCodeChange(serviceId, codeChangeId));
  }
}
