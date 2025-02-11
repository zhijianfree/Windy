package com.zj.pipeline.git.hook;

import com.alibaba.fastjson.JSON;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.pipeline.entity.enums.PlatformEnum;
import com.zj.pipeline.entity.vo.GitPushResultVo;
import com.zj.pipeline.entity.vo.GitlabBaseEventVo;
import com.zj.pipeline.entity.vo.GitlabCommitVo;
import com.zj.pipeline.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author guyuelan
 * @since 2023/6/27
 */
@Slf4j
@Component
public class GitlabWebhook extends AbstractWebhook {

  public static final String PUSH_EVENT_NAME = "push";

  public GitlabWebhook(IMicroServiceRepository serviceRepository, PipelineService pipelineService,
                       @Qualifier("webHookExecutorPool") Executor executorService,
                       IBindBranchRepository gitBindRepository, ISystemConfigRepository systemConfigRepository) {
    super(serviceRepository, pipelineService, executorService, gitBindRepository, systemConfigRepository);
  }

  @Override
  public GitPushResultVo analyzeData(String data, HttpServletRequest request) {
    log.info("gitlab notify hook param={}", data);
    if (!checkSecret(request)){
      log.info("gitlab signature check error");
      return null;
    }

    GitlabBaseEventVo gitEvent = JSON.parseObject(data, GitlabBaseEventVo.class);
    GitlabBaseEventVo.GitProject gitProject = gitEvent.getProject();
    if (!Objects.equals(gitEvent.getEventType(), PUSH_EVENT_NAME)){
      return null;
    }
    GitlabCommitVo gitlabCommitVo = JSON.parseObject(data, GitlabCommitVo.class);
    String branch = getBranchFromHookData(gitlabCommitVo.getRef());
    log.info("get repository name={} url={} branch name={}", gitProject.getName(), gitProject.getGitUrl(), branch);
    return GitPushResultVo.builder().repository(gitProject.getGitUrl()).eventType(gitEvent.getEventType()).branch(branch).build();
  }


  private boolean checkSecret(HttpServletRequest request) {
    GitAccessInfo gitAccess = getGitAccessInfo();
    String gitLabToken = request.getHeader("X-Gitlab-Token");
    log.info("secret = {} gitlab={}", gitAccess.getPushSecret(), gitLabToken);
    return Objects.equals(gitAccess.getPushSecret(), gitLabToken);
  }

  @Override
  public String platform() {
    return PlatformEnum.gitlab.name();
  }
}
