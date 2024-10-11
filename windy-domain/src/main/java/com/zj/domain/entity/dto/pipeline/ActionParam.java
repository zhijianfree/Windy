package com.zj.domain.entity.dto.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@Data
public class ActionParam {
  private String name;
  private String description;
  private String position;
  private String type;
  private String value;
}
