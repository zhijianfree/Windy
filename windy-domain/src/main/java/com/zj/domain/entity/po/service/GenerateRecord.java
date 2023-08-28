package com.zj.domain.entity.po.service;

import lombok.Data;

@Data
public class GenerateRecord {

  private Long id;

  private String recordId;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * 执行参数
   * */
  private String executeParams;

  /**
   * 执行结果记录
   * */
  private String result;

  /**
   * 执行状态
   * */
  private Integer status;


  private Long createTime;

  private Long updateTime;
}
