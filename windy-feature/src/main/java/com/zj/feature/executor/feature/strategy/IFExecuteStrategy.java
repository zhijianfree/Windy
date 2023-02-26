package com.zj.feature.executor.feature.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.feature.entity.dto.ExecutePointDTO;
import com.zj.feature.entity.po.ExecutePoint;
import com.zj.feature.entity.type.ExecutePointType;
import com.zj.feature.entity.vo.FeatureResponse;
import com.zj.feature.executor.compare.CompareHandler;
import com.zj.feature.executor.compare.ognl.OgnlDataParser;
import com.zj.feature.executor.interceptor.InterceptorProxy;
import com.zj.feature.executor.vo.ExecuteContext;
import com.zj.feature.executor.vo.ExecutorUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/1/17
 */
@Slf4j
@Component
public class IFExecuteStrategy extends BaseExecuteStrategy{
  private OgnlDataParser ognlDataParser = new OgnlDataParser();

  public IFExecuteStrategy(InterceptorProxy interceptorProxy,
      com.zj.feature.executor.feature.IRemoteInvoker IRemoteInvoker,
      CompareHandler compareHandler) {
    super(interceptorProxy, IRemoteInvoker, compareHandler);
  }

  @Override
  public ExecutePointType getType() {
    return ExecutePointType.IF;
  }

  @Override
  public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext) {
    log.info("start execute IFExecuteStrategy");
    ExecutorUnit executorUnit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit
        .class);
    String ongl = executorUnit.getMethod();
    Boolean result = (Boolean) ognlDataParser.parserExpression(executeContext.toMap(), ongl, null);
    if (!result){
      return Collections.emptyList();
    }

    List<ExecutePointDTO> executePoints = executorUnit.getExecutePoints();
    return executePoints.stream().map(executePointDTO -> {
      ExecutePoint point = ExecutePointDTO.toExecutePoint(executePointDTO);
      return executeFeature(executeContext, point);
    }).collect(Collectors.toList());
  }
}
