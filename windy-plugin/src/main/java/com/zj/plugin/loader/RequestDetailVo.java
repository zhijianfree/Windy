package com.zj.plugin.loader;

import java.util.ArrayList;
import java.util.List;

public class RequestDetailVo {
    private List<String> headerTips = new ArrayList<>();
    private Object requestBody;

    public List<String> getHeaderTips() {
        return headerTips;
    }

    public void setHeaderTips(List<String> headerTips) {
        this.headerTips = headerTips;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public void addTips(String info) {
        headerTips.add(info);
    }
}
