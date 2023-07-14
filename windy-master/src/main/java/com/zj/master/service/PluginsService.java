package com.zj.master.service;

import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author falcon
 * @since 2023/7/14
 */
@Slf4j
@Service
public class PluginsService {

  public static final String PLUGINS_PATH = "plugins";

  public Boolean uploadTemplate(MultipartFile file) {
    try {
      String currentPath =
          new File("").getCanonicalPath() + File.separator + PLUGINS_PATH + File.separator;
      String filePath = currentPath + file.getOriginalFilename();
      createIfNotExist(filePath);
      FileUtils.writeByteArrayToFile(new File(filePath), file.getBytes());
    } catch (Exception e) {
      log.error("save file error", e);
    }
    return false;
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
}
