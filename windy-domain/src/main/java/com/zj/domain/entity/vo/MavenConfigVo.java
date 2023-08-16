package com.zj.domain.entity.vo;

import lombok.Data;

@Data
public class MavenConfigVo {
    /**
     * 推送的Maven仓库地址
     * */
    private String mavenUrl;

    /**
     * 推送镜像用户
     * */
    private String userName;

    /**
     * 推送用户密码
     * */
    private String password;
}
