package com.zj.pipeline.entity.vo;

import com.zj.pipeline.entity.dto.PipelineActionDto;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/9
 */
@Data
public class ActionDetail {

  private ConfigDetail configDetail;

  private PipelineActionDto action;

  public ActionDetail(ConfigDetail configDetail, PipelineActionDto action) {
    this.configDetail = configDetail;
    this.action = action;
  }
}
