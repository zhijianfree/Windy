package com.zj.pipeline.git;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.adapter.git.CommitMessage;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.common.enums.GitType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.entity.vo.BranchInfo;
import com.zj.pipeline.entity.vo.GitlabRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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

    private static Map<String, String> getTokenHeader(GitAccessInfo gitAccessInfo) {
        Map<String, String> header = new HashMap<>();
        String accessToken = gitAccessInfo.getAccessToken();
        header.put("Private-Token", accessToken);
        return header;
    }

    private void loadGitRepositories(GitAccessInfo accessInfo) {
        try {
            List<GitlabRepository> gitlabRepositories = getGitlabRepositories(accessInfo);
            serviceIdMap = gitlabRepositories.stream()
                    .collect(Collectors.toMap(this::getRepositoryName, GitlabRepository::getId,
                            (value1, value2) -> value2));
        } catch (Exception e) {
            log.info("load gitlab repositories error ={}", e.getMessage());
        }
    }

    /**
     * 如果仓库名称与git地址上名称不一致，则使用git地址上的名称
     */
    private String getRepositoryName(GitlabRepository repo) {
        String repositoryName = repo.getName().toLowerCase();
        if (!Objects.equals(repo.getName().toLowerCase(), repo.getPath().toLowerCase())) {
            repositoryName = repo.getPath().toLowerCase();
        }
        return repositoryName;
    }

    @Override
    public String gitType() {
        return GitType.Gitlab.name();
    }

    @Override
    public List<CommitMessage> getBranchCommits(String branch, GitAccessInfo accessInfo) {
        Integer projectId = transformProjectId(accessInfo);
        String path = String.format("/api/v4/projects/%s/repository/commits?ref_name=%s&per_page=%d", projectId,
                branch, 20);
        String result = gitRequestProxy.get(accessInfo.getGitDomain() + path, getTokenHeader(accessInfo));
        List<JSONObject> commitsJson = JSON.parseArray(result, JSONObject.class);
        return commitsJson.stream()
                .map(commitJson -> {
                    CommitMessage commit = new CommitMessage();
                    commit.setCommitId(commitJson.getString("id"));
                    commit.setShortId(commitJson.getString("short_id"));
                    commit.setMessage(commitJson.getString("message"));
                    commit.setCommitUser(commitJson.getString("author_name"));
                    commit.setCommitTime(convertIso8601ToTimestamp(commitJson.getString("created_at")));
                    return commit;
                }).collect(Collectors.toList());
    }

    /**
     * 将 ISO 8601 格式的日期字符串转换为时间戳（毫秒）
     *
     * @param iso8601Date ISO 8601 日期字符串，例如 "2024-12-10T11:01:54.000+08:00"
     * @return 时间戳（毫秒）
     */
    public static long convertIso8601ToTimestamp(String iso8601Date) {
        // 使用 OffsetDateTime 解析日期
        OffsetDateTime dateTime = OffsetDateTime.parse(iso8601Date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        // 转换为时间戳（毫秒）
        return dateTime.toInstant().toEpochMilli();
    }

    @Override
    public void createBranch(String branchName, GitAccessInfo accessInfo) {
        Integer projectId = transformProjectId(accessInfo);
        String path = String.format("/api/v4/projects/%s/repository/branches?branch=%s&ref=master",
                projectId, branchName);
        String result = gitRequestProxy.post(accessInfo.getGitDomain() + path, "", getTokenHeader(accessInfo));
        log.info("gitlab create branch result = {}", result);
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
        log.info("gitlab delete branch result = {}", result);
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
            log.info("can not get project branches ={}", projectId);
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
        projectId = serviceIdMap.get(accessInfo.getGitServiceName().toLowerCase());
        log.info("get service={} gitlab project id = {}", accessInfo.getGitServiceName(), projectId);
        return projectId;
    }

    @Override
    public void checkRepository(GitAccessInfo accessInfo) {
        List<GitlabRepository> repositories = getGitlabRepository(accessInfo);
        if (CollectionUtils.isEmpty(repositories)) {
            log.info("gitlab repository not exist={}", accessInfo.getGitServiceName());
            throw new ApiException(ErrorCode.REPO_NOT_EXIST);
        }

        Optional<GitlabRepository> optional = repositories.stream().filter(repo -> isMatchRepo(accessInfo, repo))
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

    private boolean isMatchRepo(GitAccessInfo accessInfo, GitlabRepository repo) {
        return Objects.equals(repo.getName().toLowerCase(), accessInfo.getGitServiceName().toLowerCase()) ||
                Objects.equals(repo.getPath().toLowerCase(), accessInfo.getGitServiceName().toLowerCase());
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
        String result =
                gitRequestProxy.get(accessInfo.getGitDomain() + "/api/v4/projects?search=" + accessInfo.getGitServiceName(),
                        getTokenHeader(accessInfo));
        return JSON.parseArray(result, GitlabRepository.class);
    }

}
