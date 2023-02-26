package com.zj.feature.service;

public interface ICacheService {

  String getCache(String key);

  void deleteCache(String key);

  void setCache(String key, String value);
}
