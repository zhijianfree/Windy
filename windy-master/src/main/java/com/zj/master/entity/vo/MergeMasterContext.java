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

  private List<String> branches;

  private String deleteBranch;
}
