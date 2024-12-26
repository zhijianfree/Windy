package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.CodeChangeBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface ICodeChangeRepository {

  /**
   * 获取代码变更
   * @param codeChangeId 代码变更ID
   * @return 代码变更信息
   */
  CodeChangeBO getCodeChange(String codeChangeId);

  /**
   * 保存代码变更
   * @param codeChange 代码变更信息
   * @return 是否成功
   */
  boolean saveCodeChange(CodeChangeBO codeChange);

  /**
   * 更新代码变更
   * @param codeChange 代码变更信息
   * @return 是否成功
   */
  boolean updateCodeChange(CodeChangeBO codeChange);

  /**
   * 获取服务代码变更
   * @param serviceId 服务ID
   * @return 代码变更列表
   */
  List<CodeChangeBO> getServiceChanges(String serviceId);

  /**
   * 删除代码变更
   * @param codeChangeId 代码变更ID
   * @return 是否成功
   */
  boolean deleteCodeChange(String codeChangeId);

  /**
   * 批量删除代码变更
   * @param codeChangeIds 代码变更ID列表
   * @return 是否成功
   */
  boolean batchDeleteCodeChange(List<String> codeChangeIds);

  /**
   * 获取关联代码变更
   * @param relationId 关联ID
   * @param relationType 关联类型
   * @return 代码变更列表
   */
  List<CodeChangeBO> getCodeChangeByRelationId(String relationId, Integer relationType);
}
