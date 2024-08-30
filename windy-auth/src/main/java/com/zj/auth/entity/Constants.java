package com.zj.auth.entity;

public class Constants {
    public static final String TOKEN_VERSION_KEY = "version";
    public static final String TOKEN_VERSION = "v1";
    public static final String TOKEN_ISS = "Windy";
    public static final String TOKEN_ISS_KEY = "iss";
    public static final String TOKEN_SUB_KEY = "sub";
    public static final String TOKEN_IAT_KEY = "iat";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_KEY = "Authorization";
    public static final String JSON_MEDIA_TYPE = "application/json;charset=UTF-8";
    public static final String ANY_OPERATE = "*";
    public static final String USER_LOGIN_URL = "/v1/devops/user/login";
    public static final String USER_LOGOUT_URL = "/v1/devops/user/logout";
}
