package com.zj.client.entity.bo;

import com.zj.client.entity.bo.EntityItem.PropertyItem;
import java.util.List;
import lombok.Data;

@Data
public class FreemarkerContext {

  private String packageName;

  private String className;

  private List<ApiItem> paramList;

  private List<PropertyItem> properties;
}
