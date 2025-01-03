package com.zj.master.dispatch.generate;

import java.util.List;

import com.zj.common.entity.service.ApiParamModel;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiModel {

  /**
   * http方法
   */
  private String method;

  /**
   * api信息 type = http时api内容为uri type = http时api内容为service#method
   */
  private String resource;

  /**
   * api描述
   */
  private String description;

  private List<ApiParamModel> requestParamList;

  private List<ApiParamModel> responseParamList;

  private String bodyClass;

  private String resultClass;

  /**
   * 代码生成的类名
   * */
  private String className;

  /**
   * 代码生成的类方法名
   * */
  private String classMethod;
}
