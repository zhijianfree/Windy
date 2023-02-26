package com.zj.feature.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.feature.ability.ParameterDefine;
import com.zj.feature.entity.po.ExecuteTemplate;
import java.text.SimpleDateFormat;
import java.util.List;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class ExecuteTemplateDTO {

  private String templateId;
  private String method;
  private String name;
  private Integer templateType;
  private String description;
  private String service;
  private List<ParameterDefine> params;
  private Long createTime;
  private Long updateTime;
  private String author;

  private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public static ExecuteTemplateDTO toExecuteTemplateDTO(ExecuteTemplate executeTemplate) {
    ExecuteTemplateDTO executeTemplateDTO = new ExecuteTemplateDTO();
    BeanUtils.copyProperties(executeTemplate, executeTemplateDTO);
    executeTemplateDTO.setParams(JSON.parseArray(executeTemplate.getParam(), ParameterDefine.class));
    return executeTemplateDTO;
  }
}
