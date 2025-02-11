package com.zj.pipeline.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class GithubCommitVo {

    /**
     * 事件分支
     */
    private String ref;

    /**
     * 仓库信息
     */
    private GithubRepositoryVo repository;

    /**
     * 提交信息
     */
    private List<GithubCommit> commits;

    @Data
    public static class GithubCommit{

        /**
         * 提交信息
         */
        private String message;

        /**
         * 提交的时间格式如: 2025-01-05T10:00:00Z
         */
        private String timestamp;
    }
}
