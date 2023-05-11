package com.zj.pipeline.executer.vo;

import com.alibaba.fastjson.JSONObject;
import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/8
 */
@Data
public class QueryResponseModel {
  private Integer status;

  private List<String> message;

  private JSONObject data;
}