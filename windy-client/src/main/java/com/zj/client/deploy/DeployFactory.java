package com.zj.client.deploy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/6/8
 */
@Component
public class DeployFactory {

  private Map<String, IDeployMode> deployModeMap;

  public DeployFactory(List<IDeployMode> deployModes) {
    deployModeMap = deployModes.stream().collect(
        Collectors.toMap(IDeployMode::deployType, deployMode -> deployMode));
  }

  public IDeployMode getDeployMode(String mode) {
    return deployModeMap.get(mode);
  }
}
