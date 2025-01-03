package com.zj.client.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class MavenConfigDto {

    /**
     * maven的远程仓库
     */
    private List<RemoteRepository> remoteRepositories;

    /**
     * maven的安装路径
     */
    private String mavenPath;

    @Data
    public static final class RemoteRepository {

        /**
         * 仓库ID
         */
        private String repositoryId;

        /**
         * maven仓库地址
         */
        private String repositoryUrl;

        /**
         * 访问maven仓库的用户
         */
        private String userName;

        /**
         * 访问maven仓库用户的密码
         */
        private String password;
    }
}
