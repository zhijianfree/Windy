package com.zj.pipeline.executer.Invoker.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.pipeline.entity.dto.PipelineActionDto;
import com.zj.pipeline.entity.vo.ActionParam;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.HttpRequestContext;
import com.zj.pipeline.executer.vo.RequestContext;
import com.zj.pipeline.executer.vo.WaitRequestContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * @author falcon
 * @since 2023/5/8
 */
@Slf4j
public class ContextBuilder {

  public static final String DEFAULT_WAIT = "10";
  public static final String WAIT_TIME_KEY = "waitTime";

  private static Map<String, Function<PipelineActionDto, RequestContext>> factoryMap = new HashMap<>();

  static {
    factoryMap.put(ExecuteType.WAIT.name(), ContextBuilder::buildWaitContext);
    factoryMap.put(ExecuteType.HTTP.name(), ContextBuilder::buildHttpContext);
  }

  public static RequestContext createContext(String type, PipelineActionDto action) {
    Function<PipelineActionDto, RequestContext> contextFunc = factoryMap.get(type);
    if (Objects.isNull(contextFunc)){
      log.info("can not find context function type={}", type);
      return new RequestContext();
    }
    return contextFunc.apply(action);
  }

  private static RequestContext buildWaitContext(PipelineActionDto action) {
    String waitTime = action.getParamList().stream()
        .filter(param -> Objects.equals(param.getName(), WAIT_TIME_KEY)).findFirst()
        .map(ActionParam::getValue).orElse(DEFAULT_WAIT);
    return WaitRequestContext.builder().waitTime(Integer.parseInt(waitTime)).build();
  }

  private static RequestContext buildHttpContext(PipelineActionDto action){
    JSONObject jsonObject = new JSONObject();
    action.getParamList().forEach(param -> jsonObject.put(param.getName(), param.getValue()));
    return HttpRequestContext.builder().body(JSON.toJSONString(jsonObject))
        .url(action.getActionUrl()).build();
  }
}
