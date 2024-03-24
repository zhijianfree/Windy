package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.common.feature.ExecutePointDto;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.common.feature.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.client.handler.feature.executor.invoker.IExecuteStrategy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.common.feature.ExecutorUnit;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
@Slf4j
public abstract class BaseExecuteStrategy implements IExecuteStrategy {

  private final InterceptorProxy interceptorProxy;

  private final Map<Integer, IExecuteInvoker> executeInvokerMap;

  private final CompareHandler compareHandler;

  protected BaseExecuteStrategy(InterceptorProxy interceptorProxy,
      List<IExecuteInvoker> executeInvokers, CompareHandler compareHandler) {
    this.interceptorProxy = interceptorProxy;
    this.executeInvokerMap = executeInvokers.stream()
        .collect(Collectors.toMap(invoker -> invoker.type().getType(), invoker -> invoker));
    this.compareHandler = compareHandler;
  }

  public FeatureResponse executeFeature(ExecuteContext executeContext, ExecutePoint executePoint) {
    //1 执行用例，目前使用反射/http执行，后续考虑使用dubbo调用
    String featureInfo = executePoint.getFeatureInfo();
    ExecutorUnit executorUnit = JSON.parseObject(featureInfo, ExecutorUnit.class);

    //2 将全局变量配置给执行点
    interceptorProxy.beforeExecute(executorUnit, executeContext);
    log.info("step 1 execute before interceptor service={} context={}", executorUnit.getService(),
            JSON.toJSONString(executeContext));

    //3 调用方法执行
    IExecuteInvoker executeInvoker = executeInvokerMap.get(executorUnit.getInvokeType());
    ExecuteDetailVo executeDetailVo = (ExecuteDetailVo) executeInvoker.invoke(executorUnit);
    log.info("step 2 execute invoker ={} invoke", executeInvoker.type().name());

    //4 将执行之后的响应结果添加到context中，方便后面用例使用
    interceptorProxy.afterExecute(executePoint, executeDetailVo, executeContext);
    log.info("step 3 execute execute after interceptor service={} context={}", executorUnit.getService(),
            JSON.toJSONString(executeContext.toMap()));

    //5 下面开始对比
    String compareInfo = executePoint.getCompareDefine();
    List<CompareDefine> compareDefines = JSON.parseArray(compareInfo, CompareDefine.class);
    CompareResult compareResult = compareHandler.compare(executeDetailVo, compareDefines);

    //6 返回执行状态
    return FeatureResponse.builder().name(executorUnit.getName()).pointId(executePoint.getPointId())
        .executeDetailVo(executeDetailVo).compareResult(compareResult).build();
  }

  public static ExecutePoint toExecutePoint(ExecutePointDto dto) {
    ExecutePoint point = new ExecutePoint();
    point.setFeatureId(dto.getFeatureId());
    point.setPointId(dto.getPointId());
    point.setDescription(dto.getDescription());
    point.setCompareDefine(JSON.toJSONString(dto.getCompareDefine()));
    point.setVariables(JSON.toJSONString(dto.getVariableDefine()));
    point.setFeatureInfo(JSON.toJSONString(dto.getExecutorUnit()));
    point.setSortOrder(dto.getSortOrder());
    point.setTestStage(dto.getTestStage());
    point.setExecuteType(dto.getExecuteType());
    return point;
  }
}
