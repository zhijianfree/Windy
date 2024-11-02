package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IExecuteTemplateRepository {

  ExecuteTemplateBO getExecuteTemplate(String templateId);

  boolean save(ExecuteTemplateBO executeTemplate);

  boolean updateTemplate(ExecuteTemplateBO executeTemplate);

  Boolean deleteTemplate(String templateId);

  IPage<ExecuteTemplateBO> getPage(String serviceId, Integer pageNo, Integer size, String name);

  Boolean batchAddTemplates(List<ExecuteTemplateBO> templates);

  List<ExecuteTemplateBO> getServiceTemplates(String serviceId);

  List<ExecuteTemplateBO> getAllTemplates();

  List<ExecuteTemplateBO> getTemplatesByType(List<Integer> templateTypes);

  List<ExecuteTemplateBO> getToolTemplates();

  List<ExecuteTemplateBO> getTemplateByIds(List<String> templateIds);
}
