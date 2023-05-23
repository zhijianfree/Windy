package com.zj.client.entity.vo;

import lombok.Data;

@Data
public class ResponseDetailVo {
    /**
     * 响应状态码 200成功
     * */
    private Boolean responseStatus;

    /**
     * 响应结构体
     * */
    private Object responseBody;

    /**
     * 执行错误描述信息
     * */
    private String errorMessage;
}
