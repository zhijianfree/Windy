package com.zj.pipeline.git.hook;

import com.alibaba.fastjson.JSON;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.pipeline.entity.enums.GitEventType;
import com.zj.pipeline.entity.enums.PlatformEnum;
import com.zj.pipeline.entity.vo.GitPushResultVo;
import com.zj.pipeline.entity.vo.GithubCommitVo;
import com.zj.pipeline.entity.vo.GithubRepositoryVo;
import com.zj.pipeline.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class GithubWebHook extends AbstractWebhook {
    protected GithubWebHook(IMicroServiceRepository serviceRepository, PipelineService pipelineService, @Qualifier(
            "webHookExecutorPool") Executor executorService, IBindBranchRepository gitBindRepository,
                            ISystemConfigRepository systemConfigRepository) {
        super(serviceRepository, pipelineService, executorService, gitBindRepository, systemConfigRepository);
    }

    @Override
    public GitPushResultVo analyzeData(String data, HttpServletRequest request) {
        log.info("receive github event={}", data);
        if(!checkSignature(data, request)){
            log.info("check github signature error");
            return null;
        }
        GithubCommitVo githubCommit = JSON.parseObject(data, GithubCommitVo.class);
        if (Objects.isNull(githubCommit) || Objects.isNull(githubCommit.getRepository()) || CollectionUtils.isEmpty(githubCommit.getCommits())){
            log.info("can not find github commit event, not handle current web hook");
            return null;
        }
        GithubRepositoryVo repository = githubCommit.getRepository();
        String branch = getBranchFromHookData(githubCommit.getRef());
        return GitPushResultVo.builder().repository(repository.getRepository())
                .gitType(PlatformEnum.github.name())
                .branch(branch)
                .eventType(GitEventType.COMMIT.getType()).build();
    }

    private boolean checkSignature(String data, HttpServletRequest request) {
        String signature = request.getHeader("X-Hub-Signature");
        if (StringUtils.isBlank(signature) || !signature.startsWith("sha1=")) {
            log.info("can not find github Signature={}", signature);
            return false;
        }
        // 获取 GitHub 发来的签名值（去掉 "sha1=" 部分）
        String gitHubSignature = signature.substring(5);
        GitAccessInfo gitAccessInfo = getGitAccessInfo();
        String computedSignature = computeHMACSHA1(data, gitAccessInfo.getPushSecret());
        return Objects.equals(computedSignature, gitHubSignature);
    }

    /**
     * 使用 HMAC-SHA1 算法计算签名
     * */
    private String computeHMACSHA1(String data, String secret) {
        try {
            Mac hmacSHA1 = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
            hmacSHA1.init(secretKey);
            byte[] hash = hmacSHA1.doFinal(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.info("calculate signature error", e);
        }
        return null;
    }

    @Override
    public String platform() {
        return PlatformEnum.github.name();
    }
}
