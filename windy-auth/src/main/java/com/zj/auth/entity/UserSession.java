package com.zj.auth.entity;

import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.enums.UserStatus;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Data
public class UserSession implements UserDetails {

    private UserBO userBO;

    private String token;

    private Long expireTime;

    public UserSession(UserBO userBO) {
        this.userBO = userBO;
    }

    public String getUserId(){
        return userBO.getUserId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return userBO.getPassword();
    }

    @Override
    public String getUsername() {
        return userBO.getUserName();
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
        return Objects.equals(userBO.getStatus(), UserStatus.NORMAL.getType());
    }
}
