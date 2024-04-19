package com.zj.service.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.enums.Position;
import com.zj.common.enums.TemplateType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.feature.ExecuteTemplateVo;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.entity.dto.service.GenerateRecordDto;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.entity.dto.service.ServiceGenerateDto;
import com.zj.domain.entity.vo.MavenConfigVo;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IGenerateRecordRepository;
import com.zj.domain.repository.service.IGenerateRepository;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.plugin.loader.InitData;
import com.zj.plugin.loader.ParamValueType;
import com.zj.plugin.loader.ParameterDefine;
import com.zj.service.entity.ApiModel;
import com.zj.service.entity.ApiRequestVariable;
import com.zj.service.entity.GenerateTemplate;
import com.zj.service.entity.ImportApiResult;
import com.zj.service.service.imports.ApiImportFactory;
import com.zj.service.service.imports.IApiImportStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author falcon
 * @since 2023/8/8
 */
@Slf4j
@Service
public class ApiService {

    private final UniqueIdService uniqueIdService;
    private final IServiceApiRepository apiRepository;
    private final RequestProxy requestProxy;
    private final ISystemConfigRepository systemConfigRepository;
    private final IGenerateRepository generateRepository;
    private final IGenerateRecordRepository generateRecordRepository;
    private final ApiImportFactory apiImportFactory;
    private final IExecuteTemplateRepository executeTemplateRepository;

    public ApiService(UniqueIdService uniqueIdService, IServiceApiRepository apiRepository, RequestProxy requestProxy
            , ISystemConfigRepository systemConfigRepository, IGenerateRepository generateRepository,
                      IGenerateRecordRepository generateRecordRepository, ApiImportFactory apiImportFactory,
                      IExecuteTemplateRepository executeTemplateRepository) {
        this.uniqueIdService = uniqueIdService;
        this.apiRepository = apiRepository;
        this.requestProxy = requestProxy;
        this.systemConfigRepository = systemConfigRepository;
        this.generateRepository = generateRepository;
        this.generateRecordRepository = generateRecordRepository;
        this.apiImportFactory = apiImportFactory;
        this.executeTemplateRepository = executeTemplateRepository;
    }

    public ServiceApiDto getServiceApi(String apiId) {
        return apiRepository.getServiceApi(apiId);
    }

    public List<ServiceApiDto> getServiceApis(String serviceId) {
        return apiRepository.getApiByService(serviceId);
    }

    public boolean createServiceApi(ApiModel apiModel) {
        ServiceApiDto serviceApi = OrikaUtil.convert(apiModel, ServiceApiDto.class);
        String requestParams = Optional.ofNullable(apiModel.getRequestParams()).map(JSON::toJSONString).orElse(null);
        serviceApi.setRequestParams(requestParams);
        String responseParams = Optional.ofNullable(apiModel.getResponseParams()).map(JSON::toJSONString).orElse(null);
        serviceApi.setResponseParams(responseParams);
        serviceApi.setApiId(uniqueIdService.getUniqueId());
        return apiRepository.saveApi(serviceApi);
    }

    public boolean updateServiceApi(ApiModel apiModel) {
        ServiceApiDto serviceApi = OrikaUtil.convert(apiModel, ServiceApiDto.class);
        serviceApi.setRequestParams(JSON.toJSONString(apiModel.getRequestParams()));
        serviceApi.setResponseParams(JSON.toJSONString(apiModel.getResponseParams()));
        return apiRepository.updateApi(serviceApi);
    }

    public boolean deleteServiceApi(String apiId) {
        return apiRepository.deleteApi(apiId);
    }

    public boolean batchDeleteApi(List<String> apiIds) {
        return apiRepository.batchDeleteApi(apiIds);
    }

    public Boolean generateServiceApi(ServiceGenerateDto generate) {
        checkMavenConfig();
        checkVersionExist(generate.getServiceId(), generate.getVersion());
        saveOrUpdateParams(generate);

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setSourceId(generate.getServiceId());
        dispatchTaskModel.setType(LogType.GENERATE.getType());
        return requestProxy.runGenerate(dispatchTaskModel);
    }

