package com.zj.plugin.loader;

import java.util.ArrayList;
import java.util.List;

public class RequestDetailVo {
    private List<String> request = new ArrayList<>();
    private Object requestBody;

    public List<String> getRequest() {
        return request;
    }

    public void setRequest(List<String> request) {
        this.request = request;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public void addRequest(String info) {
        request.add(info);
    }
}
