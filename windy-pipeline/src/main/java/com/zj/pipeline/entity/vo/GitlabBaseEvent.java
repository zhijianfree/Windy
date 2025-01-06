package com.zj.pipeline.entity.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class GitlabBaseEvent {

    /**
     * 事件类型
     */
    @JSONField(name = "object_kind")
    private String eventType;

    /**
     * 服务git信息
     */
    private GitProject project;

    @Data
    public static class GitProject{
        /**
         * 项目名称
         */
        private String name;

        /**
         * git的http地址
         */
        @JSONField(name = "git_http_url")
        private String gitUrl;
    }
}
