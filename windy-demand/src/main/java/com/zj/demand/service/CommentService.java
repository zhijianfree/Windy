package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.auth.UserDetail;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.demand.CommentBO;
import com.zj.domain.repository.demand.ICommentRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author falcon
 * @since 2023/4/17
 */
@Service
public class CommentService {

  private final ICommentRepository commentRepository;
  private final UniqueIdService uniqueIdService;
  private final IAuthService authService;

  public CommentService(ICommentRepository commentRepository, UniqueIdService uniqueIdService, IAuthService authService) {
    this.commentRepository = commentRepository;
    this.uniqueIdService = uniqueIdService;
    this.authService = authService;
  }

  public List<CommentBO> getRelativeComments(String relativeId) {
    return commentRepository.getRelativeComments(relativeId);
  }

  public boolean addComment(CommentBO commentBO) {
    commentBO.setCommentId(uniqueIdService.getUniqueId());
    commentBO.setUserId(authService.getCurrentUserId());
    UserDetail userDetail = authService.getUserDetail();
    String name = Optional.ofNullable(userDetail.getNickName()).filter(StringUtils::isNoneBlank)
            .orElseGet(userDetail::getUserName);
    commentBO.setUserName(name);
    return commentRepository.saveComment(commentBO);
  }

  public Boolean updateComment(CommentBO commentBO) {
    return commentRepository.updateComment(commentBO);
  }

  public Boolean deleteComment(String commentId) {
    return commentRepository.deleteComment(commentId);
  }
}
