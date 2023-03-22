package com.zj.pipeline.entity.po;

import lombok.Data;

/**
 * @author falcon
 * @since 2021/9/28
 */
@Data
public class GitBind {

  private Long id;

  /**
   * 绑定Id
   * */
  private String bindId;

  /**
   * 绑定分支
   * */
  private String gitBranch;

  /**
   * git地址
   * */
  private String gitUrl;

  /**
   * 绑定类型： 0 push  1 merge
   * */
  private Integer bindType;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 更新时间
   * */
  private Long updateTime;
}
