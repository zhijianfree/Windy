package com.zj.service.entity;

import java.util.List;
import java.util.Map;

import com.zj.common.enums.ApiType;
import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiModel {

  @NotBlank(groups = Update.class)
  private String apiId;

  /**
   * api名称
   */
  @NotBlank(groups = Create.class)
  private String apiName;

  /**
   * 服务Id
   */
  @NotBlank(groups = Create.class)
  private String serviceId;

  /**
   * 父节点Id
   */
  private String parentId;

  /**
   * api 请求方式 http、dubbo
   */
  private String type;

  /**
   * API类型 0 {@link ApiType}
   */
  @Max(1)
  @Min(0)
  private Integer apiType;

  /**
   * http方法
   */
  @Length(max = 100)
  private String method;

  /**
   * api信息 type = http时api内容为uri type = http时api内容为service#method
   */
  @Length(max = 256 )
  private String resource;

  /**
   * api描述
   */
  @Length(max = 256 )
  private String description;

  /**
   * 请求参数列表
   */
  private List<ApiRequestVariable> requestParams;

  /**
   * 响应参数列表
   */
  private List<ApiResponse> responseParams;

  /**
   * 请求的body类名
   * */
  private String bodyClass;

  /**
   * 响应结果类名
   * */
  private String resultClass;

  /**
   * 代码生成的类名
   * */
  private String className;

  /**
   * 代码生成的类方法名
   * */
  private String classMethod;

  /**
   * header请求头
   */
  private Map<String, String> header;
}
