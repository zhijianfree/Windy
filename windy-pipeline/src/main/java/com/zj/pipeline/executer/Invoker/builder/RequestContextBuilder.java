package com.zj.pipeline.executer.Invoker.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.pipeline.entity.dto.PipelineActionDto;
import com.zj.pipeline.entity.vo.ActionDetail;
import com.zj.pipeline.entity.vo.ActionParam;
import com.zj.pipeline.entity.vo.ConfigDetail;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.HttpRequestContext;
import com.zj.pipeline.executer.vo.RequestContext;
import com.zj.pipeline.executer.vo.TestRequestContext;
import com.zj.pipeline.executer.vo.WaitRequestContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author falcon
 * @since 2023/5/8
 */
@Slf4j
public class RequestContextBuilder {

  public static final String DEFAULT_WAIT = "10";
  public static final String WAIT_TIME_KEY = "waitTime";
  public static final String TASK_ID = "taskId";

  private static Map<String, Function<ActionDetail, RequestContext>> factoryMap = new HashMap<>();

  static {
    factoryMap.put(ExecuteType.WAIT.name(), RequestContextBuilder::buildWaitContext);
    factoryMap.put(ExecuteType.HTTP.name(), RequestContextBuilder::buildHttpContext);
    factoryMap.put(ExecuteType.TEST.name(), RequestContextBuilder::buildTestContext);
  }

  public static RequestContext createContext(ActionDetail actionDetail) {
    PipelineActionDto action = actionDetail.getAction();
    Function<ActionDetail, RequestContext> contextFunc = factoryMap.get(action.getExecuteType());
    if (Objects.isNull(contextFunc)) {
      log.info("can not find context function type={}", action.getExecuteType());
      return new RequestContext();
    }
    return contextFunc.apply(actionDetail);
  }

  private static RequestContext buildWaitContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    Map<String, String> requestContext = configDetail.getRequestContext();
    String waitTime = Optional.ofNullable(requestContext.get(WAIT_TIME_KEY)).orElse(DEFAULT_WAIT);
    return WaitRequestContext.builder().waitTime(Integer.parseInt(waitTime)).build();
  }

  private static RequestContext buildHttpContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    Map<String, String> requestContext = configDetail.getRequestContext();
    return HttpRequestContext.builder().body(JSON.toJSONString(requestContext))
        .url(actionDetail.getAction().getActionUrl()).build();
  }

  private static RequestContext buildTestContext(ActionDetail actionDetail) {
    ConfigDetail configDetail = actionDetail.getConfigDetail();
    String taskId = configDetail.getRequestContext().get(TASK_ID);
    return TestRequestContext.builder().taskId(taskId).build();
  }
}
