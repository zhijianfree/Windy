package com.zj.common.entity.dto;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class ResponseStatusModel {

  private Integer status;
  private Object data;
  private String message;

  @Data
  public static class PercentStatics{

    private Integer percent;
  }
}
