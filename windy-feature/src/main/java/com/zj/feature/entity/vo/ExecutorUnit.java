package com.zj.feature.entity.vo;

import com.zj.feature.entity.dto.ExecutePointVo;
import com.zj.feature.entity.dto.ParamDefineDto;
import java.util.List;
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
  private Integer executeType;

  /**
   * 执行方法
   */
  private String method;

  /**
   * 方法参数
   */
  private List<ParamDefineDto> params;

  /**
   * 针对for循环的需要特殊处理
   */
  private List<ExecutePointVo> executePoints;
}
