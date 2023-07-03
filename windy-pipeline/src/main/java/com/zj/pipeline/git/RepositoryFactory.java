package com.zj.pipeline.git;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Component
public class RepositoryFactory {

  private Map<String, IRepositoryBranch> repositoryMap;

  public RepositoryFactory(List<IRepositoryBranch> branches) {
    repositoryMap = branches.stream()
        .collect(Collectors.toMap(IRepositoryBranch::gitType, repository -> repository));
  }

  public IRepositoryBranch getRepository(String gitType) {
    return repositoryMap.get(gitType);
  }
}
