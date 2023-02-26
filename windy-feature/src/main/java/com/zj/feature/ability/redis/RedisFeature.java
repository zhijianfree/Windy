package com.zj.feature.ability.redis;

import com.alibaba.fastjson.JSON;
import com.zj.feature.ability.Feature;
import com.zj.feature.ability.FeatureDefine;
import com.zj.feature.entity.vo.ExecuteDetail;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;


/**
 * @author falcon
 * @since 2023/1/11
 */
@Slf4j
public class RedisFeature implements Feature {

  public ExecuteDetail setValue(String ip, Integer port, String key, String value,
      Integer timeout) {
    ExecuteDetail executeDetail = new ExecuteDetail();
    saveRequestParam(ip, port, key, value, timeout, executeDetail);
    try {
      Jedis jedis = new Jedis(ip, port);
      String result = jedis.setex(key, timeout, value);
      log.info("get request result={}", result);
      executeDetail.setStatus(true);
      executeDetail.setResBody(result);
      jedis.close();
    } catch (Exception e) {
      executeDetail.setStatus(false);
      executeDetail.setErrorMessage(e.getMessage());
      log.error("request redis error", e);
    }
    return executeDetail;
  }

  public ExecuteDetail getValue(String ip, Integer port,String key) {
    ExecuteDetail executeDetail = new ExecuteDetail();
    executeDetail.addRequestInfo("ip: " + ip);
    executeDetail.addRequestInfo("port: " + port);
    executeDetail.addRequestInfo("key: " + key);

    try {
      Jedis jedis = new Jedis(ip, port);
      String result = jedis.get(key);
      jedis.close();
      executeDetail.setStatus(true);
      executeDetail.setResBody(result);
    }catch (Exception e){
      executeDetail.setStatus(false);
      executeDetail.setErrorMessage(e.getMessage());
      log.error("get redis error", e);
    }
    return executeDetail;
  }

  private static void saveRequestParam(String ip, Integer port, String key, String value, Integer timeout,
      ExecuteDetail executeDetail) {
    executeDetail.addRequestInfo("ip: " + ip);
    executeDetail.addRequestInfo("port: " + port);
    executeDetail.addRequestInfo("key: " + key);
    executeDetail.addRequestInfo("value: " + value);
    executeDetail.addRequestInfo("timeout: " + timeout);
  }

  @Override
  public List<FeatureDefine> scanFeatureDefines() {
    return null;
  }

  public static void main(String[] args) {
    RedisFeature redisFeature = new RedisFeature();
    ExecuteDetail executeDetail = redisFeature.setValue("10.202.162.127", 6379, "name_huhu", "guyuelan", 5);
    System.out.println(JSON.toJSONString(executeDetail));

    ExecuteDetail result = redisFeature.getValue("10.202.162.127", 6379, "name_huhu");
    System.out.println(JSON.toJSONString(result));

  }
}
