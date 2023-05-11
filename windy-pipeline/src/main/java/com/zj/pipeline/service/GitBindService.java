package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.pipeline.entity.dto.GitBindDto;
import com.zj.pipeline.entity.dto.PipelineDTO;
import com.zj.pipeline.entity.enums.PipelineExecuteType;
import com.zj.pipeline.entity.po.GitBind;
import com.zj.pipeline.entity.po.Pipeline;
import com.zj.pipeline.git.IRepositoryBranch;
import com.zj.pipeline.mapper.GitBindMapper;
import com.zj.service.entity.po.Microservice;
import com.zj.service.service.MicroserviceService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author falcon
 * @since 2021/10/15
 */
@Slf4j
@Service
public class GitBindService extends ServiceImpl<GitBindMapper, GitBind> {

  @Autowired
  private MicroserviceService microserviceService;

  @Autowired
  private PipelineService pipelineService;

  @Autowired
  @Qualifier("webHookExecutorPool")
  private ExecutorService executorService;

  @Autowired
  private IRepositoryBranch repositoryBranch;

  @Autowired
  private UniqueIdService uniqueIdService;

  public String createGitBind(GitBindDto gitBindDto) {
    List<GitBindDto> bindDtoList = listGitBinds(gitBindDto.getPipelineId());
    Optional<GitBindDto> optional = bindDtoList.stream()
        .filter(gitBind -> Objects.equals(gitBind.getGitBranch(), gitBindDto.getGitBranch()))
        .findAny();
    if (optional.isPresent()) {
      throw new ApiException(ErrorCode.BRANCH_ALREADY_BIND);
    }

    GitBind gitBind = GitBindDto.toGitBind(gitBindDto);
    gitBind.setBindId(uniqueIdService.getUniqueId());
    gitBind.setUpdateTime(System.currentTimeMillis());
    gitBind.setCreateTime(System.currentTimeMillis());
    if (save(gitBind)) {
      return gitBind.getBindId();
    }
    return "";
  }

  public List<GitBindDto> listGitBinds(String pipelineId) {
    checkPipelineExist(pipelineId);

    List<GitBind> gitBinds = list(
        Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getPipelineId, pipelineId));

    if (CollectionUtils.isEmpty(gitBinds)) {
      return Collections.emptyList();
    }
    return gitBinds.stream().map(GitBindDto::toGitBindDto).collect(Collectors.toList());
  }

  public Boolean updateGitBind(GitBindDto gitBindDto) {
    checkPipelineExist(gitBindDto.getPipelineId());

    GitBind gitBind = getGitBind(gitBindDto.getBindId());
    if (Objects.isNull(gitBind)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE_GIT_BIND);
    }
    GitBind update = GitBindDto.toGitBind(gitBindDto);
    update.setUpdateTime(System.currentTimeMillis());
    return update(update,
        Wrappers.lambdaUpdate(GitBind.class).eq(GitBind::getBindId, gitBindDto.getBindId()));
  }

  private GitBind getGitBind(String bindId) {
    return getOne(Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getBindId, bindId));
  }

  public Boolean deleteGitBind(String pipelineId, String bindId) {
    checkPipelineExist(pipelineId);

    return remove(Wrappers.lambdaQuery(GitBind.class).eq(GitBind::getBindId, bindId));
  }

  private void checkPipelineExist(String pipelineId) {
    PipelineDTO pipelineDTO = pipelineService.getPipeline(pipelineId);
    if (Objects.isNull(pipelineDTO)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }
  }

  public void notifyHook(JSONObject data) {
    log.info("get notify hook param={}", JSON.toJSONString(data));
    JSONObject repository = data.getJSONObject("repository");
    String name = repository.getString("name");
    String branch = getBranchFromHookData(data);
    log.info("get repository name={} branch name={}", name, branch);
    if (StringUtils.isEmpty(branch) || StringUtils.isEmpty(name)) {
      log.info("can not get service name or branch not trigger pipeline ={}",
          JSON.toJSONString(data));
      return;
    }

    Microservice microservice = microserviceService.getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceName, name));
    List<Pipeline> pipelines = pipelineService.list(Wrappers.lambdaQuery(Pipeline.class)
        .eq(Pipeline::getServiceId, microservice.getServiceId()));
    if (CollectionUtils.isEmpty(pipelines)) {
      log.info("can not find pipelines service={}", name);
      return;
    }

    List<Pipeline> pushPipelines = pipelines.stream()
        .filter(pipeline -> Objects.equals(PipelineExecuteType.PUSH.getType(),
            pipeline.getExecuteType())).collect(Collectors.toList());
    pushPipelines.forEach(pipeline -> executorService.execute(() -> {
      List<GitBindDto> gitBinds = listGitBinds(pipeline.getPipelineId());
      Optional<GitBindDto> optional = gitBinds.stream()
          .filter(GitBindDto::getIsChoose)
          .filter(gitBind -> Objects.equals(gitBind.getGitBranch(), branch)).findAny();
      if (optional.isPresent()) {
        pipelineService.execute(pipeline.getPipelineId());
        log.info("web hook trigger pipeline execute pipeline={}", pipeline.getPipelineId());
      }
    }));
  }

  private static String getBranchFromHookData(JSONObject data) {
    String ref = data.getString("ref");
    int index = ref.lastIndexOf("/");
    return ref.substring(index + 1);
  }

  public List<String> getServiceBranch(String serviceId) {
    Microservice serviceDetail = microserviceService.getServiceDetail(serviceId);
    return repositoryBranch.listBranch(serviceDetail.getServiceName());
  }
}
