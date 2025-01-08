package com.zj.pipeline.git.hook;

import com.alibaba.fastjson.JSON;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.pipeline.entity.enums.GitEventType;
import com.zj.pipeline.entity.enums.PlatformEnum;
import com.zj.pipeline.entity.vo.GitPushResultVo;
import com.zj.pipeline.entity.vo.GiteaHookVo;
import com.zj.pipeline.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Slf4j
@Component
public class GiteaWebhook extends AbstractWebhook {

  public GiteaWebhook(IMicroServiceRepository serviceRepository, PipelineService pipelineService,
                      @Qualifier("webHookExecutorPool") Executor executorService,
                      IBindBranchRepository gitBindRepository, ISystemConfigRepository systemConfigRepository) {
    super(serviceRepository, pipelineService, executorService, gitBindRepository, systemConfigRepository);
  }

  @Override
  public String platform() {
    return PlatformEnum.gitea.name();
  }

  @Override
  public GitPushResultVo analyzeData(String data, HttpServletRequest request) {
    log.info("get notify hook param={}", data);
    if(!checkSecret(request)){
      log.info("check gitea secret error");
      return null;
    }
    GiteaHookVo giteaHookVo = JSON.parseObject(data, GiteaHookVo.class);
    String name = giteaHookVo.getRepository().getName();
    String branch = getBranchFromHookData(giteaHookVo.getRef());
    log.info("get repository name={} branch name={}", name, branch);
    return GitPushResultVo.builder().repository(name).branch(branch).gitType(PlatformEnum.gitea.name())
            .eventType(GitEventType.COMMIT.getType()).build();
  }

  private boolean checkSecret(HttpServletRequest request) {
    String giteaToken = request.getHeader("X-Gitea-Token");
    GitAccessInfo gitAccessInfo = getGitAccessInfo();
    return Objects.equals(giteaToken, gitAccessInfo.getPushSecret());
  }
}
