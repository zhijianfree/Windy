package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.CodeChangeDto;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/18
 */
public interface ICodeChangeRepository {

  CodeChangeDto getCodeChange(String codeChangeId);

  boolean saveCodeChange(CodeChangeDto codeChange);

  boolean updateCodeChange(CodeChangeDto codeChange);

  List<CodeChangeDto> getServiceChanges(String serviceId);

  Boolean deleteCodeChange(String codeChangeId);
}
