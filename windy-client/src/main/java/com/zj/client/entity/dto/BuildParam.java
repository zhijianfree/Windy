package com.zj.client.entity.dto;

import com.zj.client.handler.pipeline.executer.vo.GitMeta;
import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class BuildParam extends GitMeta {

  /**
   * 节点记录Id
   * */
  private String recordId;

  /**
   * 流水线Id
   * */
  private String pipelineId;

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
