package com.zj.domain.entity.bo.demand;

import lombok.Data;

@Data
public class BugBO {

  private Long id;

  /**
   * bug名称
   * */
  private String bugName;

  /**
   * bug的ID
   * */
  private String bugId;

  /**
   * bug发生环境
   * */
  private String environment;

  /**
   * bug现象
   * */
  private String scene;

  /**
   * 重现步骤
   * */
  private String replayStep;

  /**
   * 期待结果
   * */
  private String expectResult;

  /**
   * 实际结果
   * */
  private String realResult;

  /**
   * 提出人名称
   * */
  private String proposerName;

  /**
   * 提出人
   * */
  private String proposer;

  /**
   * 接受人名称
   * */
  private String acceptorName;

  /**
   *
   * 接受人*/
  private String acceptor;

  /**
   * bug处理开始时间
   * */
  private Long startTime;

  /**
   * 标签
   * */
  private String tags;

  /**
   * bug级别
   * */
  private Integer level;

  /**
   * bug状态
   * */
  private Integer status;

  /**
   * 需求ID
   */
  private String relationId;

  /**
   * 空间ID
   */
  private String spaceId;

  /**
   * 迭代ID
   */
  private String iterationId;


  /**
   * 工作量
   */
  private Integer workload;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 更新时间
   * */
  private Long updateTime;
}
