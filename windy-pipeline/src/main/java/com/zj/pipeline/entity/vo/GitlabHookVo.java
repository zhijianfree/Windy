package com.zj.pipeline.entity.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Data
public class GitlabHookVo {

  private String ref;

  private String project;

  private String message;

  @JSONField(name = "commit_id")
  private String commitId;

  @JSONField(name = "event_type")
  private String eventType;
}
