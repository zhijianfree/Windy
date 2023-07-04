package com.zj.pipeline.git;

import com.alibaba.fastjson.JSON;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.pipeline.entity.enums.GitType;
import com.zj.pipeline.entity.vo.GitAccessVo;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Component
public class RepositoryFactory {

  private IRepositoryBranch repositoryBranch;

  public RepositoryFactory(List<IRepositoryBranch> repositories, ISystemConfigRepository systemConfig) {
    SystemConfigDto config = systemConfig.getSystemConfig("2");
    GitAccessVo gitAccessVo = JSON.parseObject(config.getConfigDetail(), GitAccessVo.class);
    String gitType = Optional.ofNullable(gitAccessVo).map(GitAccessVo::getGitType)
        .orElse(GitType.Gitlab.name());
    repositoryBranch = repositories.stream().filter(repository -> Objects.equals(repository.gitType(), gitType))
        .findAny().orElse(null);
  }

  public IRepositoryBranch getRepository() {
    return repositoryBranch;
  }
}
