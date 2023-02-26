package com.zj.feature.service;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;


@Service
public class CacheService implements ICacheService{

  private LoadingCache<String, String> loadingCache = Caffeine.newBuilder().maximumSize(1000)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .refreshAfterWrite(1, TimeUnit.SECONDS)
      .build(new CacheLoader<String, String>() {
        @Override
        public @Nullable String load(@NonNull String key) {
          return null;
        }
      });

  public String getCache(String key) {
    return loadingCache.get(key);
  }

  public void deleteCache(String key) {
    loadingCache.invalidate(key);
  }

  public void setCache(String key, String value) {
    loadingCache.put(key, value);
  }
}
