package com.zj.client.handler.deploy;

import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;

/**
 * @author guyuelan
 * @since 2023/6/8
 */
public interface IDeployMode<T extends DeployContext> {

  Integer deployType();

  void deploy(T deployContext);

  QueryResponseModel getDeployStatus(String recordId);
}
