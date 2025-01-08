package com.zj.domain.repository.feature.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.TemplateType;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import com.zj.domain.entity.po.feature.ExecuteTemplate;
import com.zj.domain.mapper.feeature.ExecuteTemplateMapper;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.plugin.loader.ParameterDefine;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Repository
public class ExecuteTemplateRepository extends
    ServiceImpl<ExecuteTemplateMapper, ExecuteTemplate> implements IExecuteTemplateRepository {

  @Override
  public ExecuteTemplateBO getExecuteTemplate(String templateId) {
    ExecuteTemplate executeTemplate = getOne(
        Wrappers.lambdaQuery(ExecuteTemplate.class).eq(ExecuteTemplate::getTemplateId, templateId));
    return convertExecuteTemplateBO(executeTemplate);
  }

  @Override
  public boolean save(ExecuteTemplateBO executeTemplateBO) {
    ExecuteTemplate executeTemplate = convertExecuteTemplate(executeTemplateBO);
    long dateNow = System.currentTimeMillis();
    executeTemplate.setCreateTime(dateNow);
    executeTemplate.setUpdateTime(dateNow);
    return save(executeTemplate);
  }

  @Override
  public boolean updateTemplate(ExecuteTemplateBO executeTemplateBO) {
    ExecuteTemplate executeTemplate = convertExecuteTemplate(executeTemplateBO);
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    return update(executeTemplate, Wrappers.lambdaUpdate(ExecuteTemplate.class)
        .eq(ExecuteTemplate::getTemplateId, executeTemplate.getTemplateId()));
  }

  @Override
  public Boolean deleteTemplate(String templateId) {
    return remove(
        Wrappers.lambdaQuery(ExecuteTemplate.class).eq(ExecuteTemplate::getTemplateId, templateId));
  }

  @Override
  public List<ExecuteTemplateBO> getTemplatesByType(List<Integer> templateTypes) {
    if (CollectionUtils.isEmpty(templateTypes)) {
      return Collections.emptyList();
    }
    List<ExecuteTemplate> executeTemplates = list(Wrappers.lambdaQuery(ExecuteTemplate.class)
            .in(ExecuteTemplate::getTemplateType, templateTypes).orderByDesc(ExecuteTemplate::getCreateTime));
    return convertExecuteTemplateBOList(executeTemplates);
  }

  @Override
  public List<ExecuteTemplateBO> getToolTemplates() {
    List<Integer> typeList = TemplateType.getToolTemplates();
    List<ExecuteTemplate> executeTemplates = list(Wrappers.lambdaQuery(ExecuteTemplate.class)
            .in(ExecuteTemplate::getTemplateType, typeList));
    return convertExecuteTemplateBOList(executeTemplates);
  }

  @Override
  public List<ExecuteTemplateBO> getServiceTemplates(String serviceId) {
    List<ExecuteTemplate> executeTemplates = list(Wrappers.lambdaQuery(ExecuteTemplate.class)
            .eq(ExecuteTemplate::getOwner, serviceId));
    return convertExecuteTemplateBOList(executeTemplates);
  }

  @Override
  public List<ExecuteTemplateBO> getTemplatesByServiceIds(List<String> serviceIds) {
    if (CollectionUtils.isEmpty(serviceIds)) {
      return Collections.emptyList();
    }
    List<ExecuteTemplate> executeTemplates = list(Wrappers.lambdaQuery(ExecuteTemplate.class)
            .in(ExecuteTemplate::getOwner, serviceIds));
    return convertExecuteTemplateBOList(executeTemplates);
  }

  @Override
  public List<ExecuteTemplateBO> getPluginTemplates(String pluginId) {
    List<ExecuteTemplate> executeTemplates = list(Wrappers.lambdaQuery(ExecuteTemplate.class)
            .eq(ExecuteTemplate::getSource, pluginId));
    return convertExecuteTemplateBOList(executeTemplates);
  }

  @Override
  public List<ExecuteTemplateBO> getAllTemplates() {
    List<ExecuteTemplate> executeTemplates = list();
    return convertExecuteTemplateBOList(executeTemplates);
  }

  @Override
  public IPage<ExecuteTemplateBO> getPage(String serviceId, Integer pageNo, Integer size, String name) {
    IPage<ExecuteTemplate> page = new Page<>(pageNo, size);
    LambdaQueryWrapper<ExecuteTemplate> queryWrapper =
            Wrappers.lambdaQuery(ExecuteTemplate. class).eq(ExecuteTemplate::getOwner, serviceId);
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.and(wrapper -> wrapper.like(ExecuteTemplate::getName, name));
    }
    queryWrapper.orderByDesc(ExecuteTemplate::getCreateTime);
    IPage<ExecuteTemplate> templateIPage = page(page, queryWrapper);

    IPage<ExecuteTemplateBO> templateDtoIPage = new Page<>();
    templateDtoIPage.setTotal(templateIPage.getTotal());
    templateDtoIPage.setRecords(convertExecuteTemplateBOList(templateIPage.getRecords()));
    return templateDtoIPage;
  }

  @Override
  public Boolean batchAddTemplates(List<ExecuteTemplateBO> templates) {
    if (CollectionUtils.isEmpty(templates)) {
      return false;
    }
    List<ExecuteTemplate> templateList = templates.stream().map(executeTemplateBO -> {
      ExecuteTemplate executeTemplate = convertExecuteTemplate(executeTemplateBO);
      executeTemplate.setCreateTime(System.currentTimeMillis());
      executeTemplate.setUpdateTime(System.currentTimeMillis());
      return executeTemplate;
    }).collect(Collectors.toList());
    return saveBatch(templateList);
  }


  @Override
  public List<ExecuteTemplateBO> getTemplateByIds(List<String> templateIds) {
    if (CollectionUtils.isEmpty(templateIds)) {
      return Collections.emptyList();
    }
    List<ExecuteTemplate> executeTemplates =
            list(Wrappers.lambdaQuery(ExecuteTemplate.class).in(ExecuteTemplate::getTemplateId, templateIds));
    return convertExecuteTemplateBOList(executeTemplates);
  }

  private ExecuteTemplateBO convertExecuteTemplateBO(ExecuteTemplate executeTemplate) {
    ExecuteTemplateBO executeTemplateBO = OrikaUtil.convert(executeTemplate, ExecuteTemplateBO.class);
    executeTemplateBO.setParameterDefines(JSON.parseArray(executeTemplate.getParam(), ParameterDefine.class));
    return executeTemplateBO;
  }

  private ExecuteTemplate convertExecuteTemplate(ExecuteTemplateBO executeTemplateBO) {
    ExecuteTemplate executeTemplate = OrikaUtil.convert(executeTemplateBO, ExecuteTemplate.class);
    executeTemplate.setParam(JSON.toJSONString(executeTemplateBO.getParameterDefines()));
    return executeTemplate;
  }

  private List<ExecuteTemplateBO> convertExecuteTemplateBOList(List<ExecuteTemplate> executeTemplates) {
    return executeTemplates.stream().map(this::convertExecuteTemplateBO).collect(Collectors.toList());
  }
}
