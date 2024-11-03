package com.zj.common.entity.generate;

import lombok.Data;

import java.util.List;

@Data
public class GenerateRecordBO {

  private String recordId;

  /**
   * 服务Id
   * */
  private String serviceId;

  /**
   * 执行参数
   * */
  private GenerateDetail generateParams;

  /**
   * 执行结果记录
   * */
  private List<String> generateResult;

  /**
   * 构建的版本号
   */
  private String version;

  /**
   * 执行状态
   * */
  private Integer status;


  private Long createTime;

  private Long updateTime;
}
