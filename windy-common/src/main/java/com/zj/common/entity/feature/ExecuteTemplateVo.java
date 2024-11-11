package com.zj.common.entity.feature;

import com.zj.plugin.loader.ParameterDefine;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExecuteTemplateVo {
  /**
   * 模版Id
   */
  private String templateId;

  /**
   * 模版名称
   */
  private String name;

  /**
   * 模版类型
   */
  private Integer templateType;

  /**
   * 调用类型
   */
  private Integer invokeType;

  /**
   * 模版描述
   */
  private String description;

  /**
   * 如果invokeType是2或者3的时候 service存放的是url
   * 如果invokeType是1，service存放的是类名
   */
  private String service;

  /**
   *    * 如果invokeType是2或者3的时候 method存放的是HTTP方法
   *    * 如果invokeType是1，method存放的是类的方法名
   */
  private String method;

  /**
   * 如果invokeType是2或者3的时候，headers存放请求的header信息
   */
  private Map<String, String> headers;

  /**
   * 请求的参数
   */
  private List<ParameterDefine> params;

  /**
   * 模版所属的服务
   */
  private String owner;

  /**
   * 关联的模版Id，在实际执行模版的时候使用
   */
  private String relatedId;

  private Long createTime;

  private Long updateTime;
}
