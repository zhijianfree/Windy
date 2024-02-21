package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.plugin.loader.Feature;
import com.zj.plugin.loader.FeatureDefine;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.uuid.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.entity.dto.feature.PluginInfoDto;
import com.zj.domain.entity.enums.SourceStatus;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.feature.IPluginRepository;
import com.zj.feature.entity.dto.BatchTemplates;
import com.zj.feature.entity.dto.ExecuteTemplateVo;
import com.zj.feature.entity.dto.UploadResultDto;
import com.zj.feature.entity.type.InvokeType;
import com.zj.feature.entity.type.TemplateType;
import com.zj.feature.entity.vo.ExecutorUnit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

  public PageSize<ExecuteTemplateVo> getTemplatePage(Integer pageNo, Integer size, String name) {
    IPage<ExecuteTemplateDto> templateIPage = templateRepository.getPage(pageNo, size, name);
    PageSize<ExecuteTemplateVo> pageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(templateIPage.getRecords())) {
      pageSize.setTotal(0);
      return pageSize;
    }

    List<ExecuteTemplateVo> templateDTOS = templateIPage.getRecords().stream()
        .map(ExecuteTemplateVo::toExecuteTemplateDTO).collect(Collectors.toList());
    pageSize.setData(templateDTOS);
    pageSize.setTotal(templateIPage.getTotal());
    return pageSize;
  }

  public ExecuteTemplateVo getExecuteTemplate(String templateId) {
    ExecuteTemplateDto executeTemplate = templateRepository.getExecuteTemplate(templateId);
    return ExecuteTemplateVo.toExecuteTemplateDTO(executeTemplate);
  }

  public String createTemplate(ExecuteTemplateVo executeTemplateVo) {
    ExecuteTemplateDto executeTemplate = buildExecuteTemplateDto(
        executeTemplateVo);
    boolean result = templateRepository.save(executeTemplate);
    return result ? executeTemplate.getTemplateId() : "";
  }

  public String updateTemplate(ExecuteTemplateVo executeTemplateVo) {
    ExecuteTemplateDto executeTemplate = OrikaUtil.convert(executeTemplateVo,
        ExecuteTemplateDto.class);
    executeTemplate.setAuthor("admin");
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    executeTemplate.setParam(JSON.toJSONString(executeTemplateVo.getParams()));
    executeTemplate.setHeader(JSON.toJSONString(executeTemplateVo.getHeaders()));
    return templateRepository.updateTemplate(executeTemplate)
        ? executeTemplate.getTemplateId() : "";
  }

  public Boolean deleteExecuteTemplate(String templateId) {
    return templateRepository.deleteTemplate(templateId);
  }

  public List<ExecuteTemplateVo> getFeatureList() {
    List<ExecuteTemplateDto> executeTemplates = templateRepository.getAllTemplates();
    return executeTemplates.stream().map(ExecuteTemplateVo::toExecuteTemplateDTO)
        .collect(Collectors.toList());
  }

  public Boolean refreshTemplate(String templateId) {
    List<ExecutePointDto> executePoints = executePointRepository.getTemplateExecutePoints(
        templateId);
    if (CollectionUtils.isEmpty(executePoints)) {
      return true;
    }

    ExecuteTemplateDto executeTemplate = templateRepository.getExecuteTemplate(templateId);
    List<ExecutePointDto> updatePoints = executePoints.stream().peek(executePoint -> {
      String featureInfo = executePoint.getFeatureInfo();
      ExecutorUnit executorUnit = JSON.parseObject(featureInfo, ExecutorUnit.class);
      executorUnit.setService(executeTemplate.getService());
      executorUnit.setMethod(executeTemplate.getMethod());
      executorUnit.setInvokeType(executeTemplate.getInvokeType());
      Map<String, String> map = JSON.parseObject(executeTemplate.getHeader(),
          Map.class);
      executorUnit.setHeaders(map);
      executePoint.setFeatureInfo(JSON.toJSONString(executorUnit));
    }).collect(Collectors.toList());

    return executePointRepository.updateBatch(updatePoints);
  }

  @Transactional
  public UploadResultDto uploadTemplate(MultipartFile file) {
    try {
      List<FeatureDefine> featureDefines = parseJarFile(file);
      if (CollectionUtils.isEmpty(featureDefines)) {
        return null;
      }

      //将文件存储到数据库
      PluginInfoDto pluginInfoDto = new PluginInfoDto();
      pluginInfoDto.setPluginName(file.getOriginalFilename());
      pluginInfoDto.setStatus(SourceStatus.UNAVAILABLE.getType());
      pluginInfoDto.setPluginId(uniqueIdService.getUniqueId());
      pluginInfoDto.setFileData(file.getBytes());
      pluginRepository.addPlugin(pluginInfoDto);

      UploadResultDto uploadResult = new UploadResultDto();
      uploadResult.setPluginId(pluginInfoDto.getPluginId());
      List<ExecuteTemplateVo> templates = featureDefines.stream()
          .map(TemplateService::buildExecuteTemplateVo).collect(Collectors.toList());
      uploadResult.setTemplateDefines(templates);
      return uploadResult;
    } catch (Exception e) {
      log.error("save file error", e);
      throw new ApiException(ErrorCode.PARSE_PLUGIN_ERROR);
    }
  }

  private static ExecuteTemplateVo buildExecuteTemplateVo(FeatureDefine define) {
    ExecuteTemplateVo executeTemplateVo = new ExecuteTemplateVo();
    executeTemplateVo.setTemplateType(TemplateType.CUSTOM.getType());
    executeTemplateVo.setInvokeType(InvokeType.LOCAL_METHOD.getType());
    executeTemplateVo.setName(define.getName());
    executeTemplateVo.setMethod(define.getMethod());
    executeTemplateVo.setService(define.getSource());
    executeTemplateVo.setDescription(define.getDescription());
    executeTemplateVo.setParams(define.getParams());
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
    } catch (IOException ignore) {
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
    String pluginId = batchTemplates.getPluginId();
    pluginRepository.updatePluginStatus(pluginId);

    List<ExecuteTemplateDto> templates = batchTemplates.getTemplates().stream().map(this::buildExecuteTemplateDto)
        .collect(Collectors.toList());
    return templateRepository.batchAddTemplates(templates);
  }

  private ExecuteTemplateDto buildExecuteTemplateDto(ExecuteTemplateVo executeTemplateVo) {
    ExecuteTemplateDto executeTemplate = OrikaUtil.convert(executeTemplateVo,
        ExecuteTemplateDto.class);
    executeTemplate.setTemplateId(uniqueIdService.getUniqueId());
    executeTemplate.setAuthor("admin");
    executeTemplate.setCreateTime(System.currentTimeMillis());
    executeTemplate.setUpdateTime(System.currentTimeMillis());
    executeTemplate.setHeader(JSON.toJSONString(executeTemplateVo.getHeaders()));
    executeTemplate.setParam(JSON.toJSONString(executeTemplateVo.getParams()));
    return executeTemplate;
  }

  public Boolean deletePlugin(String pluginId) {
    return pluginRepository.deletePlugin(pluginId);
  }
}
