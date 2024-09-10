package com.zj.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum TemplateType {
  NORMAL(1),
  FOR(2),
  IF(3),
  DEFAULT(4),
  PLUGIN(5),
  SCRIPT(6),
  THREAD(7);
  private final int type;

  TemplateType(int type) {
    this.type = type;
  }

  public static List<Integer> getToolTemplates(){
    return Arrays.asList(TemplateType.FOR.getType(),
            TemplateType.IF.getType(), TemplateType.DEFAULT.getType(), TemplateType.SCRIPT.getType(),
            TemplateType.THREAD.getType());
  }

}
