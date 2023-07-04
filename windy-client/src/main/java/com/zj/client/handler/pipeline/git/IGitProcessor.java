package com.zj.client.handler.pipeline.git;

import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.Ref;

/**
 * @author guyuelan
 * @since 2023/6/29
 */
public interface IGitProcessor {

  /**
   * 根据git地址拉取代码
   * */
  Git pullCodeFromGit(String gitUrl, String branch, String workspace) throws Exception;

  /**
   * 将多个分支合并成一个临时分支
   * */
  MergeResult createTempBranch(String gitUrl, List<String> branches, String workspace)
      throws Exception;


  List<Ref> getBranchesRef(Git git, List<String> branches);
}
