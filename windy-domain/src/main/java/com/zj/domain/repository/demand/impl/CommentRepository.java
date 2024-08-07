package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.demand.CommentDTO;
import com.zj.domain.entity.po.demand.Comment;
import com.zj.domain.mapper.demand.CommentMapper;
import com.zj.domain.repository.demand.ICommentRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CommentRepository extends ServiceImpl<CommentMapper, Comment> implements ICommentRepository {
    @Override
    public List<CommentDTO> getRelativeComments(String relativeId) {
        List<Comment> comments = list(Wrappers.lambdaQuery(Comment.class).eq(Comment::getRelativeId, relativeId)
                        .orderByDesc(Comment::getCreateTime));
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }
        return OrikaUtil.convertList(comments, CommentDTO.class);
    }

    @Override
    public boolean saveComment(CommentDTO commentDTO) {
        Comment comment = OrikaUtil.convert(commentDTO, Comment.class);
        comment.setCreateTime(System.currentTimeMillis());
        comment.setUpdateTime(System.currentTimeMillis());
        return save(comment);
    }

    @Override
    public boolean updateComment(CommentDTO commentDTO) {
        Comment comment = OrikaUtil.convert(commentDTO, Comment.class);
        comment.setUpdateTime(System.currentTimeMillis());
        return update(comment,
                Wrappers.lambdaUpdate(Comment.class).eq(Comment::getCommentId, comment.getCommentId()));
    }

    @Override
    public boolean deleteComment(String commentId) {
        return remove(Wrappers.lambdaQuery(Comment.class).eq(Comment::getCommentId, commentId));
    }
}
