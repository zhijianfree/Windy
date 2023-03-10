package com.zj.feature.ability.http;

import com.alibaba.fastjson.JSON;
import com.zj.feature.ability.Feature;
import com.zj.feature.ability.FeatureDefine;
import com.zj.feature.entity.vo.ExecuteDetail;
import com.zj.feature.utils.ExceptionUtils;
import java.io.IOException;
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
    private MediaType mediaType = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    public List<FeatureDefine> scanFeatureDefines() {
        return null;
    }

    public ExecuteDetail startHttp(String url, String method, Map<String, String> headers, String body) {
        if (Objects.isNull(headers)){
            headers = new HashMap<>();
        }

        Request request = requestFactory(url, method, headers, body);
        return startRequest(request, body);
    }

    private ExecuteDetail startRequest(Request request, String body) {
        ExecuteDetail executeDetail = new ExecuteDetail();
        try (Response response = okHttpClient.newCall(request).execute()) {
            Optional.ofNullable(response.body()).ifPresent(responseBody -> {
                try {
                    String string = responseBody.string();
                    executeDetail.setResBody(JSON.parse(string));
                } catch (IOException e) {
                    executeDetail.setErrorMessage(ExceptionUtils.getSimplifyError(e));
                }
            });
            executeDetail.setStatus(response.code() == HttpStatus.OK.value());
        } catch (IOException e) {
            log.error("run http feature error", e);
            executeDetail.setErrorMessage(ExceptionUtils.getSimplifyError(e));
        }

        recordExecuteDetail(request, body, executeDetail);
        return executeDetail;
    }

    private void recordExecuteDetail(Request request, String body, ExecuteDetail executeDetail) {
        executeDetail.addRequestInfo(request.method() + " " + request.url().url());
        executeDetail.addRequestInfo("Headers: ");
        Headers headers = request.headers();
        for (String name : headers.names()) {
            executeDetail.addRequestInfo(name + " - " + headers.get(name));
        }
        executeDetail.setRequestBody(body);
    }

    private Request requestFactory(String url, String method, Map<String, String> headers, String body) {
        method = method.toUpperCase();
        Request request = null;
        switch (method) {
            case "GET":
                request = new Request.Builder()
                        .url(url)
                        .headers(Headers.of(headers))
                        .build();
                break;
            case "POST":
                request = new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(mediaType, body))
                        .headers(Headers.of(headers))
                        .build();
                break;
            case "PUT":
                request = new Request.Builder()
                        .url(url)
                        .put(RequestBody.create(mediaType, body))
                        .headers(Headers.of(headers))
                        .build();
                break;
            case "DELETE":
                Request.Builder builder = new Request.Builder();
                builder.delete();
                Optional.ofNullable(body).ifPresent(requestBody -> builder.delete(RequestBody.create(mediaType, requestBody)));
                request = builder
                        .url(url)
                        .headers(Headers.of(headers))
                        .build();
                break;
        }
        return request;
    }
}
