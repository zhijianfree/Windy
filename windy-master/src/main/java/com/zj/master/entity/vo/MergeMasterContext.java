package com.zj.master.entity.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MergeMasterContext extends RequestContext{

  private String tagName;

  private String message;

  private List<String> branches;

  private String deleteBranch;
}
