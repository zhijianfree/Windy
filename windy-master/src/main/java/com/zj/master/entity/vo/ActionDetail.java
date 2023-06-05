package com.zj.master.entity.vo;

import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import lombok.Data;

/**
 * @author guyuelan
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
