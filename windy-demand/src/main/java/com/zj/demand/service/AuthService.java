package com.zj.demand.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService{
    @Override
    public String getCurrentUserId() {
        return "admin";
    }
}
