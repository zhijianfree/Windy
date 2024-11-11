package com.zj.pipeline.git;

import com.zj.common.adapter.git.IGitRepositoryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Component
public class RepositoryFactory {

  private final Map<String, IGitRepositoryHandler> repositoryHandlerMap;

  public RepositoryFactory(List<IGitRepositoryHandler> repositories) {
    repositoryHandlerMap = repositories.stream().collect(Collectors.toMap(IGitRepositoryHandler::gitType,
            handler -> handler));
  }

  public IGitRepositoryHandler getRepository(String gitType) {
    return repositoryHandlerMap.get(gitType);
  }
}
