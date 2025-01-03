package com.zj.plugin.loader;

public class ResponseDetailVo {
    /**
     * 响应状态
     * */
    private Boolean responseStatus;

    /**
     * 处理状态
     */
    private Integer processStatus;

    /**
     * 响应结构体
     * */
    private Object responseBody;

    /**
     * 执行错误描述信息
     * */
    private String errorMessage;

    public Integer getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }

    public Boolean getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Boolean responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Object getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
