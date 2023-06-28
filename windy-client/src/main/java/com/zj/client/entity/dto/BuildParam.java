package com.zj.client.entity.dto;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class BuildParam {

  /**
   * 节点记录Id
   * */
  private String recordId;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * git地址
   * */
  private String gitUrl;

  /**
   * 代码pom地址
   * */
  private String pomPath;

  /**
   * 是否是发布流水线构建
   * */
  private Boolean isPublish;

  /**
   * 构建的分支列表
   * */
  private List<String> branches;
}
