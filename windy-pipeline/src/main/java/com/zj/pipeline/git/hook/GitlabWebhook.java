package com.zj.pipeline.git.hook;

import com.alibaba.fastjson.JSON;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.pipeline.entity.enums.PlatformEnum;
import com.zj.pipeline.entity.vo.GitPushResult;
import com.zj.pipeline.entity.vo.GitlabBaseEvent;
import com.zj.pipeline.entity.vo.GitlabCommitVo;
import com.zj.pipeline.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
  public GitPushResult analzeData(String data, HttpServletRequest request) {
    log.info("gitlab notify hook param={}", data);
    if (!checkSecret(data, request)){
      log.info("gitlab signature check error");
      return null;
    }

    GitlabBaseEvent gitEvent = JSON.parseObject(data, GitlabBaseEvent.class);
    GitlabBaseEvent.GitProject gitProject = gitEvent.getProject();
    if (!Objects.equals(gitEvent.getEventType(), PUSH_EVENT_NAME)){
      return null;
    }
    GitlabCommitVo gitlabCommitVo = JSON.parseObject(data, GitlabCommitVo.class);
    String branch = getBranchFromHookData(gitlabCommitVo.getRef());
    log.info("get repository name={} url={} branch name={}", gitProject.getName(), gitProject.getGitUrl(), branch);
    return GitPushResult.builder().repository(gitProject.getGitUrl()).eventType(gitEvent.getEventType()).branch(branch).build();
  }


  private boolean checkSecret(String gitString, HttpServletRequest request) {
    GitAccessInfo gitAccess = getGitAccessInfo();
    String signature = computeHMAC(gitString, gitAccess.getPushSecret());
    if (StringUtils.isBlank(signature)) {
      log.info("signature body is empty, check fail");
      return false;
    }
    String gitLabToken = request.getHeader("X-Gitlab-Token");
    log.info("signature = {} gitlab={}", signature, gitLabToken);
    return Objects.equals(signature, gitLabToken);
  }

  /**
   * 计算 HMAC-SHA256 签名
   */
  private static String computeHMAC(String payload, String secret) {
    try {
      // 使用 HMAC-SHA256 算法
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(secretKeySpec);

      // 计算哈希
      byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

      // 转换为十六进制字符串
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        hexString.append(String.format("%02x", b)); // 确保是两位十六进制格式
      }

      return hexString.toString();
    } catch (Exception e) {
      log.info("calculate sha256 secret error", e);
    }
    return null;
  }

  @Override
  public String platform() {
    return PlatformEnum.gitlab.name();
  }
}
