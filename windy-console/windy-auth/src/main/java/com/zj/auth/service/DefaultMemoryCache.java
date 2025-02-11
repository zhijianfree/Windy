package com.zj.auth.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zj.auth.entity.UserSession;
import com.zj.domain.entity.bo.auth.UserBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DefaultMemoryCache implements ISessionCache{

    private final Cache<String, UserSession> memoryCache;

    public DefaultMemoryCache(Integer tokenExpire) {
        memoryCache = CacheBuilder.newBuilder()
                .expireAfterAccess(tokenExpire, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void setCacheValue(String cacheKey, UserSession userSession) {
        memoryCache.put(cacheKey, userSession);
    }

    @Override
    public void removeCache(String cacheKey) {
        memoryCache.invalidate(cacheKey);
    }

    @Override
    public UserSession getCacheValue(String cacheKey) {
        try {
            return memoryCache.get(cacheKey, () -> null);
        } catch (Exception e) {
            log.info("can not get session from memory cache = {}", cacheKey);
            return null;
        }
    }
}
