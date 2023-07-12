package com.zj.client.handler.feature.ability.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author falcon
 * @since 2023/7/12
 */
public class PluginClassLoader extends URLClassLoader {
  public PluginClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }
}
