package com.zj.master.entity.vo;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Data
public class BuildCodeContext extends RequestContext{

  private String pomPath;

  private Boolean isPublish;

  private List<String> branches;
}
