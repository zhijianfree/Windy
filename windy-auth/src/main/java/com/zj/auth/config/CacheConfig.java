package com.zj.auth.config;

import com.zj.auth.service.DefaultMemoryCache;
import com.zj.auth.service.ISessionCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    @Bean
    @ConditionalOnMissingBean(ISessionCache.class)
    public ISessionCache getSessionCache(CacheProperties cacheProperties) {
        return new DefaultMemoryCache(cacheProperties.getExpire());
    }
}
