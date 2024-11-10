package com.zj.client.handler.generate;

import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.GenerateDto;
import com.zj.client.entity.bo.ApiItem;
import com.zj.client.entity.bo.ApiItem.MethodParam;
import com.zj.client.entity.bo.ApiModel;
import com.zj.client.entity.bo.ApiParamModel;
import com.zj.client.entity.bo.EntityItem.PropertyItem;
import com.zj.client.entity.bo.FreemarkerContext;
import com.zj.client.handler.notify.IResultEventNotify;
import com.zj.client.utils.FreemarkerUtils;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.entity.generate.GenerateDetail;
import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
import com.zj.plugin.loader.ParamValueType;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MavenGenerator {

  public static final String ROOT_MAIN_PATH =
      File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;
  public static final String POM_XML_FILE = "pom.xml";
  public static final String GENERATE = "generate";
  public static final String SETTING_XML = "setting.xml";
  public static final int SUCCESS_CODE = 0;
  private Template entityFtl;
  private Template serviceFtl;
  private Template restFtl;
  private final String SERVICE_FILE_PATH =
      File.separator + "service" + File.separator + "I%sService.java";
  private final String ENTITY_FILE_PATH = File.separator + "model" + File.separator + "%s.java";
  private final String REST_FILE_PATH = File.separator + "rest" + File.separator + "%sRest.java";
  private final Map<String, List<String>> recordMap = new ConcurrentHashMap<>();
  private final GlobalEnvConfig globalEnvConfig;
  private final UniqueIdService uniqueIdService;
  private final IResultEventNotify resultEventNotify;
  private final Executor generatePool;

  public MavenGenerator(GlobalEnvConfig globalEnvConfig, UniqueIdService uniqueIdService,
      IResultEventNotify resultEventNotify, @Qualifier("generatePool") Executor generatePool) {
    this.globalEnvConfig = globalEnvConfig;
    this.uniqueIdService = uniqueIdService;
    this.resultEventNotify = resultEventNotify;
    this.generatePool = generatePool;
    try {
      entityFtl = FreemarkerUtils.getInstance().getTemplate("Entity.ftl");
      serviceFtl = FreemarkerUtils.getInstance().getTemplate("Service.ftl");
      restFtl = FreemarkerUtils.getInstance().getTemplate("Controller.ftl");
    } catch (Exception e) {
      log.error("load ftl template file error", e);
    }
  }

  public void startGenerate(GenerateDto generateDto) {
    String recordId = uniqueIdService.getUniqueId();
    saveGenerateRecord(generateDto, recordId);
    CompletableFuture.runAsync(() -> runGenerate(generateDto, recordId), generatePool);
  }

  private void saveGenerateRecord(GenerateDto generateDto, String recordId) {
    GenerateRecordBO generateRecordBO = new GenerateRecordBO();
    generateRecordBO.setServiceId(generateDto.getServiceId());
    generateRecordBO.setRecordId(recordId);
    generateRecordBO.setCreateTime(System.currentTimeMillis());
    generateRecordBO.setUpdateTime(System.currentTimeMillis());
    generateRecordBO.setGenerateResult(Collections.singletonList("start generate maven version"));
    generateRecordBO.setStatus(ProcessStatus.RUNNING.getType());
    GenerateDetail params = OrikaUtil.convert(generateDto, GenerateDetail.class);
    generateRecordBO.setGenerateParams(params);

    ResultEvent resultEvent = new ResultEvent().executeId(recordId)
        .notifyType(NotifyType.CREATE_GENERATE_MAVEN).status(ProcessStatus.RUNNING)
        .params(generateRecordBO);
    resultEventNotify.notifyEvent(resultEvent);
  }

  public void runGenerate(GenerateDto generateDto, String recordId) {
    try {
      //1 创建项目目录
      log.info("step1 create project dir");
      String projectPath =
          globalEnvConfig.getWorkspace() + File.separator + GENERATE + File.separator
              + generateDto.getService();
      boolean result = createProjectDir(projectPath);
      if (!result) {
        updateMessage(recordId, ProcessStatus.FAIL, "create project dir error");
        return;
      }

      //2 创建pom文件
      log.info("step2 create project pom.xml");
      String pomPath = projectPath + File.separator + POM_XML_FILE;
      result = createPomFile(pomPath, generateDto);
      if (!result) {
        updateMessage(recordId, ProcessStatus.FAIL, "create project pom file error");
        return;
      }

      //3 创建项目代码
      log.info("step3 create project code");
      result = createProjectCode(generateDto, projectPath);
      if (!result) {
        updateMessage(recordId, ProcessStatus.FAIL, "create project java files error");
        return;
      }

      //4 创建setting文件
      log.info("step4 create project setting.xml");
      String settingPath = projectPath + File.separator + SETTING_XML;
      result = createSettingFile(settingPath, generateDto.getMavenUser(),
          generateDto.getMavenPwd());
      if (!result) {
        updateMessage(recordId, ProcessStatus.FAIL, "create project java files error");
        return;
      }

      //5 部署到远程仓库
      log.info("step5 start deploy remote repository");
      Integer deployResult = deployRepository(projectPath, pomPath, globalEnvConfig.getMavenPath(),
          generateDto.getMavenRepository(), line -> updateMessage(recordId, line));
      ProcessStatus status = Optional.of(deployResult).filter(res -> Objects.equals(res, SUCCESS_CODE))
          .map(res -> ProcessStatus.SUCCESS).orElse(ProcessStatus.FAIL);
      updateMessage(recordId, status,
              "generate maven code result " + status.name());
      log.info("deploy maven jar result = {}", deployResult);
    } catch (Exception e) {
      log.error("generate error", e);
      updateMessage(recordId, ProcessStatus.FAIL, "generate maven code error");
    }finally {
      recordMap.remove(recordId);
    }
  }

  public void updateMessage(String recordId, String message) {
    updateMessage(recordId, ProcessStatus.RUNNING, message);
  }

  public void updateMessage(String recordId, ProcessStatus status, String message) {
    List<String> messages = Optional.ofNullable(recordMap.get(recordId)).orElseGet(ArrayList::new);
    messages.add(message);
    GenerateRecordBO generateParam = new GenerateRecordBO();
    generateParam.setRecordId(recordId);
    generateParam.setGenerateResult(messages);
    ResultEvent resultEvent = new ResultEvent().executeId(recordId)
        .notifyType(NotifyType.UPDATE_GENERATE_MAVEN).status(status).params(generateParam);
    resultEventNotify.notifyEvent(resultEvent);
    recordMap.put(recordId, messages);
  }

  private Integer deployRepository(String projectPath, String pomPath, String mavenPath,
      String repository, InvocationOutputHandler outputHandler) throws MavenInvocationException {
    InvocationRequest ideaRequest = new DefaultInvocationRequest();
    ideaRequest.setBaseDirectory(new File(pomPath));
    ideaRequest.setAlsoMakeDependents(true);
    ideaRequest.setGoals(
        Arrays.asList("-U", "clean", "deploy", "-T 1C", "-Dmaven.compile.fork=true",
            "-Dmaven.test.skip=true", "-Dmaven.install.skip=true",
            "-s " + projectPath + File.separator + SETTING_XML,
            "-DaltDeploymentRepository=maven_remote::default::" + repository));

    Invoker ideaInvoker = new DefaultInvoker();
    ideaInvoker.setMavenHome(new File(mavenPath));
    ideaInvoker.setOutputHandler(outputHandler);
    InvocationResult ideaResult = ideaInvoker.execute(ideaRequest);
    return ideaResult.getExitCode();
  }

  private boolean createSettingFile(String settingPath, String userName, String password) {
    try {
      Document doc = buildSettingsFileDocument(userName, password);
      writeDocumentToFile(doc, settingPath);
      return true;
    } catch (Exception e) {
      log.error("create settings file error", e);
    }
    return false;
  }

  private static Document buildSettingsFileDocument(String userName, String password)
      throws ParserConfigurationException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    Element settings = doc.createElement("settings");
    doc.appendChild(settings);
    Element servers = doc.createElement("servers");
    settings.appendChild(servers);
    Element server = doc.createElement("server");
    servers.appendChild(server);
    Element id = doc.createElement("id");
    id.appendChild(doc.createTextNode("maven_remote"));
    server.appendChild(id);
    Element user = doc.createElement("username");
    user.appendChild(doc.createTextNode(userName));
    server.appendChild(user);
    Element pwd = doc.createElement("password");
    pwd.appendChild(doc.createTextNode(password));
    server.appendChild(pwd);
    return doc;
  }

  private void writeDocumentToFile(Document doc, String filePath) throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(filePath));
    transformer.transform(source, result);
  }

  private boolean createProjectCode(GenerateDto generateDto, String projectPath) {
    //按照类名分组对应同一个类存在不同的方法
    Map<String, List<ApiModel>> apiMap = generateDto.getApiList().stream()
        .collect(Collectors.groupingBy(ApiModel::getClassName));
    AtomicBoolean createResult = new AtomicBoolean(true);
    apiMap.keySet().forEach(className -> {
      try {
        List<ApiModel> apiModels = apiMap.get(className);
        List<ApiItem> apiItems = convertApiItems(apiModels);
        FreemarkerContext freemarkerContext = new FreemarkerContext();
        freemarkerContext.setPackageName(generateDto.getPackageName());
        freemarkerContext.setClassName(className);
        freemarkerContext.setParamList(apiItems);

        //生成rest 类文件地址
        String restClassPath = createClassFilePath(projectPath, generateDto.getPackageName(),
            REST_FILE_PATH, className);
        writeData2File(freemarkerContext, restClassPath, restFtl);

        //生成rest 类文件地址
        String serviceClassPath = createClassFilePath(projectPath, generateDto.getPackageName(),
            SERVICE_FILE_PATH, className);
        writeData2File(freemarkerContext, serviceClassPath, serviceFtl);

        apiModels.forEach(apiModel -> {
          //创建请求的Model对象
          List<ApiParamModel> requestParams = apiModel.getRequestParamList();
          createEntity(projectPath, requestParams, apiModel.getBodyClass(),
              generateDto.getPackageName());

          //创建响应的Model对象
          List<ApiParamModel> resParams = apiModel.getResponseParamList();
          createEntity(projectPath, resParams, apiModel.getResultClass(),
              generateDto.getPackageName());
        });
      } catch (Exception e) {
        createResult.set(false);
        log.error("create files error", e);
      }

    });
    return createResult.get();
  }

  public void createEntity(String projectPath, List<ApiParamModel> apiParamModels, String className,
      String packageName) {
    if (StringUtils.isEmpty(className)) {
      return;
    }
    try {
      List<PropertyItem> properties = apiParamModels.stream().map(apiParamModel -> {
        PropertyItem propertyItem = new PropertyItem();
        propertyItem.setName(apiParamModel.getParamKey());
        propertyItem.setType(apiParamModel.getObjectName());
        propertyItem.setNameUpper(capitalize(apiParamModel.getParamKey(), true));
        if (Objects.equals(apiParamModel.getType(), ParamValueType.Object.name())) {
          List<ApiParamModel> children = apiParamModel.getChildren();
          createEntity(projectPath, children, apiParamModel.getObjectName(), packageName);
        }
        return propertyItem;
      }).collect(Collectors.toList());

      FreemarkerContext freemarkerContext = new FreemarkerContext();
      freemarkerContext.setPackageName(packageName);
      freemarkerContext.setClassName(className);
      freemarkerContext.setProperties(properties);
      String restClassPath = createClassFilePath(projectPath, packageName, ENTITY_FILE_PATH,
          className);
      writeData2File(freemarkerContext, restClassPath, entityFtl);
    } catch (Exception e) {
      log.error("generate model error", e);
    }
  }

  private List<ApiItem> convertApiItems(List<ApiModel> apiModels) {
    return apiModels.stream().map(apiModel -> {
      ApiItem apiItem = new ApiItem();
      apiItem.setUri(apiModel.getResource());
      apiItem.setHttpMethod(apiModel.getMethod());
      apiItem.setResultClass(apiModel.getResultClass());
      apiItem.setMethodName(apiModel.getClassMethod());
      apiItem.setBodyClass(apiModel.getBodyClass());
      apiItem.setLowerBodyClass(capitalize(apiModel.getBodyClass(), false));

      List<ApiParamModel> requestParams = apiModel.getRequestParamList();
      List<MethodParam> methodParamList = requestParams.stream().map(req -> {
        MethodParam methodParam = new MethodParam();
        methodParam.setName(req.getParamKey());
        methodParam.setRequired(String.valueOf(req.isRequired()));
        methodParam.setPosition(req.getPosition());
        methodParam.setType(req.getObjectName());
        return methodParam;
      }).collect(Collectors.toList());
      apiItem.setParams(methodParamList);
      return apiItem;
    }).collect(Collectors.toList());
  }

  public void writeData2File(Object data, String filePath, Template tpl)
      throws TemplateException, IOException {
    File file = new File(filePath);
    if (file.exists()) {
      String fileName = file.getCanonicalPath();
      log.info("file already exist pls check file={}", fileName);
      return;
    }

    // 填充数据
    StringWriter writer = new StringWriter();
    tpl.process(data, writer);
    writer.flush();
    // 写入文件
    int position = filePath.lastIndexOf(File.separator);
    File f = new File(filePath.substring(0, position));
    if (!f.exists()) {
      boolean mkdirs = f.mkdirs();
      log.debug("generate file mkdir result ={}", mkdirs);
    }
    FileOutputStream fos = new FileOutputStream(filePath);
    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
    BufferedWriter bw = new BufferedWriter(osw, 1024);
    tpl.process(data, bw);
    fos.close();
  }

  private static String createClassFilePath(String projectPath, String packageName,
      String fileTypePath, String className) {
    String packagePath = packageName.replace(".", "/");
    return projectPath + ROOT_MAIN_PATH + packagePath + String.format(fileTypePath, className);
  }

  public String capitalize(String input, boolean isUpper) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    char str = Optional.of(isUpper).filter(upper -> upper)
        .map(upper -> Character.toUpperCase(input.charAt(0)))
        .orElse(Character.toLowerCase(input.charAt(0)));
    return str + input.substring(1);
  }

  private boolean createPomFile(String pomPath, GenerateDto generateDto) throws IOException {
    Model model = getDefaultPom(generateDto);
    if (!initPomFile(pomPath)) {
      return false;
    }
    MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
    Writer writer = new FileWriter(pomPath);
    mavenXpp3Writer.write(writer, model);
    return true;
  }

  public boolean initPomFile(String path) throws IOException {
    if (StringUtils.isEmpty(path)) {
      return false;
    }
    File file = new File(path);
    if (!file.exists()) {
      return file.createNewFile();
    }
    return true;
  }

  private Model getDefaultPom(GenerateDto generateDto) {
    Model model = new Model();
    model.setModelVersion("4.0.0");
    model.setModelEncoding("utf-8");
    model.setGroupId(generateDto.getGroupId());
    model.setArtifactId(generateDto.getArtifactId());
    model.setVersion(generateDto.getVersion());
    model.setName(generateDto.getService());

    Parent parent = new Parent();
    parent.setGroupId("org.springframework.boot");
    parent.setArtifactId("spring-boot-starter-parent");
    parent.setVersion("2.3.0.RELEASE");
    model.setParent(parent);

    Dependency web = new Dependency();
    web.setArtifactId("spring-boot-starter-web");
    web.setGroupId("org.springframework.boot");
    model.addDependency(web);

    Build build = initBuild();
    model.setBuild(build);

    DistributionManagement distributionManagement = new DistributionManagement();
    DeploymentRepository deploymentRepository = new DeploymentRepository();
    deploymentRepository.setId("maven_remote");
    deploymentRepository.setUrl(generateDto.getMavenRepository());
    distributionManagement.setRepository(deploymentRepository);
    model.setDistributionManagement(distributionManagement);
    return model;
  }

  private Build initBuild() {
    Build build = new Build();
    List<Plugin> plugins = new ArrayList<>();
    Plugin compliePlugin = new Plugin();
    compliePlugin.setGroupId("org.apache.maven.plugins");
    compliePlugin.setArtifactId("maven-compiler-plugin");
    compliePlugin.setVersion("3.8.1");
    plugins.add(compliePlugin);

    Plugin sourcePlugin = new Plugin();
    sourcePlugin.setGroupId("org.apache.maven.plugins");
    sourcePlugin.setArtifactId("maven-source-plugin");
    sourcePlugin.setVersion("3.2.0");
    PluginExecution pluginExecution = new PluginExecution();
    pluginExecution.setId("attach-sources");
    pluginExecution.addGoal("jar");
    sourcePlugin.addExecution(pluginExecution);
    plugins.add(sourcePlugin);
    build.setPlugins(plugins);
    return build;
  }

  private boolean createProjectDir(String projectPath) throws IOException {
    File file = new File(projectPath);
    if (file.exists()) {
      FileUtils.cleanDirectory(file);
      return true;
    }
    boolean mkdirs = file.mkdirs();
    log.info("create project dir result = {}", mkdirs);
    return mkdirs;
  }
}
