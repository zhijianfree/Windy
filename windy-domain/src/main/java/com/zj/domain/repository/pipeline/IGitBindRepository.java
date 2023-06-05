package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.GitBindDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IGitBindRepository {

  boolean saveGitBind(GitBindDto gitBindDto);

  List<GitBindDto> getPipelineGitBinds(String pipelineId);

  GitBindDto getGitBind(String bindId);

  Boolean updateGitBind(GitBindDto gitBindDto);

  Boolean deleteGitBind(String bindId);
}
