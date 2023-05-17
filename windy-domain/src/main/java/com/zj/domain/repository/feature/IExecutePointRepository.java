package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.po.feature.ExecutePoint;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/17
 */
public interface IExecutePointRepository {

  ExecutePointDto getExecutePoint(String executePointId);

  List<ExecutePointDto> getExecutePointByFeatureId(String featureId);

  List<ExecutePointDto> getExecutePointByFeatureIds(List<String> featureIds);

}
