package com.zj.demand.rest;


import com.zj.common.exception.ErrorCode;
import java.util.List;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.demand.service.CommentService;
import com.zj.domain.entity.bo.demand.CommentBO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author falcon
 * @since 2023/4/17
 */
@RestController
@RequestMapping("/v1/devops")
public class CommentRest {

  private final CommentService commentService;

  public CommentRest(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping("/{relativeId}/comments")
  public ResponseMeta<List<CommentBO>>  getComments(@PathVariable("relativeId") String relativeId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS,
        commentService.getRelativeComments(relativeId));
  }

  @PostMapping("/comments")
  public ResponseMeta<Boolean> createComment(@Validated @RequestBody CommentBO commentBO) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, commentService.addComment(commentBO));
  }

  @PutMapping("/comments")
  public ResponseMeta<Boolean> updateComment(@Validated @RequestBody CommentBO commentBO) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, commentService.updateComment(commentBO));
  }

  @DeleteMapping("/{commentId}/comment")
  public ResponseMeta<Boolean> deleteComment(@PathVariable("commentId") String commentId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, commentService.deleteComment(commentId));
  }
}
