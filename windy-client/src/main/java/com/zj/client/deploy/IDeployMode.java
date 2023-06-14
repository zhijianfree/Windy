package com.zj.client.deploy;

/**
 * @author falcon
 * @since 2023/6/8
 */
public interface IDeployMode<T extends DeployContext> {

  String deployType();

  void deploy(T deployContext);
}
