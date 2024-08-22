package com.zj.auth.rest;

import com.zj.auth.entity.LoginResult;
import com.zj.auth.entity.LoginUser;
import com.zj.auth.service.UserService;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/devops")
public class UserRest {

    private final UserService userService;

    public UserRest(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseMeta<LoginResult> login(@RequestBody LoginUser loginUser) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.login(loginUser));
    }
}
