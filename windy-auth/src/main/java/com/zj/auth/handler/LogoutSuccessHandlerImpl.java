package com.zj.auth.handler;

import com.alibaba.fastjson2.JSON;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //todo 用户退出登录需要清除相关数据
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriter writer = response.getWriter();
        ResponseMeta responseMeta = new ResponseMeta(ErrorCode.SUCCESS);
        writer.print(JSON.toJSONString(responseMeta));
        writer.flush();
    }
}
