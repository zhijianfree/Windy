package com.zj.pipeline.executer.notify;

import com.alibaba.fastjson.JSON;
import com.zj.pipeline.executer.IStatusNotifyListener;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeExecuteStatusListener implements IStatusNotifyListener {

  @Override
  public void statusChange(PipelineStatusEvent event) {
    log.info("receive pipeline notify={}", JSON.toJSONString(event));
  }
}
