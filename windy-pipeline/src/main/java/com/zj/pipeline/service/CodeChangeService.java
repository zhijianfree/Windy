package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.CodeChangeDto;
import com.zj.domain.entity.dto.pipeline.RelationDemandBug;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.repository.pipeline.ICodeChangeRepository;
import com.zj.pipeline.git.IRepositoryBranch;
import com.zj.pipeline.git.RepositoryFactory;
import com.zj.service.service.MicroserviceService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class CodeChangeService {

  private RepositoryFactory repositoryFactory;
  private MicroserviceService microservice;
  private UniqueIdService uniqueIdService;
  private ICodeChangeRepository codeChangeRepository;

  public CodeChangeService(RepositoryFactory repositoryFactory, MicroserviceService microservice,
      UniqueIdService uniqueIdService, ICodeChangeRepository codeChangeRepository) {
    this.repositoryFactory = repositoryFactory;
    this.microservice = microservice;
    this.uniqueIdService = uniqueIdService;
    this.codeChangeRepository = codeChangeRepository;
  }

  public CodeChangeDto getCodeChange(String serviceId, String codeChangeId) {
    Assert.notEmpty(serviceId, "serviceId can not be null");
    return codeChangeRepository.getCodeChange(codeChangeId);
  }

  public String createCodeChange(CodeChangeDto codeChange) {
    MicroserviceDto service = checkServiceExist(codeChange.getServiceId());
    IRepositoryBranch repository = repositoryFactory.getRepository();
    repository.createBranch(service.getServiceName(), codeChange.getChangeBranch());

    codeChange.setChangeId(uniqueIdService.getUniqueId());
    codeChange.setUpdateTime(System.currentTimeMillis());
    codeChange.setCreateTime(System.currentTimeMillis());
    return codeChangeRepository.saveCodeChange(codeChange) ? codeChange.getChangeId() : "";
  }

  public boolean updateCodeChange(String serviceId, String codeChangeId, CodeChangeDto codeChange) {
    CodeChangeDto changeDto = getCodeChange(serviceId, codeChangeId);
    if (Objects.isNull(changeDto)) {
      throw new ApiException(ErrorCode.NOT_FOUND_CODE_CHANGE);
    }

    codeChange.setChangeId(codeChangeId);
    codeChange.setUpdateTime(System.currentTimeMillis());
    return codeChangeRepository.updateCodeChange(codeChange);
  }

  public List<CodeChangeDto> listCodeChanges(String serviceId) {
    checkServiceExist(serviceId);
    return codeChangeRepository.getServiceChanges(serviceId);
  }

  public Boolean deleteCodeChange(String serviceId, String codeChangeId) {
    MicroserviceDto service = checkServiceExist(serviceId);
    CodeChangeDto codeChange = getCodeChange(serviceId, codeChangeId);
    IRepositoryBranch repository = repositoryFactory.getRepository();
    repository.deleteBranch(service.getServiceName(), codeChange.getChangeBranch());
    return codeChangeRepository.deleteCodeChange(codeChangeId);
  }

  public List<RelationDemandBug> queryRelationIds(String queryName) {
    RelationDemandBug relationDemandBug = new RelationDemandBug();
    String random = uniqueIdService.getUniqueId();
    relationDemandBug.setRelationId(random);
    relationDemandBug.setRelationType(1);
    relationDemandBug.setName(queryName + random.substring(0, 6));
    return Collections.singletonList(relationDemandBug);
  }

  private MicroserviceDto checkServiceExist(String serviceId) {
    MicroserviceDto serviceDetail = microservice.queryServiceDetail(serviceId);
    if (Objects.isNull(serviceDetail)) {
      log.warn("can not find serviceId ={}", serviceId);
      throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
    }

    return serviceDetail;
  }
}
