package com.zj.pipeline.entity.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class GithubRepositoryVo {

    /**
     * 仓库ID
     */
    private Long id;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 仓库的完整名称，包含所有者
     */
    @JSONField(name = "full_name")
    private String fullName;

    /**
     * 仓库默认分支
     */
    @JSONField(name = "default_branch")
    private String defaultBranch;

    @JSONField(name = "git_url")
    private String repository;

}
