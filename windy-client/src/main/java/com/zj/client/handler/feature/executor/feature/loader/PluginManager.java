package com.zj.client.handler.feature.executor.feature.loader;

import com.alibaba.fastjson.JSON;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.loader.Feature;
import com.zj.client.loader.FeatureDefine;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/7/12
 */
@Slf4j
@Component
public class PluginManager {

  private GlobalEnvConfig globalEnvConfig;

  public PluginManager(GlobalEnvConfig globalEnvConfig) {
    this.globalEnvConfig = globalEnvConfig;
  }

  public List<Feature> loadPlugins() {
    List<Feature> features = new ArrayList<>();
    try {
      String path = "file:" + globalEnvConfig.getWorkspace() + "/plugins/*.jar";
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resolver.getResources(path);
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

  private static JarFile getJarFile(File file) {
    try {
      return new JarFile(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private URL[] getJarUrls(String directory) {
    try {
      File[] jarFiles = new File(directory).listFiles((dir, name) -> name.endsWith(".jar"));
      if (jarFiles != null) {
        URL[] urls = new URL[jarFiles.length];
        for (int i = 0; i < jarFiles.length; i++) {
          urls[i] = jarFiles[i].toURI().toURL();
        }
        return urls;
      }
    } catch (Exception e) {
      log.error("load jar file error", e);
    }
    return new URL[0];
  }
}
