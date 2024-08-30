package com.zj.auth.entity;

import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.enums.UserStatus;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Data
public class UserSession implements UserDetails {

    private UserDto userDto;

    private String token;

    private Long expireTime;

    public UserSession(UserDto userDto) {
        this.userDto = userDto;
    }

    public String getUserId(){
        return userDto.getUserId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return userDto.getPassword();
    }

    @Override
    public String getUsername() {
        return userDto.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (Objects.isNull(expireTime)) {
            return true;
        }
        return System.currentTimeMillis() < expireTime;
    }

    @Override
    public boolean isEnabled() {
        return Objects.equals(userDto.getStatus(), UserStatus.NORMAL.getType());
    }
}
