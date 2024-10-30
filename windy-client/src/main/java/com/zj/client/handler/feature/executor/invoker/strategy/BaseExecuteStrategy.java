package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.InvokerType;
import com.zj.common.feature.ExecutePointDto;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.common.feature.VariableDefine;
import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.common.feature.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.client.handler.feature.executor.invoker.IExecuteStrategy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.feature.ExecutorUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  public FeatureResponse executeFeature(FeatureExecuteContext featureExecuteContext, ExecutePoint executePoint) {
    //1 执行用例，目前使用反射/http执行，后续考虑使用dubbo调用
    String featureInfo = executePoint.getFeatureInfo();
    ExecutorUnit executorUnit = JSON.parseObject(featureInfo, ExecutorUnit.class);

    //2 将全局变量配置给执行点
    interceptorProxy.beforeExecute(executorUnit, featureExecuteContext);
    log.info("step 1 execute before interceptor service={} context={}", executorUnit.getService(),
            JSON.toJSONString(featureExecuteContext.toMap()));

    //3 调用方法执行
    Integer invokeType = Optional.ofNullable(executorUnit.getRelatedTemplate())
            .map(executor -> InvokerType.RELATED_TEMPLATE.getType()).orElseGet(executorUnit::getInvokeType);
    IExecuteInvoker executeInvoker = executeInvokerMap.get(invokeType);
    ExecuteDetailVo executeDetailVo = (ExecuteDetailVo) executeInvoker.invoke(executorUnit, featureExecuteContext);
    log.info("step 2 execute invoker ={} invoke", executeInvoker.type().name());

    //4 将执行之后的响应结果添加到context中，方便后面用例使用
    interceptorProxy.afterExecute(executePoint, executeDetailVo, featureExecuteContext);
    log.info("step 3 execute execute after interceptor service={} context={}", executorUnit.getService(),
            JSON.toJSONString(featureExecuteContext.toMap()));

    //5 下面开始对比
    String compareInfo = executePoint.getCompareDefine();
    List<CompareDefine> compareDefines = JSON.parseArray(compareInfo, CompareDefine.class);
    CompareResult compareResult = compareHandler.compare(executeDetailVo, compareDefines);

    //6 获取临时全局环境变量
    List<VariableDefine> variableDefines = JSON.parseArray(executePoint.getVariables(), VariableDefine.class);
    Map<String, Object> globalContext = new HashMap<>();
    if (CollectionUtils.isNotEmpty(variableDefines)) {
      variableDefines.stream().filter(VariableDefine::isGlobal).forEach(variableDefine -> {
        Object runtimeValue = featureExecuteContext.toMap().get(variableDefine.getVariableKey());
        globalContext.put(variableDefine.getVariableKey(), runtimeValue);
      });
    }

    //6 返回执行状态
    FeatureResponse response = FeatureResponse.builder().context(globalContext).name(executorUnit.getName()).pointId(executePoint.getPointId())
            .executeDetailVo(executeDetailVo).compareResult(compareResult).build();
    response.setStatus(response.getExecuteStatus());
    return response;
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
