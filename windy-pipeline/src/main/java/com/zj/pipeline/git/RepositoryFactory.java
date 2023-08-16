package com.zj.pipeline.git;

import com.zj.common.enums.GitType;
import com.zj.common.git.IRepositoryBranch;
import com.zj.domain.entity.vo.GitAccessVo;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Component
public class RepositoryFactory {

  private final IRepositoryBranch repositoryBranch;

  public RepositoryFactory(List<IRepositoryBranch> repositories,
      ISystemConfigRepository systemConfig) {
    GitAccessVo gitAccessVo = systemConfig.getGitAccess();
    String gitType = Optional.ofNullable(gitAccessVo).map(GitAccessVo::getGitType)
        .filter(StringUtils::isNotBlank).orElse(GitType.Gitlab.name());
    repositoryBranch = repositories.stream()
        .filter(repository -> Objects.equals(repository.gitType(), gitType)).findAny().orElse(null);
  }

  public IRepositoryBranch getRepository() {
    return repositoryBranch;
  }
}
