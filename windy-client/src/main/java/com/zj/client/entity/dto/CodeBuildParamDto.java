package com.zj.client.entity.dto;

import com.zj.client.handler.pipeline.executer.vo.GitMeta;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodeBuildParamDto extends GitMeta {

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
  private boolean publish;

  /**
   * 构建的分支列表
   * */
  private List<String> branches;

  /**
   * 镜像仓库地址
   * */
  private String repository;

  /**
   * 推送仓库用户
   * */
  private String user;

  /**
   * 推送仓库密码
   * */
  private String password;
}