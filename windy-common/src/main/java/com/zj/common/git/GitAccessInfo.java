package com.zj.common.git;

import lombok.Data;

@Data
public class GitAccessInfo {

    private String gitDomain;

    private String accessToken;

    private String owner;

    private String gitType;
}
