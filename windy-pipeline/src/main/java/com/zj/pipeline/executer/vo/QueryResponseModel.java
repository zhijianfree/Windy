package com.zj.pipeline.executer.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/8
 */
@Data
public class QueryResponseModel {

  private String message;

  private ResponseData data;

  @Data
  public static class ResponseData{

    private Integer status;
  }
}
