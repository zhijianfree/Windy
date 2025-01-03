package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.CommentBO;

import java.util.List;

public interface ICommentRepository {
    /**
     * 获取关联资源的评论列表
     * @param relativeId 关联ID
     * @return 评论列表
     */
    List<CommentBO> getRelativeComments(String relativeId);

    /**
     * 保存评论
     * @param commentBO 评论信息
     * @return 是否成功
     */
    boolean saveComment(CommentBO commentBO);

    /**
     * 更新评论
     * @param commentBO 评论信息
     * @return 是否成功
     */
    boolean updateComment(CommentBO commentBO);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean deleteComment(String commentId);
}
