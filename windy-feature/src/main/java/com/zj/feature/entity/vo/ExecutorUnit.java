package com.zj.feature.entity.vo;

import com.zj.plugin.loader.ParameterDefine;
import com.zj.feature.entity.dto.ExecutePointVo;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ExecutorUnit {

  /**
   * 用例名称
   */
  private String name;

  /**
   * 执行类名称
   */
  private String service;


  /**
   * 执行方式
   * */
  private Integer invokeType;

  /**
   * 执行方法
   */
  private String method;

  /**
   * http类型时使用
   * */
  private Map<String, String> headers;

  /**
   * 方法参数
   */
  private List<ParameterDefine> params;

  /**
   * 针对for循环的需要特殊处理
   */
  private List<ExecutePointVo> executePoints;
}
