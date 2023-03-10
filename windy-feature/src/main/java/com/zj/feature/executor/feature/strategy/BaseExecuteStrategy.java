package com.zj.feature.executor.feature.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.feature.entity.po.ExecutePoint;
import com.zj.feature.entity.vo.ExecuteDetail;
import com.zj.feature.entity.vo.FeatureResponse;
import com.zj.feature.executor.compare.CompareDefine;
import com.zj.feature.executor.compare.CompareHandler;
import com.zj.feature.executor.compare.CompareResult;
import com.zj.feature.executor.feature.IExecuteStrategy;
import com.zj.feature.executor.feature.IRemoteInvoker;
import com.zj.feature.executor.interceptor.InterceptorProxy;
import com.zj.feature.executor.vo.ExecuteContext;
import com.zj.feature.executor.vo.ExecutorUnit;
import java.util.List;

/**
 * @author falcon
 * @since 2023/1/17
 */
public abstract class BaseExecuteStrategy implements IExecuteStrategy {

  private InterceptorProxy interceptorProxy;

  private IRemoteInvoker IRemoteInvoker;

  private final CompareHandler compareHandler;

  public BaseExecuteStrategy(InterceptorProxy interceptorProxy,
      com.zj.feature.executor.feature.IRemoteInvoker IRemoteInvoker,
      CompareHandler compareHandler) {
    this.interceptorProxy = interceptorProxy;
    this.IRemoteInvoker = IRemoteInvoker;
    this.compareHandler = compareHandler;
  }

  public FeatureResponse executeFeature(ExecuteContext executeContext,
      ExecutePoint executePoint) {
    //1 执行用例，目前使用反射执行，后续考虑使用dubbo调用
    String featureInfo = executePoint.getFeatureInfo();
    ExecutorUnit executorUnit = JSON.parseObject(featureInfo, ExecutorUnit.class);

    //2 将全局变量配置给执行点
    interceptorProxy.beforeExecute(executorUnit, executeContext);

    //3 调用方法执行
    ExecuteDetail executeDetail = (ExecuteDetail) IRemoteInvoker.invoke(executorUnit);

    //4 将执行之后的响应结果添加到context中，方便后面用例使用
    interceptorProxy.afterExecute(executePoint, executeDetail, executeContext);

    //5 下面开始对比
    String compareInfo = executePoint.getCompareDefine();
    List<CompareDefine> compareDefines = JSON.parseArray(compareInfo, CompareDefine.class);
    CompareResult compareResult = compareHandler.compare(executeDetail, compareDefines);

    //6 返回执行状态
    return FeatureResponse.builder().name(executorUnit.getName())
        .pointId(executePoint.getPointId()).executeDetail(executeDetail)
        .compareResult(compareResult).build();
  }
}
