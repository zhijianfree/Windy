package com.zj.pipeline.git;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.GitType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.git.GitAccessInfo;
import com.zj.common.git.IGitRepositoryHandler;
import com.zj.pipeline.entity.vo.BranchInfo;
import com.zj.pipeline.entity.vo.GitlabRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/7/3
 */
@Slf4j
@Component
public class GitlabGitRepositoryHandler implements IGitRepositoryHandler {

    public static final String MASTER = "master";
    public static final String TEMP_PREFIX = "temp_";
    private final GitRequestProxy gitRequestProxy;

    private Map<String, Integer> serviceIdMap = new HashMap<>();

    public GitlabGitRepositoryHandler(GitRequestProxy gitRequestProxy) {
        this.gitRequestProxy = gitRequestProxy;
    }

    private Map<String, String> getTokenHeader(GitAccessInfo gitAccessInfo) {
        Map<String, String> header = new HashMap<>();
        String accessToken = gitAccessInfo.getAccessToken();
        header.put("Private-Token", accessToken);
        return header;
    }

    private void loadGitRepositories(GitAccessInfo accessInfo) {
        try {
            List<GitlabRepository> gitlabRepositories = getGitlabRepositories(accessInfo);
            serviceIdMap = gitlabRepositories.stream()
                    .collect(Collectors.toMap(repo -> repo.getName().toLowerCase(), GitlabRepository::getId,
                            (value1, value2) -> value2));
        } catch (Exception e) {
            log.info("load gitlab repositories error ={}", e.getMessage());
        }
    }

    @Override
    public String gitType() {
        return GitType.Gitlab.name();
    }

    @Override
    public void createBranch(String branchName, GitAccessInfo accessInfo) {
        Integer projectId = transformProjectId(accessInfo);
        String path = String.format("/api/v4/projects/%s/repository/branches?branch=%s&ref=master",
                projectId, branchName);
        String result = gitRequestProxy.post(accessInfo.getGitDomain() + path, "", getTokenHeader(accessInfo));
        log.info("gitea create branch result = {}", result);
        BranchInfo branchInfo = JSON.parseObject(result, BranchInfo.class);
        if (Objects.isNull(branchInfo) || !Objects.equals(branchInfo.getName(), branchName)) {
            throw new ApiException(ErrorCode.CREATE_BRANCH_ERROR);
        }
    }

    @Override
    public void deleteBranch(String branchName, GitAccessInfo accessInfo) {
        Integer projectId = transformProjectId(accessInfo);
        String path = String.format("/api/v4/projects/%s/repository/branches/%s", projectId,
                branchName);
        String result = gitRequestProxy.delete(accessInfo.getGitDomain() + path, getTokenHeader(accessInfo));
        log.info("gitea delete branch result = {}", result);
    }

    @Override
    public List<String> listBranch(GitAccessInfo accessInfo) {
        Integer projectId = transformProjectId(accessInfo);
        if (Objects.isNull(projectId)) {
            log.info("can not get service project id={}", accessInfo.getGitServiceName());
            return Collections.emptyList();
        }

        String path = String.format("/api/v4/projects/%s/repository/branches", projectId);
        String result = gitRequestProxy.get(accessInfo.getGitDomain() + path, getTokenHeader(accessInfo));
        List<BranchInfo> branches = JSON.parseArray(result, BranchInfo.class);
        if (CollectionUtils.isEmpty(branches)) {
            return Collections.emptyList();
        }
        return branches.stream().map(BranchInfo::getName)
                //不显示master分支以及构建的临时分支
                .filter(branch -> Objects.nonNull(branch) && !Objects.equals(branch, MASTER)
                        && !branch.startsWith(TEMP_PREFIX))
                .collect(Collectors.toList());
    }

    private Integer transformProjectId(GitAccessInfo accessInfo) {
        Integer projectId = serviceIdMap.get(accessInfo.getGitServiceName().toLowerCase());
        if (Objects.isNull(projectId)) {
            loadGitRepositories(accessInfo);
        }
        return serviceIdMap.get(accessInfo.getGitServiceName().toLowerCase());
    }

    @Override
    public void checkRepository(GitAccessInfo accessInfo) {
        List<GitlabRepository> repositories = getGitlabRepository(accessInfo);
        if (CollectionUtils.isEmpty(repositories)) {
            log.info("gitlab repository not exist={}", accessInfo.getGitServiceName());
            throw new ApiException(ErrorCode.REPO_NOT_EXIST);
        }

        Optional<GitlabRepository> optional = repositories.stream()
                .filter(repo -> Objects.equals(repo.getName().toLowerCase(), accessInfo.getGitServiceName().toLowerCase()))
                .findAny();
        if (!optional.isPresent()) {
            log.info("user can not access gitlab repository permission={}", accessInfo.getGitServiceName());
            throw new ApiException(ErrorCode.USER_NO_PERMISSION);
        }

        boolean permission = optional.get().getPermissions().checkPermission();
        if (!permission) {
            log.info("user do not have gitlab repository permission={}", accessInfo.getGitServiceName());
            throw new ApiException(ErrorCode.GIT_NO_PERMISSION);
        }
    }

    private List<GitlabRepository> getGitlabRepositories(GitAccessInfo accessInfo) {
        Response response = gitRequestProxy.getWithResponse(accessInfo.getGitDomain() + "/api/v4/projects?per_page" +
                        "=100&page=1", getTokenHeader(accessInfo));
        try {
            String result = response.body().string();
            List<GitlabRepository> allRepositories = JSON.parseArray(result, GitlabRepository.class);
            String pageNum = response.header("x-total-pages");
            allRepositories.addAll(requestAllRepositories(pageNum, accessInfo));
            return allRepositories;
        } catch (Exception e) {
            log.info("get gitlab repositories error", e);
            return Collections.emptyList();
        }
    }

    private List<GitlabRepository> requestAllRepositories(String numString, GitAccessInfo accessInfo) {
        if (StringUtils.isBlank(numString)) {
            log.info("gitlab response header not find x-total-pages");
            return Collections.emptyList();
        }

        int num = Integer.parseInt(numString);
        List<GitlabRepository> list = new ArrayList<>();
        for (int i = 2; i <= num; i++) {
            Response response = gitRequestProxy.getWithResponse(accessInfo.getGitDomain() + "/api/v4/projects" +
                            "?per_page=100&page=" + i,
                    getTokenHeader(accessInfo));
            try {
                String result = response.body().string();
                list.addAll(JSON.parseArray(result, GitlabRepository.class));
            } catch (Exception e) {
                log.info("get gitlab repositories error", e);
            }
        }
        return list;
    }

    private List<GitlabRepository> getGitlabRepository(GitAccessInfo accessInfo) {
        String result = gitRequestProxy.get(accessInfo.getGitDomain() + "/api/v4/projects?search=" + accessInfo.getGitServiceName(),
                getTokenHeader(accessInfo));
        return JSON.parseArray(result, GitlabRepository.class);
    }

}
