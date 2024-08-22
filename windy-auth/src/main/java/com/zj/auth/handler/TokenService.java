package com.zj.auth.handler;

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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TokenService {

    @Value("${windy.token.expire}")
    private Integer tokenExpire;

    @Value("${windy.token.secret}")
    private String tokenSecret;

    public LoginResult createToken(UserSession userSession) {
        String token = generateJWTToken();
        userSession.setToken(token);
        userSession.setExpireTime(System.currentTimeMillis() + tokenExpire * 1000);
        return new LoginResult(token, tokenExpire);
    }

    private static Map<String, Object> getPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.TOKEN_VERSION_KEY, Constants.TOKEN_VERSION);
        payload.put(Constants.TOKEN_IAT_KEY, System.currentTimeMillis());
        payload.put(Constants.TOKEN_ISS_KEY, Constants.TOKEN_ISS);
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
            log.info("verify token error", e);
        }
        return false;
    }

    public String generateJWTToken() {
        JWTSigner jwtSigner = JWTSignerUtil.hs256(tokenSecret.getBytes(StandardCharsets.UTF_8));
        return JWTUtil.createToken(getPayload(), jwtSigner);
    }

}
