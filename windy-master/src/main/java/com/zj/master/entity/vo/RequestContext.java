package com.zj.master.entity.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Data
public class RequestContext {

  /**
   * 是否请求单个节点，不是用负载均衡的能力。因为在流水线的场景下
   * git操作的文件处理需要在同一个节点执行完成，所以添加这个标志来
   * 判断是否使用单个节点(注意这个逻辑只针对git相关的流水线子节点，
   * 当前流水线的其他类型子节点不受影响).
   * */
  private boolean requestSingle;

  private String singleClientIp;
}
