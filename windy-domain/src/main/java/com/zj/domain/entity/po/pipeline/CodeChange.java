package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Data
public class CodeChange {

  private Long id;

  /**
   * 变更ID
   */
  private String changeId;

  /**
   * 变更名称
   */
  private String changeName;

  /**
   * 变更描述
   */
  private String changeDesc;

  /**
   * 变更分支
   */
  private String changeBranch;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * 关联ID 每次的变更触发可以与需求或者是bug或者是一个优化项关联，通过这个关联的ID就可以在后续的代码工作中串联起来 达到观察工作流的作用
   * <p>
   * 同样关联ID也可以作为与第三方合作的打通ID
   */
  private String relationId;

  /**
   * 关联业务类型
   */
  private Integer relationType;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 更新时间
   * */
  private Long updateTime;
}
