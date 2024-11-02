package com.zj.auth.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zj.auth.entity.Constants;
import com.zj.auth.entity.UserSession;
import com.zj.auth.service.PermissionService;
import com.zj.auth.service.TokenHolder;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final TokenHolder tokenHolder;

    private final PermissionService permissionService;


    public JwtAuthenticationTokenFilter(TokenHolder tokenHolder, PermissionService permissionService) {
        this.tokenHolder = tokenHolder;
        this.permissionService = permissionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        UserSession userSession = tokenHolder.getUserSessionByToken(request);
        if (!permissionService.isInWhiteList(request.getRequestURI()) && Objects.isNull(userSession)) {
            forbiddenAccess(response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userSession, null, Collections.emptyList());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    private void forbiddenAccess(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(Constants.JSON_MEDIA_TYPE);
        PrintWriter writer = response.getWriter();
        ResponseMeta responseMeta = new ResponseMeta(ErrorCode.USER_TOKEN_INVALID);
        writer.print(JSON.toJSONString(responseMeta, SerializerFeature.WriteMapNullValue));
        writer.flush();
    }
}
