package com.zj.service.entity;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiRequest {

  private String paramKey;

  private String type;

  private String position;

  private String description;

  private boolean isRequired;

  private String objectName;

  private List<ApiRequest> children;
}
