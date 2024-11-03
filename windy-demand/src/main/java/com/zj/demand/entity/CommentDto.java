package com.zj.demand.entity;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CommentDto {

    /**
     * 评论Id
     */
    private String commentId;

    /**
     * 关联Id（需求或者缺陷Id）
     */
    @NotEmpty(message = "关联Id不能为空")
    private String relativeId;

    /**
     * 评论内容
     */
    @NotEmpty(message = "评论内容不能为空")
    private String comment;

    /**
     * 评论用户Id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;
}
