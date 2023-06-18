package com.zj.client.pipeline.executer.vo;

import java.util.List;
import lombok.Data;

@Data
public class TempStatus {

  /**
   * 状态
   * */
  private Integer status;

  /**
   * 运行过程消息列表
   * */
  private List<String> message;
}
