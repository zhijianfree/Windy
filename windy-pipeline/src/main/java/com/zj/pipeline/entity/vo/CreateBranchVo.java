package com.zj.pipeline.entity.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Data
public class CreateBranchVo {

  /**
   * 新分支名称
   * */
  @JSONField(name = "new_branch_name")
  private String branchName;
}
