package com.zj.service.entity;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiRequestVariable {

  private String paramKey;

  private String type;

  private String position;

  private String description;

  private boolean isRequired;

  private String objectName;

  private String defaultValue;

  private List<ApiRequestVariable> children;
}
