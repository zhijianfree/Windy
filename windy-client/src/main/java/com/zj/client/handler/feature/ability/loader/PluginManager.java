package com.zj.client.handler.feature.ability.loader;

import com.zj.client.handler.feature.ability.Feature;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;

/**
 * @author falcon
 * @since 2023/7/12
 */
@Slf4j
public class PluginManager {

  private final String pluginDirectory;
  private final ClassLoader classLoader;

  public PluginManager(String pluginDirectory) {
    this.pluginDirectory = pluginDirectory;
    this.classLoader = new PluginClassLoader(getJarUrls(pluginDirectory),
        getClass().getClassLoader());
  }

  public List<Feature> loadPlugins() {
    List<Feature> plugins = new ArrayList<>();
    File[] jarFiles = new File(pluginDirectory).listFiles((dir, name) -> name.endsWith(".jar"));
    if (jarFiles != null) {
      for (File jarFile : jarFiles) {
        try (JarFile jf = new JarFile(jarFile)) {
          for (JarEntry entry : Collections.list(jf.entries())) {
            if (entry.getName().endsWith(".class")) {
              String className = entry.getName().replace('/', '.').replace(".class", "");
              Class<?> clazz = ClassUtils.forName(className, getClass().getClassLoader());
              if (Feature.class.isAssignableFrom(clazz)) {
                Feature plugin = (Feature) clazz.getDeclaredConstructor().newInstance();
                plugins.add(plugin);
              }
            }
          }
        } catch (IOException | ReflectiveOperationException e) {
          // 处理异常
        }
      }
    }
    return plugins;
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
