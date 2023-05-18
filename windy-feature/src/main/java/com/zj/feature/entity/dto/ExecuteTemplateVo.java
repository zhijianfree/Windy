package com.zj.feature.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.entity.po.feature.ExecuteTemplate;
import com.zj.feature.entity.vo.ParameterDefine;
import java.text.SimpleDateFormat;
import java.util.List;
import lombok.Data;

@Data
public class ExecuteTemplateVo {

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

  public static ExecuteTemplateVo toExecuteTemplateDTO(ExecuteTemplateDto executeTemplate) {
    ExecuteTemplateVo executeTemplateDTO = OrikaUtil.convert(executeTemplate,
        ExecuteTemplateVo.class);
    executeTemplateDTO.setParams(JSON.parseArray(executeTemplate.getParam(), ParameterDefine.class));
    return executeTemplateDTO;
  }
}
