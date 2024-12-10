package com.zj.common.adapter.git;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class CommitMessage {

    /**
     * 代码提交ID
     */
    @JSONField(name = "id")
    private String commitId;

    /**
     * 代码提交短ID
     */
    @JSONField(name = "short_id")
    private String shortId;

    /**
     * 提交的描述信息
     */
    @JSONField(name = "message")
    private String message;

    /**
     * 提交的用户
     */
    @JSONField(name = "author_name")
    private String commitUser;
}
