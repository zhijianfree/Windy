package com.zj.client.handler.feature.executor.invoker.loader;

import com.zj.client.config.GlobalEnvConfig;
import com.zj.common.entity.dto.PluginInfo;
import com.zj.plugin.loader.Feature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author guyuelan
 * @since 2023/7/12
 */
@Slf4j
@Component
public class PluginManager {

  public static final String PLUGIN_PATH = "plugins";
  public static final String PLUGINS_PATH_FORMAT = "file:%s/" + PLUGIN_PATH + "/*.jar";

  private final GlobalEnvConfig globalEnvConfig;

  public PluginManager(GlobalEnvConfig globalEnvConfig) {
    this.globalEnvConfig = globalEnvConfig;
  }

  public List<Feature> loadPlugins() {
    List<Feature> features = new ArrayList<>();
    try {
      URL[] urls = getFileUrls();

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

  public URL[] getFileUrls() throws IOException {
    String path = String.format(PLUGINS_PATH_FORMAT, globalEnvConfig.getWorkspace());
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources(path);
    URL[] urls = new URL[resources.length];
    for (int i = 0; i < resources.length; i++) {
      urls[i] = resources[i].getURL();
    }
    return urls;
  }

  public void saveJarPlugin(PluginInfo plugin) {
    try {
      String path = globalEnvConfig.getWorkspace() + File.separator + PLUGIN_PATH + File.separator
          + plugin.getPluginName();
      File jarPath = new File(path);
      FileUtils.createParentDirectories(jarPath);
      FileUtils.writeByteArrayToFile(jarPath, plugin.getFileData());
    } catch (Exception e) {
      log.error("save jar file error");
    }
  }
}