    private void checkVersionExist(String serviceId, String version) {
        GenerateRecordDto generateRecord = generateRecordRepository.getGenerateRecord(serviceId, version);
        if (Objects.nonNull(generateRecord)) {
            log.info("generate version exist, can not execute serviceId={} version={}", serviceId, version);
            throw new ApiException(ErrorCode.GENERATE_VERSION_EXIST);
        }
    }

    private void checkMavenConfig() {
        MavenConfigVo mavenConfig = systemConfigRepository.getMavenConfig();
        if (Objects.isNull(mavenConfig) || !mavenConfig.checkConfig()) {
            throw new ApiException(ErrorCode.MAVEN_NOT_CONFIG);
        }
    }

    private void saveOrUpdateParams(ServiceGenerateDto generate) {
        ServiceGenerateDto serviceGenerateDto = generateRepository.getByService(generate.getServiceId());
        if (Objects.isNull(serviceGenerateDto)) {
            generate.setGenerateId(uniqueIdService.getUniqueId());
            generateRepository.create(generate);
        } else {
            generateRepository.update(generate);
        }
    }

    public ServiceGenerateDto getGenerateParams(String serviceId) {
        return generateRepository.getByService(serviceId);
    }

    public List<GenerateRecordDto> getLatestGenerateLog(String serviceId) {
        List<GenerateRecordDto> serviceRecords = generateRecordRepository.getServiceRecords(serviceId);
        if (CollectionUtils.isEmpty(serviceRecords)) {
            return Collections.emptyList();
        }
        return serviceRecords.stream().sorted(Comparator.comparing(GenerateRecordDto::getUpdateTime).reversed()).collect(Collectors.toList());
    }

    public ImportApiResult importApiFile(MultipartFile file, String importType, String serviceId) {
        try {
            String fileContent = new String(file.getBytes());
            IApiImportStrategy importStrategy = apiImportFactory.getImportStrategy(importType);
            if (Objects.isNull(importStrategy)) {
                log.info("can not support import type={}", importType);
                return null;
            }
            List<ServiceApiDto> serviceApiList = importStrategy.importContent(serviceId, fileContent);
            return new ImportApiResult(serviceApiList);
        } catch (IOException exception) {
            log.error("import api error importType={}", importType, exception);
        }
        return null;
    }

