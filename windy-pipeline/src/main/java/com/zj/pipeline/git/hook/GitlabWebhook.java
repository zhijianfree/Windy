package com.zj.pipeline.git.hook;

import com.alibaba.fastjson.JSON;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.pipeline.entity.enums.PlatformEnum;
import com.zj.pipeline.entity.vo.GitParseResult;
import com.zj.pipeline.entity.vo.GitlabHookVo;
import com.zj.pipeline.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Slf4j
@Component
public class GitlabWebhook extends AbstractWebhook {

  public GitlabWebhook(IMicroServiceRepository serviceRepository, PipelineService pipelineService,
      @Qualifier("webHookExecutorPool") Executor executorService,
      IBindBranchRepository gitBindRepository) {
    super(serviceRepository, pipelineService, executorService, gitBindRepository);
  }

  @Override
  public GitParseResult parseData(Object data) {
    log.info("get notify hook param={}", JSON.toJSONString(data));
    GitlabHookVo giteaHookVo = JSON.parseObject(JSON.toJSONString(data), GitlabHookVo.class);
    String name = giteaHookVo.getProject();
    String branch = getBranchFromHookData(giteaHookVo.getRef());
    log.info("get repository name={} branch name={}", name, branch);
    return GitParseResult.builder().repository(name).branch(branch).build();
  }

  @Override
  public String platform() {
    return PlatformEnum.gitlab.name();
  }
}
