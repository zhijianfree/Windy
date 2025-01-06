package com.zj.pipeline.git.hook;

import com.alibaba.fastjson.JSON;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.pipeline.entity.enums.PlatformEnum;
import com.zj.pipeline.entity.vo.GitParseResult;
import com.zj.pipeline.entity.vo.GitlabBaseEvent;
import com.zj.pipeline.entity.vo.GitlabCommitVo;
import com.zj.pipeline.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
                       IBindBranchRepository gitBindRepository) {
    super(serviceRepository, pipelineService, executorService, gitBindRepository);
  }

  @Override
  public GitParseResult parseData(Object data) {
    String gitString = JSON.toJSONString(data);
    log.info("get notify hook param={}", gitString);
    GitlabBaseEvent gitEvent = JSON.parseObject(gitString, GitlabBaseEvent.class);
    GitlabBaseEvent.GitProject gitProject = gitEvent.getGitProject();
    if (Objects.equals(gitEvent.getEventType(), PUSH_EVENT_NAME)){
      GitlabCommitVo gitlabCommitVo = JSON.parseObject(gitString, GitlabCommitVo.class);
      String branch = getBranchFromHookData(gitlabCommitVo.getRef());
      log.info("get repository name={} url={} branch name={}", gitProject.getName(), gitProject.getGitUrl(), branch);
      return GitParseResult.builder().repository(gitProject.getGitUrl()).eventType(gitEvent.getEventType()).branch(branch).build();
    }

    return null;
  }

  @Override
  public String platform() {
    return PlatformEnum.gitlab.name();
  }
}
