package com.zj.domain.repository.demand;

import com.zj.domain.entity.bo.demand.CommentBO;

import java.util.List;

public interface ICommentRepository {
    List<CommentBO> getRelativeComments(String relativeId);

    boolean saveComment(CommentBO commentBO);

    boolean updateComment(CommentBO commentBO);

    boolean deleteComment(String commentId);
}
