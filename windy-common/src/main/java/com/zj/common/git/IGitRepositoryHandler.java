package com.zj.common.git;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
public interface IGitRepositoryHandler {

  String gitType();

  /**
   * 创建分支
   */
  void createBranch(String serviceName, String branchName, GitAccessInfo accessInfo);

  /**
   * 删除分支
   */
  void deleteBranch(String serviceName, String branchName, GitAccessInfo accessInfo);

  /**
   * 查看分支列表
   */
  List<String> listBranch(String serviceName, GitAccessInfo accessInfo);

  /**
   * 查询仓库列表
   */
  void checkRepository(String serviceName, GitAccessInfo accessInfo);
}
