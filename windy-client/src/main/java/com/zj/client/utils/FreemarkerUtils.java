package com.zj.client.utils;

import freemarker.template.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FreemarkerUtils {

  private static Configuration configuration;
  private static String ftlPath = new File(
      FreemarkerUtils.class.getClassLoader().getResource("ftls").getFile()).getPath();
  public static synchronized Configuration getInstance() {
    if (Objects.isNull(configuration)) {
      configuration = new Configuration(Configuration.VERSION_2_3_23);
      try {
        if (ftlPath.contains("jar")) {
          configuration.setClassForTemplateLoading(FreemarkerUtils.class, "/ftls");
        } else {
          configuration.setDirectoryForTemplateLoading(new File(ftlPath));
        }
      } catch (IOException e) {
        log.error("load ftl template file error", e);
      }
      configuration.setEncoding(Locale.CHINA, "utf-8");
    }
    return configuration;
  }
}
