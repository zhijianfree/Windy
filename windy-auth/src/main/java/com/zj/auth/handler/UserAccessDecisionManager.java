package com.zj.auth.handler;

import com.zj.auth.entity.UserSession;
import com.zj.auth.service.PermissionService;
import com.zj.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Component
public class UserAccessDecisionManager implements AccessDecisionManager {
    private final PermissionService permissionService;

    public UserAccessDecisionManager(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        FilterInvocation fi = (FilterInvocation) object;
        if (permissionService.isInWhiteList(fi.getRequestUrl())){
            return;
        }

        if (Objects.isNull(authentication.getPrincipal())){
            return;
        }

        UserSession userSession = (UserSession) authentication.getPrincipal();
        if (Objects.isNull(userSession)) {
            throw new InsufficientAuthenticationException(ErrorCode.USER_TOKEN_INVALID.getMessage());
        }

        HttpServletRequest request = fi.getRequest();
        boolean authCheck = permissionService.authCheck(request, userSession);
        if (!authCheck) {
            throw new AccessDeniedException(ErrorCode.USER_NOT_HAVE_PERMISSION.getMessage());
        }
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return false;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return FilterInvocation.class.isAssignableFrom(aClass);
    }
}