    public List<ExecuteTemplateVo> apiGenerateTemplate(GenerateTemplate generateTemplate) {
        List<ServiceApiDto> serviceApis = apiRepository.getServiceApiList(generateTemplate.getApiIds());
        if (CollectionUtils.isEmpty(serviceApis)) {
            return Collections.emptyList();
        }

        List<String> templateIds = serviceApis.stream().map(ServiceApiDto::getApiId).collect(Collectors.toList());
        List<String> existTemplateIds =
                executeTemplateRepository.getTemplateByIds(templateIds).stream().map(ExecuteTemplateDto::getTemplateId).collect(Collectors.toList());

        return serviceApis.stream().filter(serviceApi -> generateTemplate.getCover() || !existTemplateIds.contains(serviceApi.getApiId())).map(serviceApi -> convertApi2Template(serviceApi, generateTemplate.getInvokeType(), generateTemplate.getRelatedId())).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private ExecuteTemplateVo convertApi2Template(ServiceApiDto serviceApi, Integer invokeType, String relatedId) {
        if (!serviceApi.isApi()) {
            return null;
        }

        ExecuteTemplateDto templateDto = new ExecuteTemplateDto();
        templateDto.setTemplateId(serviceApi.getApiId());
        templateDto.setName(serviceApi.getApiName());
        templateDto.setMethod(serviceApi.getMethod());
        templateDto.setInvokeType(invokeType);
        templateDto.setTemplateType(TemplateType.NORMAL.getType());
        templateDto.setDescription(serviceApi.getDescription());
        templateDto.setOwner(serviceApi.getServiceId());
        templateDto.setRelatedId(relatedId);

        List<ApiRequestVariable> apiVariables = JSON.parseArray(serviceApi.getRequestParams(),
                ApiRequestVariable.class);
        Map<String, String> header = new HashMap<>();
        List<ParameterDefine> parameterDefines = apiVariables.stream().map(variable -> {
            if (Objects.equals(variable.getPosition(), Position.Header.name())) {
                header.put(variable.getParamKey(), variable.getDefaultValue());
                return null;
            }
            ParameterDefine parameterDefine = getParameterDefine(variable);
            InitData initData = new InitData(variable.getDefaultValue());
            setParamRangeData(variable, initData);
            parameterDefine.setInitData(initData);
            return parameterDefine;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        templateDto.setParam(JSON.toJSONString(parameterDefines));
        templateDto.setHeader(JSON.toJSONString(header));

        String assembledUrl = assembledApiUrl(serviceApi.getResource(), apiVariables);
        templateDto.setService(assembledUrl);
        return toExecuteTemplateDTO(templateDto);
    }

    private void setParamRangeData(ApiRequestVariable apiVariable, InitData initData) {
        if (CollectionUtils.isEmpty(apiVariable.getChildren())) {
            return;
        }

        if (Objects.equals(apiVariable.getType(), ParamValueType.Object.name())) {
            List<ParameterDefine> parameters =
                    apiVariable.getChildren().stream().map(this::getParameterDefine).collect(Collectors.toList());
            initData.setRange(parameters);
            return;
        }


        // 对象数组参数默认创建了一个空child，然后将对象的属性添加到空child的子对象中
        setArrayInitDta(apiVariable, initData);
    }

    private void setArrayInitDta(ApiRequestVariable apiVariable, InitData initData) {
        ApiRequestVariable emptyChild = apiVariable.getChildren().get(0);
        initData.setRangeType(emptyChild.getType());
        if (Objects.equals(emptyChild.getType(), ParamValueType.Object.name())) {
            List<ParameterDefine> rangeList = emptyChild.getChildren().stream().map(childVariable -> {
                InitData init = new InitData(childVariable.getDefaultValue());
                if (Objects.equals(childVariable.getType(), "Array") && CollectionUtils.isNotEmpty(childVariable.getChildren())) {
                    setArrayInitDta(childVariable, init);
                }
                ParameterDefine param = getParameterDefine(childVariable);
                param.setInitData(init);
                return param;
            }).collect(Collectors.toList());
            initData.setRange(rangeList);
        }
    }

    private ParameterDefine getParameterDefine(ApiRequestVariable variable) {
        ParameterDefine parameterDefine = new ParameterDefine();
        parameterDefine.setParamKey(variable.getParamKey());
        parameterDefine.setType(variable.getType());
        parameterDefine.setDescription(variable.getDescription());
        parameterDefine.setPosition(variable.getPosition());
        return parameterDefine;
    }

    /**
     * 将uri路径参数添加进去
     *
     * @param uri          原始rest请求的api
     * @param apiVariables 请求的参数
     */
    private String assembledApiUrl(String uri, List<ApiRequestVariable> apiVariables) {
        StringBuilder uriBuilder = new StringBuilder(uri);
        for (ApiRequestVariable variable : apiVariables) {
            if (Objects.equals(variable.getPosition(), Position.Query.name())) {
                String paramKey = variable.getParamKey();
                uriBuilder.append(uriBuilder.indexOf("?") >= 0 ? "&" : "?").append(String.format("%s=${%s}", paramKey
                        , paramKey));
            }
            if (Objects.equals(variable.getPosition(), Position.Path.name())) {
                uriBuilder = new StringBuilder(uriBuilder.toString().replace("{" + variable.getParamKey() + "}",
                        "${" + variable.getParamKey() + "}"));
            }
        }
        uri = uriBuilder.toString();
        return "${host}" + uri;
    }

    public ExecuteTemplateVo toExecuteTemplateDTO(ExecuteTemplateDto executeTemplate) {
        ExecuteTemplateVo templateVo = OrikaUtil.convert(executeTemplate, ExecuteTemplateVo.class);
        templateVo.setParams(JSON.parseArray(executeTemplate.getParam(), ParameterDefine.class));
        templateVo.setHeaders((Map<String, String>) JSON.parse(executeTemplate.getHeader()));
        return templateVo;
    }
}
