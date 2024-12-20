package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.enums.InvokerType;
import com.zj.common.enums.TemplateType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.feature.ExecuteTemplateVo;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import com.zj.domain.entity.bo.feature.PluginInfoBO;
import com.zj.domain.entity.enums.SourceStatus;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.feature.IPluginRepository;
import com.zj.feature.entity.BatchTemplates;
import com.zj.feature.entity.UploadResultDto;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.plugin.loader.ParameterDefine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TemplateService {

    public static final String PLUGINS_PATH = "plugins";
    private final UniqueIdService uniqueIdService;
    private final IExecuteTemplateRepository templateRepository;
    private final IExecutePointRepository executePointRepository;
    private final IPluginRepository pluginRepository;

    public TemplateService(UniqueIdService uniqueIdService,
                           IExecuteTemplateRepository templateRepository,
                           IExecutePointRepository executePointRepository, IPluginRepository pluginRepository) {
        this.uniqueIdService = uniqueIdService;
        this.templateRepository = templateRepository;
        this.executePointRepository = executePointRepository;
        this.pluginRepository = pluginRepository;
    }

    public PageSize<ExecuteTemplateVo> getTemplatePage(String serviceId, Integer pageNo, Integer size, String name) {
        IPage<ExecuteTemplateBO> templateIPage = templateRepository.getPage(serviceId, pageNo, size, name);
        PageSize<ExecuteTemplateVo> pageSize = new PageSize<>();
        if (CollectionUtils.isEmpty(templateIPage.getRecords())) {
            pageSize.setTotal(0);
            return pageSize;
        }

        List<ExecuteTemplateVo> templateDTOS = templateIPage.getRecords().stream()
                .map(this::toExecuteTemplateDTO).collect(Collectors.toList());
        pageSize.setData(templateDTOS);
        pageSize.setTotal(templateIPage.getTotal());
        return pageSize;
    }

    public ExecuteTemplateVo getExecuteTemplate(String templateId) {
        ExecuteTemplateBO executeTemplate = templateRepository.getExecuteTemplate(templateId);
        return toExecuteTemplateDTO(executeTemplate);
    }

    public String createTemplate(ExecuteTemplateVo executeTemplateVo) {
        ExecuteTemplateBO executeTemplate = buildExecuteTemplateDto(
                executeTemplateVo);
        boolean result = templateRepository.save(executeTemplate);
        return result ? executeTemplate.getTemplateId() : "";
    }

    public String updateTemplate(ExecuteTemplateVo executeTemplateVo) {
        ExecuteTemplateBO executeTemplate = OrikaUtil.convert(executeTemplateVo,
                ExecuteTemplateBO.class);
        executeTemplate.setUpdateTime(System.currentTimeMillis());
        executeTemplate.setParameterDefines(executeTemplateVo.getParams());
        executeTemplate.setHeader(JSON.toJSONString(executeTemplateVo.getHeaders()));
        return templateRepository.updateTemplate(executeTemplate)
                ? executeTemplate.getTemplateId() : "";
    }

    public Boolean deleteExecuteTemplate(String templateId) {
        return templateRepository.deleteTemplate(templateId);
    }

    public List<ExecuteTemplateVo> getFeatureList(String serviceId) {
        List<ExecuteTemplateBO> defaultTemplates = templateRepository.getToolTemplates();
        List<ExecuteTemplateBO> executeTemplates = templateRepository.getServiceTemplates(serviceId);
        executeTemplates.addAll(defaultTemplates);
        return executeTemplates.stream().map(this::toExecuteTemplateDTO)
                .collect(Collectors.toList());
    }

    public Boolean refreshTemplate(String templateId) {
        List<ExecutePointBO> executePoints = executePointRepository.getTemplateExecutePoints(
                templateId);
        if (CollectionUtils.isEmpty(executePoints)) {
            return true;
        }

        ExecuteTemplateBO executeTemplate = templateRepository.getExecuteTemplate(templateId);
        List<ExecutePointBO> updatePoints = executePoints.stream().peek(executePoint -> {
            ExecutorUnit executorUnit = exchangeTemplate(executePoint, executeTemplate);
            executePoint.setExecutorUnit(executorUnit);
        }).collect(Collectors.toList());

        return executePointRepository.updateBatch(updatePoints);
    }

    private ExecutorUnit exchangeTemplate(ExecutePointBO executePoint, ExecuteTemplateBO executeTemplate) {
        ExecutorUnit executorUnit = executePoint.getExecutorUnit();
        executorUnit.setService(executeTemplate.getService());
        executorUnit.setMethod(executeTemplate.getMethod());
        executorUnit.setInvokeType(executeTemplate.getInvokeType());
        Map<String, String> map = JSON.parseObject(executeTemplate.getHeader(), Map.class);
        executorUnit.setHeaders(map);
        Map<String, ParameterDefine> pointParameterMap = executorUnit.getParams().stream().collect(
                Collectors.toMap(ParameterDefine::getParamKey, param -> param));

        //存量执行点的执行菜单使用最新模版的配置，只需替换value即可
        List<ParameterDefine> parameterDefines = executeTemplate.getParameterDefines();
        parameterDefines.forEach(param -> {
            ParameterDefine parameterDefine = pointParameterMap.get(param.getParamKey());
            if (Objects.nonNull(parameterDefine)) {
                param.setValue(parameterDefine.getValue());
            }
        });
        executorUnit.setParams(parameterDefines);
        executorUnit.setRelatedId(executeTemplate.getRelatedId());
        return executorUnit;
    }

    @Transactional
    public UploadResultDto uploadTemplate(MultipartFile file, String serviceId) {
        try {
            List<FeatureDefine> featureDefines = parseJarFile(file);
            if (CollectionUtils.isEmpty(featureDefines)) {
                return null;
            }

            //将文件存储到数据库
            PluginInfoBO pluginInfoBO = new PluginInfoBO();
            pluginInfoBO.setPluginName(file.getOriginalFilename());
            pluginInfoBO.setStatus(SourceStatus.UNAVAILABLE.getType());
            pluginInfoBO.setPluginId(uniqueIdService.getUniqueId());
            pluginInfoBO.setFileData(file.getBytes());
            pluginRepository.addPlugin(pluginInfoBO);

            UploadResultDto uploadResult = new UploadResultDto();
            uploadResult.setPluginId(pluginInfoBO.getPluginId());
            List<ExecuteTemplateVo> templates = featureDefines.stream()
                    .map(featureDefine -> buildExecuteTemplateVo(featureDefine, serviceId)).collect(Collectors.toList());
            uploadResult.setTemplateDefines(templates);
            return uploadResult;
        } catch (Exception e) {
            log.error("save file error", e);
            throw new ApiException(ErrorCode.PARSE_PLUGIN_ERROR);
        }
    }

    private ExecuteTemplateVo buildExecuteTemplateVo(FeatureDefine define, String serviceId) {
        ExecuteTemplateVo executeTemplateVo = new ExecuteTemplateVo();
        executeTemplateVo.setTemplateType(TemplateType.PLUGIN.getType());
        executeTemplateVo.setInvokeType(InvokerType.METHOD.getType());
        executeTemplateVo.setName(define.getName());
        executeTemplateVo.setMethod(define.getMethod());
        executeTemplateVo.setService(define.getSource());
        executeTemplateVo.setDescription(define.getDescription());
        executeTemplateVo.setParams(define.getParams());
        executeTemplateVo.setOwner(serviceId);
        return executeTemplateVo;
    }

    private List<FeatureDefine> parseJarFile(MultipartFile file) throws IOException {
        String currentPath =
                new File("").getCanonicalPath() + File.separator + PLUGINS_PATH + File.separator;
        String filePath = currentPath + file.getOriginalFilename();
        createIfNotExist(filePath);
        FileUtils.writeByteArrayToFile(new File(filePath), file.getBytes());
        List<Feature> features = loadPlugins(filePath);
        FileUtils.delete(new File(filePath));
        return features.stream().map(Feature::scanFeatureDefines).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private void createIfNotExist(String filePath) {
        File fileDir = new File(filePath);
        try {
            if (!fileDir.exists()) {
                FileUtils.createParentDirectories(fileDir);
            }
        } catch (IOException e) {
            log.info("create jar dir error", e);
        }
    }

    public List<Feature> loadPlugins(String path) {
        List<Feature> features = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("file:" + path);
            URL[] urls = new URL[resources.length];
            for (int i = 0; i < resources.length; i++) {
                urls[i] = resources[i].getURL();
            }

            URLClassLoader urlClassLoader = new URLClassLoader(urls,
                    Thread.currentThread().getContextClassLoader());
            ServiceLoader<Feature> serviceLoader = ServiceLoader.load(Feature.class, urlClassLoader);
            for (Feature feature : serviceLoader) {
                features.add(feature);
            }
            return features;
        } catch (Exception e) {
            log.error("load class error", e);
        }
        return features;
    }

    @Transactional
    public Boolean batchCreateTemplates(BatchTemplates batchTemplates) {
        if (StringUtils.isNotBlank(batchTemplates.getPluginId())) {
            String pluginId = batchTemplates.getPluginId();
            pluginRepository.updatePluginStatus(pluginId);
        }

        //获取已存在的模版
        List<String> templateIds =
                batchTemplates.getTemplates().stream().map(ExecuteTemplateVo::getTemplateId).collect(Collectors.toList());
        List<String> existTemplates =
                templateRepository.getTemplateByIds(templateIds).stream().map(ExecuteTemplateBO::getTemplateId).collect(Collectors.toList());

        //更新已存在的模版
        List<ExecuteTemplateBO> templates = batchTemplates.getTemplates().stream().map(this::buildExecuteTemplateDto)
                .collect(Collectors.toList());
        templates.stream().filter(template -> existTemplates.contains(template.getTemplateId()))
                .forEach(templateRepository::updateTemplate);

        //添加新的模版
        List<ExecuteTemplateBO> createList =
                templates.stream().filter(template -> !existTemplates.contains(template.getTemplateId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(createList)) {
            return true;
        }
        return templateRepository.batchAddTemplates(createList);
    }

    private ExecuteTemplateBO buildExecuteTemplateDto(ExecuteTemplateVo executeTemplateVo) {
        ExecuteTemplateBO executeTemplate = OrikaUtil.convert(executeTemplateVo,
                ExecuteTemplateBO.class);
        String templateId =
                Optional.ofNullable(executeTemplateVo.getTemplateId()).orElseGet(uniqueIdService::getUniqueId);
        executeTemplate.setTemplateId(templateId);
        executeTemplate.setCreateTime(System.currentTimeMillis());
        executeTemplate.setUpdateTime(System.currentTimeMillis());
        executeTemplate.setHeader(JSON.toJSONString(executeTemplateVo.getHeaders()));
        executeTemplate.setParameterDefines(executeTemplateVo.getParams());
        return executeTemplate;
    }

    public Boolean deletePlugin(String pluginId) {
        return pluginRepository.deletePlugin(pluginId);
    }

    public List<ExecuteTemplateVo> getTemplatesByInvokeType(Integer invokeType) {
        List<ExecuteTemplateBO> templateList =
                templateRepository.getTemplatesByType(Collections.singletonList(invokeType));
        return templateList.stream().map(this::toExecuteTemplateDTO).collect(Collectors.toList());
    }

    public ExecuteTemplateVo toExecuteTemplateDTO(ExecuteTemplateBO executeTemplate) {
        ExecuteTemplateVo templateVo = OrikaUtil.convert(executeTemplate, ExecuteTemplateVo.class);
        templateVo.setParams(executeTemplate.getParameterDefines());
        templateVo.setHeaders((Map<String, String>) JSON.parse(executeTemplate.getHeader()));
        return templateVo;
    }

    public List<ExecuteTemplateVo> getAllTemplates() {
        List<ExecuteTemplateBO> executeTemplates = templateRepository.getAllTemplates();
        return executeTemplates.stream().map(this::toExecuteTemplateDTO)
                .collect(Collectors.toList());
    }
}
