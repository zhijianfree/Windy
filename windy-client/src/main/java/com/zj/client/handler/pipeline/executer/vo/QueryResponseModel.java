package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Data
public class QueryResponseModel {

  private Integer status;

  private Map<String, Object> context;

  private List<String> message;

  private Object data;

  public void addMessage(String msg) {
    if (CollectionUtils.isEmpty(message)) {
      message = new ArrayList<>();
    }
    message.add(msg);
  }

  @Data
  public static class ResponseStatus{

    private Integer status;
  }
}