package com.zj.master.dispatch.pipeline.builder;

import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.common.enums.ExecuteType;
import com.zj.master.entity.vo.ActionDetail;
import com.zj.master.entity.vo.ConfigDetail;
import com.zj.master.entity.vo.RefreshContext;
import java.util.HashMap;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
public class RefreshContextBuilder {

  private static final String TASK_ID = "taskId";

  public static RefreshContext createContext(ActionDetail actionDetail) {
    PipelineActionDto action = actionDetail.getAction();
    if (Objects.equals(action.getExecuteType(), ExecuteType.TEST.name())) {
      log.info("handle test context function type={}", action.getExecuteType());
      return buildTestContext(actionDetail);
    }
    return buildDefaultContext(actionDetail);
  }


  private static RefreshContext buildDefaultContext(ActionDetail actionDetail) {
    PipelineActionDto action = actionDetail.getAction();
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return RefreshContext.builder().url(action.getQueryUrl())
        .compareConfig(configDetail.getCompareInfo()).headers(new HashMap<>()).build();
  }

  private static RefreshContext buildTestContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return RefreshContext.builder().compareConfig(configDetail.getCompareInfo())
        .headers(new HashMap<>()).build();
  }
}
