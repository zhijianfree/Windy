package com.zj.pipeline.git.hook;

import com.alibaba.fastjson.JSON;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.pipeline.entity.enums.PipelineExecuteType;
import com.zj.pipeline.entity.vo.GitParseResult;
import com.zj.pipeline.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Slf4j
public abstract class AbstractWebhook implements IGitWebhook {

  private final IMicroServiceRepository serviceRepository;
  private final PipelineService pipelineService;
  private final IBindBranchRepository gitBindRepository;
  private final Executor executorService;

  public AbstractWebhook(IMicroServiceRepository serviceRepository, PipelineService pipelineService,
      Executor executorService, IBindBranchRepository gitBindRepository) {
    this.serviceRepository = serviceRepository;
    this.pipelineService = pipelineService;
    this.gitBindRepository = gitBindRepository;
    this.executorService = executorService;
  }

  @Override
  public void webhook(Object data) {
    GitParseResult parseResult = parseData(data);
    if (StringUtils.isEmpty(parseResult.getBranch()) || StringUtils.isEmpty(
        parseResult.getRepository())) {
      log.info("can not get service name or branch not trigger pipeline ={}",
          JSON.toJSONString(data));
      return;
    }

    MicroserviceDto microservice = serviceRepository.queryServiceByName(
        parseResult.getRepository());
    List<PipelineDto> pipelines = pipelineService.getServicePipelines(microservice.getServiceId());
    if (CollectionUtils.isEmpty(pipelines)) {
      log.info("can not find pipelines service={}", parseResult.getRepository());
      return;
    }

    List<PipelineDto> pushPipelines = pipelines.stream().filter(
            pipeline -> Objects.equals(PipelineExecuteType.PUSH.getType(), pipeline.getExecuteType()))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(pushPipelines)) {
      log.info("not find pushed pipelines service={} serviceId={}", microservice.getServiceName(),
          microservice.getServiceId());
      return;
    }
    pushPipelines.forEach(pipeline -> executorService.execute(() -> {
      List<BindBranchDto> gitBinds = listGitBinds(pipeline.getPipelineId());
      Optional<BindBranchDto> optional = gitBinds.stream().filter(BindBranchDto::getIsChoose)
          .filter(gitBind -> Objects.equals(gitBind.getGitBranch(), parseResult.getBranch()))
          .findAny();
      if (optional.isPresent()) {
        pipelineService.execute(pipeline.getPipelineId());
        log.info("web hook trigger pipeline execute pipeline={}", pipeline.getPipelineId());
      }
    }));
  }

  public abstract GitParseResult parseData(Object data);

  public List<BindBranchDto> listGitBinds(String pipelineId) {
    checkPipelineExist(pipelineId);
    return gitBindRepository.getPipelineRelatedBranches(pipelineId);
  }

  private void checkPipelineExist(String pipelineId) {
    PipelineDto pipelineDTO = pipelineService.getPipeline(pipelineId);
    if (Objects.isNull(pipelineDTO)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }
  }

  protected String getBranchFromHookData(String ref) {
    int index = ref.lastIndexOf("/");
    return ref.substring(index + 1);
  }
}
