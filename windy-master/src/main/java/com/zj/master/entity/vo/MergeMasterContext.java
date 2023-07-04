package com.zj.master.entity.vo;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/29
 */
@Data
public class MergeMasterContext extends RequestContext{

  private List<String> branches;

  private String deleteBranch;
}
