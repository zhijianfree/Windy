package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.demand.TaskQuery;
import com.zj.domain.entity.dto.demand.WorkTaskDTO;
import com.zj.domain.entity.po.demand.WorkTask;
import com.zj.domain.mapper.demand.WorkTaskMapper;
import com.zj.domain.repository.demand.IWorkTaskRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class IWorkTaskRepositoryImpl extends ServiceImpl<WorkTaskMapper, WorkTask> implements IWorkTaskRepository {
    @Override
    public boolean createTask(WorkTaskDTO workTaskDTO) {
        WorkTask workTask = OrikaUtil.convert(workTaskDTO, WorkTask.class);
        workTask.setCreateTime(System.currentTimeMillis());
        workTask.setUpdateTime(System.currentTimeMillis());
        return save(workTask);
    }

    @Override
    public boolean updateWorkTask(WorkTaskDTO workTaskDTO) {
        WorkTask workTask = OrikaUtil.convert(workTaskDTO, WorkTask.class);
        workTask.setUpdateTime(System.currentTimeMillis());
        return update(workTask, Wrappers.lambdaUpdate(WorkTask.class).eq(WorkTask::getTaskId, workTask.getTaskId()));
    }

    @Override
    public WorkTaskDTO getWorkTask(String taskId) {
        WorkTask workTask = getOne(Wrappers.lambdaUpdate(WorkTask.class).eq(WorkTask::getTaskId, taskId));
        return OrikaUtil.convert(workTask, WorkTaskDTO.class);
    }

    @Override
    public boolean deleteWorkTask(String taskId) {
        return remove(Wrappers.lambdaQuery(WorkTask.class).eq(WorkTask::getTaskId, taskId));
    }

    @Override
    public List<WorkTaskDTO> getWorkTaskByName(String queryName) {
        List<WorkTask> workTasks = list(Wrappers.lambdaQuery(WorkTask.class).like(WorkTask::getTaskName, queryName));
        return OrikaUtil.convertList(workTasks, WorkTaskDTO.class);
    }

    @Override
    public PageSize<WorkTaskDTO> getWorkTaskPage(TaskQuery taskQuery) {
        LambdaQueryWrapper<WorkTask> wrapper = Wrappers.lambdaQuery(WorkTask.class).eq(WorkTask::getCreator,
                taskQuery.getUserId());
        Optional.ofNullable(taskQuery.getStatus()).ifPresent(status -> wrapper.eq(WorkTask::getStatus, status));
        IPage<WorkTask> pageQuery = new Page<>(taskQuery.getPage(), taskQuery.getSize());
        IPage<WorkTask> page = page(pageQuery, wrapper);
        PageSize<WorkTaskDTO> pageSize = new PageSize<>(Collections.emptyList());
        pageSize.setTotal(page.getTotal());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(page.getRecords(), WorkTaskDTO.class));
        }
        return pageSize;
    }

    @Override
    public Integer countIteration(String iterationId) {
        return 0;
    }
}
