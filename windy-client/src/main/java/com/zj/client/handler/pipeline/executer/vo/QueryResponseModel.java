package com.zj.client.handler.pipeline.executer.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

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

  private List<String> message = new ArrayList<>();

  private Object data;

  public void addMessage(String msg) {
    message.add(msg);
  }

  public void addAllMessage(List<String> msgList) {
    message.addAll(msgList);
  }

  @Data
  @AllArgsConstructor
  public static class ResponseStatus{

    private Integer status;
  }
}
