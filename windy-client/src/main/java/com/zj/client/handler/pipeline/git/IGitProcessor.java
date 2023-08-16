package com.zj.client.handler.pipeline.git;

import com.zj.client.handler.pipeline.executer.vo.GitMeta;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.Ref;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/29
 */
public interface IGitProcessor {

  /**
   * 根据git地址拉取代码
   * */
  Git pullCodeFromGit(GitMeta gitMeta, String branch, String workspace) throws Exception;

  /**
   * 将多个分支合并成一个临时分支
   * */
  MergeResult createTempBranch(GitMeta gitMeta, List<String> branches, String workspace)
      throws Exception;


  List<Ref> getBranchesRef(Git git, List<String> branches);
}
