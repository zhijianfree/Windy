package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IExecuteTemplateRepository {

  ExecuteTemplateDto getExecuteTemplate(String templateId);

  boolean save(ExecuteTemplateDto executeTemplate);

  boolean updateTemplate(ExecuteTemplateDto executeTemplate);

  Boolean deleteTemplate(String templateId);

  IPage<ExecuteTemplateDto> getPage(String serviceId, Integer pageNo, Integer size, String name);

  Boolean batchAddTemplates(List<ExecuteTemplateDto> templates);

    List<ExecuteTemplateDto> getServiceTemplates(String serviceId);
}
