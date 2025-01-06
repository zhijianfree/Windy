package com.zj.pipeline.entity.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Data
public class GitlabCommitVo {

  /**
   * 事件分支
   */
  private String ref;

  /**
   * 事件触发人
   */
  @JSONField(name = "user_name")
  private String userName;

  /**
   * 事件类型
   */
  @JSONField(name = "object_kind")
  private String eventType;
}
