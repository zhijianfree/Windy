package com.zj.client.deploy;

/**
 * @author falcon
 * @since 2023/6/8
 */
public interface IDeployMode {

  String deployType();

  void deploy(String jarPath);
}
