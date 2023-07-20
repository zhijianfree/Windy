package com.zj.master.schedule;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.model.DispatchTaskModel;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.repository.pipeline.IOptimisticLockRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.master.entity.vo.PipelineConfig;
import com.zj.master.service.TaskLogService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/7/19
 */
@Slf4j
@Component
public class PipelineSchedule implements CommandLineRunner {

  public static final String PIPELINE_SCHEDULE = "pipeline_schedule";
  private IOptimisticLockRepository lockRepository;
  private IPipelineRepository pipelineRepository;
  private TaskScheduler taskScheduler;
  private TaskLogService taskLogService;

  public PipelineSchedule(IOptimisticLockRepository lockRepository,
      IPipelineRepository pipelineRepository, TaskScheduler taskScheduler,
      TaskLogService taskLogService) {
    this.lockRepository = lockRepository;
    this.pipelineRepository = pipelineRepository;
    this.taskScheduler = taskScheduler;
    this.taskLogService = taskLogService;
  }

  @Override
  public void run(String... args) throws Exception {
    List<PipelineDto> pipelines = pipelineRepository.getSchedulePipelines().stream()
        .filter(pipeline -> StringUtils.isNotBlank(pipeline.getPipelineConfig()))
        .collect(Collectors.toList());
    pipelines.forEach(pipeline -> {
      PipelineConfig pipelineConfig = JSON.parseObject(pipeline.getPipelineConfig(),
          PipelineConfig.class);
      Trigger trigger = new CronTrigger(pipelineConfig.getSchedule());
      taskScheduler.schedule(() -> {
        if (!lockRepository.tryLock(PIPELINE_SCHEDULE)) {
          return;
        }
        DispatchTaskModel task = new DispatchTaskModel();
        task.setType(LogType.PIPELINE.getType());
        task.setSourceName(pipeline.getPipelineName());
        task.setSourceId(pipeline.getPipelineId());
        taskLogService.createTask(task);
      }, trigger);
    });
  }
}
