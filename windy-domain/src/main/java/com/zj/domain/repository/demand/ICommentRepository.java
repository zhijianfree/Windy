package com.zj.domain.repository.demand;

import com.zj.domain.entity.dto.demand.CommentDTO;

import java.util.List;

public interface ICommentRepository {
    List<CommentDTO> getRelativeComments(String relativeId);

    boolean saveComment(CommentDTO commentDTO);

    boolean updateComment(CommentDTO commentDTO);

    boolean deleteComment(String commentId);
}
