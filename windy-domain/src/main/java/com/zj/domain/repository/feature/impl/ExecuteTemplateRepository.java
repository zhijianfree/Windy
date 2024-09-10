package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.TemplateType;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.entity.po.feature.ExecuteTemplate;
import com.zj.domain.mapper.feeature.ExecuteTemplateMapper;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
  public ExecuteTemplateDto getExecuteTemplate(String templateId) {
    ExecuteTemplate executeTemplate = getOne(
        Wrappers.lambdaQuery(ExecuteTemplate.class).eq(ExecuteTemplate::getTemplateId, templateId));
    return OrikaUtil.convert(executeTemplate, ExecuteTemplateDto.class);
  }

  @Override
  public boolean save(ExecuteTemplateDto executeTemplateDto) {
    ExecuteTemplate executeTemplate = OrikaUtil.convert(executeTemplateDto, ExecuteTemplate.class);
    long dateNow = System.currentTimeMillis();
    executeTemplate.setCreateTime(dateNow);
    executeTemplate.setUpdateTime(dateNow);
    return save(executeTemplate);
  }

  @Override
  public boolean updateTemplate(ExecuteTemplateDto executeTemplateDto) {
    ExecuteTemplate executeTemplate = OrikaUtil.convert(executeTemplateDto, ExecuteTemplate.class);
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
  public List<ExecuteTemplateDto> getTemplatesByType(List<Integer> templateTypes) {
    List<ExecuteTemplate> executeTemplates = list(Wrappers.lambdaQuery(ExecuteTemplate.class)
            .in(ExecuteTemplate::getTemplateType, templateTypes));
    return OrikaUtil.convertList(executeTemplates, ExecuteTemplateDto.class);
  }

  @Override
  public List<ExecuteTemplateDto> getToolTemplates() {
    List<Integer> typeList = TemplateType.getToolTemplates();
    List<ExecuteTemplate> executeTemplates = list(Wrappers.lambdaQuery(ExecuteTemplate.class)
            .in(ExecuteTemplate::getTemplateType, typeList));
    return OrikaUtil.convertList(executeTemplates, ExecuteTemplateDto.class);
  }

  @Override
  public List<ExecuteTemplateDto> getServiceTemplates(String serviceId) {
    List<ExecuteTemplate> executeTemplates = list(Wrappers.lambdaQuery(ExecuteTemplate.class).eq(ExecuteTemplate::getOwner, serviceId));
    return OrikaUtil.convertList(executeTemplates, ExecuteTemplateDto.class);
  }

  @Override
  public List<ExecuteTemplateDto> getAllTemplates() {
    List<ExecuteTemplate> executeTemplates = list();
    return OrikaUtil.convertList(executeTemplates, ExecuteTemplateDto.class);
  }

  @Override
  public IPage<ExecuteTemplateDto> getPage(String serviceId, Integer pageNo, Integer size, String name) {
    IPage<ExecuteTemplate> page = new Page<>(pageNo, size);
    LambdaQueryWrapper<ExecuteTemplate> queryWrapper =
            Wrappers.lambdaQuery(ExecuteTemplate. class).eq(ExecuteTemplate::getOwner, serviceId);
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.and(wrapper -> wrapper.like(ExecuteTemplate::getName, name));
    }
    queryWrapper.orderByDesc(ExecuteTemplate::getCreateTime);
    IPage<ExecuteTemplate> templateIPage = page(page, queryWrapper);

    IPage<ExecuteTemplateDto> templateDtoIPage = new Page<>();
    templateDtoIPage.setTotal(templateIPage.getTotal());
    templateDtoIPage.setRecords(
        OrikaUtil.convertList(templateIPage.getRecords(), ExecuteTemplateDto.class));
    return templateDtoIPage;
  }

  @Override
  public Boolean batchAddTemplates(List<ExecuteTemplateDto> templates) {
    List<ExecuteTemplate> templateList = templates.stream().map(dto -> {
      ExecuteTemplate executeTemplate = OrikaUtil.convert(dto, ExecuteTemplate.class);
      executeTemplate.setCreateTime(System.currentTimeMillis());
      executeTemplate.setUpdateTime(System.currentTimeMillis());
      return executeTemplate;
    }).collect(Collectors.toList());
    return saveBatch(templateList);
  }



  @Override
  public List<ExecuteTemplateDto> getTemplateByIds(List<String> templateIds) {
    List<ExecuteTemplate> executeTemplates =
            list(Wrappers.lambdaQuery(ExecuteTemplate.class).in(ExecuteTemplate::getTemplateId, templateIds));
    return OrikaUtil.convertList(executeTemplates, ExecuteTemplateDto.class);
  }
}
