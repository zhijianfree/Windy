package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.BindBranchDto;
import com.zj.domain.entity.po.pipeline.BindBranch;
import com.zj.domain.mapper.pipeline.BindBranchMapper;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class BindBranchRepository extends ServiceImpl<BindBranchMapper, BindBranch> implements
    IBindBranchRepository {

  @Override
  public boolean saveGitBranch(BindBranchDto bindBranchDto) {
    BindBranch bindBranch = OrikaUtil.convert(bindBranchDto, BindBranch.class);
    bindBranch.setUpdateTime(System.currentTimeMillis());
    bindBranch.setCreateTime(System.currentTimeMillis());
    return save(bindBranch);
  }

  @Override
  public List<BindBranchDto> getPipelineRelatedBranches(String pipelineId) {
    List<BindBranch> bindBranches = list(
        Wrappers.lambdaQuery(BindBranch.class).eq(BindBranch::getPipelineId, pipelineId));
    return OrikaUtil.convertList(bindBranches, BindBranchDto.class);
  }

  @Override
  public BindBranchDto getGitBranch(String bindId) {
    BindBranch bindBranch = getOne(
        Wrappers.lambdaQuery(BindBranch.class).eq(BindBranch::getBindId, bindId));
    return OrikaUtil.convert(bindBranch, BindBranchDto.class);
  }

  @Override
  public Boolean updateGitBranch(BindBranchDto bindBranchDto) {
    BindBranch update = OrikaUtil.convert(bindBranchDto, BindBranch.class);
    update.setUpdateTime(System.currentTimeMillis());
    return update(update, Wrappers.lambdaUpdate(BindBranch.class)
        .eq(BindBranch::getBindId, bindBranchDto.getBindId()));
  }

  @Override
  public Boolean deleteGitBranch(String bindId) {
    return remove(Wrappers.lambdaQuery(BindBranch.class).eq(BindBranch::getBindId, bindId));
  }

  @Override
  public void batchUnbindBranches(List<String> unbindBranches) {
    BindBranch update = new BindBranch();
    update.setIsChoose(false);
    update(update, Wrappers.lambdaUpdate(BindBranch.class)
        .in(BindBranch::getBindId, unbindBranches));
  }

  @Override
  public BindBranchDto getPipelineBindBranch(String pipelineId) {
    BindBranch bindBranch = getOne(
        Wrappers.lambdaQuery(BindBranch.class).eq(BindBranch::getPipelineId, pipelineId)
            .eq(BindBranch::getIsChoose, true));
    return OrikaUtil.convert(bindBranch, BindBranchDto.class);
  }
}
