package com.zj.auth.rest;

import com.zj.auth.entity.LoginResult;
import com.zj.auth.entity.LoginUser;
import com.zj.auth.service.UserService;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.auth.UserDto;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops")
public class UserRest {

    private final UserService userService;

    public UserRest(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/login")
    public ResponseMeta<LoginResult> login(@RequestBody LoginUser loginUser) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.login(loginUser));
    }

    @PostMapping("/users")
    public ResponseMeta<Boolean> createUser(@RequestBody UserDto userDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.createUser(userDto));
    }

    @PutMapping("/users/{userId}")
    public ResponseMeta<Boolean> updateUser(@PathVariable("userId") String userId, @RequestBody UserDto userDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.updateUser(userId, userDto));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseMeta<Boolean> deleteUser(@PathVariable("userId") String userId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.deleteUser(userId));
    }

    @GetMapping("/user")
    public ResponseMeta<List<UserDto>> getUserByName(@RequestParam("name") String name) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.getUserByName(name));
    }

    @GetMapping("/users/{userId}")
    public ResponseMeta<UserDto> getUser(@PathVariable("userId") String userId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.getUser(userId));
    }

    @GetMapping("/groups/{groupId}/users")
    public ResponseMeta<PageSize<UserDto>> getGroupUsers(@PathVariable("groupId") String groupId,
                                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.getGroupUsers(groupId, page, size));
    }

    @GetMapping("/user/detail")
    public ResponseMeta<UserDto> login() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, userService.getUser());
    }
}
