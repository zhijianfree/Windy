package com.zj.client.handler.feature.ability.redis;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.vo.ExecuteDetailVo;
import com.zj.client.handler.feature.ability.Feature;
import com.zj.client.handler.feature.ability.FeatureDefine;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;


/**
 * @author guyuelan
 * @since 2023/1/11
 */
@Slf4j
public class RedisFeature implements Feature {

  public ExecuteDetailVo setValue(String ip, Integer port, String key, String value,
      Integer timeout) {
    ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
    saveRequestParam(ip, port, key, value, timeout, executeDetailVo);
    try {
      Jedis jedis = new Jedis(ip, port);
      String result = jedis.setex(key, timeout, value);
      log.info("get request result={}", result);
      executeDetailVo.setStatus(true);
      executeDetailVo.setResBody(result);
      jedis.close();
    } catch (Exception e) {
      executeDetailVo.setStatus(false);
      executeDetailVo.setErrorMessage(e.getMessage());
      log.error("request redis error", e);
    }
    return executeDetailVo;
  }

  public ExecuteDetailVo getValue(String ip, Integer port,String key) {
    ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
    executeDetailVo.addRequestInfo("ip: " + ip);
    executeDetailVo.addRequestInfo("port: " + port);
    executeDetailVo.addRequestInfo("key: " + key);

    try {
      Jedis jedis = new Jedis(ip, port);
      String result = jedis.get(key);
      jedis.close();
      executeDetailVo.setStatus(true);
      executeDetailVo.setResBody(result);
    }catch (Exception e){
      executeDetailVo.setStatus(false);
      executeDetailVo.setErrorMessage(e.getMessage());
      log.error("get redis error", e);
    }
    return executeDetailVo;
  }

  private static void saveRequestParam(String ip, Integer port, String key, String value, Integer timeout,
      ExecuteDetailVo executeDetailVo) {
    executeDetailVo.addRequestInfo("ip: " + ip);
    executeDetailVo.addRequestInfo("port: " + port);
    executeDetailVo.addRequestInfo("key: " + key);
    executeDetailVo.addRequestInfo("value: " + value);
    executeDetailVo.addRequestInfo("timeout: " + timeout);
  }

  @Override
  public List<FeatureDefine> scanFeatureDefines() {
    return null;
  }

  public static void main(String[] args) {
    RedisFeature redisFeature = new RedisFeature();
    ExecuteDetailVo executeDetailVo = redisFeature.setValue("10.202.162.127", 6379, "name_huhu", "guyuelan", 5);
    System.out.println(JSON.toJSONString(executeDetailVo));

    ExecuteDetailVo result = redisFeature.getValue("10.202.162.127", 6379, "name_huhu");
    System.out.println(JSON.toJSONString(result));

  }
}
