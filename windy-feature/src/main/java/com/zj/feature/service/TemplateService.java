package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.feature.entity.dto.ExecuteTemplateVo;
import com.zj.feature.entity.vo.ExecutorUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TemplateService {

  private UniqueIdService uniqueIdService;
  private IExecuteTemplateRepository executeTemplateRepository;
  private IExecutePointRepository executePointRepository;

  public TemplateService(UniqueIdService uniqueIdService,
      IExecuteTemplateRepository executeTemplateRepository,
      IExecutePointRepository executePointRepository) {
    this.uniqueIdService = uniqueIdService;
    this.executeTemplateRepository = executeTemplateRepository;
    this.executePointRepository = executePointRepository;
  }

  public PageSize<ExecuteTemplateVo> getTemplatePage(Integer pageNo, Integer size, String name) {
    IPage<ExecuteTemplateDto> templateIPage = executeTemplateRepository.getPage(pageNo, size, name);
    PageSize<ExecuteTemplateVo> pageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(templateIPage.getRecords())) {
      pageSize.setTotal(0);
      return pageSize;
    }

    List<ExecuteTemplateVo> templateDTOS = templateIPage.getRecords().stream()
        .map(ExecuteTemplateVo::toExecuteTemplateDTO).collect(Collectors.toList());
    pageSize.setData(templateDTOS);
    pageSize.setTotal(templateIPage.getTotal());
    return pageSize;
  }

  public ExecuteTemplateVo getExecuteTemplate(String templateId) {
    ExecuteTemplateDto executeTemplate = executeTemplateRepository.getExecuteTemplate(templateId);
    return ExecuteTemplateVo.toExecuteTemplateDTO(executeTemplate);
  }

  public String createTemplate(ExecuteTemplateVo executeTemplateVo) {
    ExecuteTemplateDto executeTemplate = OrikaUtil.convert(executeTemplateVo,
        ExecuteTemplateDto.class);
    executeTemplate.setTemplateId(uniqueIdService.getUniqueId());
    executeTemplate.setAuthor("admin");
    executeTemplate.setCreateTime(System.currentTimeMillis());
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    executeTemplate.setHeader(JSON.toJSONString(executeTemplateVo.getHeaders()));
    executeTemplate.setParam(JSON.toJSONString(executeTemplateVo.getParams()));
    boolean result = executeTemplateRepository.save(executeTemplate);
    return result ? executeTemplate.getTemplateId() : "";
  }

  public String updateTemplate(ExecuteTemplateVo executeTemplateVo) {
    ExecuteTemplateDto executeTemplate = OrikaUtil.convert(executeTemplateVo,
        ExecuteTemplateDto.class);
    executeTemplate.setAuthor("admin");
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    executeTemplate.setParam(JSON.toJSONString(executeTemplateVo.getParams()));
    executeTemplate.setHeader(JSON.toJSONString(executeTemplateVo.getHeaders()));
    return executeTemplateRepository.updateTemplate(executeTemplate)
        ? executeTemplate.getTemplateId() : "";
  }

  public Boolean deleteExecuteTemplate(String templateId) {
    return executeTemplateRepository.deleteTemplate(templateId);
  }

  public List<ExecuteTemplateVo> getFeatureList() {
    List<ExecuteTemplateDto> executeTemplates =  executeTemplateRepository.getAllTemplates();
    return executeTemplates.stream().map(ExecuteTemplateVo::toExecuteTemplateDTO)
        .collect(Collectors.toList());
  }

  public Boolean refreshTemplate(String templateId) {
    List<ExecutePointDto> executePoints = executePointRepository.getTemplateExecutePoints(
        templateId);
    if (CollectionUtils.isEmpty(executePoints)) {
      return true;
    }

    ExecuteTemplateDto executeTemplate = executeTemplateRepository.getExecuteTemplate(templateId);
    List<ExecutePointDto> updatePoints = executePoints.stream().peek(executePoint -> {
      String featureInfo = executePoint.getFeatureInfo();
      ExecutorUnit executorUnit = JSON.parseObject(featureInfo, ExecutorUnit.class);
      executorUnit.setService(executeTemplate.getService());
      executorUnit.setMethod(executeTemplate.getMethod());
      executorUnit.setInvokeType(executeTemplate.getInvokeType());
      Map<String, String> map = (Map<String, String>) JSON.parseObject(executeTemplate.getHeader(),
          Map.class);
      executorUnit.setHeaders(map);
      executePoint.setFeatureInfo(JSON.toJSONString(executorUnit));
    }).collect(Collectors.toList());

    return executePointRepository.updateBatch(updatePoints);
  }
}
