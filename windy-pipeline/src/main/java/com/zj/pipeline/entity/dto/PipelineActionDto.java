package com.zj.pipeline.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.pipeline.entity.po.PipelineAction;
import com.zj.pipeline.entity.vo.ActionParam;
import com.zj.pipeline.entity.vo.CompareResult;
import java.util.List;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @author falcon
 * @since 2023/3/27
 */
@Data
public class PipelineActionDto {

  private String actionId;
  private String actionName;
  private String nodeId;
  private String description;
  private String userId;
  private String actionUrl;
  private List<ActionParam> paramDetail;
  private String queryUrl;
  private List<CompareResult> results;
  private Long createTime;
  private Long updateTime;

  public PipelineAction toPipelineAction() {
    PipelineAction pipelineAction = new PipelineAction();
    BeanUtils.copyProperties(this, pipelineAction);
    pipelineAction.setParamDetail(JSON.toJSONString(this.getParamDetail()));
    pipelineAction.setResult(JSON.toJSONString(this.getResults()));
    return pipelineAction;
  }

  public static PipelineActionDto toPipelineActionDto(PipelineAction action) {
    PipelineActionDto pipelineAction = new PipelineActionDto();
    BeanUtils.copyProperties(action, pipelineAction);
    pipelineAction.setParamDetail(JSON.parseArray(action.getParamDetail(), ActionParam.class));
    pipelineAction.setResults(JSON.parseArray(action.getResult(), CompareResult.class));
    return pipelineAction;
  }
}
