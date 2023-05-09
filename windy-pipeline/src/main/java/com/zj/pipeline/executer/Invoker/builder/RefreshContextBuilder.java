package com.zj.pipeline.executer.Invoker.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.pipeline.entity.dto.PipelineActionDto;
import com.zj.pipeline.entity.vo.ActionDetail;
import com.zj.pipeline.entity.vo.ActionParam;
import com.zj.pipeline.entity.vo.ConfigDetail;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.HttpRequestContext;
import com.zj.pipeline.executer.vo.RefreshContext;
import com.zj.pipeline.executer.vo.RequestContext;
import com.zj.pipeline.executer.vo.TestRequestContext;
import com.zj.pipeline.executer.vo.WaitRequestContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author falcon
 * @since 2023/5/8
 */
@Slf4j
public class RefreshContextBuilder {

  private static final String TASK_ID = "taskId";
  private static final String TASK_STATUS_URL = "http://localhost:9768/v1/devops/feature/task/%s/status";

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
    Map<String, String> paramMap = actionDetail.getConfigDetail().getRequestContext();
    String url = String.format(TASK_STATUS_URL, paramMap.get(TASK_ID));
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    return RefreshContext.builder().url(url).compareConfig(configDetail.getCompareInfo())
        .headers(new HashMap<>()).build();
  }
}
