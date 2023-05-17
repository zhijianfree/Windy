package com.zj.pipeline.rest;

import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.pipeline.NodeBindDto;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.po.pipeline.NodeBind;
import com.zj.pipeline.service.NodeBindService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@RestController
@RequestMapping("/v1/devops/pipeline")
public class NodeBindRest {

  @Autowired
  private NodeBindService service;

  @PostMapping("/nodes")
  public ResponseMeta<Boolean> createNode(@RequestBody NodeBindDto nodeBindDto) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.createNodes(nodeBindDto));
  }

  @PutMapping("/node")
  public ResponseMeta<Boolean> updateNode(@RequestBody NodeBindDto nodeBindDto) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.updateNode(nodeBindDto));
  }

  @GetMapping("/node/{nodeId}")
  public ResponseMeta<NodeBind> getNode(@PathVariable("nodeId") String nodeId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, service.getNode(nodeId));
  }

  @DeleteMapping("/node/{nodeId}")
  public ResponseMeta<Boolean> deleteNode(@PathVariable("nodeId") String nodeId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.deleteNode(nodeId));
  }

  @GetMapping("/nodes")
  public PageSize<NodeBind> getNodes(@RequestParam("page") Integer page,
      @RequestParam("size") Integer size, @RequestParam("name") String name) {
    return service.getNodes(page, size, name);
  }

  @GetMapping("/nodes/all")
  public ResponseMeta<List<NodeBind>> getAllNodes() {
    return new ResponseMeta<List<NodeBind>>(ErrorCode.SUCCESS, service.getAllNodes());
  }

  @GetMapping("/{nodeId}/executors")
  public ResponseMeta<List<PipelineActionDto>> getNodeExecutors(@PathVariable("nodeId") String nodeId) {
    return new ResponseMeta<List<PipelineActionDto>>(ErrorCode.SUCCESS, service.getNodeExecutors(nodeId));
  }
}
