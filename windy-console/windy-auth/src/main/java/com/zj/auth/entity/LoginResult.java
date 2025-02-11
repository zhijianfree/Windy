package com.zj.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {

    private String token;

    /**
     * token过期时间，单位秒
     */
    private Integer expireTime;
}
