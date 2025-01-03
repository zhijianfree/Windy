package com.zj.client.handler.pipeline.deploy;

import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/8
 */
@Component
public class DeployFactory {

  private Map<Integer, IDeployMode> deployModeMap;

  public DeployFactory(List<IDeployMode> deployModes) {
    deployModeMap = deployModes.stream()
        .collect(Collectors.toMap(IDeployMode::deployType, deployMode -> deployMode));
  }

  public IDeployMode getDeployMode(Integer mode) {
    return deployModeMap.get(mode);
  }

  public QueryResponseModel getDeployStatus(String recordId) {
    for (IDeployMode deployMode : deployModeMap.values()) {
      QueryResponseModel deployStatus = deployMode.getDeployStatus(recordId);
      if (Objects.nonNull(deployStatus)) {
        return deployStatus;
      }
    }
    return new QueryResponseModel();
  }
}
