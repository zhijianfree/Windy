package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.CodeChangeDto;
import com.zj.domain.entity.po.pipeline.CodeChange;
import com.zj.domain.mapper.pipeline.CodeChangeMapper;
import com.zj.domain.repository.pipeline.ICodeChangeRepository;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Slf4j
@Repository
public class CodeChangeRepository extends ServiceImpl<CodeChangeMapper, CodeChange> implements
    ICodeChangeRepository {

  @Override
  public CodeChangeDto getCodeChange(String codeChangeId) {
    CodeChange codeChange = getOne(
        Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getChangeId, codeChangeId));
    return OrikaUtil.convert(codeChange, CodeChangeDto.class);
  }

  @Override
  public boolean saveCodeChange(CodeChangeDto codeChange) {
    CodeChange change = OrikaUtil.convert(codeChange, CodeChange.class);
    long dateNow = System.currentTimeMillis();
    change.setCreateTime(dateNow);
    change.setUpdateTime(dateNow);
    return save(change);
  }

  @Override
  public boolean updateCodeChange(CodeChangeDto codeChangeDto) {
    CodeChange codeChange = OrikaUtil.convert(codeChangeDto, CodeChange.class);
    codeChange.setUpdateTime(System.currentTimeMillis());
    return update(codeChange, Wrappers.lambdaUpdate(CodeChange.class)
        .eq(CodeChange::getChangeId, codeChange.getChangeId()));
  }

  @Override
  public List<CodeChangeDto> getServiceChanges(String serviceId) {
    List<CodeChange> codeChanges = list(
        Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getServiceId, serviceId));
    return OrikaUtil.convertList(codeChanges, CodeChangeDto.class);
  }

  @Override
  public boolean deleteCodeChange(String codeChangeId) {
    return remove(Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getChangeId, codeChangeId));
  }

  @Override
  public boolean batchDeleteCodeChange(List<String> codeChangeIds) {
    if (CollectionUtils.isEmpty(codeChangeIds)) {
      log.info("change id list is empty, delete false");
      return false;
    }
    return remove(Wrappers.lambdaQuery(CodeChange.class).in(CodeChange::getChangeId, codeChangeIds));
  }
}
