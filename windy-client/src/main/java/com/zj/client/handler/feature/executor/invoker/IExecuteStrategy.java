package com.zj.client.handler.feature.executor.invoker;

import com.zj.client.entity.vo.ExecutePoint;
import com.zj.common.entity.feature.FeatureResponse;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.common.enums.TemplateType;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
public interface IExecuteStrategy {

  List<TemplateType> getType();

  List<FeatureResponse> execute(ExecutePoint executePoint,
      FeatureExecuteContext featureExecuteContext);
}
