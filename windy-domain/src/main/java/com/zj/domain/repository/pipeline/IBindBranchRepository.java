package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.BindBranchBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IBindBranchRepository {

  /**
   * 保存绑定分支
   * @param bindBranchBO 分支信息
   * @return 是否成功
   */
  boolean saveGitBranch(BindBranchBO bindBranchBO);

  /**
   * 获取流水线绑定的分支
   * @param pipelineId 流水线ID
   * @return 分支列表
   */
  List<BindBranchBO> getPipelineRelatedBranches(String pipelineId);

  /**
   * 获取流水线已绑定分支
   * @param pipelineId 流水线ID
   * @return 分支信息
   */
  BindBranchBO getPipelineBindBranch(String pipelineId);

  /**
   * 获取绑定分支
   * @param bindId 绑定ID
   * @return 分支信息
   */
  BindBranchBO getGitBranch(String bindId);

  /**
   * 更新绑定分支
   * @param bindBranchBO 分支信息
   * @return 是否成功
   */
  boolean updateGitBranch(BindBranchBO bindBranchBO);

  /**
   * 删除绑定分支
   * @param bindId 绑定ID
   * @return 是否成功
   */
  boolean deleteGitBranch(String bindId);

  /**
   * 批量解绑分支
   * @param unbindBranches 分支ID列表
   */
  void batchUnbindBranches(List<String> unbindBranches);

  /**
   * 删除流水线绑定分支
   * @param pipelineId 流水线ID
   * @return 是否成功
   */
  boolean deleteByPipelineId(String pipelineId);
}
