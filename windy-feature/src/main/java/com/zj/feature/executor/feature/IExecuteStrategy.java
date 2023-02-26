package com.zj.feature.executor.feature;

import com.zj.feature.entity.po.ExecutePoint;
import com.zj.feature.entity.type.ExecutePointType;
import com.zj.feature.entity.vo.FeatureResponse;
import com.zj.feature.executor.vo.ExecuteContext;
import java.util.List;

/**
 * @author falcon
 * @since 2023/1/17
 */
public interface IExecuteStrategy {

  ExecutePointType getType();

  List<FeatureResponse> execute(ExecutePoint executePoint,
      ExecuteContext executeContext);
}
