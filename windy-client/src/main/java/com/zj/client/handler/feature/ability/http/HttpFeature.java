package com.zj.client.handler.feature.ability.http;

import com.alibaba.fastjson.JSON;

import com.zj.plugin.loader.ExecuteDetailVo;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.plugin.loader.ParamValueType;
import com.zj.plugin.loader.ParameterDefine;
import com.zj.client.utils.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpStatus;

@Slf4j
public class HttpFeature implements Feature {

    private static final MediaType mediaType = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient okHttpClient = new OkHttpClient();

    public ExecuteDetailVo startHttp(String url, String method, Map<String, String> headers,
                                     String body) {
        boolean emptyHeader = headers.keySet().stream().anyMatch(this::isEmpty);
        if (emptyHeader) {
            headers = new HashMap<>();
        }
        Request request = requestFactory(url, method, headers, body);
        return startRequest(request, body);
    }

    private boolean isEmpty(String headerKey) {
        return Objects.isNull(headerKey) || Objects.equals(headerKey, "");
    }

    private ExecuteDetailVo startRequest(Request request, String body) {
        ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (Objects.nonNull(response.body())){
                String string = response.body().string();
                try {
                    executeDetailVo.setResBody(JSON.parse(string));
                } catch (Exception e) {
                    executeDetailVo.setResBody(string);
                    log.info("parse http response error", e);
                }
            }
            executeDetailVo.setStatus(response.code() == HttpStatus.OK.value());
        } catch (Exception e) {
            log.error("run http feature error", e);
            executeDetailVo.setErrorMessage(ExceptionUtils.getSimplifyError(e));
        }

        recordExecuteDetail(request, body, executeDetailVo);
        return executeDetailVo;
    }

    private void recordExecuteDetail(Request request, String body, ExecuteDetailVo executeDetailVo) {
        executeDetailVo.addRequestInfo("HTTP Method", request.method());
        executeDetailVo.addRequestInfo("url", request.url().url().toString());

        Map<String, String> header = new HashMap<>();
        Headers headers = request.headers();
        for (String name : headers.names()) {
            header.put(name, headers.get(name));
        }
        executeDetailVo.addRequestInfo("Header", header);
        executeDetailVo.addRequestInfo("body", body);
    }

    public static Request requestFactory(String url, String method, Map<String, String> headers,
                                         String body) {
        method = method.toUpperCase();
        Request request = null;
        switch (method) {
            case "GET":
                request = new Request.Builder().url(url).headers(Headers.of(headers)).build();
                break;
            case "POST":
                request = new Request.Builder().url(url).post(RequestBody.create(mediaType, body))
                        .headers(Headers.of(headers)).build();
                break;
            case "PUT":
                request = new Request.Builder().url(url).put(RequestBody.create(mediaType, body))
                        .headers(Headers.of(headers)).build();
                break;
            case "DELETE":
                Request.Builder builder = new Request.Builder();
                builder.delete();
                Optional.ofNullable(body)
                        .ifPresent(requestBody -> builder.delete(RequestBody.create(mediaType, requestBody)));
                request = builder.url(url).headers(Headers.of(headers)).build();
                break;
            default:
                break;
        }
        return request;
    }

    @Override
    public List<FeatureDefine> scanFeatureDefines() {
        FeatureDefine featureDefine = new FeatureDefine();
        featureDefine.setName("HttpRequest");
        featureDefine.setSource("com.zj.client.handler.feature.ability.http.HttpFeature");
        featureDefine.setMethod("startHttp");
        featureDefine.setDescription("简单http请求");

        List<ParameterDefine> params = new ArrayList<>();
        ParameterDefine url = new ParameterDefine();
        url.setParamKey("url");
        url.setType(ParamValueType.String.name());
        url.setDescription("http请求的url");
        params.add(url);

        ParameterDefine method = new ParameterDefine();
        method.setParamKey("method");
        method.setType(ParamValueType.String.name());
        method.setDescription("http请求的方法");
        params.add(method);

        ParameterDefine headers = new ParameterDefine();
        headers.setParamKey("headers");
        headers.setType(ParamValueType.Map.name());
        headers.setDescription("http请求的Headers");
        params.add(headers);

        ParameterDefine body = new ParameterDefine();
        body.setParamKey("body");
        body.setType(ParamValueType.String.name());
        body.setDescription("http请求的请求体");
        params.add(body);
        featureDefine.setParams(params);
        return Collections.singletonList(featureDefine);
    }
}
