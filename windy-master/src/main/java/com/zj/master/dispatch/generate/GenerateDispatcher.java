package com.zj.master.dispatch.generate;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.DispatchType;
import com.zj.common.enums.LogType;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.entity.dto.service.ServiceGenerateDto;
import com.zj.domain.entity.vo.MavenConfigVo;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IGenerateRepository;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class GenerateDispatcher implements IDispatchExecutor {

  private final IGenerateRepository generateRepository;
  private final IServiceApiRepository serviceApiRepository;
  private final ISystemConfigRepository systemConfigRepository;
  private final RequestProxy requestProxy;

  public GenerateDispatcher(IGenerateRepository generateRepository,
      IServiceApiRepository serviceApiRepository, ISystemConfigRepository systemConfigRepository,
      RequestProxy requestProxy) {
    this.generateRepository = generateRepository;
    this.serviceApiRepository = serviceApiRepository;
    this.systemConfigRepository = systemConfigRepository;
    this.requestProxy = requestProxy;
  }

  @Override
  public LogType type() {
    return LogType.GENERATE;
  }

  @Override
  public boolean isExistInJvm(DispatchLogDto taskLog) {
    return false;
  }

  @Override
  public Boolean dispatch(DispatchTaskModel task, String logId) {
    ServiceGenerateDto serviceGenerate = generateRepository.getByService(task.getSourceId());
    if (Objects.isNull(serviceGenerate)) {
      return false;
    }

    GenerateParam generateParam = OrikaUtil.convert(serviceGenerate, GenerateParam.class);
    generateParam.setDispatchType(DispatchType.GENERATE.name());
    MavenConfigVo mavenConfig = systemConfigRepository.getMavenConfig();
    generateParam.setMavenUser(mavenConfig.getUserName());
    generateParam.setMavenPwd(mavenConfig.getUserName());
    generateParam.setMavenRepository(mavenConfig.getMavenUrl());
    List<ServiceApiDto> apiList = serviceApiRepository.getApiByService(task.getSourceId());
    if (CollectionUtils.isEmpty(apiList)) {
      return false;
    }
    List<ApiModel> models = apiList.stream().map(api -> {
      ApiModel apiModel = OrikaUtil.convert(api, ApiModel.class);
      apiModel.setRequestParams(JSON.parseArray(api.getRequestParams(), ApiParamModel.class));
      apiModel.setResponseParams(JSON.parseArray(api.getRequestParams(), ApiParamModel.class));
      return apiModel;
    }).collect(Collectors.toList());
    generateParam.setApiList(models);
    return requestProxy.sendDispatchTask(generateParam, false, null);
  }

  @Override
  public boolean resume(DispatchLogDto taskLog) {
    return false;
  }

  @Override
  public Integer getExecuteCount() {
    return null;
  }
}
