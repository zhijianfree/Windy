package com.zj.domain.repository.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.demand.TaskQueryBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
import com.zj.domain.entity.enums.WorkTaskStatus;
import com.zj.domain.entity.po.demand.WorkTask;
import com.zj.domain.mapper.demand.WorkTaskMapper;
import com.zj.domain.repository.demand.IWorkTaskRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        if (Objects.nonNull(taskQueryBO.getStatus())) {
            wrapper.eq(WorkTask::getStatus, taskQueryBO.getStatus());
        } else {
            wrapper.in(WorkTask::getStatus,
                    WorkTaskStatus.getNotHandleWorks().stream().map(WorkTaskStatus::getType).collect(Collectors.toList()));
        }
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
    public List<WorkTaskBO> getNotCompleteWorkTasks(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        List<WorkTask> list =
                list(Wrappers.lambdaUpdate(WorkTask.class).in(WorkTask::getTaskId, taskIds).in(WorkTask::getStatus,
                        WorkTaskStatus.getNotHandleWorks().stream().map(WorkTaskStatus::getType)
                                .collect(Collectors.toList())));
        return OrikaUtil.convertList(list, WorkTaskBO.class);
    }

    @Override
    public boolean batchUpdateStatus(List<String> taskIds, int status) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return false;
        }
        UpdateWrapper<WorkTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", status).in("task_id", taskIds);
        return baseMapper.update(null, updateWrapper) > 0;
    }
}
