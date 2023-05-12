package com.zj.master.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.master.entity.po.TaskLog;
import com.zj.master.mapper.TaskLogMapper;
import org.springframework.stereotype.Service;

/**
 * @author falcon
 * @since 2023/5/12
 */
@Service
public class TaskLogService extends ServiceImpl<TaskLogMapper, TaskLog> {

  public Boolean createTask(TaskDetailDto taskDetailDto) {
    return null;
  }
}
