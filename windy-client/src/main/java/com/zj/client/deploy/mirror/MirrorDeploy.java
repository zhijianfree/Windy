package com.zj.client.deploy.mirror;

import com.zj.client.deploy.IDeployMode;
import com.zj.client.entity.enuns.DeployType;
import com.zj.common.enums.ProcessStatus;
import org.springframework.stereotype.Component;

/**
 * k8s镜像部署
 * @author falcon
 * @since 2023/6/8
 */
@Component
public class MirrorDeploy implements IDeployMode<MirrorDeployContext> {

  @Override
  public String deployType() {
    return DeployType.MIRROR.name();
  }

  @Override
  public void deploy(MirrorDeployContext deployContext) {

  }

  @Override
  public ProcessStatus getDeployStatus(String recordId) {
    return null;
  }
}
