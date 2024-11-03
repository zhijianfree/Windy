package com.zj.domain.entity.bo.demand;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/4/17
 */
@Data
public class CommentBO {

  private Long id;

  /**
   * 评论Id
   */
  private String commentId;

  /**
   * 关联Id（需求或者缺陷Id）
   */
  private String relativeId;

  /**
   * 评论内容
   */
  private String comment;

  /**
   * 评论用户Id
   */
  private String userId;

  /**
   * 用户名称
   */
  private String userName;

  private Long createTime;

  private Long updateTime;
}
