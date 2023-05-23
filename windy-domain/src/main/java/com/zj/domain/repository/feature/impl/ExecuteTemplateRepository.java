package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.entity.enums.ExecutePointType;
import com.zj.domain.entity.po.feature.ExecuteTemplate;
import com.zj.domain.mapper.feeature.ExecuteTemplateMapper;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
  public List<ExecuteTemplateDto> getAllTemplates() {
    List<ExecuteTemplate> executeTemplates = list();
    return OrikaUtil.convertList(executeTemplates, ExecuteTemplateDto.class);
  }

  @Override
  public IPage<ExecuteTemplateDto> getPage(Integer pageNo, Integer size, String name) {
    IPage<ExecuteTemplate> page = new Page<>(pageNo, size);
    LambdaQueryWrapper<ExecuteTemplate> queryWrapper = Wrappers.lambdaQuery(ExecuteTemplate.class)
        .eq(ExecuteTemplate::getTemplateType, ExecutePointType.NORMAL.getType());
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.and(wrapper -> wrapper.like(ExecuteTemplate::getName, name));
    }
    IPage<ExecuteTemplate> templateIPage = page(page, queryWrapper);

    IPage<ExecuteTemplateDto> templateDtoIPage = new Page<>();
    templateDtoIPage.setTotal(templateIPage.getTotal());
    templateDtoIPage.setRecords(OrikaUtil.convertList(templateIPage.getRecords(),
        ExecuteTemplateDto.class));
    return templateDtoIPage;
  }
}
