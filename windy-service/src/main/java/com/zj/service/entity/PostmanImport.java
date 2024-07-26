package com.zj.service.entity;

import lombok.Data;

import java.util.List;

@Data
public class PostmanImport {

    private PostmanInfo info;

    private List<PostmanApiModel> item;


    @Data
    public static class PostmanInfo{
        private String name;
    }

    @Data
    public static class PostmanApiModel{
        private String name;

        private PostmanApiRequest request;

    }

    @Data
    public static class PostmanApiRequest{
        private String method;

        private List<PostmanApiHeader> header;

        private PostmanApiBody body;

        private PostmanApiUrl url;
    }

    @Data
    public static class PostmanApiUrl{

        private List<String> path;

        private List<PostmanApiQuery> query;
    }

    @Data
    public static class PostmanApiQuery{

        private String key;

        private String value;
    }

    @Data
    public static class PostmanApiBody{

        private String raw;
    }
    @Data
    public static class PostmanApiHeader{
        private String key;

        private String value;
    }
}
