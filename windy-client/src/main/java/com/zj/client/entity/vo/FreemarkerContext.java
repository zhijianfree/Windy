package com.zj.client.entity.vo;

import com.zj.client.entity.vo.EntityItem.PropertyItem;
import java.util.List;
import lombok.Data;

@Data
public class FreemarkerContext {

  private String packageName;

  private String className;

  private List<ApiItem> paramList;

  private List<PropertyItem> properties;
}
