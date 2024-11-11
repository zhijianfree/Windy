package com.zj.client.entity.bo;

import lombok.Data;

import java.util.List;

/**
 * @author falcon
 * @since 2023/8/9
 */
@Data
public class ApiParamModel {

  private String paramKey;

  private String type;

  private String position;

  private String description;

  private boolean isRequired;

  private String objectName;

  private List<ApiParamModel> children;
}
