package com.zj.auth.service;

import com.zj.auth.entity.UserSession;
import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.auth.UserDetail;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Slf4j
@Component
public class WindyAuthService implements IAuthService {

    private final TokenHolder tokenHolder;

    public WindyAuthService(TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    @Override
    public String getCurrentUserId() {
        UserSession userSession = getUserSession();
        if (userSession == null) return null;
        return userSession.getUserId();
    }

    private UserSession getUserSession() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            return null;
        }
        HttpServletRequest servletRequest = requestAttributes.getRequest();
        return tokenHolder.getUserSessionByToken(servletRequest);
    }

    @Override
    public UserDetail getUserDetail() {
        UserSession userSession = getUserSession();
        if (Objects.isNull(userSession)) {
            log.info("user session is invalid, can not get user detail");
            throw new ApiException(ErrorCode.USER_TOKEN_INVALID);
        }
        return UserDetail.builder()
                .userId(userSession.getUserId())
                .userName(userSession.getUsername())
                .nickName(userSession.getUserBO().getNickName())
                .groupId(userSession.getUserId())
                .build();
    }
}
