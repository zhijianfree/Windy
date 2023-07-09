package com.zj.domain.entity.dto.service;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zj.domain.entity.enums.EnvType;
import lombok.Data;

@Data
@TableName("environment")
public class DeployEnvironmentDto {

  /**
   * 环境Id
   * */
  private String envId;

  /**
   * 环境名称
   * */
  private String envName;

  /**
   * 环境状态
   * */
  private Integer envStatus;


  /**
   * 环境类型 {@link EnvType}
   * */
  private Integer envType;

  /**
   * 环境执行的必要参数
   * */
  private String envParams;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 修改时间
   * */
  private Long updateTime;

}
