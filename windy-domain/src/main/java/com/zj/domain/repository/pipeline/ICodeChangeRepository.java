package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface ICodeChangeRepository {

  CodeChangeBO getCodeChange(String codeChangeId);

  boolean saveCodeChange(CodeChangeBO codeChange);

  boolean updateCodeChange(CodeChangeBO codeChange);

  List<CodeChangeBO> getServiceChanges(String serviceId);

  boolean deleteCodeChange(String codeChangeId);

  boolean batchDeleteCodeChange(List<String> codeChangeIds);
}
