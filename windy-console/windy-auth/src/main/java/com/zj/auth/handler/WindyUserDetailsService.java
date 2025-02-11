package com.zj.auth.handler;

import com.zj.auth.entity.UserSession;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.enums.UserStatus;
import com.zj.domain.repository.auth.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class WindyUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    public WindyUserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserBO user = userRepository.getUserByUserName(userName);
        if (Objects.isNull(user)) {
            log.info("user is not find ={}", userName);
            throw new ApiException(ErrorCode.USER_NOT_FIND);
        }

        if (Objects.equals(UserStatus.DISABLE.getType(), user.getStatus())) {
            log.info("user is not find ={}", userName);
            throw new ApiException(ErrorCode.USER_WAS_BLOCKED);
        }

        return new UserSession(user);
    }

}
