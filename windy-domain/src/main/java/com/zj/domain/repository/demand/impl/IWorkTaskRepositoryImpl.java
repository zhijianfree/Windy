package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.demand.TaskQueryBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
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
    public boolean createTask(WorkTaskBO workTaskBO) {
        WorkTask workTask = OrikaUtil.convert(workTaskBO, WorkTask.class);
        workTask.setCreateTime(System.currentTimeMillis());
        workTask.setUpdateTime(System.currentTimeMillis());
        return save(workTask);
    }

    @Override
    public boolean updateWorkTask(WorkTaskBO workTaskBO) {
        WorkTask workTask = OrikaUtil.convert(workTaskBO, WorkTask.class);
        workTask.setUpdateTime(System.currentTimeMillis());
        return update(workTask, Wrappers.lambdaUpdate(WorkTask.class).eq(WorkTask::getTaskId, workTask.getTaskId()));
    }

    @Override
    public WorkTaskBO getWorkTask(String taskId) {
        WorkTask workTask = getOne(Wrappers.lambdaUpdate(WorkTask.class).eq(WorkTask::getTaskId, taskId));
        return OrikaUtil.convert(workTask, WorkTaskBO.class);
    }

    @Override
    public boolean deleteWorkTask(String taskId) {
        return remove(Wrappers.lambdaQuery(WorkTask.class).eq(WorkTask::getTaskId, taskId));
    }

    @Override
    public List<WorkTaskBO> getWorkTaskByName(String queryName) {
        List<WorkTask> workTasks = list(Wrappers.lambdaQuery(WorkTask.class).like(WorkTask::getTaskName, queryName));
        return OrikaUtil.convertList(workTasks, WorkTaskBO.class);
    }

    @Override
    public PageSize<WorkTaskBO> getWorkTaskPage(TaskQueryBO taskQueryBO) {
        LambdaQueryWrapper<WorkTask> wrapper = Wrappers.lambdaQuery(WorkTask.class).eq(WorkTask::getCreator,
                taskQueryBO.getUserId());
        Optional.ofNullable(taskQueryBO.getStatus()).ifPresent(status -> wrapper.eq(WorkTask::getStatus, status));
        IPage<WorkTask> pageQuery = new Page<>(taskQueryBO.getPage(), taskQueryBO.getSize());
        IPage<WorkTask> page = page(pageQuery, wrapper);
        PageSize<WorkTaskBO> pageSize = new PageSize<>(Collections.emptyList());
        pageSize.setTotal(page.getTotal());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            pageSize.setData(OrikaUtil.convertList(page.getRecords(), WorkTaskBO.class));
        }
        return pageSize;
    }

    @Override
    public Integer countIteration(String iterationId) {
        return 0;
    }
}
