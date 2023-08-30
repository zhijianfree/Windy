package com.zj.master.schedule;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.model.DispatchTaskModel;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.repository.pipeline.IOptimisticLockRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.master.entity.vo.PipelineConfig;
import com.zj.master.entity.vo.ScheduleHolder;
import com.zj.master.service.TaskLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * @author falcon
 * @since 2023/7/19
 */
@Slf4j
@Component
public class PipelineSchedule implements CommandLineRunner {

  public static final String PIPELINE_SCHEDULE = "pipeline_schedule";
  private final IOptimisticLockRepository lockRepository;
  private final IPipelineRepository pipelineRepository;
  private final TaskScheduler taskScheduler;
  private final TaskLogService taskLogService;

  private final Map<String, ScheduleHolder> scheduledMap = new ConcurrentHashMap<>();

  public PipelineSchedule(IOptimisticLockRepository lockRepository,
      IPipelineRepository pipelineRepository, TaskScheduler taskScheduler,
      TaskLogService taskLogService) {
    this.lockRepository = lockRepository;
    this.pipelineRepository = pipelineRepository;
    this.taskScheduler = taskScheduler;
    this.taskLogService = taskLogService;
  }

  /**
   * 30 分钟检查一次是否有新的定时任务需要执行或者修改
   * */
  @Scheduled(cron = "0/5 * * * * ? ")
  public void scanTaskLog() {
    log.debug("start scan schedule pipeline");
    loadSchedulePipeline();
  }



  @Override
  public void run(String... args) throws Exception {
    loadSchedulePipeline();
  }

  private void loadSchedulePipeline() {
    List<PipelineDto> pipelines = pipelineRepository.getSchedulePipelines().stream()
        .filter(pipeline -> StringUtils.isNotBlank(pipeline.getPipelineConfig()))
        .filter(this::notExistLocalMap)
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(pipelines)) {
      return;
    }

    lockRepository.tryLock(PIPELINE_SCHEDULE);
    pipelines.forEach(pipeline -> {
      PipelineConfig pipelineConfig = JSON.parseObject(pipeline.getPipelineConfig(),
          PipelineConfig.class);
      Trigger trigger = new CronTrigger(pipelineConfig.getSchedule());
      ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> {
        if (!lockRepository.hasLock(PIPELINE_SCHEDULE)) {
          return;
        }
        DispatchTaskModel task = new DispatchTaskModel();
        task.setType(LogType.PIPELINE.getType());
        task.setSourceName(pipeline.getPipelineName());
        task.setSourceId(pipeline.getPipelineId());
        taskLogService.createTask(task);
      }, trigger);

      ScheduleHolder scheduleHolder = new ScheduleHolder();
      scheduleHolder.setScheduledFuture(scheduledFuture);
      scheduleHolder.setCron(pipelineConfig.getSchedule());
      scheduledMap.put(pipeline.getPipelineId(), scheduleHolder);
    });
  }

  private boolean notExistLocalMap(PipelineDto pipeline) {
    ScheduleHolder holder = scheduledMap.get(pipeline.getPipelineId());
    if (Objects.isNull(holder)) {
      return true;
    }

    PipelineConfig pipelineConfig = JSON.parseObject(pipeline.getPipelineConfig(),
        PipelineConfig.class);
    if (!Objects.equals(holder.getCron(), pipelineConfig.getSchedule())){
      //如果定时任务发生改变需要将之前的定时任务取消，后面会重新添加新的定时任务
      holder.getScheduledFuture().cancel(true);
      scheduledMap.remove(pipeline.getPipelineId());
      return true;
    }
    return false;
  }
}
