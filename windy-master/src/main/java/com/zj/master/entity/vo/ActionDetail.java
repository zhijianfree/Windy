package com.zj.master.entity.vo;

import com.zj.common.entity.pipeline.ConfigDetail;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Data
public class ActionDetail {

  private ConfigDetail configDetail;

  private PipelineActionBO action;

  public ActionDetail(ConfigDetail configDetail, PipelineActionBO action) {
    this.configDetail = configDetail;
    this.action = action;
  }
}
