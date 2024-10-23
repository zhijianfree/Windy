package com.zj.master.dispatch.pipeline.builder;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.Position;
import com.zj.domain.entity.dto.pipeline.ActionParam;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.master.entity.vo.ActionDetail;
import com.zj.master.entity.vo.ApprovalContext;
import com.zj.master.entity.vo.BuildCodeContext;
import com.zj.master.entity.vo.ConfigDetail;
import com.zj.master.entity.vo.DeployContext;
import com.zj.master.entity.vo.FeatureContext;
import com.zj.master.entity.vo.HttpContext;
import com.zj.master.entity.vo.MergeMasterContext;
import com.zj.master.entity.vo.RequestContext;
import com.zj.master.entity.vo.WaitContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
public class RequestContextBuilder {

    private static final Map<String, Function<ActionDetail, RequestContext>> factoryMap = new HashMap<>();

    static {
        factoryMap.put(ExecuteType.WAIT.name(), RequestContextBuilder::buildWaitContext);
        factoryMap.put(ExecuteType.HTTP.name(), RequestContextBuilder::buildHttpContext);
        factoryMap.put(ExecuteType.TEST.name(), RequestContextBuilder::buildTestContext);
        factoryMap.put(ExecuteType.DEPLOY.name(), RequestContextBuilder::buildDeployContext);
        factoryMap.put(ExecuteType.APPROVAL.name(), RequestContextBuilder::buildApprovalContext);
        factoryMap.put(ExecuteType.BUILD.name(), RequestContextBuilder::buildCodeContext);
        factoryMap.put(ExecuteType.MERGE.name(), RequestContextBuilder::buildMergeContext);
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
        return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()), WaitContext.class);
    }

    private static RequestContext buildApprovalContext(ActionDetail actionDetail) {
        ConfigDetail configDetail = actionDetail.getConfigDetail();
        return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()), ApprovalContext.class);
    }


    private static RequestContext buildHttpContext(ActionDetail actionDetail) {
        ConfigDetail configDetail = actionDetail.getConfigDetail();
        Map<String, String> requestContext = configDetail.getParamList();
        String actionUrl = actionDetail.getAction().getActionUrl();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> body = new HashMap<>();
        if (CollectionUtils.isNotEmpty(actionDetail.getAction().getParamList())) {
            actionUrl = exchangeRequestUrl(actionDetail, requestContext, actionUrl);
            headers = getActionParamsByPosition(actionDetail, Position.Header.name(), requestContext);
            actionDetail.getAction().setHeaders(headers);
            actionUrl = warpPathParam(actionDetail, requestContext, actionUrl);
            body = getActionParamsByPosition(actionDetail, Position.Body.name(), requestContext);
        }
        return HttpContext.builder().body(JSON.toJSONString(body)).bodyType(actionDetail.getAction().getBodyType())
                .url(actionUrl).headers(headers).build();
    }

    private static String warpPathParam(ActionDetail actionDetail, Map<String, String> requestContext, String actionUrl) {
        Map<String, String> pathParams = getActionParamsByPosition(actionDetail, Position.Path.name(),
                requestContext);
        if (MapUtils.isEmpty(pathParams)) {
            return actionUrl;
        }
        StrSubstitutor strSubstitutor = new StrSubstitutor(pathParams);
        actionUrl = strSubstitutor.replace(actionUrl);
        return actionUrl;
    }

    private static String exchangeRequestUrl(ActionDetail actionDetail, Map<String, String> requestContext, String actionUrl) {
        Map<String, String> queryParams = getActionParamsByPosition(actionDetail, Position.Query.name(), requestContext);
        actionUrl = exchangeUrl(actionUrl, queryParams);
        return actionUrl;
    }

    private static String exchangeUrl(String actionUrl, Map<String, String> queryParams) {
        List<String> queryList = queryParams.entrySet().stream().map(param -> param.getKey() + "=" + param.getValue())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queryList)) {
            return actionUrl;
        }

        String queryLine = String.join(",", queryList);
        if (!actionUrl.contains("?")){
            return actionUrl + "?" + queryLine;
        }
        return actionUrl + "&" + queryList;
    }

    private static Map<String, String> getActionParamsByPosition(ActionDetail actionDetail, String position, Map<String,
            String> requestContext) {
        return actionDetail.getAction().getParamList().stream().filter(param -> Objects.equals(position,
                param.getPosition())).map(param -> {
            param.setValue(requestContext.get(param.getName()));
            return param;
        }).collect(Collectors.toMap(ActionParam::getName, ActionParam::getValue));
    }

    private static RequestContext buildTestContext(ActionDetail actionDetail) {
        ConfigDetail configDetail = actionDetail.getConfigDetail();
        return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()), FeatureContext.class);
    }

    private static RequestContext buildDeployContext(ActionDetail actionDetail) {
        ConfigDetail configDetail = actionDetail.getConfigDetail();
        return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()), DeployContext.class);
    }

    private static RequestContext buildCodeContext(ActionDetail actionDetail) {
        ConfigDetail configDetail = actionDetail.getConfigDetail();
        return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()), BuildCodeContext.class);
    }

    private static RequestContext buildMergeContext(ActionDetail actionDetail) {
        ConfigDetail configDetail = actionDetail.getConfigDetail();
        return JSON.parseObject(JSON.toJSONString(configDetail.getParamList()), MergeMasterContext.class);
    }
}
