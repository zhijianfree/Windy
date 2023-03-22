package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.pipeline.entity.dto.GitBindDto;
import com.zj.pipeline.entity.dto.PipelineDTO;
import com.zj.pipeline.entity.po.GitBind;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.mapper.GitBindMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author falcon
 * @since 2021/10/15
 */
@Service
public class GitBindService {

  @Autowired
  private GitBindMapper gitBindMapper;

  @Autowired
  private PipelineService pipelineService;

  public String createGitBind(GitBindDto gitBindDto) {
    GitBind gitBind = GitBindDto.toGitBind(gitBindDto);
    gitBind.setBindId(UUID.randomUUID().toString());
    gitBind.setUpdateTime(System.currentTimeMillis());
    gitBind.setCreateTime(System.currentTimeMillis());
    int result = gitBindMapper.insert(gitBind);
    if (result > 0){
      return gitBind.getBindId();
    }
    return "";
  }

  public List<GitBindDto> listGitBinds(String pipelineId) {
    checkPipelineExist(pipelineId);

    List<GitBind> gitBinds = gitBindMapper.selectList(
        Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getPipelineId, pipelineId));

    if (CollectionUtils.isEmpty(gitBinds)) {
      return Collections.emptyList();
    }
    return gitBinds.stream().map(GitBindDto::toGitBindDto).collect(Collectors.toList());
  }

  public Integer updateGitBind(String pipelineId, String bindId, GitBindDto gitBindDto) {
    checkPipelineExist(pipelineId);

    GitBind gitBind = getGitBind(bindId);
    if (Objects.isNull(gitBind)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE_GIT_BIND);
    }
    GitBind update = GitBindDto.toGitBind(gitBindDto);
    update.setUpdateTime(System.currentTimeMillis());
    return gitBindMapper.update(update,
        Wrappers.lambdaUpdate(GitBind.class).eq(GitBind::getBindId, bindId));
  }

  private GitBind getGitBind(String bindId) {
    return gitBindMapper.selectOne(
        Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getBindId, bindId));
  }

  public Integer deleteGitBind(String pipelineId, String bindId) {
    checkPipelineExist(pipelineId);

    return gitBindMapper.delete(Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getBindId, bindId));
  }

  private void checkPipelineExist(String pipelineId) {
    PipelineDTO pipelineDTO = pipelineService.getPipeline(pipelineId);
    if (Objects.isNull(pipelineDTO)){
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }
  }
}
