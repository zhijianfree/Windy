package com.zj.pipeline.git.hook;

import com.alibaba.fastjson.JSON;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.pipeline.entity.enums.PlatformEnum;
import com.zj.pipeline.entity.vo.GitParseResult;
import com.zj.pipeline.entity.vo.GiteaHookVo;
import com.zj.pipeline.service.PipelineService;
import com.zj.service.service.MicroserviceService;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Slf4j
@Component
public class GiteeWebhook extends AbstractWebhook{

  public GiteeWebhook(MicroserviceService microserviceService, PipelineService pipelineService,
      @Qualifier("webHookExecutorPool") Executor executorService,
      IBindBranchRepository gitBindRepository) {
    super(microserviceService, pipelineService, executorService, gitBindRepository);
  }
  @Override
  public GitParseResult parseData(Object data) {
    log.info("get notify hook param={}", JSON.toJSONString(data));
    GiteaHookVo giteaHookVo = JSON.parseObject(JSON.toJSONString(data), GiteaHookVo.class);
    String name = giteaHookVo.getRepository().getName();
    String branch = getBranchFromHookData(giteaHookVo.getRef());
    log.info("get repository name={} branch name={}", name, branch);
    return GitParseResult.builder().repository(name).branch(branch).build();
  }

  @Override
  public String platform() {
    return PlatformEnum.gitee.name();
  }
}
