package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.GitBindDto;
import com.zj.domain.entity.po.pipeline.GitBind;
import com.zj.domain.mapper.pipeline.GitBindMapper;
import com.zj.domain.repository.pipeline.IGitBindRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class GitBindRepository extends ServiceImpl<GitBindMapper, GitBind> implements
    IGitBindRepository {

  @Override
  public boolean saveGitBind(GitBindDto gitBindDto) {
    GitBind gitBind = OrikaUtil.convert(gitBindDto, GitBind.class);
    gitBind.setUpdateTime(System.currentTimeMillis());
    gitBind.setCreateTime(System.currentTimeMillis());
    return save(gitBind);
  }

  @Override
  public List<GitBindDto> getPipelineGitBinds(String pipelineId) {
    List<GitBind> gitBinds = list(
        Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getPipelineId, pipelineId));
    return OrikaUtil.convertList(gitBinds, GitBindDto.class);
  }

  @Override
  public GitBindDto getGitBind(String bindId) {
    GitBind gitBind = getOne(Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getBindId, bindId));
    return OrikaUtil.convert(gitBind, GitBindDto.class);
  }

  @Override
  public Boolean updateGitBind(GitBindDto gitBindDto) {
    GitBind update = OrikaUtil.convert(gitBindDto, GitBind.class);
    update.setUpdateTime(System.currentTimeMillis());
    return update(update,
        Wrappers.lambdaUpdate(GitBind.class).eq(GitBind::getBindId, gitBindDto.getBindId()));
  }

  @Override
  public Boolean deleteGitBind(String bindId) {
    return remove(Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getBindId, bindId));
  }
}
