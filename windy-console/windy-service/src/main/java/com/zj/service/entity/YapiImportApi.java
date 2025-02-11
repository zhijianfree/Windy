package com.zj.service.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class YapiImportApi {

    private String name;

    private List<YapiApiModel> list;


    @Data
    public static class YapiApiModel{
        private String title;
        private String path;
        private String method;
        @JSONField(name = "req_headers")
        private List<HeaderParam> headers;
        @JSONField(name = "req_body_other")
        private String requestBody;
        @JSONField(name = "res_body")
        private String resBody;
        @JSONField(name = "req_params")
        private List<PathParam> pathParams;

        @JSONField(name = "req_query")
        private List<QueryParam> queryParams;
    }

    @Data
    public static class QueryParam {
        private String name;
        private String desc;
    }

    @Data
    public static class PathParam {
        private String name;
        private String desc;
    }

    @Data
    public static class HeaderParam{
        private String name;
        private String value;
    }
}
