package com.zj.domain.entity.vo;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

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

    public boolean checkConfig() {
        return StringUtils.isNotBlank(mavenUrl) && StringUtils.isNotBlank(userName)
            && StringUtils.isNotBlank(password);
    }
}
