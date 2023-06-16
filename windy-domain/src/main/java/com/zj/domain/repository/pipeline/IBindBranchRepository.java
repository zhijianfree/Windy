package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IBindBranchRepository {

  boolean saveGitBranch(BindBranchDto bindBranchDto);

  List<BindBranchDto> getPipelineRelatedBranches(String pipelineId);

  /**
   * 获取流水线绑定的分支
   * */
  BindBranchDto getPipelineBindBranch(String pipelineId);

  BindBranchDto getGitBranch(String bindId);

  Boolean updateGitBranch(BindBranchDto bindBranchDto);

  Boolean deleteGitBranch(String bindId);
}
