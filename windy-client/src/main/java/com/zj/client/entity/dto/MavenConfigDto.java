package com.zj.client.entity.dto;

import lombok.Data;

@Data
public class MavenConfigDto {

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

    /**
     * maven的安装路径
     */
    private String mavenPath;
}
