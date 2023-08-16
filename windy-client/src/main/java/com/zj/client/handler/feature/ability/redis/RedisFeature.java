package com.zj.client.handler.feature.ability.redis;

import com.alibaba.fastjson.JSON;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.plugin.loader.ParamValueType;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.plugin.loader.ParameterDefine;
import java.util.ArrayList;
import java.util.Arrays;
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
    FeatureDefine setDefine = getRedisSetDefine();
    FeatureDefine queryDefine = getRedisQueryDefine();
    return Arrays.asList(setDefine, queryDefine);
  }

  private FeatureDefine getRedisQueryDefine() {
    List<ParameterDefine> parameterDefines = new ArrayList<>();
    ParameterDefine ip = new ParameterDefine();
    ip.setParamKey("ip");
    ip.setType(ParamValueType.String.getType());
    parameterDefines.add(ip);

    ParameterDefine port = new ParameterDefine();
    port.setParamKey("port");
    port.setType(ParamValueType.Integer.getType());
    parameterDefines.add(port);

    ParameterDefine key = new ParameterDefine();
    key.setParamKey("key");
    key.setType(ParamValueType.String.getType());
    parameterDefines.add(key);

    FeatureDefine featureDefine = new FeatureDefine();
    featureDefine.setSource(RedisFeature.class.getName());
    featureDefine.setDescription("Redis读操作");
    featureDefine.setMethod("getValue");
    featureDefine.setName("Redis-Query");
    featureDefine.setParams(parameterDefines);
    return featureDefine;
  }

  private FeatureDefine getRedisSetDefine() {
    List<ParameterDefine> parameterDefines = new ArrayList<>();
    ParameterDefine ip = new ParameterDefine();
    ip.setParamKey("ip");
    ip.setType(ParamValueType.String.getType());
    parameterDefines.add(ip);

    ParameterDefine port = new ParameterDefine();
    port.setParamKey("port");
    port.setType(ParamValueType.Integer.getType());
    parameterDefines.add(port);

    ParameterDefine key = new ParameterDefine();
    key.setParamKey("key");
    key.setType(ParamValueType.String.getType());
    parameterDefines.add(key);

    ParameterDefine value = new ParameterDefine();
    value.setParamKey("value");
    value.setType(ParamValueType.String.getType());
    parameterDefines.add(value);

    ParameterDefine timeout = new ParameterDefine();
    timeout.setParamKey("timeout");
    timeout.setType(ParamValueType.Integer.getType());
    parameterDefines.add(timeout);

    FeatureDefine featureDefine = new FeatureDefine();
    featureDefine.setSource(RedisFeature.class.getName());
    featureDefine.setDescription("Redis写操作");
    featureDefine.setMethod("setValue");
    featureDefine.setName("Redis-Set");
    featureDefine.setParams(parameterDefines);
    return featureDefine;
  }

  public static void main(String[] args) {
    RedisFeature redisFeature = new RedisFeature();
    ExecuteDetailVo executeDetailVo = redisFeature.setValue("10.202.162.127", 6379, "name_huhu", "guyuelan", 5);
    System.out.println(JSON.toJSONString(executeDetailVo));

    ExecuteDetailVo result = redisFeature.getValue("10.202.162.127", 6379, "name_huhu");
    System.out.println(JSON.toJSONString(result));

  }
}
