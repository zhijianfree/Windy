package com.zj.auth.handler;

import com.alibaba.fastjson2.JSON;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFailPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriter writer = response.getWriter();
        ResponseMeta responseMeta = new ResponseMeta(ErrorCode.USER_TOKEN_INVALID);
        writer.print(JSON.toJSONString(responseMeta));
        writer.flush();
    }
}
