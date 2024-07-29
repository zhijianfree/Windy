package com.zj.demand.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.demand.CommentDTO;
import com.zj.domain.repository.demand.ICommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author falcon
 * @since 2023/4/17
 */
@Service
public class CommentService {

  private final ICommentRepository commentRepository;
  private final UniqueIdService uniqueIdService;

  public CommentService(ICommentRepository commentRepository, UniqueIdService uniqueIdService) {
    this.commentRepository = commentRepository;
    this.uniqueIdService = uniqueIdService;
  }

  public List<CommentDTO> getRelativeComments(String relativeId) {
    return commentRepository.getRelativeComments(relativeId);
  }

  public boolean addComment(CommentDTO commentDTO) {
    commentDTO.setCommentId(uniqueIdService.getUniqueId());
    return commentRepository.saveComment(commentDTO);
  }

  public Boolean updateComment(CommentDTO commentDTO) {
    return commentRepository.updateComment(commentDTO);
  }

  public Boolean deleteComment(String commentId) {
    return commentRepository.deleteComment(commentId);
  }
}
