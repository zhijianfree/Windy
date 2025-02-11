package com.zj.auth.service;

import com.zj.auth.entity.UserSession;

public interface ISessionCache {

    void setCacheValue(String cacheKey, UserSession userSession);

    void removeCache(String cacheKey);

    UserSession getCacheValue(String cacheKey);
}
