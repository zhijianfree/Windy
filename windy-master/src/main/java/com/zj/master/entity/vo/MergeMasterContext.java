package com.zj.master.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MergeMasterContext extends RequestContext{

  /**
   * 项目tag名称
   */
  private String tagName;

  /**
   * tag描述信息
   */
  private String message;

  /**
   * 合并的分支列表
   */
  private List<String> branches;

  /**
   * 是否删除分支
   */
  private String deleteBranch;

  /**
   * 主干分支列表
   */
  private String mainBranch;
}
