package com.zj.demand.rest;


import com.zj.common.exception.ErrorCode;
import java.util.List;

import com.zj.common.model.ResponseMeta;
import com.zj.demand.service.CommentService;
import com.zj.domain.entity.dto.demand.CommentDTO;
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
  public ResponseMeta<List<CommentDTO>>  getComments(@PathVariable("relativeId") String relativeId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS,
        commentService.getRelativeComments(relativeId));
  }

  @PostMapping("/comments")
  public ResponseMeta<Boolean> createComment(@Validated @RequestBody CommentDTO commentDTO) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, commentService.addComment(commentDTO));
  }

  @PutMapping("/comments")
  public ResponseMeta<Boolean> updateComment(@Validated @RequestBody CommentDTO commentDTO) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, commentService.updateComment(commentDTO));
  }

  @DeleteMapping("/{commentId}/comment")
  public ResponseMeta<Boolean> deleteComment(@PathVariable("commentId") String commentId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, commentService.deleteComment(commentId));
  }
}
