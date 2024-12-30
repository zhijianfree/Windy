package com.zj.common.adapter.git;

import java.util.Collections;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
public interface IGitRepositoryHandler {

  String gitType();

  default List<CommitMessage> getBranchCommits(String branch, GitAccessInfo accessInfo){
    return Collections.emptyList();
  }

  /**
   * 创建分支
   */
  void createBranch(String branchName, GitAccessInfo accessInfo);

  /**
   * 删除分支
   */
  void deleteBranch(String branchName, GitAccessInfo accessInfo);

  /**
   * 查看分支列表
   */
  List<String> listBranch(GitAccessInfo accessInfo);

  /**
   * 查询仓库列表
   */
  void checkRepository(GitAccessInfo accessInfo);
}
