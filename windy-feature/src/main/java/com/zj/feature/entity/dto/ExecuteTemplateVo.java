package com.zj.feature.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.plugin.loader.ParameterDefine;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ExecuteTemplateVo {

  private String templateId;
  private String method;
  private String name;
  private Integer templateType;
  private Integer invokeType;
  private Map<String, String> headers;
  private String description;
  private String service;
  private List<ParameterDefine> params;
  private Long createTime;
  private Long updateTime;
  private String author;

  public static ExecuteTemplateVo toExecuteTemplateDTO(ExecuteTemplateDto executeTemplate) {
    ExecuteTemplateVo executeTemplateVo = OrikaUtil.convert(executeTemplate,
        ExecuteTemplateVo.class);
    executeTemplateVo.setParams(JSON.parseArray(executeTemplate.getParam(), ParameterDefine.class));
    executeTemplateVo.setHeaders((Map<String, String>)JSON.parse(executeTemplate.getHeader()));
    return executeTemplateVo;
  }
}
