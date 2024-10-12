package com.zj.common.git;

import com.zj.common.utils.GitUtils;
import lombok.Data;

@Data
public class GitAccessInfo {

    private String gitDomain;

    private String accessToken;

    private String owner;

    private String gitType;

    public String getGitServiceName() {
        return GitUtils.getServiceFromUrl(getGitDomain());
    }
}
