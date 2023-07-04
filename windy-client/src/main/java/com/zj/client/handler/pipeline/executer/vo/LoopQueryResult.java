package com.zj.client.handler.pipeline.executer.vo;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/4/11
 */
@Data
public class LoopQueryResult {

  /**
   * 响应状态
   * */
  private Integer status;

  /**
   * 提示信息
   * */
  private List<String> messageList;
}
