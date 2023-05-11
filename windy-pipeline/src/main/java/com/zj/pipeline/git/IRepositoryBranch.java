package com.zj.pipeline.git;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
public interface IRepositoryBranch {

  /**
   * 创建分支
   */
  boolean createBranch(String serviceName, String branchName);

  /**
   * 删除分支
   */
  boolean deleteBranch(String serviceName, String branchName);

  /**
   * 查看分支列表
   */
  List<String> listBranch(String serviceName);
}
