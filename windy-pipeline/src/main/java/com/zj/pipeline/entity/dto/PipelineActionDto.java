package com.zj.pipeline.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.common.utils.OrikaUtil;
import com.zj.pipeline.entity.po.PipelineAction;
import com.zj.pipeline.entity.vo.ActionParam;
import com.zj.pipeline.entity.vo.CompareResult;
import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@Data
public class PipelineActionDto {
  /**
   * 执行动作Id
   */
  private String actionId;

  /**
   * 执行动作名称
   */
  private String actionName;

  /**
   * 所属节点Id
   */
  private String nodeId;

  /**
   * 用户Id
   */
  private String userId;

  /**
   * 描述
   */
  private String description;

  /**
   * 执行请求url
   */
  private String actionUrl;
  private List<ActionParam> paramList;
  private String queryUrl;
  private List<CompareResult> compareResults;

  /**
   * 节点执行方式
   */
  private String executeType;
  private Long createTime;
  private Long updateTime;

  public PipelineAction toPipelineAction() {
    PipelineAction pipelineAction = OrikaUtil.convert(this, PipelineAction.class);
    pipelineAction.setParamDetail(JSON.toJSONString(this.getParamList()));
    pipelineAction.setResult(JSON.toJSONString(this.getCompareResults()));
    return pipelineAction;
  }

  public static PipelineActionDto toPipelineActionDto(PipelineAction action) {
    PipelineActionDto pipelineAction = OrikaUtil.convert(action, PipelineActionDto.class);
    pipelineAction.setParamList(JSON.parseArray(action.getParamDetail(), ActionParam.class));
    pipelineAction.setCompareResults(JSON.parseArray(action.getResult(), CompareResult.class));
    return pipelineAction;
  }
}
