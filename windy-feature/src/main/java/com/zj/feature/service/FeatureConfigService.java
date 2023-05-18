package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.feature.entity.dto.ExecuteTemplateVo;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FeatureConfigService {

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private IExecuteTemplateRepository executeTemplateRepository;

  public PageSize<ExecuteTemplateVo> getFeaturePage(Integer pageNo, Integer size, String name) {
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

  public String createTemplate(ExecuteTemplateVo executeTemplateDTO) {
    ExecuteTemplateDto executeTemplate = OrikaUtil.convert(executeTemplateDTO,
        ExecuteTemplateDto.class);
    executeTemplate.setTemplateId(uniqueIdService.getUniqueId());
    executeTemplate.setAuthor("admin");
    executeTemplate.setCreateTime(System.currentTimeMillis());
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    executeTemplate.setParam(JSON.toJSONString(executeTemplateDTO.getParams()));
    boolean result = executeTemplateRepository.save(executeTemplate);
    return result ? executeTemplate.getTemplateId() : "";
  }

  public String updateTemplate(ExecuteTemplateVo executeTemplateVo) {
    ExecuteTemplateDto executeTemplate = OrikaUtil.convert(executeTemplateVo,
        ExecuteTemplateDto.class);
    executeTemplate.setAuthor("admin");
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    executeTemplate.setParam(JSON.toJSONString(executeTemplateVo.getParams()));
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
}
