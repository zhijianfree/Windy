package com.zj.client.handler.pipeline.executer.trigger.strategy;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zj.client.handler.feature.executor.compare.CompareOperator;
import com.zj.common.entity.feature.CompareResult;
import com.zj.client.handler.feature.executor.compare.operator.CompareFactory;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.CompareInfo;
import com.zj.client.handler.pipeline.executer.vo.HttpRequestContext;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.utils.OrikaUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Http请求处理
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class HttpTrigger implements INodeTrigger {
    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");
    public static final String RECORD_ID = "recordId";
    private final OkHttpClient okHttpClient =
            new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS).build();
    private final CompareFactory compareFactory;

    public HttpTrigger(CompareFactory compareFactory) {
        this.compareFactory = compareFactory;
    }

    @Override
    public ExecuteType type() {
        return ExecuteType.HTTP;
    }

    public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) throws IOException {
        log.info("start run http executor context={}", JSON.toJSONString(triggerContext));
        HttpRequestContext context = OrikaUtil.convert(triggerContext.getData(), HttpRequestContext.class);
        Map<String, Object> param = JSON.parseObject(context.getBody(), new TypeReference<Map<String, Object>>() {
        });
        param.put(RECORD_ID, taskNode.getRecordId());
        RequestBody requestBody = createBody(context.getBodyType(), param);
        Map<String, String> headers = Optional.ofNullable(context.getHeaders()).orElseGet(HashMap::new);
        Request request =
                new Request.Builder().url(context.getUrl()).post(requestBody).headers(Headers.of(headers)).build();
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new ExecuteException(response.body().string());
        }
    }

    public RequestBody createBody(String bodyType, Map<String, Object> param) {
        if (Objects.equals(bodyType, "form-data")) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            param.forEach((key, value) -> builder.addFormDataPart(key, String.valueOf(value)));
            return builder.build();
        }

        if (Objects.equals(bodyType, "x-www-form-urlencoded")) {
            FormBody.Builder builder = new FormBody.Builder();
            param.forEach((key, value) -> builder.add(key, String.valueOf(value)));
            return builder.build();
        }

        return RequestBody.create(MEDIA_TYPE, JSON.toJSONString(param));
    }

    @Override
    public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
        try {
            Map<String, String> headers = Optional.ofNullable(refreshContext.getHeaders()).orElseGet(HashMap::new);
            Request request =
                    new Request.Builder().url(refreshContext.getUrl()).get().headers(Headers.of(headers)).build();
            Response response = okHttpClient.newCall(request).execute();
            String result = response.body().string();
            Map<String, Object> resultMap = JSON.parseObject(result, new TypeReference<Map<String, Object>>() {
            });
            CompareResult compareResult = handleCompare(resultMap, refreshContext.getLoopExpression());
            QueryResponseModel queryResponseModel = new QueryResponseModel();
            queryResponseModel.setData(resultMap);
            if (Objects.nonNull(compareResult) && compareResult.isCompareSuccess()) {
                queryResponseModel.setStatus(ProcessStatus.RUNNING.getType());
                return queryResponseModel;
            }
            queryResponseModel.setStatus(response.isSuccessful() ? ProcessStatus.SUCCESS.getType() :
                    ProcessStatus.FAIL.getType());
            return queryResponseModel;
        } catch (IOException e) {
            log.error("request http error", e);
        }
        return null;
    }

    private CompareResult handleCompare(Map<String, Object> response, CompareInfo compareInfo) {
        if (Objects.isNull(compareInfo)) {
            return null;
        }

        if (Objects.isNull(response)) {
            CompareResult compareResult = new CompareResult();
            compareResult.setCompareSuccess(false);
            compareResult.setErrorMessage("response is null");
            return compareResult;
        }
        CompareOperator compareOperator = compareFactory.getOperator(compareInfo.getOperator());
        CompareDefine compareDefine = new CompareDefine();
        compareDefine.setResponseValue(response.get(compareInfo.getCompareKey()));
        compareDefine.setExpectValue(compareInfo.getValue());
        return compareOperator.compare(compareDefine);
    }
}
