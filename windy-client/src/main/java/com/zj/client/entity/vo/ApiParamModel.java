package com.zj.client.entity.vo;

import java.util.List;

/**
 * @author falcon
 * @since 2023/8/9
 */
public class ApiParamModel {

  private String paramKey;

  private String type;

  private String position;

  private String description;

  private boolean isRequired;

  private String objectName;

  private List<ApiParamModel> children;

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getParamKey() {
    return paramKey;
  }

  public void setParamKey(String paramKey) {
    this.paramKey = paramKey;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public void setRequired(boolean required) {
    isRequired = required;
  }

  public List<ApiParamModel> getChildren() {
    return children;
  }

  public void setChildren(List<ApiParamModel> children) {
    this.children = children;
  }
}
