package com.zj.pipeline.entity.dto;

import com.zj.pipeline.entity.po.GitBind;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author falcon
 * @since 2021/10/15
 */

@Builder
@Data
public class GitBindDto {

    /**
     * 绑定Id
     */
    private String bindId;

    /**
     * 绑定分支
     */
    @NotEmpty
    private String gitBranch;

    /**
     * git地址
     */
    @NotEmpty
    private String gitUrl;

    /**
     * 绑定类型： 0 push  1 merge
     */
    @NotNull
    private Integer bindType;

    /**
     * 流水线Id
     */
    @NotEmpty
    private String pipelineId;

    /**
     * 创建时间
     * */
    private Long createTime;

    /**
     * 更新时间
     * */
    private Long updateTime;

    public static GitBind toGitBind(GitBindDto gitBindDto) {
        GitBind gitBind = new GitBind();
        gitBind.setBindId(gitBindDto.getBindId());
        gitBind.setGitBranch(gitBindDto.getGitBranch());
        gitBind.setGitUrl(gitBindDto.getGitUrl());
        gitBind.setBindType(gitBindDto.getBindType());
        gitBind.setPipelineId(gitBindDto.getPipelineId());
        return gitBind;
    }

    public static GitBindDto toGitBindDto(GitBind gitBind) {
        return GitBindDto.builder().bindId(gitBind.getBindId())
                .bindType(gitBind.getBindType())
                .gitBranch(gitBind.getGitBranch())
                .gitUrl(gitBind.getGitUrl())
                .pipelineId(gitBind.getPipelineId())
                .createTime(gitBind.getCreateTime())
                .updateTime(gitBind.getUpdateTime())
                .build();
    }
}
