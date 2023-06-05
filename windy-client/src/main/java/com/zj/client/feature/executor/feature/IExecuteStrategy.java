package com.zj.client.feature.executor.feature;

import com.zj.client.entity.enuns.ExecutePointType;
import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.feature.executor.vo.ExecuteContext;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/1/17
 */
public interface IExecuteStrategy {

  ExecutePointType getType();

  List<FeatureResponse> execute(ExecutePoint executePoint,
      ExecuteContext executeContext);
}
