package com.zj.auth.service;

import com.zj.auth.entity.LoginResult;
import com.zj.auth.entity.LoginUser;
import com.zj.auth.entity.UpdatePassword;
import com.zj.auth.entity.UserSession;
import com.zj.common.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.auth.GroupDto;
import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.repository.auth.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserService {

    public static final String DEFAULT_PASSWORD = "123456";
    private final TokenHolder tokenHolder;
    private final AuthenticationManager authenticationManager;
    private final IAuthService authService;
    private final IUserRepository userRepository;
    private final UniqueIdService uniqueIdService;

    public UserService(TokenHolder tokenHolder, AuthenticationManager authenticationManager, IAuthService authService, IUserRepository userRepository, UniqueIdService uniqueIdService) {
        this.tokenHolder = tokenHolder;
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.userRepository = userRepository;
        this.uniqueIdService = uniqueIdService;
    }

    public LoginResult login(LoginUser loginUser) {
        Authentication authenticate;
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
        return tokenHolder.createToken(userSession);
    }

    public UserDto getUser() {
        String currentUserId = authService.getCurrentUserId();
        return userRepository.getUserByUserId(currentUserId);
    }

    public UserDto getUser(String userId) {
        return userRepository.getUserByUserId(userId);
    }

    public PageSize<UserDto> getGroupUsers(String groupId, Integer page, Integer size) {
        return userRepository.getGroupUserPage(groupId, page, size);
    }

    public Boolean createUser(UserDto userDto) {
        userDto.setUserId(uniqueIdService.getUniqueId());
        return userRepository.createUser(userDto);
    }

    public Boolean updateUser(String userId, UserDto userDto) {
        userDto.setUserId(userId);
        return userRepository.updateUser(userDto);
    }

    public Boolean deleteUser(String userId) {
        return userRepository.deleteUser(userId);
    }

    public List<UserDto> getUserByName(String name) {
        return userRepository.fuzzyQueryUserName(name);
    }

    public Boolean updatePassword(String userId, UpdatePassword updatePassword) {
        UserDto user = getUser(userId);
        if (Objects.isNull(user)) {
            log.info("user id not find = {}", userId);
            throw new ApiException(ErrorCode.USER_NOT_FIND);
        }

        if (!Objects.equals(user.getPassword(), updatePassword.getOldPassword())) {
            log.info("user old password not match ={}", userId);
            throw new ApiException(ErrorCode.USER_PASSWORD_ERROR);
        }
        user.setPassword(updatePassword.getNewPassword());
        return userRepository.updateUser(user);
    }

    public Boolean resetPassword(String userId) {
        UserDto user = getUser(userId);
        if (Objects.isNull(user)) {
            log.info("user id not find = {}", userId);
            throw new ApiException(ErrorCode.USER_NOT_FIND);
        }
        user.setPassword(DEFAULT_PASSWORD);
        return userRepository.updateUser(user);
    }
}