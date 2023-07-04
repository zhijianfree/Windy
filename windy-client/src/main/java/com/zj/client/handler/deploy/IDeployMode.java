package com.zj.client.handler.deploy;

import com.zj.common.enums.ProcessStatus;

/**
 * @author guyuelan
 * @since 2023/6/8
 */
public interface IDeployMode<T extends DeployContext> {

  String deployType();

  void deploy(T deployContext);

  ProcessStatus getDeployStatus(String recordId);
}
