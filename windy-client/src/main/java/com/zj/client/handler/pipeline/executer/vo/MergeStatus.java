package com.zj.client.handler.pipeline.executer.vo;

import java.util.List;
import lombok.Data;

@Data
public class MergeStatus {

  /**
   * 状态
   * */
  private Integer status;

  /**
   * 运行过程消息列表
   * */
  private List<String> message;

  public MergeStatus(Integer status) {
    this.status = status;
  }

  public MergeStatus(Integer status, List<String> message) {
    this.status = status;
    this.message = message;
  }
}
