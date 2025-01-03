package com.zj.feature.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.feature.entity.HistoryNodeDto;
import com.zj.feature.service.TaskRecordService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops/feature")
public class TaskRecordRest {
    private final TaskRecordService taskRecordService;

    public TaskRecordRest(TaskRecordService taskRecordService) {
        this.taskRecordService = taskRecordService;
    }

    @GetMapping("/task/records")
    public ResponseMeta<PageSize<TaskRecordBO>> getTaskRecordList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.getTaskRecordPage(page, size));
    }

    @GetMapping("/task/record/{recordId}")
    public ResponseMeta<TaskRecordBO> getTaskRecordDetail(@PathVariable("recordId") String recordId) {
        return new ResponseMeta<TaskRecordBO>(ErrorCode.SUCCESS,
                taskRecordService.getTaskRecord(recordId));
    }

    @GetMapping("/task/{triggerId}/record")
    public ResponseMeta<TaskRecordBO> getTaskRecordByTrigger(@PathVariable("triggerId") String triggerId) {
        return new ResponseMeta<TaskRecordBO>(ErrorCode.SUCCESS, taskRecordService.getTaskRecordByTrigger(triggerId));
    }

    @GetMapping("/task/{triggerId}/records")
    public ResponseMeta<PageSize<TaskRecordBO>> getTriggerTaskRecords(@PathVariable("triggerId") String triggerId,
                                                                      @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                      @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return new ResponseMeta<PageSize<TaskRecordBO>>(ErrorCode.SUCCESS,
                taskRecordService.getTriggerTaskRecords(triggerId, page, size));
    }

    @GetMapping("/task/records/{recordId}/histories")
    public ResponseMeta<List<FeatureHistoryBO>> getTaskFeatureHistories(
            @PathVariable("recordId") String recordId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.getTaskFeatureHistories(recordId));
    }

    @GetMapping("/task/records/{recordId}/history/tree")
    public ResponseMeta<List<HistoryNodeDto>> getTaskFeatureHistoryTree(
            @PathVariable("recordId") String recordId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.getTaskFeatureHistoryTree(recordId));
    }

    @DeleteMapping("/task/record/{recordId}")
    public ResponseMeta<Boolean> deleteTaskRecord(@PathVariable("recordId") String recordId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.deleteTaskRecord(recordId));
    }

    @PutMapping("/task/stop/{recordId}")
    public ResponseMeta<Boolean> stopTaskRecord(
            @PathVariable("recordId") String recordId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
                taskRecordService.stopTaskRecord(recordId));
    }
}
