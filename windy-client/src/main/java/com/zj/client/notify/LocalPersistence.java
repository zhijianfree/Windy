package com.zj.client.notify;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.zj.common.model.ResultEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * Client 将通知失败的数据缓存到本地文件， 然后慢慢轮询通知master节点
 *
 * @author falcon
 * @since 2023/5/24
 */
@Slf4j
@Component
public class LocalPersistence implements DisposableBean {

  public static final String PERSIST_LOG = "persist.log";
  public static final String LINUX_PATH = "/opt/windy/persist/" + PERSIST_LOG;
  public static final String WINDOWS_PATH = "C:\\\\windy\\\\persist\\\\" + PERSIST_LOG;
  private final CopyOnWriteArrayList<ResultEvent> unPersistEventList = new CopyOnWriteArrayList<>();

  private final Integer maxSaveSize = 0;
  private File file;

  public LocalPersistence() {
    file = new File(getDefaultDir());
    if (!file.exists()) {
      try {
        Files.createParentDirs(file);
      } catch (IOException e) {
        log.error("create target file error");

        tryRuntimeDir();
      }
    }
  }

  private void tryRuntimeDir() {
    try {
      String path = new File("").getCanonicalPath();
      file = new File(path + File.separator + PERSIST_LOG);
      Files.touch(file);
    } catch (IOException ex) {
      log.error("create current file error");
    }
  }

  private String getDefaultDir() {
    return isWindowsSystem() ? WINDOWS_PATH : LINUX_PATH;
  }

  public void persistNotify(ResultEvent resultEvent) {
    unPersistEventList.add(resultEvent);
    if (unPersistEventList.size() > maxSaveSize) {
      saveEvent2File();
    }
  }

  @Override
  public void destroy() throws Exception {
    log.info("LocalPersistence start run destroy ");
    if (CollectionUtils.isEmpty(unPersistEventList)) {
      return;
    }
    saveEvent2File();
  }

  public List<ResultEvent> readEventsFromFile() {
    try {
      List<String> list = Files.readLines(file, Charsets.UTF_8);
      if (CollectionUtils.isEmpty(list)) {
        return Collections.emptyList();
      }
      return list.stream().map(str -> JSON.parseObject(str, ResultEvent.class))
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("read persist log error", e);
    }
    return Collections.emptyList();
  }

  public void clearFileContent() {
    try {
      BufferedWriter writer = Files.newWriter(file, Charsets.UTF_8);
      writer.write("");
      writer.flush();
      writer.close();
    } catch (Exception exception) {
      log.error("clear file error", exception);
    }
  }

  private void saveEvent2File() {
    CharSink sink = Files.asCharSink(file, Charsets.UTF_8, FileWriteMode.APPEND);
    while (unPersistEventList.size() > 0) {
      ResultEvent event = unPersistEventList.remove(0);
      try {
        sink.write(JSON.toJSONString(event) + "\r\n");
      } catch (IOException e) {
        log.error("write event to file error", e);
      }
    }
  }

  public boolean isWindowsSystem() {
    String os = System.getProperty("os.name");
    return Objects.nonNull(os) && os.toLowerCase().startsWith("windows");
  }
}
