package com.zj.client.deploy;

import com.zj.common.enums.ProcessStatus;

/**
 * @author falcon
 * @since 2023/6/8
 */
public interface IDeployMode<T extends DeployContext> {

  String deployType();

  void deploy(T deployContext);

  ProcessStatus getDeployStatus(String recordId);
}