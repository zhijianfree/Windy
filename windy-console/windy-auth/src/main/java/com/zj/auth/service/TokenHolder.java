package com.zj.auth.service;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.zj.auth.entity.Constants;
import com.zj.auth.entity.LoginResult;
import com.zj.auth.entity.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TokenHolder {

    @Value("${windy.token.expire}")
    private Integer tokenExpire;

    @Value("${windy.token.secret}")
    private String tokenSecret;

    private final ISessionCache sessionCache;

    public TokenHolder(ISessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }

    public LoginResult createToken(UserSession userSession) {
        String userId = userSession.getUserId();
        String token = generateJWTToken(userId);
        refreshToken(token, userSession);

        saveCache(userSession);
        return new LoginResult(token, tokenExpire);
    }

    private void saveCache(UserSession userSession) {
        sessionCache.setCacheValue(userSession.getUserId(), userSession);
    }

    private void refreshToken(String token, UserSession userSession) {
        userSession.setToken(token);
        userSession.setExpireTime(System.currentTimeMillis() + tokenExpire * 1000);
    }

    private static Map<String, Object> getPayload(String userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.TOKEN_VERSION_KEY, Constants.TOKEN_VERSION);
        payload.put(Constants.TOKEN_IAT_KEY, System.currentTimeMillis());
        payload.put(Constants.TOKEN_ISS_KEY, Constants.TOKEN_ISS);
        payload.put(Constants.TOKEN_SUB_KEY, userId);
        return payload;
    }

    public boolean verifyToken(String token, String secret) {
        if (StringUtils.isBlank(token)) {
            log.info("token is empty verify error");
            return false;
        }
        JWTSigner jwtSigner = JWTSignerUtil.hs256(secret.getBytes(StandardCharsets.UTF_8));
        try {
            return JWTUtil.verify(token, jwtSigner);
        } catch (Exception e) {
            log.info("verify token error");
        }
        return false;
    }

    public String generateJWTToken(String userId) {
        JWTSigner jwtSigner = JWTSignerUtil.hs256(tokenSecret.getBytes(StandardCharsets.UTF_8));
        return JWTUtil.createToken(getPayload(userId), jwtSigner);
    }

    public UserSession getUserSessionByToken(HttpServletRequest request) {
        String authTokenValue = request.getHeader(Constants.AUTHORIZATION_KEY);
        if (StringUtils.isBlank(authTokenValue) || !authTokenValue.startsWith(Constants.TOKEN_PREFIX)){
            log.info("token value is invalid, not start with: {}", Constants.TOKEN_PREFIX);
            return null;
        }

        authTokenValue =  authTokenValue.replace(Constants.TOKEN_PREFIX,"");
        boolean verifyResult = verifyToken(authTokenValue, tokenSecret);
        if (!verifyResult) {
            log.info("verify token fail");
            return null;
        }

        JWT jwt = JWTUtil.parseToken(authTokenValue);
        return sessionCache.getCacheValue((String) jwt.getPayload(Constants.TOKEN_SUB_KEY));
    }

    public void deleteToken(UserSession session) {
        sessionCache.removeCache(session.getUserId());
    }
}
