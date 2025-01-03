package com.zj.service.entity;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiResponse {

  private String paramKey;

  private String type;

  private String description;

  private boolean required;

  private String objectName;

  private List<ApiResponse> children;
}
