package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.pipeline.entity.dto.CodeChangeDto;
import com.zj.pipeline.entity.dto.RelationDemandBug;
import com.zj.pipeline.entity.po.CodeChange;
import com.zj.pipeline.git.IRepositoryBranch;
import com.zj.pipeline.mapper.CodeChangeMapper;
import com.zj.service.entity.po.Microservice;
import com.zj.service.service.MicroserviceService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class CodeChangeService extends ServiceImpl<CodeChangeMapper, CodeChange> {

  @Autowired
  private IRepositoryBranch repositoryBranch;

  @Autowired
  private MicroserviceService microservice;

  @Autowired
  private UniqueIdService uniqueIdService;

  public CodeChangeDto getCodeChange(String serviceId, String codeChangeId) {
    Assert.notEmpty(serviceId, "serviceId can not be null");
    CodeChange codeChange = getOne(
        Wrappers.<CodeChange>lambdaQuery().eq(CodeChange::getChangeId, codeChangeId));
    return CodeChangeDto.toCodeChangeDto(codeChange);
  }

  public String createCodeChange(CodeChangeDto codeChangeDto) {
    Microservice service = checkServiceExist(codeChangeDto.getServiceId());
    repositoryBranch.createBranch(service.getServiceName(), codeChangeDto.getChangeBranch());

    CodeChange codeChange = CodeChangeDto.toCodeChange(codeChangeDto);
    codeChange.setChangeId(uniqueIdService.getUniqueId());
    codeChange.setUpdateTime(System.currentTimeMillis());
    codeChange.setCreateTime(System.currentTimeMillis());
    return save(codeChange) ? codeChange.getChangeId() : "";
  }

  public boolean updateCodeChange(String serviceId, String codeChangeId,
      CodeChangeDto codeChangeDto) {
    CodeChangeDto changeDto = getCodeChange(serviceId, codeChangeId);
    if (Objects.isNull(changeDto)) {
      throw new ApiException(ErrorCode.NOT_FOUND_CODE_CHANGE);
    }

    CodeChange codeChange = CodeChangeDto.toCodeChange(codeChangeDto);
    codeChange.setChangeId(codeChangeId);
    codeChange.setUpdateTime(System.currentTimeMillis());
    return update(codeChange, Wrappers.lambdaUpdate(CodeChange.class)
        .eq(CodeChange::getChangeId, codeChange.getChangeId()));
  }

  public List<CodeChangeDto> listCodeChanges(String serviceId) {
    checkServiceExist(serviceId);

    List<CodeChange> codeChanges = list(
        Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getServiceId, serviceId));
    if (CollectionUtils.isEmpty(codeChanges)) {
      return Collections.emptyList();
    }

    return codeChanges.stream().map(CodeChangeDto::toCodeChangeDto).collect(Collectors.toList());
  }

  public Boolean deleteCodeChange(String serviceId, String codeChangeId) {
    Microservice service = checkServiceExist(serviceId);
    CodeChangeDto codeChange = getCodeChange(serviceId, codeChangeId);
    repositoryBranch.deleteBranch(service.getServiceName(), codeChange.getChangeBranch());
    return remove(Wrappers.lambdaQuery(CodeChange.class).eq(CodeChange::getChangeId, codeChangeId));
  }

  public List<RelationDemandBug> queryRelationIds(String queryName) {
    RelationDemandBug relationDemandBug = new RelationDemandBug();
    String random = uniqueIdService.getUniqueId();
    relationDemandBug.setRelationId(random);
    relationDemandBug.setRelationType(1);
    relationDemandBug.setName(queryName + random.substring(0, 6));
    return Collections.singletonList(relationDemandBug);
  }

  private Microservice checkServiceExist(String serviceId) {
    Microservice serviceDetail = microservice.getServiceDetail(serviceId);
    if (Objects.isNull(serviceDetail)) {
      log.warn("can not find serviceId ={}", serviceId);
      throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
    }

    return serviceDetail;
  }
}
