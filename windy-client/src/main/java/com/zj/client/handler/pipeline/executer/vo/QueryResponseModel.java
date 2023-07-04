package com.zj.client.handler.pipeline.executer.vo;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Data
public class QueryResponseModel {
  private Integer status;

  private List<String> message;

  private JSONObject data;

  public void addMessage(String msg) {
    if (CollectionUtils.isEmpty(message)) {
      message = new ArrayList<>();
    }
    message.add(msg);
  }
}
