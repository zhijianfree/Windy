package com.zj.auth.service;

import com.zj.auth.entity.LoginResult;
import com.zj.auth.entity.LoginUser;
import com.zj.auth.entity.UserSession;
import com.zj.auth.handler.TokenService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public UserService(TokenService tokenService, AuthenticationManager authenticationManager) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    public LoginResult login(LoginUser loginUser) {
        Authentication authenticate = null;
        try {
            authenticate =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getUserName(), loginUser.getPassword()));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                log.info("user or password error user={}", loginUser.getUserName());
                throw new ApiException(ErrorCode.USER_PASSWORD_ERROR);
            }

            log.info("user login error", e);
            throw new ApiException(ErrorCode.ERROR);
        }

        UserSession userSession = (UserSession) authenticate.getPrincipal();
        return tokenService.createToken(userSession);
    }
}
