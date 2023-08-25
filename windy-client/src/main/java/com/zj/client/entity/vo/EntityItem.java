package com.zj.client.entity.vo;

import java.util.List;
import lombok.Data;

@Data
public class EntityItem {

  private String className;
  private List<PropertyItem> properties;

  @Data
  public static class PropertyItem{

    private String name;

    private String type;

    private String nameUpper;
  }
}
