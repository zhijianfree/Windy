package com.zj.master.entity.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Data
public class RequestContext {

  private String version = "1.0";

  /**
   * 是否请求单个节点，不是用负载均衡的能力。因为在流水线的场景下
   * git操作的文件处理需要在同一个节点执行完成，所以添加这个标志来
   * 判断是否使用单个节点(注意这个逻辑只针对git相关的流水线子节点，
   * 当前流水线的其他类型子节点不受影响).
   * */
  private boolean requestSingle;

  /**
   * 和requestSingle一起使用，singleClientIp用来指定具体是哪一个client节点
   * */
  private String singleClientIp;

  private String pipelineId;

  /**
   * 当前流水线所属服务的git地址
   * */
  private String gitUrl;

  /**
   * 全局git类型
   * */
  private String gitType;
  /**
   * git访问的凭据
   * gitlab场景： oauth2
   * gitea场景： 任意
   * */
  private String tokenName;

  /**
   * 访问git的token
   * */
  private String token;
}
