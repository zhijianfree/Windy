package com.zj.client.entity.vo;

import lombok.Data;

@Data
public class GenerateParam {

  private String serviceId;

  private String recordId;

  private String executeParams;

  private String result;

  private Integer status;
}
