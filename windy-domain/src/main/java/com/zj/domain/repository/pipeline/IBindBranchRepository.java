package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IBindBranchRepository {

  boolean saveGitBranch(BindBranchBO bindBranchBO);

  List<BindBranchBO> getPipelineRelatedBranches(String pipelineId);

  /**
   * 获取流水线绑定的分支
   * */
  BindBranchBO getPipelineBindBranch(String pipelineId);

  BindBranchBO getGitBranch(String bindId);

  Boolean updateGitBranch(BindBranchBO bindBranchBO);

  Boolean deleteGitBranch(String bindId);

  void batchUnbindBranches(List<String> unbindBranches);

  boolean deleteByPipelineId(String pipelineId);
}
