package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.pipeline.entity.enums.PipelineExecuteType;
import com.zj.pipeline.git.IRepositoryBranch;
import com.zj.service.entity.po.Microservice;
import com.zj.service.service.MicroserviceService;
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
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class GitBindService {

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

  @Autowired
  private IBindBranchRepository gitBindRepository;

  public String createGitBind(BindBranchDto bindBranchDto) {
    List<BindBranchDto> bindDtoList = listGitBinds(bindBranchDto.getPipelineId());
    Optional<BindBranchDto> optional = bindDtoList.stream()
        .filter(gitBind -> Objects.equals(gitBind.getGitBranch(), bindBranchDto.getGitBranch()))
        .findAny();
    if (optional.isPresent()) {
      throw new ApiException(ErrorCode.BRANCH_ALREADY_BIND);
    }

    bindBranchDto.setBindId(uniqueIdService.getUniqueId());
    boolean result = gitBindRepository.saveGitBranch(bindBranchDto);
    return result ? bindBranchDto.getBindId() : "";
  }

  public List<BindBranchDto> listGitBinds(String pipelineId) {
    checkPipelineExist(pipelineId);
    return gitBindRepository.getPipelineRelatedBranches(pipelineId);
  }

  public Boolean updateGitBind(BindBranchDto bindBranchDto) {
    checkPipelineExist(bindBranchDto.getPipelineId());

    BindBranchDto gitBind = getGitBind(bindBranchDto.getBindId());
    if (Objects.isNull(gitBind)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE_GIT_BIND);
    }

    return gitBindRepository.updateGitBranch(bindBranchDto);
  }

  private BindBranchDto getGitBind(String bindId) {
    return gitBindRepository.getGitBranch(bindId);
  }

  public Boolean deleteGitBind(String pipelineId, String bindId) {
    checkPipelineExist(pipelineId);
    return gitBindRepository.deleteGitBranch(bindId);
  }

  private void checkPipelineExist(String pipelineId) {
    PipelineDto pipelineDTO = pipelineService.getPipeline(pipelineId);
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
    List<PipelineDto> pipelines = pipelineService.getServicePipelines(microservice.getServiceId());
    if (CollectionUtils.isEmpty(pipelines)) {
      log.info("can not find pipelines service={}", name);
      return;
    }

    List<PipelineDto> pushPipelines = pipelines.stream()
        .filter(pipeline -> Objects.equals(PipelineExecuteType.PUSH.getType(),
            pipeline.getExecuteType())).collect(Collectors.toList());
    pushPipelines.forEach(pipeline -> executorService.execute(() -> {
      List<BindBranchDto> gitBinds = listGitBinds(pipeline.getPipelineId());
      Optional<BindBranchDto> optional = gitBinds.stream()
          .filter(BindBranchDto::getIsChoose)
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
