package com.zj.pipeline.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.domain.entity.bo.pipeline.NodeBindBO;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import com.zj.pipeline.service.NodeBindService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@RestController
@RequestMapping("/v1/devops/pipeline")
public class NodeBindRest {

  private final NodeBindService service;

  public NodeBindRest(NodeBindService service) {
    this.service = service;
  }

  @PostMapping("/nodes")
  public ResponseMeta<Boolean> createNode(@RequestBody NodeBindBO nodeBindBO) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.createNodes(nodeBindBO));
  }

  @PutMapping("/node")
  public ResponseMeta<Boolean> updateNode(@RequestBody NodeBindBO nodeBindBO) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.updateNode(nodeBindBO));
  }

  @GetMapping("/node/{nodeId}")
  public ResponseMeta<NodeBindBO> getNode(@PathVariable("nodeId") String nodeId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, service.getNode(nodeId));
  }

  @DeleteMapping("/node/{nodeId}")
  public ResponseMeta<Boolean> deleteNode(@PathVariable("nodeId") String nodeId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.deleteNode(nodeId));
  }

  @GetMapping("/nodes")
  public PageSize<NodeBindBO> getNodes(@RequestParam("page") Integer page,
                                       @RequestParam("size") Integer size, @RequestParam("name") String name) {
    return service.getNodes(page, size, name);
  }

  @GetMapping("/nodes/all")
  public ResponseMeta<List<NodeBindBO>> getAllNodes() {
    return new ResponseMeta<List<NodeBindBO>>(ErrorCode.SUCCESS, service.getAllNodes());
  }

  @GetMapping("/{nodeId}/executors")
  public ResponseMeta<List<PipelineActionBO>> getNodeExecutors(
      @PathVariable("nodeId") String nodeId) {
    return new ResponseMeta<List<PipelineActionBO>>(ErrorCode.SUCCESS, service.getNodeExecutors(nodeId));
  }
}
