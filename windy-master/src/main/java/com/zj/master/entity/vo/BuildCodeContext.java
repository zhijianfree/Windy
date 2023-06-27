package com.zj.master.entity.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Data
public class BuildCodeContext extends RequestContext{

  private String pomPath;

  private String branch;
}
